package com.gaas.threeKingdoms.handcard.basiccard;

/**
 * 虛擬殺 — 由武器效果產生的殺（如丈八蛇矛），不對應 PlayCard enum 中的實體牌。
 * 效果同一般殺：扣 1 滴血。
 */
public class VirtualKill extends Kill {

    public static final String VIRTUAL_CARD_ID = "VIPER_SPEAR_VIRTUAL_KILL";
    public static final String VIRTUAL_CARD_NAME = "殺（虛擬）";

    public VirtualKill() {
        super(VIRTUAL_CARD_NAME, VIRTUAL_CARD_ID, null, null);
    }
}
