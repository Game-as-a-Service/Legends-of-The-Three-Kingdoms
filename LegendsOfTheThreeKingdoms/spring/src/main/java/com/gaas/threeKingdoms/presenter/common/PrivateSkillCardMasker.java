package com.gaas.threeKingdoms.presenter.common;

import com.gaas.threeKingdoms.presenter.RoundStartPresenter;
import com.gaas.threeKingdoms.presenter.UseSkillEffectPresenter;
import com.gaas.threeKingdoms.presenter.ViewModel;
import com.gaas.threeKingdoms.skill.shu.GuanXingSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 只有發動者能看的技能牌資訊（per-player 推播遮蔽）。
 *
 * 觀星：AskSkillEffectEvent.dataCardIds（觀看的牌堆頂）與 SkillEffectEvent.dataCardIds（重排後放回堆頂的順序）
 * 只有諸葛亮本人（data.playerId）能看；其他玩家收到同一個 event、但 dataCardIds 為空 list
 * （張數由 event / GameStatus message 告知）。諸葛亮本人的 payload 不變。
 *
 * 摸牌：DrawCardEvent.cards 只有摸牌者本人能看（其他玩家只看 size）。多數 presenter 已各自以
 * hiddenOtherPlayerCardIds 遮蔽，但技能結算（如觀星 ARRANGE 後接續摸牌）走的 presenter 沒有，
 * 會把剛排好的堆頂牌推給所有人；這裡統一補上（只會遮得更多，不會讓原本看不到的人看到）。
 *
 * 由每個 per-player GameViewModel 的建構子呼叫（{@code GameProcessViewModel(events, data, message, viewerPlayerId)}），
 * 因此任何會推進到諸葛亮回合開始的 presenter（結束出牌、棄牌、開局…）都會套用。
 */
public final class PrivateSkillCardMasker {

    /** dataCardIds 僅限發動者本人可見的技能。 */
    static final Set<String> OWNER_ONLY_CARD_SKILLS = Set.of(GuanXingSkill.SKILL_NAME);

    private PrivateSkillCardMasker() {
    }

    /**
     * 回傳 viewer 視角的 view models；無需遮蔽時回傳原 list（不複製）。
     */
    public static List<ViewModel<?>> maskFor(List<ViewModel<?>> viewModels, String viewerPlayerId) {
        if (viewModels == null || viewerPlayerId == null || viewModels.stream().noneMatch(vm -> needsMask(vm, viewerPlayerId))) {
            return viewModels;
        }
        List<ViewModel<?>> masked = new ArrayList<>(viewModels.size());
        for (ViewModel<?> vm : viewModels) {
            masked.add(needsMask(vm, viewerPlayerId) ? mask(vm) : vm);
        }
        return masked;
    }

    private static boolean needsMask(ViewModel<?> vm, String viewerPlayerId) {
        if (vm instanceof RoundStartPresenter.DrawCardViewModel draw && draw.getData() != null) {
            List<String> cards = draw.getData().getCards();
            return cards != null && !cards.isEmpty() && !viewerPlayerId.equals(draw.getData().getDrawCardPlayerId());
        }
        if (vm instanceof UseSkillEffectPresenter.AskSkillEffectViewModel ask && ask.getData() != null) {
            return isOthersPrivateCards(ask.getData().getSkillName(), ask.getData().getPlayerId(), viewerPlayerId);
        }
        if (vm instanceof UseSkillEffectPresenter.SkillEffectViewModel effect && effect.getData() != null) {
            return isOthersPrivateCards(effect.getData().getSkillName(), effect.getData().getPlayerId(), viewerPlayerId);
        }
        return false;
    }

    private static boolean isOthersPrivateCards(String skillName, String ownerPlayerId, String viewerPlayerId) {
        return OWNER_ONLY_CARD_SKILLS.contains(skillName) && !viewerPlayerId.equals(ownerPlayerId);
    }

    private static ViewModel<?> mask(ViewModel<?> vm) {
        if (vm instanceof RoundStartPresenter.DrawCardViewModel draw) {
            RoundStartPresenter.DrawCardDataViewModel d = draw.getData();
            return new RoundStartPresenter.DrawCardViewModel(
                    new RoundStartPresenter.DrawCardDataViewModel(d.getSize(), new ArrayList<>(), d.getDrawCardPlayerId()));
        }
        if (vm instanceof UseSkillEffectPresenter.AskSkillEffectViewModel ask) {
            UseSkillEffectPresenter.AskSkillEffectDataViewModel d = ask.getData();
            return new UseSkillEffectPresenter.AskSkillEffectViewModel(
                    new UseSkillEffectPresenter.AskSkillEffectDataViewModel(
                            d.getSkillName(), d.getPlayerId(), List.of(), d.getDataPlayerId(), d.getOptions()),
                    ask.getMessage());
        }
        UseSkillEffectPresenter.SkillEffectViewModel effect = (UseSkillEffectPresenter.SkillEffectViewModel) vm;
        UseSkillEffectPresenter.SkillEffectDataViewModel d = effect.getData();
        return new UseSkillEffectPresenter.SkillEffectViewModel(
                new UseSkillEffectPresenter.SkillEffectDataViewModel(
                        d.getSkillName(), d.getPlayerId(), d.isAccepted(), List.of(), d.getDataPlayerId()),
                effect.getMessage());
    }
}
