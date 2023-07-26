package com.imyuanxiao.rbac.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

/**
 * @description  User Profile Saving
 * @author: <a href="https://github.com/imyuanxiao">imyuanxiao</a>
 **/
@Slf4j
@RestController
@RequestMapping("/save")
@Api(tags = "User Profile Saving")
public class ProfileController {

    @PostMapping("/save")
    @ApiOperation(value = "Add user's profile")
    public String addUserProfile(@RequestParam String playerDataJson, @RequestParam int userId) {
        //json转PlayerDataParam
        PlayerDataParam playerDataParam = JSONUtil.toBean(json, PlayerDataParam.class);
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rbac", "root", "Wabbhmm3123804!")) {
            String selectSql = "SELECT id, creation_time FROM user_profile WHERE user_id = ?";
            String insertSql = "INSERT INTO user_profile (user_id, created_time, player_data, spaceship_score, achievement_points, completed_chapters, boss_challenge_times, avatar_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String updateSql = "UPDATE user_profile SET update_time = ?, player_data = ?, spaceship_score = ?, achievement_points = ?, completed_chapters = ?, boss_challenge_times = ? WHERE id = ?";

            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

                // Check if the record with the given userId exists
                selectStatement.setInt(1, playerDataParam.getUserId());
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Record exists, update it
                        int id = resultSet.getInt("id");
                        updateStatement.setTimestamp(1, Timestamp.valueof(LocalDateTime.now()));
                        updateStatement.setString(2, playerDataJson);
                        updateStatement.setString(3, JSONUtil.toJsonStr(playerDataParam.getSpaceshipScore()));//飞船评分
                        updateStatement.setString(4, JSONUtil.toJsonStr(playerDataParam.getAchievementPoint()));//成就点数
                        updateStatement.setString(5, JSONUtil.toJsonStr(playerDataParam.getPlayerChapterNum()));//完成章节数
                        updateStatement.setString(6, JSONUtil.toJsonStr(playerDataParam.getBattleVictoryCount()));//boss挑战成功次数
                        updateStatement.setInt(7, id);
                        updateStatement.executeUpdate();
                    } else {
                        // Record does not exist, create a new one
                        insertStatement.setInt(1, userId);
                        insertStatement.setTimestamp(2, Timestamp.valueof(LocalDateTime.now()));//记录创建时间
                        insertStatement.setString(3, playerDataJson);
                        insertStatement.setString(4, JSONUtil.toJsonStr(playerDataParam.getSpaceshipScore()));//飞船评分
                        insertStatement.setString(5, JSONUtil.toJsonStr(playerDataParam.getAchievementPoint()));//成就点数
                        insertStatement.setString(5, JSONUtil.toJsonStr(playerDataParam.getPlayerChapterNum()));//完成章节数
                        insertStatement.setString(7, JSONUtil.toJsonStr(playerDataParam.getBattleVictoryCount()));//boss
                        insertStatement.setString(8, JSONUtil.toJsonStr(playerDataParam.getAvatarImageUrl()));//头像
                        insertStatement.executeUpdate();
                    }

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                // 处理异常
                e.printStackTrace();
            }
            return ACTION_SUCCESSFUL
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
    public char getPlayerSaving(int user_id) {
        char playerData = 0;

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/rbac", "root", "Wabbhmm3123804!");

            // 查询语句
            String sql = "SELECT player_data FROM user_profile WHERE user_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user_id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // 获取player_data
                playerData = resultSet.getString("player_data").charAt(0);
            }

            // 关闭
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerData;
    }

    public List<List<char>> RankByScore() {
        return null;
    }
    public List<List<char>> RankByPoint() {
        return null;
    }
    public List<List<char>> RankByChapter() {
        return null;
    }
}
