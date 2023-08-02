package com.imyuanxiao.rbac.model.param;

import java.util.Map;
import java.util.List;
import com.imyuanxiao.rbac.util.ValidationGroups;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class PlayerDataParam {
    // 飞船ID
//    private int initialSpaceshipId;
//    // 玩家道具
//    private Map<Integer, Integer> playerArtifacts;
//    // 玩家总道具
//    private Map<Integer, Integer> playerTotalArtifacts;
//    // 飞船模组
//    private List<Integer> playerModules;
//    // 装备的模组
//    private int[] equippedModules;
//    // 玩家技能
//    private Map<Integer, Integer> playerSkills;
//    // NPC数据
//    private Map<Integer, PlayerNPCData> playerNPCs;
//    // 玩家成就
//    private Map<Integer, Integer> playerAchievement;
//    // 完成章节
//    private List<Integer> playerChapters;
//    // 完成战斗数量
//    private int battleVictoryCount;
    // 头像Id
    private char avatarImageId;
    // 飞船分数
    private int spaceshipScore;
    // 成就点数
    private int achievementPoint;
    // 章节数
    private int chapterNum;
//     Boss1100击败时间
    private float BossTime1100;
    // Boss1101击败时间
    private float BossTime1101;
    // Boss1102击败时间
    private float BossTime1102;
    // Boss1103击败时间
    private float BossTime1103;
    // Boss1104击败时间
    private float BossTime1104;
    // Boss1105击败时间
    private float BossTime1105;
    // Boss1106击败时间
    private float BossTime1106;
    // Boss1107击败时间
    private float BossTime1107;
    private char userName;
    private char userId;


    //////////////////////////
    // Getters
//    public int getInitialSpaceshipId() {
//        return initialSpaceshipId;
//    }
//
//    public Map<Integer, Integer> getPlayerArtifacts() {
//        return playerArtifacts;
//    }
//
//    public Map<Integer, Integer> getPlayerTotalArtifacts() {
//        return playerTotalArtifacts;
//    }
//
//    public List<Integer> getPlayerModules() {
//        return playerModules;
//    }
//
//    public int[] getEquippedModules() {
//        return equippedModules;
//    }
//
//    public Map<Integer, Integer> getPlayerSkills() {
//        return playerSkills;
//    }
//
//    public Map<Integer, PlayerNPCData> getPlayerNPCs() {
//        return playerNPCs;
//    }
//
//    public Map<Integer, Integer> getPlayerAchievement() {
//        return playerAchievement;
//    }
//
//    public List<Integer> getPlayerChapters() {
//        return playerChapters;
//    }
//
//    public int getBattleVictoryCount() {
//        return battleVictoryCount;
//    }

    public char getAvatarImageId() {
        return avatarImageId;
    }

    public int getSpaceshipScore() {
        return spaceshipScore;
    }

    public int getAchievementPoint() {
        return achievementPoint;
    }

    public int getChapterNum() {
        return chapterNum;
    }

    public float getBossTime1100() {
        return BossTime1100;
    }

    public float getBossTime1101() {
        return BossTime1101;
    }

    public float getBossTime1102() {
        return BossTime1102;
    }

    public float getBossTime1103() {
        return BossTime1103;
    }

    public float getBossTime1104() {
        return BossTime1104;
    }

    public float getBossTime1105() {
        return BossTime1105;
    }

    public float getBossTime1106() {
        return BossTime1106;
    }

    public float getBossTime1107() {
        return BossTime1107;
    }

    public char getUserName() {
        return userName;
    }

    public char getUserId() {
        return userId;
    }
//    public int getUserId() {
//        return userId;
//    }
//
//    public int getInitialSpaceshipId() {
//        return initialSpaceshipId;
//    }
//
//    public Map<Integer, Integer> getPlayerArtifacts() {
//        return playerArtifacts;
//    }
//
//    public List<Integer> getPlayerModules() {
//        return playerModules;
//    }
//
//    public int[] getEquippedModules() {
//        return equippedModules;
//    }
//
//    public Map<Integer, Integer> getPlayerSkills() {
//        return playerSkills;
//    }
//
//    public Map<Integer, PlayerNPCData> getPlayerNPCs() {
//        return playerNPCs;
//    }
//
//    public Map<Integer, Integer> getPlayerAchievement() {
//        return playerAchievement;
//    }
//
//    public int getPlayerChapterNum() {
//        int num = 0;
//        num = playerChapters.size();
//        return num;
//    }
//
//    public int getBattleVictoryCount() {
//        return battleVictoryCount;
//    }
//
//    public int getSpaceshipScore() {
//        return score;
//    }
//
//    public int getAchievementPoint() {
//        return point;
//    }
//
//    public char getAvatarImageUrl(){
//        return avatarImageUrl;
//    }
}
