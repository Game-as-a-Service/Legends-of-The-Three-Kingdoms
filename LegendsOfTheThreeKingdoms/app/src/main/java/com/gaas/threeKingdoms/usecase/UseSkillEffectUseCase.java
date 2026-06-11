package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.exception.NotFoundException;
import com.gaas.threeKingdoms.outport.GameRepository;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 通用武將技回應 usecase — 配合 WaitingSkillEffectBehavior，
 * 單一 endpoint 服務所有 ACCEPT/SKIP/自訂選擇型技能（反饋/遺計/剛烈...）。
 */
@RequiredArgsConstructor
@Named
public class UseSkillEffectUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseSkillEffectRequest request, UseSkillEffectPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseSkillEffect(
                request.playerId, request.skillName, request.choice,
                request.cardIds, request.targetPlayerId);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseSkillEffectRequest {
        private String playerId;
        private String skillName;
        private String choice;            // ACCEPT / SKIP / 技能自訂（GIVE / DISCARD / DAMAGE ...）
        private List<String> cardIds;     // 可為 null
        private String targetPlayerId;    // 可為 null
    }

    public interface UseSkillEffectPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
