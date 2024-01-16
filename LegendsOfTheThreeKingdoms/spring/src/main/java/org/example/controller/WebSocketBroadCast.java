package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.*;
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

    public void pushFindGameEvent(FindGamePresenter presenter) {
        FindGamePresenter.FindGameViewModel findGameViewModel = presenter.present();
        try {
            String findGameJson = objectMapper.writeValueAsString(findGameViewModel);
            messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", findGameViewModel.getGameId(), findGameViewModel.getPlayerId()), findGameJson);
        } catch (Exception e) {
            System.err.println("****************** pushFindGameEvent ");
            e.printStackTrace();
        }
    }

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

    public void pushInitialEndEvent(InitialEndPresenter presenter) {
        List<InitialEndPresenter.InitialEndViewModel> initialEndViewModels = presenter.present();
        try {
            for (InitialEndPresenter.InitialEndViewModel initialEndViewModel : initialEndViewModels) {
                String initialEndJson = objectMapper.writeValueAsString(initialEndViewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", initialEndViewModel.getGameId(), initialEndViewModel.getPlayerId()), initialEndJson);
            }
        } catch (Exception e) {
            System.err.println("****************** pushInitialEndEventd");
            e.printStackTrace();
        }
    }

    public void pushPlayerTakeTurnEvent(RoundStartPresenter presenter) {
        List<RoundStartPresenter.PlayerTakeTurnViewModel> roundStartViewModels = presenter.present();
        try {
            for (RoundStartPresenter.PlayerTakeTurnViewModel roundStartViewModel : roundStartViewModels) {
                String playerTakeTurnJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(roundStartViewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", roundStartViewModel.getGameId(), roundStartViewModel.getPlayerId()), playerTakeTurnJson);
            }
        } catch (Exception e) {
            System.err.println("****************** pushRoundStartEvent ");
            e.printStackTrace();
        }
    }

    public void pushPlayerCardEvent(PlayCardPresenter presenter) {
        List<PlayCardPresenter.GameViewModel> playCardViewModels = presenter.present();
        try {
            for (PlayCardPresenter.GameViewModel playCardViewModel : playCardViewModels) {
                String playerCardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playCardViewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", playCardViewModel.getGameId(), playCardViewModel.getPlayerId()), playerCardJson);
            }
        } catch (Exception e) {
            System.err.println("****************** pushPlayerCardEvent ");
            e.printStackTrace();
        }
    }

    public void pushFinishActionEvent(FinishActionPresenter presenter) {
        List<FinishActionPresenter.GameViewModel> finishActionViewModels = presenter.present();
        try {
            for (FinishActionPresenter.GameViewModel finishActionViewModel : finishActionViewModels) {
                String finishActionJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(finishActionViewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", finishActionViewModel.getGameId(), finishActionViewModel.getPlayerId()), finishActionJson);
            }
        } catch (Exception e) {
            System.err.println("****************** pushFinishActionEvent ");
            e.printStackTrace();
        }
    }

    public void pushDiscardEvent(DiscardPresenter presenter) {
        List<DiscardPresenter.GameViewModel> discardViewModels = presenter.present();
        try {
            for (DiscardPresenter.GameViewModel discardViewModel : discardViewModels) {
                String discardCardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(discardViewModel);
                messagingTemplate.convertAndSend(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", discardViewModel.getGameId(), discardViewModel.getPlayerId()), discardCardJson);
            }
        } catch (Exception e) {
            System.err.println("****************** pushDiscardCardEvent ");
            e.printStackTrace();
        }
    }
}
