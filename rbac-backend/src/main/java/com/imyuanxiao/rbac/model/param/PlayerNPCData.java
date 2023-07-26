using System;

public class PlayerNPCData
{
    public int NPCId { get; set; }
    public string NPCName { get; set; }
    public int NPCLevel { get; set; }
    // 添加其他NPC属性

    public PlayerNPCData(int npcId, string npcName, int npcLevel)
    {
        NPCId = npcId;
        NPCName = npcName;
        NPCLevel = npcLevel;
    }
}
