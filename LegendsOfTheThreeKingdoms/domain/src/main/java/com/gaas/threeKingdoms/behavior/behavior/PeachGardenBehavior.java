package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PeachEvent;
import com.gaas.threeKingdoms.events.PeachGardenEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PeachGardenBehavior extends Behavior {

    public PeachGardenBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true, false);
    }

    @Override
    public boolean isOneRoundDefault(){return true;}

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);

        List<PeachEvent> peachEvents = game.getPlayers().stream().map(player -> {
            int originHp = player.getHP();
            card.effect(player);
            return new PeachEvent(player.getId(), originHp, player.getHP());
        }).collect(Collectors.toList());

        return List.of(
                game.getGameStatusEvent("出牌"),
                new PlayCardEvent(
                        "出牌",
                        behaviorPlayer.getId(),
                        "",
                        cardId,
                        playType),
                new PeachGardenEvent(behaviorPlayer, peachEvents)
        );
    }
}