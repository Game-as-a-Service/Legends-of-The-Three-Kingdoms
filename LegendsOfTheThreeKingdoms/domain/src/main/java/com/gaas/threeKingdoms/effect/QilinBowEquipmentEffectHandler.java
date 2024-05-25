package com.gaas.threeKingdoms.effect;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.behavior.Behavior;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QilinBowEquipmentEffectHandler  extends EquipmentEffectHandler {

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
        Round currentRound = game.getCurrentRound();
        Stage currentStage = currentRound.getStage();
        if (!currentStage.equals(Stage.Wait_Equipment_Effect)) {
            throw new IllegalStateException(String.format("CurrentRound stage not Wait_Equipment_Effect but [%s]", currentStage));
        }
        currentRound.setStage(Stage.Normal);

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
                return List.of(playerDamagedEvent, playerDyingEvent, askPeachEvent);
            }
        }
        Player player = getPlayer(playerId);
        WeaponCard weaponCard = player.getEquipment().getWeapon();

        List<DomainEvent> domainEvents = weaponCard.equipmentEffect(game);

        //這時候 TopBehavior 應該是 NormalActiveKill
        game.peekTopBehavior().setIsOneRound(true);
        return domainEvents;
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
