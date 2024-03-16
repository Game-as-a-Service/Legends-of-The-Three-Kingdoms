package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class CreateGameEvent extends DomainEvent {
      private String gameId;
      private List<Player> players;

     public CreateGameEvent(String gameId,List<Player> players){
         super("createGameEvent", "請選擇武將");
           this.gameId = gameId;
           this.players = players;
     }
}
