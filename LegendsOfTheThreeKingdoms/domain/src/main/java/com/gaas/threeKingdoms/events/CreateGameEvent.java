package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class CreateGameEvent extends DomainEvent {
      private String gameId;
      private String name = "createGameEvent";
      private List<Player> players;
      private String message = "請選擇武將";

     public CreateGameEvent(String gameId,List<Player> players){
           this.gameId = gameId;
           this.players = players;
     }
}
