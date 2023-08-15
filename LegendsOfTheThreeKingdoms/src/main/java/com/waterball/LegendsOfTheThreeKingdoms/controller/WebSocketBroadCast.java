package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.CreateGamePresenter;
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

    public void pushCreateGameEvent(CreateGamePresenter presenter) {
        List<CreateGamePresenter.CreateGameViewModel> createGameViewModels = presenter.present();
        createGameViewModels.forEach(viewModel -> {
            try {
                String createGameMessage = objectMapper.writeValueAsString(viewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", viewModel.getGameId(), viewModel.getPlayerId()), createGameMessage);
            } catch (JsonProcessingException e) {
                System.err.println("****************** pushCreateGameEvent ");
                e.printStackTrace();
            }
        });
    }

    public void pushGeneralsCardEvent(GeneralCardPresenter presenter) {
        try {
            GeneralCardPresenter.GeneralCardViewModel generalCardViewModel = presenter.present();
            String generalCardMessage = objectMapper.writeValueAsString(generalCardViewModel);
            generalCardViewModel.getPlayers().forEach(player -> {
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", generalCardViewModel.getGameId(), player.getId()), generalCardMessage);
            });
        } catch (Exception e) {
            System.err.println("****************** pushGeneralsCardEvent ");
            e.printStackTrace();
        }
    }

}
