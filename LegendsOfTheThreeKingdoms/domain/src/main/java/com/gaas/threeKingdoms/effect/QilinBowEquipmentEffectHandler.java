package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingQilinBowResponseBehavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QilinBowEquipmentEffectHandler extends EquipmentEffectHandler {

    public QilinBowEquipmentEffectHandler(EquipmentEffectHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = Optional.ofNullable(player.getEquipmentWeaponCard());
        return card.filter(weaponCard -> weaponCard instanceof QilinBowCard && cardId.equals(weaponCard.getId())).isPresent();
    }

    @Override
    protected List<DomainEvent> doHandle(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        List<DomainEvent> events = new ArrayList<>();
        Round currentRound = game.getCurrentRound();
        Stage currentStage = currentRound.getStage();
        Behavior behavior = game.peekTopBehavior(); // NormalActiveKill
        if (!currentStage.equals(Stage.Wait_Equipment_Effect)) {
            throw new IllegalStateException(String.format("CurrentRound stage not Wait_Equipment_Effect but [%s]", currentStage));
        }
        if (skipEquipmentEffect(playType)) {
            Player damagedPlayer = game.getPlayer(behavior.getReactionPlayers().get(0));
            HandCard card = behavior.getCard(); // Kill
            int originalHp = damagedPlayer.getHP();
            card.effect(damagedPlayer);
            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);
            RoundEvent roundEvent;
            if (isPlayerStillAlive(damagedPlayer)) {
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                roundEvent = new RoundEvent(currentRound);
                behavior.setIsOneRound(true);
                events.add(playerDamagedEvent);
            } else {
                PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
                AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
                game.enterPhase(new GeneralDying(game));
                currentRound.setDyingPlayer(damagedPlayer);
                currentRound.setActivePlayer(damagedPlayer);
                roundEvent = new RoundEvent(currentRound);
                behavior.setIsOneRound(false);
                events.addAll(List.of(playerDamagedEvent, playerDyingEvent, askPeachEvent));
            }
            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            GameStatusEvent gameStatusEvent = new GameStatusEvent(game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName(), "發動");
            events.add(gameStatusEvent);
            return events;
        }

        Player player = getPlayer(playerId);
        WeaponCard weaponCard = player.getEquipment().getWeapon();
        boolean onlyHasOneMount = game.getPlayer(targetPlayerId).onlyHasOneMount();
        events = weaponCard.equipmentEffect(game);
        // 用作紀錄麒麟弓在遊戲內的狀態
        if (!onlyHasOneMount) {
            game.updateTopBehavior(new WaitingQilinBowResponseBehavior(game, behavior.getBehaviorPlayer(), Collections.singletonList(behavior.getBehaviorPlayer().getId()), behavior.getCurrentReactionPlayer(),
                    cardId, PlayType.QilinBow.getPlayType(), weaponCard));
        }

        return events;

    }

    private List<DomainEvent> getPlayerDamagedEvent(EquipmentPlayType playType, Round currentRound) {
        if (skipEquipmentEffect(playType)) {
            Behavior behavior = game.peekTopBehavior();
            Player damagedPlayer = game.getPlayer(behavior.getReactionPlayers().get(0));
            HandCard card = behavior.getCard(); // Kill
            int originalHp = damagedPlayer.getHP();
            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);
            card.effect(damagedPlayer);

            if (isPlayerStillAlive(damagedPlayer)) {
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                behavior.setIsOneRound(true);
                return List.of(playerDamagedEvent);
            } else {
                PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
                AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
                game.enterPhase(new GeneralDying(game));
                currentRound.setDyingPlayer(damagedPlayer);
                currentRound.setActivePlayer(damagedPlayer);
                RoundEvent roundEvent = new RoundEvent(currentRound);
                behavior.setIsOneRound(false);
                return List.of(playerDamagedEvent, playerDyingEvent, askPeachEvent, roundEvent);
            }
        }
        return null;
    }

    private boolean isPlayerStillAlive(Player damagedPlayer) {
        return damagedPlayer.getHP() > 0;
    }

    private PlayerDamagedEvent createPlayerDamagedEvent(int originalHp, Player damagedPlayer) {
        return new PlayerDamagedEvent(damagedPlayer.getId(), originalHp, damagedPlayer.getHP());
    }

    private PlayerDyingEvent createPlayerDyingEvent(Player player) {
        return new PlayerDyingEvent(player.getId());
    }

    private AskPeachEvent createAskPeachEvent(Player player, Player dyingPlayer) {
        return new AskPeachEvent(player.getId(), dyingPlayer.getId());
    }

    private boolean skipEquipmentEffect(EquipmentPlayType equipmentPlayType) {
        return equipmentPlayType.equals(EquipmentPlayType.SKIP);
    }

}
