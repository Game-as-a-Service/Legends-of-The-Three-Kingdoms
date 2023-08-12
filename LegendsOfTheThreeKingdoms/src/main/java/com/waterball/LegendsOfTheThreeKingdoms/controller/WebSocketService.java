package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.waterball.LegendsOfTheThreeKingdoms.presenter.GeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.MonarchGeneralCardPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void pushGeneralsCardEvent(GeneralCardPresenter presenter) {
        GeneralCardPresenter.GeneralCardViewModel generalCardViewModel = presenter.present();
        messagingTemplate.convertAndSend("/topic/generalCardEvent", generalCardViewModel);
    }

    public MonarchGeneralCardPresenter.MonarchGeneralViewModel pushMonarchGeneralEvent(MonarchGeneralCardPresenter presenter){
        MonarchGeneralCardPresenter.MonarchGeneralViewModel monarchGeneralViewModel = presenter.present();
        messagingTemplate.convertAndSend("/topic/monarchGeneralCardEvent",monarchGeneralViewModel);
        return monarchGeneralViewModel;
    }
}
