package com.gaas.threeKingdoms.handcard.basiccard;

import java.util.List;

/**
 * 虛擬殺 — 由武器效果產生的殺（如丈八蛇矛），不對應 PlayCard enum 中的實體牌。
 * 效果同一般殺：扣 1 滴血。
 *
 * TODO: 目前的 VIRTUAL_CARD_ID ("VIPER_SPEAR_VIRTUAL_KILL") 把 ViperSpear 語意綁進了
 * 通用的 VirtualKill class。當未來方天畫戟等武器也需要虛擬殺時，需要重構為以下其中一種：
 *   (a) 改為通用 id ("VIRTUAL_KILL")，並把語意從 class name 移除
 *   (b) 讓 VirtualKill 設為 abstract，由子類（如 ViperSpearVirtualKill）指定各自的 id
 */
public class VirtualKill extends Kill {

    public static final String VIRTUAL_CARD_ID = "VIPER_SPEAR_VIRTUAL_KILL";
    public static final String VIRTUAL_CARD_NAME = "殺（虛擬）";

    /**
     * 轉化殺（武聖 / 龍膽 等）的真實來源牌 id。奸雄要獲得「造成傷害的牌」＝這些實體牌。
     * 武器虛擬殺（丈八蛇矛）不填 — 其棄牌由 ViperSpearKillBehavior 持有。
     */
    private final List<String> sourceCardIds;

    public VirtualKill() {
        this(List.of());
    }

    public VirtualKill(List<String> sourceCardIds) {
        super(VIRTUAL_CARD_NAME, VIRTUAL_CARD_ID, null, null);
        this.sourceCardIds = sourceCardIds == null ? List.of() : List.copyOf(sourceCardIds);
    }

    public List<String> getSourceCardIds() {
        return sourceCardIds;
    }
}
