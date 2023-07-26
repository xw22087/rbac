import java.util.Map;
import java.util.List;

public class PlayerDataParam {
    // 飞船ID
    private int initialSpaceshipId;
    // 玩家道具
    private Map<Integer, Integer> playerArtifacts;
    // 飞船模组
    private List<Integer> playerModules;
    // 装备的模组
    private int[] equippedModules;
    // 玩家技能
    private Map<Integer, Integer> playerSkills;
    // NPC数据
    private Map<Integer, PlayerNPCData> playerNPCs;
    // 玩家成就
    private Map<Integer, Integer> playerAchievement;
    // 完成章节
    private List<Integer> playerChapters;
    // 完成战斗数量
    private int battleVictoryCount;
    private char avatarImageUrl;

    // Getters
    public int getUserId() {
        return userId;
    }

    public int getInitialSpaceshipId() {
        return initialSpaceshipId;
    }

    public Map<Integer, Integer> getPlayerArtifacts() {
        return playerArtifacts;
    }

    public List<Integer> getPlayerModules() {
        return playerModules;
    }

    public int[] getEquippedModules() {
        return equippedModules;
    }

    public Map<Integer, Integer> getPlayerSkills() {
        return playerSkills;
    }

    public Map<Integer, PlayerNPCData> getPlayerNPCs() {
        return playerNPCs;
    }

    public Map<Integer, Integer> getPlayerAchievement() {
        return playerAchievement;
    }

    public int getPlayerChapterNum() {
        int num = 0;
        num = playerChapters.size();
        return num;
    }

    public int getBattleVictoryCount() {
        return battleVictoryCount;
    }

    public int getSpaceshipScore() {
        int score = 0;
        return score;
    }

    public int getAchievementPoint() {
        int point = 0;
        return point;
    }

    public char getAvatarImageUrl(){
        return avatarImageUrl;
    }
}
