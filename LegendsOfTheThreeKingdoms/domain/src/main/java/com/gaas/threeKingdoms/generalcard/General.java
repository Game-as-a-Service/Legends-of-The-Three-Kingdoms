package com.gaas.threeKingdoms.generalcard;

import java.util.HashMap;
import java.util.Map;

public enum General {
    劉備("劉備", 4, "SHU001", Gender.MALE, Faction.SHU),
    曹操("曹操", 4, "WEI001", Gender.MALE, Faction.WEI),
    孫權("孫權", 4, "WU001", Gender.MALE, Faction.WU),
    關羽("關羽", 4, "SHU002", Gender.MALE, Faction.SHU),
    張飛("張飛", 4, "SHU003", Gender.MALE, Faction.SHU),
    馬超("馬超", 4, "SHU006", Gender.MALE, Faction.SHU),
    趙雲("趙雲", 4, "SHU005", Gender.MALE, Faction.SHU),
    黃月英("黃月英", 3, "SHU007", Gender.FEMALE, Faction.SHU),
    諸葛亮("諸葛亮", 3, "SHU004", Gender.MALE, Faction.SHU),
    司馬懿("司馬懿", 3, "WEI002", Gender.MALE, Faction.WEI),
    夏侯惇("夏侯惇", 4, "WEI003", Gender.MALE, Faction.WEI),
    許褚("許褚", 4, "WEI005", Gender.MALE, Faction.WEI),
    郭嘉("郭嘉", 3, "WEI006", Gender.MALE, Faction.WEI),
    甄姬("甄姬", 3, "WEI007", Gender.FEMALE, Faction.WEI),
    張遼("張遼", 4, "WEI004", Gender.MALE, Faction.WEI),
    甘寧("甘寧", 4, "WU002", Gender.MALE, Faction.WU),
    呂蒙("呂蒙", 4, "WU003", Gender.MALE, Faction.WU),
    黃蓋("黃蓋", 4, "WU004", Gender.MALE, Faction.WU),
    周瑜("周瑜", 3, "WU005", Gender.MALE, Faction.WU),
    大喬("大喬", 3, "WU006", Gender.FEMALE, Faction.WU),
    陸遜("陸遜", 3, "WU007", Gender.MALE, Faction.WU),
    孫尚香("孫尚香", 3, "WU008", Gender.FEMALE, Faction.WU);

    public final String generalName;
    public final int healthPoint;
    public final String generalId;
    public final Gender gender;
    public final Faction faction;

    private static final Map<String, General> GENERAL_MAP = new HashMap<>();

    static {
        for (General general : General.values()) {
            GENERAL_MAP.put(general.getGeneralId(), general);
        }
    }

    General(String generalName, int healthPoint, String generalId, Gender gender, Faction faction) {
        this.generalName = generalName;
        this.healthPoint = healthPoint;
        this.generalId = generalId;
        this.gender = gender;
        this.faction = faction;
    }

    public static General findById(String generalId) {
        General general = GENERAL_MAP.get(generalId);
        if (general == null) {
            throw new IllegalArgumentException("General with ID " + generalId + " not found.");
        }
        return general;
    }

    public String getGeneralName() {
        return generalName;
    }

    public String getGeneralId() {
        return generalId;
    }

    public int getHealthPoint() {
        return healthPoint;
    }

    public Gender getGender() {
        return gender;
    }

    public Faction getFaction() {
        return faction;
    }
}
