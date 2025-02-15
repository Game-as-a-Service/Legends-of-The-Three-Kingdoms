package com.gaas.threeKingdoms.rolecard;

public enum Role {
    MONARCH("Monarch", "主公"), //主公
    MINISTER("Minister", "忠臣"), //忠臣
    REBEL("Rebel", "反賊"), // 反賊
    TRAITOR("Traitor", "內奸"); // 內奸

    private final String role;
    private final String chineseRoleName;

    Role(String role, String chineseRoleName) {
        this.role = role;
        this.chineseRoleName = chineseRoleName;
    }

    public String getRoleName() {
        return role;
    }
    public String getChinesRoleName() {
        return chineseRoleName;
    }
}