package com.imyuanxiao.rbac.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imyuanxiao.rbac.model.entity.UserLoginHistory;
import com.imyuanxiao.rbac.model.entity.UserProfile;
import com.imyuanxiao.rbac.model.dto.UserAddRequest;
import com.imyuanxiao.rbac.model.vo.LoginResponse;
import com.imyuanxiao.rbac.model.vo.UserDetailsVO;
import com.imyuanxiao.rbac.model.vo.UserPageVO;
import com.imyuanxiao.rbac.security.JwtManager;
import com.imyuanxiao.rbac.service.*;
import com.imyuanxiao.rbac.util.CommonConst;
import com.imyuanxiao.rbac.util.RedisUtil;
import com.imyuanxiao.rbac.util.SecurityContextUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imyuanxiao.rbac.enums.ResultCode;
import com.imyuanxiao.rbac.exception.ApiException;
import com.imyuanxiao.rbac.model.entity.User;
import com.imyuanxiao.rbac.mapper.UserMapper;
import com.imyuanxiao.rbac.model.dto.LoginRequest;
import com.imyuanxiao.rbac.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【user(user table)】的数据库操作Service实现
* @createDate 2023-05-26 17:15:53
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService, UserDetailsService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserLoginHistoryService loginHistoryService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String sendCaptcha(String phone) {
        String code = RandomUtil.randomNumbers(4);
        // Save email and code in redis
        redisUtil.saveCode(phone, code);
        // TODO send code to email
        return  "Verification code has been sent: " + code;
    }

    @Override
    public String register(LoginRequest registerRequest) {

        // 通过注册类型判断应该采用哪种注册方式
        String type = registerRequest.getType();

        // 1. 手机号注册
        if(CommonConst.MOBILE.equals(type) && registerByPhone(registerRequest.getMobile(), registerRequest.getCaptcha())){
            return "Register successfully";
        }
        // 2. 密码注册
        if(StrUtil.isBlank(registerRequest.getPassword())){
            throw new ApiException(ResultCode.PARAMS_ERROR, "密码格式错误！");
        }
        // 3. 普通用户名密码注册
        User user = new User()
                .setUsername(registerRequest.getUsername())
                .setUserStatus(0)
                .setUserPassword(passwordEncoder.encode(registerRequest.getPassword()));
        try {
            this.save(user);
            // Add default user role - 2L user
            roleService.insertRolesByUserId(user.getId(), List.of(2L));
            return "Register successfully";
        } catch (Exception e) {
            throw new ApiException(ResultCode.FAILED, "Account is already in use.");
        }
    }

    public boolean registerByPhone(String phone, String captcha){
        // Get captcha from redis
        redisUtil.getCaptcha(phone, captcha);
        User user = new User()
                .setUsername(phone)
                .setUserPhone(phone)
                .setUserStatus(0)
                // 默认密码为手机号
                .setUserPassword(passwordEncoder.encode(phone));
        try {
            this.save(user);
            // Add default user role - 2L user
            roleService.insertRolesByUserId(user.getId(), List.of(2L));
            redisUtil.removeCaptcha(phone);
            return true;
        } catch (Exception e) {
            throw new ApiException(ResultCode.FAILED, "Phone is already in use.");
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request) {

        User userResult = null;
        // 如果登录类型为手机号验证码
        if(CommonConst.MOBILE.equals(loginRequest.getType())){
            String phone = loginRequest.getMobile();
            // Get user by phone from database
            userResult = this.lambdaQuery()
                    .eq(StrUtil.isNotBlank(phone), User::getUserPhone, phone)
                    .one();
            // 用户不存在，且则自动注册新账号
            if(userResult == null){
                registerByPhone(phone, loginRequest.getCaptcha());
                // 注册成功，重新查询用户信息
                userResult = this.lambdaQuery()
                        .eq(StrUtil.isNotBlank(phone), User::getUserPhone, phone)
                        .one();
            }else{
                // 用户存在，表示正在使用验证码登录
                // 从redis验证手机号和验证码
                redisUtil.getCaptcha(phone, loginRequest.getCaptcha());
                // 移除手机号和验证码，登录成功
                redisUtil.removeCaptcha(phone);
                // 下一步，验证账户有效性，需要返回token
            }
        }else {
            String username = loginRequest.getUsername();
            userResult = this.lambdaQuery()
                    .eq(StrUtil.isNotBlank(username), User::getUsername, username)
                    .one();
            if(userResult == null || !passwordEncoder.matches(loginRequest.getPassword(), userResult.getUserPassword())){
                throw new ApiException(ResultCode.VALIDATE_FAILED, "Username or password is incorrect！");
            }
        }

        // If state is abnormal
        if(userResult.getUserStatus() != 0){
            throw new ApiException(userResult.getUserStatus() == 1 ?
                    ResultCode.ACCOUNT_STATE_DISABLED :
                    ResultCode.ACCOUNT_STATE_DELETED);
        }

        // Save login info
        loginHistoryService.save(new UserLoginHistory()
                .setUserId(userResult.getId())
                .setType(CommonConst.LOG_IN)
                .setUserAgent(request.getHeader("User-Agent"))
                .setIpAddress(request.getRemoteAddr()));

        // Put user basic info, profile, token, permissions in UserVO object
        UserVO userVO = getUserVO(userResult);

        // save UserMap to redis
        // Manually handle or use util to convert id 'long' to 'string'.
        Map<String, Object> userMap = BeanUtil.beanToMap(userVO, new HashMap<>(),
                CopyOptions.create().
                        setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue != null ? fieldValue.toString() : null));
        // Generate token
        String token = JwtManager.generate(userResult.getUsername());
        // Add token to userMap
        userMap.put("token", token);
        // Save user info and token in redis
        redisUtil.saveUserMap(userMap);

        // return loginResponse
        return new LoginResponse().setUserVO(userVO).setToken(token);
    }

    private UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        // Copy basic info
        BeanUtil.copyProperties(user, userVO);
        // Copy user profile
        UserProfile userProfile = userProfileService.getByUserId(user.getId());
        // Initialize user profile if new user
        if(userProfile == null){
            userProfile =  new UserProfile()
                    .setNickName(RandomUtil.randomString(4))
                    .setAvatar("https://i.328888.xyz/2023/05/15/VZpOIx.png");
            userProfile.setUserId(user.getId());
            userProfileService.save(userProfile);
        }
        // 讲userProfile数据拷贝到userVO，但是忽视id
        BeanUtil.copyProperties(userProfile, userVO, "id", "userID");
        // Set roleIds and permissionIds
        userVO.setRoleIds(roleService.getIdsByUserId(user.getId()))
                .setPermissionIds(permissionService.getIdsByUserId(user.getId()));
        return userVO;
    }



    @Override
    public void logout(HttpServletRequest request) {
        loginHistoryService.save(new UserLoginHistory()
                .setUserId(SecurityContextUtil.getCurrentUserDetailsVO().getUser().getId())
                .setType(CommonConst.LOG_OUT)
                .setUserAgent(request.getHeader("User-Agent"))
                .setIpAddress(request.getRemoteAddr()));
        redisUtil.removeUserMap();
    }

    @Override
    public void currentUser(HttpServletRequest request) {

    }

    @Override
    public Set<Long> myPermission() {
        Long currentUserId = SecurityContextUtil.getCurrentUserId();
        return permissionService.getIdsByUserId(currentUserId);
    }

    @Override
    public String updateToken() {
        return redisUtil.refreshToken();
    }

    @Override
    public void createUser(UserAddRequest userAddRequest) {
        if (lambdaQuery().eq(User::getUsername, userAddRequest.getUsername()).one() != null) {
            throw new ApiException(ResultCode.FAILED,"Account already exists.");
        }
        User user = new User();
        user.setUsername(userAddRequest.getUsername()).setUserPassword(passwordEncoder.encode(userAddRequest.getUsername()));
        save(user);
        if (CollectionUtil.isEmpty(userAddRequest.getRoleIds())) {
            return;
        }
        // Add info in table [user-role]
        roleService.insertRolesByUserId(user.getId(), userAddRequest.getRoleIds());
    }

    @Override
    public boolean removeByIds(Collection<?> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return false;
        }
        // Delete info from table [user-role]
        for (Object userId : idList) {
            roleService.removeByUserId((Long)userId);
        }
        // Delete user
        baseMapper.deleteBatchIds(idList);
        return true;
    }

    @Override
    public void update(UserAddRequest param) {
        updateRoles(param);
    }

    private void updateRoles(UserAddRequest param) {
        // Delete the original user role
        roleService.removeByUserId(param.getId());
        // If roleIds is empty, delete all roles for this user
        if (CollectionUtil.isEmpty(param.getRoleIds())) {
            return;
        }
        // If roleIds not empty, add new roles for this user
        roleService.insertRolesByUserId(param.getId(), param.getRoleIds());
    }

    @Override
    public User getUserByUsername(String username) {
        if(StrUtil.isBlank(username)){
            throw new ApiException(ResultCode.VALIDATE_FAILED);
        }
        return this.lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public UserDetailsVO loadUserByUsername(String username) throws UsernameNotFoundException {
        // Get user by username
        User user = this.getUserByUsername(username);
        // Get permissionIds and tranfer them to `SimpleGrantedAuthority` Object
        Set<SimpleGrantedAuthority> authorities = permissionService.getIdsByUserId(user.getId())
                .stream()
                .map(String::valueOf)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        return new UserDetailsVO(user, authorities);
    }

    @Override
    public IPage<UserPageVO> selectPage(Page<UserPageVO> page) {
        QueryWrapper<UserPageVO> queryWrapper = new QueryWrapper<>();
        // Don't show super admin (id:1) and current user
        Long myId = SecurityContextUtil.getCurrentUserId();
        queryWrapper.ne("id", myId).ne("id", 1);
        // Get page info
        IPage<UserPageVO> pages = baseMapper.selectPage(page, queryWrapper);
        // Get roles and organizations for all users
        for (UserPageVO vo : pages.getRecords()) {
            vo.setRoleIds(roleService.getIdsByUserId(vo.getId()));
            vo.setOrgIds(organizationService.getIdsByUserId(vo.getId()));
        }
        return pages;
    }

}




