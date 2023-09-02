package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.CreateGamePresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GetGeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.MonarchChooseGeneralCardPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketBroadCast {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void pushCreateGameEventToAllPlayers(CreateGamePresenter presenter) {
        List<CreateGamePresenter.CreateGameViewModel> createGameViewModels = presenter.present();
        createGameViewModels.forEach(viewModel -> {
            try {
                String createGameJson = objectMapper.writeValueAsString(viewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", viewModel.getGameId(), viewModel.getPlayerId()), createGameJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("推播createGameEvent錯誤");
            }
        });
    }

    public void pushMonarchGetGeneralCardsEvent(GetGeneralCardPresenter presenter) {
        GetGeneralCardPresenter.GetGeneralCardViewModel getGeneralCardViewModel = presenter.present();
        try {
            String generalCardMessage = objectMapper.writeValueAsString(getGeneralCardViewModel);
            messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", getGeneralCardViewModel.getGameId(), getGeneralCardViewModel.getPlayerId()), generalCardMessage);
        } catch (JsonProcessingException e) {
            System.err.println("****************** pushGetGeneralCardEvent ");
            e.printStackTrace();
        }
    }

    public void pushMonarchChooseGeneralsCardEvent(MonarchChooseGeneralCardPresenter presenter) {
        MonarchChooseGeneralCardPresenter.MonarchChooseGeneralCardViewModel monarchChooseGeneralCardViewModel = presenter.present();

        List<String> playerIds = monarchChooseGeneralCardViewModel.getPlayerIds();
        List<MonarchChooseGeneralCardPresenter.GetGeneralCardByOthersViewModel> getGeneralCardByOthersViewModels = presenter.presentGeneralCardByOthers();

        try {
            String monarchChooseGeneralJson = objectMapper.writeValueAsString(monarchChooseGeneralCardViewModel);
            playerIds.forEach(playerId -> {
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", monarchChooseGeneralCardViewModel.getGameId(), playerId), monarchChooseGeneralJson);
            });

            for (MonarchChooseGeneralCardPresenter.GetGeneralCardByOthersViewModel getGeneralCardByOthersViewModel : getGeneralCardByOthersViewModels) {
                String json = objectMapper.writeValueAsString(getGeneralCardByOthersViewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", getGeneralCardByOthersViewModel.getGameId(), getGeneralCardByOthersViewModel.getPlayerId()), json);
            }


        } catch (Exception e) {
            System.err.println("****************** pushGeneralsCardEvent ");
            e.printStackTrace();
        }
    }
}
