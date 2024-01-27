package com.gaas.threeKingdoms.rolecard;

public enum Role {
    MONARCH("Monarch"), //主公
    MINISTER("Minister"), //忠臣
    REBEL("Rebel"), // 反賊
    TRAITOR("Traitor"); // 內奸

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRoleName() {
        return role;
    }
}