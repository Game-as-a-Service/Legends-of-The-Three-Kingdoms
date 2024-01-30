package com.gaas.threeKingdoms.generalcard;

public enum General {
    劉備("劉備",4, "SHU001"),
    曹操("曹操",4,"WEI001"),
    孫權("孫權",4,"WU001"),
    關羽("關羽",4, "SHU002"),
    張飛("張飛",4, "SHU003"),
    馬超("馬超",4,"SHU006"),
    趙雲("趙雲",4,"SHU005"),
    黃月英("黃月英",3,"SHU007"),
    諸葛亮("諸葛亮",3,"SHU004"),
    黃忠("黃忠",4,"SHU008"),
    魏延("魏延",4,"SHU009"),
    司馬懿("司馬懿",3, "WEI002"),
    夏侯敦("夏侯敦",4, "WEI003"),
    許褚("許褚",4, "WEI005"),
    郭嘉("郭嘉",3, "WEI006"),
    甄姬("甄姬",3, "WEI007"),
    張遼("張遼",4, "WEI004"),
    甘寧("甘寧",4, "WU002"),
    呂蒙("呂蒙",4, "WU003"),
    黃蓋("黃蓋",4, "WU004"),
    周瑜("周瑜",3, "WU005"),
    大喬("大喬",3, "WU006"),
    陸遜("陸遜",3, "WU007"),
    孫尚香("孫尚香",3, "WU008"),
    華佗("華佗",3, "QUN001"),
    呂布("呂布",4, "QUN002"),
    貂蟬("貂蟬",3, "QUN003");

    private final String generalName;
    private final int healthPoint;
    private final String generalId;

    General(String generalName, int healthPoint, String generalId) {
        this.generalName = generalName;
        this.healthPoint = healthPoint;
        this.generalId = generalId;
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
}
