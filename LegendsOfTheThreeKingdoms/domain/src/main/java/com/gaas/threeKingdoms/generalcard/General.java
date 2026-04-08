package com.gaas.threeKingdoms.generalcard;

import java.util.HashMap;
import java.util.Map;

public enum General {
    劉備("劉備",4, "SHU001", Gender.MALE),
    曹操("曹操",4,"WEI001", Gender.MALE),
    孫權("孫權",4,"WU001", Gender.MALE),
    關羽("關羽",4, "SHU002", Gender.MALE),
    張飛("張飛",4, "SHU003", Gender.MALE),
    馬超("馬超",4,"SHU006", Gender.MALE),
    趙雲("趙雲",4,"SHU005", Gender.MALE),
    黃月英("黃月英",3,"SHU007", Gender.FEMALE),
    諸葛亮("諸葛亮",3,"SHU004", Gender.MALE),
    黃忠("黃忠",4,"SHU008", Gender.MALE),
    魏延("魏延",4,"SHU009", Gender.MALE),
    司馬懿("司馬懿",3, "WEI002", Gender.MALE),
    夏侯敦("夏侯敦",4, "WEI003", Gender.MALE),
    許褚("許褚",4, "WEI005", Gender.MALE),
    郭嘉("郭嘉",3, "WEI006", Gender.MALE),
    甄姬("甄姬",3, "WEI007", Gender.FEMALE),
    張遼("張遼",4, "WEI004", Gender.MALE),
    甘寧("甘寧",4, "WU002", Gender.MALE),
    呂蒙("呂蒙",4, "WU003", Gender.MALE),
    黃蓋("黃蓋",4, "WU004", Gender.MALE),
    周瑜("周瑜",3, "WU005", Gender.MALE),
    大喬("大喬",3, "WU006", Gender.FEMALE),
    陸遜("陸遜",3, "WU007", Gender.MALE),
    孫尚香("孫尚香",3, "WU008", Gender.FEMALE),
    華佗("華佗",3, "QUN001", Gender.MALE),
    呂布("呂布",4, "QUN002", Gender.MALE),
    貂蟬("貂蟬",3, "QUN003", Gender.FEMALE);

    public final String generalName;
    public final int healthPoint;
    public final String generalId;
    public final Gender gender;

    private static final Map<String, General> GENERAL_MAP = new HashMap<>();

    static {
        for (General general : General.values()) {
            GENERAL_MAP.put(general.getGeneralId(), general);
        }
    }

    General(String generalName, int healthPoint, String generalId, Gender gender) {
        this.generalName = generalName;
        this.healthPoint = healthPoint;
        this.generalId = generalId;
        this.gender = gender;
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
}
