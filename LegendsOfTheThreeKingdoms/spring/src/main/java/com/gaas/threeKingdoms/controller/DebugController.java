package com.gaas.threeKingdoms.controller;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.controller.dto.DeckResponse;
import com.gaas.threeKingdoms.controller.dto.SetDeckRequest;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.outport.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class DebugController {

    private final GameRepository gameRepository;

    @GetMapping("/api/debug/games/{gameId}/deck")
    public ResponseEntity<?> getDeck(@PathVariable String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found: " + gameId));

        List<HandCard> cards = game.getDeck().getCardDeck();
        // Stack pop 取最後一個元素，所以反轉顯示 draw order（index 0 = 下一張抽的）
        List<String> drawOrder = new ArrayList<>();
        for (int i = cards.size() - 1; i >= 0; i--) {
            drawOrder.add(cards.get(i).getId());
        }

        return ResponseEntity.ok(new DeckResponse(gameId, drawOrder.size(), drawOrder));
    }

    @PutMapping("/api/debug/games/{gameId}/deck")
    public ResponseEntity<?> setDeck(@PathVariable String gameId, @RequestBody SetDeckRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found: " + gameId));

        List<String> cardIds = request.getCardIds();
        if (cardIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "cardIds is required"));
        }

        // 反序 push，讓 cardIds[0] 在 stack top（下一張抽的）
        Stack<HandCard> stack = new Stack<>();
        for (int i = cardIds.size() - 1; i >= 0; i--) {
            String id = cardIds.get(i);
            HandCard card = PlayCard.findById(id);
            if (card == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid card ID: " + id));
            }
            stack.push(card);
        }

        Deck newDeck = new Deck();
        newDeck.setCardDeck(stack);
        game.setDeck(newDeck);
        gameRepository.save(game);

        return ResponseEntity.ok(new DeckResponse(gameId, cardIds.size(), cardIds));
    }
}
