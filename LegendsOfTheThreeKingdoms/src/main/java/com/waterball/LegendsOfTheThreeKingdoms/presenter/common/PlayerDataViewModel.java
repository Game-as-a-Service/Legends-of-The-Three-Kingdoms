package com.waterball.LegendsOfTheThreeKingdoms.presenter.common;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.HandEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayerEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDataViewModel {
    private String id;
    private String generalId;
    private String roleId;
    private int hp;
    private HandEvent hand;
    private List<String> equipments;
    private List<String> delayScrolls;

    public PlayerDataViewModel(PlayerEvent playerEvent) {
        this.id = playerEvent.getId();
        this.generalId = playerEvent.getGeneralId();
        this.roleId = playerEvent.getRoleId();
        this.hp = playerEvent.getHp();
        this.hand = playerEvent.getHand();
        this.equipments = playerEvent.getEquipments();
        this.delayScrolls = playerEvent.getDelayScrolls();
    }

    public static PlayerDataViewModel deepCopy(PlayerDataViewModel p) {
        return new PlayerDataViewModel(p.getId(), p.getGeneralId(), p.getRoleId(), p.getHp(), HandEvent.deepCopy(p.getHand()), new ArrayList<>(p.getEquipments()), new ArrayList<>(p.getDelayScrolls()));
    }

    public static List<PlayerDataViewModel> hiddenOtherPlayerRoleInformation(List<PlayerDataViewModel> viewModels, String targetPlayerId) {
        List<PlayerDataViewModel> playerDataViewModels = new ArrayList<>();

        for (PlayerDataViewModel viewModel : viewModels) {
            playerDataViewModels.add(PlayerDataViewModel.deepCopy(viewModel));
        }

        for (int i = 0; i < playerDataViewModels.size(); i++) {
            PlayerDataViewModel viewModel = playerDataViewModels.get(i);
            int size = viewModel.getHand().getSize();
            if (isNotTargetPlayer(targetPlayerId, viewModel)) {
                if (isNotMonarch(viewModel)) {
                    viewModel.setRoleId("");
                }
                viewModel.setHand(new HandEvent(size, new ArrayList<>()));
            }
        }
        return playerDataViewModels;
    }

    public static boolean isNotTargetPlayer(String playerId, PlayerDataViewModel viewModel) {
        return !viewModel.getId().equals(playerId);
    }

    public static boolean isNotMonarch(PlayerDataViewModel viewModel) {
        return !viewModel.getRoleId().equals(Role.MONARCH.getRole());
    }

    public static boolean isMonarch(PlayerDataViewModel viewModel) {
        return viewModel.getRoleId().equals(Role.MONARCH.getRole());
    }
}