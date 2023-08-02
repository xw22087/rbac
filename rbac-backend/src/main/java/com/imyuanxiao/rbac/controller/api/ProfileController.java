package com.imyuanxiao.rbac.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.baomidou.mybatisplus.core.metadata.OrderItem;
//import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.imyuanxiao.rbac.annotation.Auth;
import com.imyuanxiao.rbac.enums.ResultCode;
import com.imyuanxiao.rbac.exception.ApiException;
import com.imyuanxiao.rbac.model.param.*;
import com.imyuanxiao.rbac.model.vo.LoginHistoryPageVO;
import com.imyuanxiao.rbac.model.entity.User;
import com.imyuanxiao.rbac.model.vo.UserPageVO;
import com.imyuanxiao.rbac.service.PermissionService;
import com.imyuanxiao.rbac.service.UserProfileService;
import com.imyuanxiao.rbac.service.UserService;
import com.imyuanxiao.rbac.util.SecurityContextUtil;
import com.imyuanxiao.rbac.util.ValidationGroups;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;
import java.time.LocalDateTime;

import static com.imyuanxiao.rbac.util.CommonConst.ACTION_SUCCESSFUL;
import java.sql.Connection;
import java.util.List;
import java.sql.DriverManager;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;



/**
 * @description  User Profile Management
 * @author: <a href="https://github.com/imyuanxiao">imyuanxiao</a>
 **/
@Slf4j
@RestController
@RequestMapping("/profile")
@Api(tags = "User Profile Management")
public class ProfileController {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rbac";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Wabbhmm3123804!";
    private Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

    @PostMapping("/save")
    @ApiOperation(value = "Add user's profile")
    public String addUserProfile(@RequestParam String playerDataJson, @RequestParam int userId) {
        //json转PlayerDataParam
        PlayerDataParam playerDataParam = JSONUtil.toBean(playerDataJson, PlayerDataParam.class);
        try (connection){
            String selectSql = "SELECT id, creation_time FROM user_profile WHERE user_id = ?";
            String insertSql = "INSERT INTO user_profile (user_id, created_time, updated_time, player_data, spaceship_score, achievement_points, completed_chapters," +
                    " boss1100_time, boss1101_time, boss1102_time, boss1103_time, boss1104_time, boss1105_time, boss1106_time, avatar_imageId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String updateSql = "UPDATE user_profile SET updated_time = ?, player_data = ?, spaceship_score = ?, achievement_points = ?, completed_chapters = ?," +
                    " boss1100_time = ?, boss1101_time = ?, boss1102_time = ?, boss1103_time = ?, boss1104_time = ?, boss1105_time = ?, boss1106_time = ? WHERE user_id = ?";

            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

                //检查是否存在该记录userid
                selectStatement.setInt(1, playerDataParam.getUserId());
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        //如果存在，update
                        int user_id = resultSet.getInt("user_id");
                        updateStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                        updateStatement.setString(2, playerDataJson);
                        updateStatement.setInt(3, playerDataParam.getSpaceshipScore());//飞船评分
                        updateStatement.setInt(4, playerDataParam.getAchievementPoint());//成就点数
                        updateStatement.setInt(5, playerDataParam.getPlayerChapterNum());//完成章节数
                        //boss time
                        updateStatement.setString(6, JSONUtil.toJsonStr(playerDataParam.getBossTime1100()));
                        updateStatement.setString(7, JSONUtil.toJsonStr(playerDataParam.getBossTime1101()));
                        updateStatement.setString(8, JSONUtil.toJsonStr(playerDataParam.getBossTime1102()));
                        updateStatement.setString(9, JSONUtil.toJsonStr(playerDataParam.getBossTime1103()));
                        updateStatement.setString(10, JSONUtil.toJsonStr(playerDataParam.getBossTime1104()));
                        updateStatement.setString(11, JSONUtil.toJsonStr(playerDataParam.getBossTime1105()));
                        updateStatement.setString(12, JSONUtil.toJsonStr(playerDataParam.getBossTime1106()));
                        //user to be updated
                        updateStatement.setInt(13, user_id);
                        updateStatement.executeUpdate();
                    } else {
                        //如果不存在，create
                        insertStatement.setInt(1, playerDataParam.getUserId);
                        insertStatement.setTimestamp(2, Timestamp.valueof(LocalDateTime.now()));//记录创建时间
                        insertStatement.setTimestamp(3, Timestamp.valueof(LocalDateTime.now()));//记录更新时间

                        insertStatement.setString(4, playerDataJson);
                        insertStatement.setInt(5, playerDataParam.getSpaceshipScore());//飞船评分
                        insertStatement.setInt(6, playerDataParam.getAchievementPoint());//成就点数
                        insertStatement.setInt(7, playerDataParam.getPlayerChapterNum());//完成章节数
                        //boss
                        insertStatement.setString(8, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1100()));
                        insertStatement.setString(9, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1101()));
                        insertStatement.setString(10, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1102()));
                        insertStatement.setString(11, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1103()));
                        insertStatement.setString(12, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1104()));
                        insertStatement.setString(13, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1105()));
                        insertStatement.setString(14, JSONUtil.toJsonStr(playerDataParam.playerDataParam.getBossTime1106()));
                        //头像
                        insertStatement.setInt(15, playerDataParam.getAvatarImageId());
                        insertStatement.executeUpdate();
                    }

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection.close();
            return ACTION_SUCCESSFUL;
        }

        //将PlayerDataParam对象转换为JSON字符串
