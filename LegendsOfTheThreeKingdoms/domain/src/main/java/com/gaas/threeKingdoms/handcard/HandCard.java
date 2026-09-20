package com.gaas.threeKingdoms.handcard;


import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class HandCard {
    protected String name;
    protected String id;
    protected Suit suit;
    protected Rank rank;

    public HandCard(PlayCard playCard) {
        this.name = playCard.getCardName();
        this.id = playCard.getCardId();
        this.suit = playCard.getSuit();
        this.rank = playCard.getRank();
    }

    public abstract void effect(Player player);

    /**
     * 判定牌的人類可讀描述：花色 + 點數 + 牌名（如「紅心5 桃」）。
     * 給判定類事件的 message 用 —— 前端的遊戲 log 只顯示 message，
     * 光寫「成功／失敗」看不出是哪張牌造成的（issue #235 起的慣例，見八卦陣／鐵騎／洛神）。
     */
    public String judgementDescription() {
        return suit.getDisplayName() + rank.getRepresentation() + " " + name;
    }

}
