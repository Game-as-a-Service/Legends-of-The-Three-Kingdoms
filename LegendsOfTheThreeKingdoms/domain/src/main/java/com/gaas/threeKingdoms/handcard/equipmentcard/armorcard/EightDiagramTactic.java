package com.gaas.threeKingdoms.handcard.equipmentcard.armorcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.Suit;

import java.util.ArrayList;
import java.util.List;

public class EightDiagramTactic extends ArmorCard {

    public EightDiagramTactic(PlayCard playCard) {
        super(playCard);
        hasSpecialEffect = true;
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        List<HandCard> cards = game.drawCardForCardEffect(1);
        return resolveEquipmentEffect(game, cards.get(0));
    }

    /** 以指定判定牌結算八卦陣（鬼才替換後 / 無鬼才直接）。 */
    public List<DomainEvent> resolveEquipmentEffect(Game game, HandCard card) {
        boolean isEffectSuccess = isEffectSuccess(card);
        List<DomainEvent> events = new ArrayList<>();
        Round currentRound = game.getCurrentRound();
        if (isEffectSuccess) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        }
        events.add(new EightDiagramTacticEffectEvent(judgementMessage(card, isEffectSuccess), isEffectSuccess, card.getId()));
        return events;
    }

    /**
     * 判定結果的具體文字。前端遊戲 log 只顯示 message，原本固定是「發動效果 成功／失敗」，
     * 看不出是哪張牌造成成功或失敗（使用者回報）。格式對齊鐵騎／洛神（issue #235、#244）。
     */
    private String judgementMessage(HandCard card, boolean isEffectSuccess) {
        return isEffectSuccess
                ? String.format("八卦陣判定：%s → 紅色，視為出閃", card.judgementDescription())
                : String.format("八卦陣判定：%s → 黑色，未生效，照常問閃", card.judgementDescription());
    }

    private boolean isEffectSuccess(HandCard card) {
        return Suit.DIAMOND == card.getSuit() || Suit.HEART == card.getSuit();
    }
}
