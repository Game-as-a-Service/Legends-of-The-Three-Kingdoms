package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.MonarchGeneralCardPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketBroadCast {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ObjectMapper objectMapper;


    public void pushGeneralsCardEvent(GeneralCardPresenter presenter) {
        try {
            GeneralCardPresenter.GeneralCardViewModel generalCardViewModel = presenter.present();
            String s = objectMapper.writeValueAsString(generalCardViewModel);
            messagingTemplate.convertAndSend(String.format("/topic/generalCardEvent/%s", generalCardViewModel.getGameId()), s);
        } catch (Exception e) {
            System.err.println("****************** pushGeneralsCardEvent ");
            e.printStackTrace();
        }
    }

    public MonarchGeneralCardPresenter.MonarchGeneralViewModel pushMonarchGeneralEvent(MonarchGeneralCardPresenter presenter) {
        MonarchGeneralCardPresenter.MonarchGeneralViewModel monarchGeneralViewModel = presenter.present();
        messagingTemplate.convertAndSend("/topic/monarchGeneralCardEvent", monarchGeneralViewModel);
        return monarchGeneralViewModel;
    }
}