//    private String convertPlayerDataToJson(PlayerDataParam playerDataParam) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            return objectMapper.writeValueAsString(playerDataParam);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    }
    @PostMapping("/getsave")
    @ApiOperation(value = "Get user's profile")
    public char getPlayerSaving(@RequestParam int user_id) {
        char playerData = 0;
        try {
            //查询语句
            String sql = "SELECT player_data FROM user_profile WHERE user_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user_id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                //获取playerdata
                playerData = resultSet.getString("player_data").charAt(0);
            }

            //关闭
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerData;
    }

    @PostMapping("/getrank")
    @ApiOperation(value = "Get rank list")
    //RankMode: 0=score;1=achievement;2=chapter;
    public List<List<Object>> GetRank(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam int rankMode) {
        char orderBy = null;
        switch (rankMode) {
            case 0: orderBy = "spaceship_score";
            case 1: orderBy = "achievement_points";
//            case 2: orderBy = "completed_chapters";
            case 2: orderBy = "boss1100_time";
            case 3: orderBy = "boss1101_time";
            case 4: orderBy = "boss1102_time";
            case 5: orderBy = "boss1103_time";
            case 6: orderBy = "boss1104_time";
            case 7: orderBy = "boss1105_time";
            case 8: orderBy = "boss1106_time";
        }
        try {
            // 构造SQL查询语句，按评分需要的数据排序并获取指定排名范围的数据
            String sql = "SELECT u.user_name, u.user_id, up.spaceship_score, up.achievement_points " +
                    "FROM user u " +
                    "JOIN user_profile up ON u.user_id = up.user_id " +
                    "ORDER BY up.? DESC " +
                    "LIMIT ?, ?";

            // 预处理语句与参数
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, orderBy);
            preparedStatement.setString(2, orderBy);
            preparedStatement.setInt(3, (pageNumber - 1) * pageSize);
            preparedStatement.setInt(4, pageNumber * pageSize);

            // 执行查询
            ResultSet resultSet = preparedStatement.executeQuery();

            // 处理查询结果
            while (resultSet.next()) {
                List<Object> rowData = new ArrayList<>();
                rowData.add(resultSet.getString("user_name"));
                rowData.add(resultSet.getInt("user_id"));
                rowData.add(resultSet.getInt("spaceship_score"));
                rowData.add(resultSet.getInt("achievement_points"));
                rowData.add(resultSet.getInt("completed_chapters"));

                if (rankMode > 1) {
                    rowData.add(resultSet.getFloat(orderBy));
                }
                result.add(rowData);
            }

            // 关闭
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
