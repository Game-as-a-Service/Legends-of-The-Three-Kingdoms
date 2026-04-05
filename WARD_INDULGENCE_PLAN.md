# Ward (無懈可擊) + Indulgence/Contentment (樂不思蜀) Implementation Plan

## 1. Current Flow: How Contentment Judgement Works

### Turn Start Sequence

When a player's turn begins, the game calls:

1. `Game.playerTakeTurn(player)` (line 156) sets `RoundPhase.Judgement` and calls `playerTakeTurnStartInJudgement(player)`.

2. `Game.playerTakeTurnStartInJudgement(player)` (line 165) calls `judgePlayerShouldDelay()` to process all delay scroll cards:
   ```java
   public List<DomainEvent> playerTakeTurnStartInJudgement(Player currentRoundPlayer) {
       List<DomainEvent> events = new ArrayList<>();
       if (RoundPhase.Judgement.equals(currentRound.getRoundPhase())) {
           List<DomainEvent> judgeEvents = judgePlayerShouldDelay();  // line 168
           events.addAll(judgeEvents);
           boolean contentmentEventSuccess = judgeEvents.stream()
                   .filter(event -> event instanceof ContentmentEvent)
                   .map(event -> (ContentmentEvent) event)
                   .anyMatch(ContentmentEvent::isSuccess);

           if (RoundPhase.Drawing.equals(currentRound.getRoundPhase())) {
               DomainEvent drawCardEvent = drawCardToPlayer(currentRoundPlayer, !contentmentEventSuccess);
               events.add(drawCardEvent);
               if (contentmentEventSuccess) {
                   events.addAll(finishAction(currentRoundPlayer.getId()));  // skip to Discard
               } else {
                   events.add(getGameStatusEvent(drawCardEvent.getMessage()));
               }
           }
       }
   ```
   **Critical observation:** This method checks `contentmentEventSuccess` from the returned events, then decides whether to skip Action phase. When Ward is involved, `judgePlayerShouldDelay()` will return early (due to `!topBehavior.isEmpty()`), so `RoundPhase` will still be `Judgement` -- the Drawing/Action logic won't execute yet. This is correct: the game pauses.

3. `Game.judgePlayerShouldDelay()` (line 445) is the key method:
   ```java
   private List<DomainEvent> judgePlayerShouldDelay() {
       Player player = currentRound.getCurrentRoundPlayer();
       List<DomainEvent> judgementEvents = new ArrayList<>();
       if (player.hasAnyDelayScrollCard()) {
           Stack<ScrollCard> delayCards = player.getDelayScrollCards();
           while (!delayCards.isEmpty()) {
               ScrollCard card = delayCards.pop();  // line 451: removes card from delay area
               currentRound.setCurrentCard(card);
               if (card instanceof Contentment) {
                   DomainEvent contentmentEvent = handleContentmentJudgement(player); // line 454
                   judgementEvents.add(contentmentEvent);
               } else if (card instanceof Lightning) {
                   List<DomainEvent> lightningEvents = handleLightningJudgement(card, player);
                   judgementEvents.addAll(lightningEvents);
               }
               if (!topBehavior.isEmpty()) { // line 461: early return if behaviors pending
                   return judgementEvents;
               }
           }
       }
       judgementEvents.add(new JudgementEvent());
       currentRound.setRoundPhase(RoundPhase.Drawing);  // line 468
       return judgementEvents;
   }
   ```

4. `Game.handleContentmentJudgement(player)` (line 513):
   ```java
   private ContentmentEvent handleContentmentJudgement(Player player) {
       List<HandCard> cards = drawCardForCardEffect(1);  // draws judgement card from deck
       HandCard drawnCard = cards.get(0);
       boolean contentmentSuccess = Suit.HEART != drawnCard.getSuit();  // Heart = fail
       return new ContentmentEvent(contentmentSuccess, player.getId(), drawnCard.getId(), drawnCard.getSuit());
   }
   ```

### Key Problem

The judgement card draw happens **immediately** with NO opportunity for anyone to play Ward. In real 三國殺 rules, **before** the judgement card is drawn, all players should be asked if they want to play Ward to cancel the Contentment effect entirely. If Ward cancels it, the judgement draw is skipped and the Contentment card is removed without effect.

### Why This Is the Same Problem Pattern as Other Cards

This is the same Ward integration pattern already implemented for:
- `BarbarianInvasionBehavior` -- Phase 1 cancel whole AoE, Phase 2 protect individual targets
- `ArrowBarrageBehavior` -- same Phase 1/Phase 2 pattern
- `BountifulHarvestBehavior` -- Phase 1 cancel whole harvest, Phase 2 protect individual pickers
- `DuelBehavior` -- single-target cancel

Contentment judgement is closest to the **single-target** pattern (like Duel), but with an important difference: it's **system-initiated** during Judgement phase, not player-initiated during Action phase.

### File Locations

| File | Path | Purpose |
|------|------|---------|
| Game.java | `domain/.../Game.java` | `judgePlayerShouldDelay()` (line 445), `handleContentmentJudgement()` (line 513), `playerTakeTurnStartInJudgement()` (line 165) |
| ContentmentBehavior.java | `domain/.../behavior/behavior/ContentmentBehavior.java` | Handles **playing** Contentment onto a target (Action phase) -- NOT the judgement |
| ContentmentEvent.java | `domain/.../events/ContentmentEvent.java` | Event for judgement result (has `drawCardId`, `suit`, `isSuccess`) |
| WardBehavior.java | `domain/.../behavior/behavior/WardBehavior.java` | Core Ward counter-chain: `playerAction()`, `doResponseToPlayerAction()`, `doBehaviorAction()` |
| Round.java | `domain/.../round/Round.java` | Round state: `RoundPhase`, `Stage`, `activePlayer`, `currentRoundPlayer` |
| BehaviorData.java | `spring/.../repository/data/BehaviorData.java` | Serialization/deserialization of all Behavior types |

---

## 2. Target Flow: Where Ward Check Should Be Inserted

### Why Ward Must Go BEFORE the Judgement Card Draw

In 三國殺 rules, Ward (無懈可擊) targets the **scroll card itself**, not the judgement result. The purpose of Ward against Contentment is to prevent the entire judgement from happening -- the Contentment card is removed from the delay area without any effect.

If Ward were inserted after the draw, the judgement card would already have been consumed from the deck, which would incorrectly alter game state even when Ward cancels the effect. The Ward check must happen before `drawCardForCardEffect(1)` is called.

### Proposed Flow

```
Player turn starts
  -> playerTakeTurn()
    -> playerTakeTurnStartInJudgement()
      -> judgePlayerShouldDelay()
        -> For each delay scroll card (delayCards.pop()):
          -> If card is Contentment:
            -> [NEW] Check doesAnyPlayerHaveWard(null)  // null = no one excluded
              -> YES: Create ContentmentJudgementBehavior, push onto topBehavior stack
                      ContentmentJudgementBehavior.playerAction() creates WardBehavior,
                      pushes it on top, emits WaitForWardEvent
                      judgePlayerShouldDelay() returns early (topBehavior not empty)
                      Game pauses, waits for Ward responses via playWardCard API
              -> NO:  Proceed to handleContentmentJudgement() as before (draw card, judge)
```

### What Happens When Ward Responses Complete

The WardBehavior resolves (all players skip or counter-chain ends) and calls `doBehaviorAction()` on `ContentmentJudgementBehavior`:

**Ward count is EVEN (effect proceeds):**
1. `ContentmentJudgementBehavior.doBehaviorAction()` calls `game.handleContentmentJudgement(player)` to draw judgement card.
2. Emits `ContentmentEvent` with the result.
3. Calls `game.playerTakeTurnStartInJudgement(player)` to resume the turn flow.
4. `playerTakeTurnStartInJudgement` re-enters `judgePlayerShouldDelay()` which processes remaining delay cards, then transitions to Drawing phase.

**Ward count is ODD (effect cancelled):**
1. `ContentmentJudgementBehavior.doWardCancelledAction()` is called.
2. The Contentment card was already popped from delayCards (at line 451), so it's simply discarded -- no judgement.
3. Calls `game.playerTakeTurnStartInJudgement(player)` to resume the turn flow.
4. Remaining delay scrolls (if any) are processed, then Drawing phase.

### Why a New Behavior Class Is Needed

The existing `ContentmentBehavior` handles **playing** the Contentment card onto a target during the Action phase. The judgement-phase Ward check is a completely different game moment:

| Aspect | ContentmentBehavior (existing) | ContentmentJudgementBehavior (new) |
|--------|-------------------------------|-------------------------------------|
| When | Action phase, player plays card | Judgement phase, system-initiated |
| Who triggers | The player who plays Contentment | The system, at turn start |
| behaviorPlayer | The caster | null (system) or the affected player |
| Purpose | Place Contentment in target's delay area | Ward check before judgement draw |
| Needs doBehaviorAction | No | Yes (draw judgement card) |
| Needs doWardCancelledAction | No | Yes (skip judgement, resume) |

---

## 3. Files That Need Modification

### 3.1 New File: `ContentmentJudgementBehavior.java`

**Location:** `domain/src/main/java/com/gaas/threeKingdoms/behavior/behavior/ContentmentJudgementBehavior.java`

**Why:** Core of the feature. Represents the system-initiated moment when a Contentment card is about to be judged.

**Implementation sketch:**
```java
public class ContentmentJudgementBehavior extends Behavior {

    public ContentmentJudgementBehavior(Game game, Player affectedPlayer,
            List<String> reactionPlayers, Player currentReactionPlayer,
            String cardId, String playType, HandCard card) {
        // isTargetPlayerNeedToResponse=true, isOneRound=false, isNeed2ndApiToUseEffect=false
        super(game, affectedPlayer, reactionPlayers, currentReactionPlayer,
              cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        // Called from judgePlayerShouldDelay() when Ward holders exist
        // 1. Set stage to Wait_Accept_Ward_Effect
        // 2. Create WardBehavior with:
        //    - behaviorPlayer = null (system-initiated)
        //    - reactionPlayers = all players with Ward (doesAnyPlayerHaveWard(null))
        //    - WARD_TRIGGER_PLAYER_ID = null (no excludes -- everyone can Ward)
        //    - WARD_TARGET_PLAYER_IDS = [affectedPlayer.getId()]
        // 3. Push WardBehavior on top
        // 4. Return WaitForWardEvent + playerAction() events
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        // Called when Ward count is EVEN (effect proceeds)
        // 1. Call game.handleContentmentJudgement(behaviorPlayer)
        // 2. Emit ContentmentEvent
        // 3. Mark isOneRound = true
        // 4. Call game.playerTakeTurnStartInJudgement(behaviorPlayer) to resume
        // 5. Return all events
    }

    @Override
    public List<DomainEvent> doWardCancelledAction() {
        // Called when Ward count is ODD (effect cancelled)
        // 1. Contentment already popped from delay area -- nothing to restore
        // 2. Mark isOneRound = true
        // 3. Call game.playerTakeTurnStartInJudgement(behaviorPlayer) to resume
        // 4. Return events from resumed judgement flow
    }
}
```

**Key pattern references:**
- `BountifulHarvestBehavior.playerAction()` lines 40-77 for Ward setup pattern
- `DuelBehavior.playerAction()` lines 32-69 for single-target Ward pattern
- WardBehavior constants: `WARD_TRIGGER_PLAYER_ID`, `WARD_TARGET_PLAYER_IDS`

### 3.2 Modify: `Game.java`

**Changes needed:**

1. **`judgePlayerShouldDelay()` (line 445):** In the `if (card instanceof Contentment)` block (line 453-455), add Ward check before `handleContentmentJudgement()`:
   ```java
   if (card instanceof Contentment) {
       if (doesAnyPlayerHaveWard(null)) {  // null = no player excluded
           // Create ContentmentJudgementBehavior, push onto topBehavior
           ContentmentJudgementBehavior cjb = new ContentmentJudgementBehavior(
               this, player, /* reactionPlayers */, null, card.getId(),
               PlayType.INACTIVE.getPlayType(), card
           );
           topBehavior.push(cjb);
           judgementEvents.addAll(cjb.playerAction());
           // Early return will happen via existing check at line 461
       } else {
           DomainEvent contentmentEvent = handleContentmentJudgement(player);
           judgementEvents.add(contentmentEvent);
       }
   }
   ```

2. **`handleContentmentJudgement()` (line 513):** Change visibility from `private` to `public` (or package-private) so `ContentmentJudgementBehavior.doBehaviorAction()` can call it.

3. **`playerTakeTurnStartInJudgement()` (line 165):** Currently `public`. The ContentmentJudgementBehavior needs to call this to resume the turn after Ward resolves. No changes needed to the method itself -- it already correctly handles re-entry because:
   - If `RoundPhase` is still `Judgement`, it calls `judgePlayerShouldDelay()` again
   - `judgePlayerShouldDelay()` will process remaining delay cards (if any)
   - Eventually sets `RoundPhase.Drawing` and returns

   **IMPORTANT:** The `contentmentEventSuccess` check in `playerTakeTurnStartInJudgement()` (line 170-173) needs consideration. When `doBehaviorAction()` calls this method, the `ContentmentEvent` will be in the returned events from the resumed `judgePlayerShouldDelay()` call. This should work correctly as-is.

   When `doWardCancelledAction()` calls this method, there will be no `ContentmentEvent` in the events (judgement was skipped), so `contentmentEventSuccess` will be `false`, which means the player gets a normal turn. This is correct.

### 3.3 Modify: `BehaviorData.java`

**Location:** `spring/src/main/java/com/gaas/threeKingdoms/repository/data/BehaviorData.java`

**Why:** Add serialization/deserialization for the new `ContentmentJudgementBehavior`.

**Changes:**
1. In `createBehavior()` switch (line 41), add:
   ```java
   case "ContentmentJudgementBehavior" -> new ContentmentJudgementBehavior(
       game,
       behaviorPlayerId != null ? game.getPlayer(behaviorPlayerId) : null,
       reactionPlayers,
       currentReactionPlayerId != null ? game.getPlayer(currentReactionPlayerId) : null,
       cardId,
       playType,
       PlayCard.findById(cardId)
   );
   ```
2. `fromDomain()` (line 290) already uses `behavior.getClass().getSimpleName()` which will produce `"ContentmentJudgementBehavior"` automatically. No changes needed there.

### 3.4 No Changes Needed: Event Classes and Presenters

- **`ContentmentEvent.java`:** No changes. It is only emitted when the judgement actually occurs (Ward even or no Ward). When Ward cancels, we rely on `WardEvent` to communicate the cancellation.
- **`WaitForWardEvent.java`:** Already works for this case. `wardTriggerPlayerId` will be `null` (system-initiated). `targetPlayerIds` will be `[affectedPlayerId]`.
- **`DomainEventToViewModelMapper.java` / `FinishActionPresenter.java`:** Already map `WardEvent` and `WaitForWardEvent`. No new event types needed.

---

## 4. Domain Test Cases

All domain tests go in: `domain/src/test/java/com/gaas/threeKingdoms/Ward/WardWithContentmentTest.java`

### Test 1: Ward Trigger on Contentment Judgement

**What:** When a player's turn starts and they have Contentment in their delay area, and another player has Ward, a `WaitForWardEvent` should be emitted.

**Why:** Verifies the entry point -- that the Ward check is properly inserted before the judgement card draw. Without this, Ward would never be offered for delay scrolls.

**Setup:**
- Players A, B, C, D. A's turn ends, triggering B's turn.
- Player B has Contentment in delay area.
- Player C has Ward in hand.
- Deck has enough cards.

**Assertions:**
- `WaitForWardEvent` is present in the returned events.
- `WaitForWardEvent.playerIds` contains player C.
- No `ContentmentEvent` is emitted (judgement hasn't happened yet).
- `topBehavior` stack contains WardBehavior on top of ContentmentJudgementBehavior.
- `RoundPhase` is still `Judgement` (game is paused).

### Test 2: Ward Cancels Contentment (Odd Ward Count = 1)

**What:** Player C plays Ward, no counter-Ward. Ward count is 1 (odd) -> Contentment is cancelled. Player B proceeds to normal turn.

**Why:** Verifies the core cancel path. The judgement card must NOT be drawn, and the player should get Drawing -> Action.

**Setup:** Same as Test 1. After WaitForWardEvent, player C plays Ward. Only C has Ward.

**Assertions:**
- `WardEvent` is emitted.
- No `ContentmentEvent` is emitted (judgement was skipped).
- Player B draws 2 cards (normal Drawing phase).
- `RoundPhase` is `Action` after drawing.
- Player B's delay scroll area is empty.

### Test 3: Double Ward, Contentment Proceeds (Even Ward Count = 2)

**What:** Player C plays Ward (count=1), Player D counter-Wards (count=2, even) -> Contentment proceeds. Judgement card is drawn.

**Why:** Verifies counter-chain resolution. Even count means the scroll effect proceeds.

**Setup:**
- Player B has Contentment in delay area.
- Players C and D both have Ward.
- Deck top card is a Spade (non-Heart -> Contentment succeeds, player skips Action).

**Assertions:**
- `WardEvent`s are emitted for both Ward plays.
- `ContentmentEvent` is emitted with `isSuccess=true`.
- Player B enters Discard phase (skips Action).

### Test 4: All Players Skip Ward, Contentment Proceeds

**What:** Players C and D both have Ward but both skip. Contentment judgement proceeds normally.

**Why:** Verifies the "all skip" path falls through to the existing judgement logic correctly.

**Setup:**
- Player B has Contentment in delay area.
- Players C and D both have Ward.
- After WaitForWardEvent, C skips, then D skips.

**Assertions:**
- `ContentmentEvent` is emitted (judgement card drawn).
- Normal Contentment effect applies based on drawn card suit.
- No `WardEvent` is emitted (nobody played Ward).

### Test 5: Ward Cancels Contentment, Player Still Has Lightning

**What:** Player B has both Contentment and Lightning in delay area. Ward cancels Contentment. Lightning judgement still processes.

**Why:** This is a critical edge case. `judgePlayerShouldDelay()` loops through all delay cards. After Ward cancels Contentment, the game must resume and process the Lightning. This verifies that `doWardCancelledAction()` -> `playerTakeTurnStartInJudgement()` -> `judgePlayerShouldDelay()` correctly handles remaining delay scrolls.

**Setup:**
- Player B has Contentment AND Lightning in delay area (Contentment on top of stack).
- Player C has Ward. Player C plays Ward to cancel Contentment.

**Assertions:**
- Contentment is cancelled (no `ContentmentEvent`).
- Lightning judgement still occurs (`LightningEvent` emitted).
- Delay scroll processing completes correctly.

### Test 6: No Player Has Ward, Contentment Proceeds Immediately

**What:** No player has Ward -> Contentment judgement happens immediately with no WaitForWardEvent.

**Why:** Backward compatibility. Existing behavior must be preserved when no Ward cards exist.

**Setup:** Player B has Contentment in delay area. No player has Ward cards.

**Assertions:**
- No `WaitForWardEvent` emitted.
- `ContentmentEvent` is emitted immediately.
- Behavior identical to current implementation (this is a regression test).

### Test 7: Affected Player Can Ward Their Own Contentment

**What:** Player B has Contentment in delay area AND Ward in hand. Player B should be eligible to play Ward on their own Contentment.

**Why:** In 三國殺 rules, any player can Ward any scroll card, including the affected player themselves. Since there is no "caster" at judgement time (the caster played Contentment in a previous turn), `doesAnyPlayerHaveWard(null)` should include ALL players. This is different from the Action-phase Ward pattern where the caster is excluded.

**Setup:** Player B has Contentment in delay area AND Ward in hand. No other player has Ward.

**Assertions:**
- `WaitForWardEvent.playerIds` includes player B.
- Player B can play Ward to cancel their own Contentment.
- After Ward, player B proceeds to normal turn.

---

## 5. E2E Test Cases

All E2E tests go in: `spring/src/test/java/com/gaas/threeKingdoms/e2e/scrollcard/ward/WardWithContentmentTest.java`

JSON fixtures go in: `spring/src/test/resources/TestJsonFile/ScrollTest/Ward/Contentment/`

### E2E Test 1: Ward Trigger When Turn Starts with Contentment

**What:** Player A plays Contentment on Player B. Player A finishes action. B's turn starts. Player C has Ward -> all players receive `WaitForWardEvent` via websocket.

**Why:** Full-stack verification that the Ward prompt is sent at the right time (turn start, judgement phase). Tests domain -> repository persistence -> websocket broadcast.

**Flow:**
1. Given: Player A has Contentment. Player C has Ward. Deck set up.
2. Player A plays Contentment targeting Player B (`mockMvcUtil.playCard`).
3. Player A finishes action (`mockMvcUtil.finishAction`).
4. Assert: All 4 players receive JSON containing `WaitForWardEvent` with Player C as eligible.

### E2E Test 2: Ward Cancels Contentment, Player B Gets Normal Turn

**What:** Player C plays Ward -> Contentment cancelled -> Player B gets normal Drawing -> Action turn.

**Why:** Full-stack verification of the cancel path including websocket state transitions.

**Flow:**
1. Given: Same setup as E2E Test 1.
2. Player A plays Contentment on B, finishes action.
3. Pop all messages.
4. Player C plays Ward (`mockMvcUtil.playWardCard(gameId, "player-c", wardCardId, PlayType.ACTIVE.getPlayType())`).
5. Assert: All players receive JSON with `WardEvent` and Player B's normal turn start (draw cards, Action phase).

### E2E Test 3: All Skip Ward, Contentment Succeeds (Non-Heart Drawn)

**What:** Player C has Ward but skips. Contentment judgement proceeds, Spade drawn -> Player B's turn skipped.

**Why:** Full-stack verification of skip-then-judge path when Contentment succeeds.

**Flow:**
1. Given: Player B has Contentment. Player C has Ward. Deck top is Spade.
2. Player A finishes action.
3. Pop messages (WaitForWardEvent).
4. Player C skips (`mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())`).
5. Assert: All players receive JSON with `ContentmentEvent(isSuccess=true)` and Player B in Discard phase.

### E2E Test 4: All Skip Ward, Contentment Fails (Heart Drawn)

**What:** Same as Test 3 but deck top is Heart -> Contentment fails -> Player B gets normal turn.

**Why:** Verifies the Ward-skip + Contentment-fail path produces correct websocket output.

**Flow:**
1. Given: Same as Test 3 but deck top is Heart card.
2. Player A finishes action, pop messages.
3. Player C skips Ward.
4. Assert: `ContentmentEvent(isSuccess=false)` and Player B in Action phase.

### E2E Test 5: Ward Counter-Chain (Two Wards, Even = Contentment Proceeds)

**What:** Player C plays Ward, Player D counter-Wards. Even count -> Contentment proceeds -> judgement drawn.

**Why:** Full-stack verification of counter-chain with delay scroll, including behavior stack persistence across multiple API calls.

**Flow:**
1. Given: Player B has Contentment. Players C and D both have Ward. Deck top is Spade.
2. Player A finishes action, pop messages.
3. Player C plays Ward -> pop messages -> new WaitForWardEvent for counter-Ward.
4. Player D counter-Wards.
5. Assert: `ContentmentEvent(isSuccess=true)` and Player B enters Discard phase.

---

## 6. Implementation Order

1. **Create `ContentmentJudgementBehavior.java`** -- the new Behavior class with `playerAction()`, `doBehaviorAction()`, `doWardCancelledAction()`.
2. **Modify `Game.java`:**
   - Change `handleContentmentJudgement()` from `private` to `public`.
   - Modify `judgePlayerShouldDelay()`: add Ward check in the Contentment branch.
3. **Modify `BehaviorData.java`** -- add `case "ContentmentJudgementBehavior"` for persistence.
4. **Write domain tests** (`WardWithContentmentTest.java` in `domain/src/test/java/.../Ward/`).
5. **Write E2E tests** (`WardWithContentmentTest.java` in `spring/src/test/java/.../e2e/scrollcard/ward/`).
6. **Generate JSON fixtures** using `JsonFileWriterUtil.writeJsonToFile()`, verify they look correct, then switch to `websocketUtil.getValue()`.

---

## 7. Key Design Decisions

### 7.1 Who is excluded from Ward targeting? (`WARD_TRIGGER_PLAYER_ID`)

**Decision:** Pass `null` -- no player is excluded.

**Why:** For Action-phase cards (Duel, BarbarianInvasion, etc.), the caster is excluded because they initiated the effect. For Contentment judgement, the original caster played Contentment in a **previous turn** -- they are not the "trigger" now. The trigger is the system. ALL players (including the affected player) should be able to Ward.

**How this differs from existing patterns:**
- `DuelBehavior` line 58: `WARD_TRIGGER_PLAYER_ID = behaviorPlayer.getId()` (caster excluded)
- `BountifulHarvestBehavior` line 62: `WARD_TRIGGER_PLAYER_ID = behaviorPlayer.getId()` (caster excluded)
- `ContentmentJudgementBehavior`: `WARD_TRIGGER_PLAYER_ID = null` (nobody excluded)

This means `doesAnyPlayerHaveWard(null)` and `whichPlayersHaveWard(null)` will include all players. Looking at `Game.java` line 904-911 and 917-923, passing `null` correctly skips the filter.

### 7.2 How to resume judgement after Ward resolves?

**Decision:** Call `game.playerTakeTurnStartInJudgement(behaviorPlayer)` from both `doBehaviorAction()` and `doWardCancelledAction()`.

**Why:** This is the cleanest approach because:
1. `playerTakeTurnStartInJudgement()` is already public (line 165).
2. It re-enters `judgePlayerShouldDelay()` which handles remaining delay cards.
3. It handles the Drawing -> Action / Drawing -> Discard transition.
4. The `contentmentEventSuccess` check (line 170) will work correctly:
   - For `doBehaviorAction()`: The `ContentmentEvent` from `handleContentmentJudgement()` is returned in the events from the resumed `judgePlayerShouldDelay()`.
   - For `doWardCancelledAction()`: No `ContentmentEvent` is returned -> `contentmentEventSuccess = false` -> player gets normal turn.

**Alternative considered:** Refactoring `judgePlayerShouldDelay()` to be resumable. Rejected because it would require tracking "which delay cards have been processed" as state, which is more complex than re-entering from `playerTakeTurnStartInJudgement()`. Since the Contentment card is already popped from the delay stack before the Behavior is created, re-entering will correctly process only the remaining cards.

### 7.3 What about the `WardBehavior.doBehaviorAction()` Stage/activePlayer reset?

**Observation:** `WardBehavior.doBehaviorAction()` (line 119-208) ends by setting:
```java
game.getCurrentRound().setStage(Stage.Normal);       // line 202
game.getCurrentRound().setActivePlayer(activePlayer); // line 204
```

For Action-phase cards, this is correct -- it restores the stage and sets the active player back to the round player. For Contentment judgement, we need to verify:
- `Stage.Normal` is correct (judgement phase doesn't use a special stage).
- `activePlayer` should be the current round player (the one with Contentment).

The even-count path calls `firstNotWardBehavior.doBehaviorAction()` (line 176), which is `ContentmentJudgementBehavior.doBehaviorAction()`. This method should set `activePlayer` correctly by having `playerTakeTurnStartInJudgement()` handle it. The `WardBehavior` code at line 182 sets `activePlayer = game.getCurrentRound().getActivePlayer()`, so `ContentmentJudgementBehavior.doBehaviorAction()` should ensure the round's activePlayer is set correctly before returning.

### 7.4 State stored in ContentmentJudgementBehavior

| Field | Value | Why |
|-------|-------|-----|
| `behaviorPlayer` | The player who has Contentment (current round player) | Needed to call `handleContentmentJudgement(player)` and `playerTakeTurnStartInJudgement(player)` |
| `card` | The Contentment ScrollCard reference | For card ID in events and WardBehavior |
| `cardId` | The Contentment card's ID | For WardBehavior and events |
| `reactionPlayers` | Empty list or `[affectedPlayer.getId()]` | ContentmentJudgementBehavior doesn't need reaction players itself -- Ward handles that |
| `isOneRound` | Initially `false` | Set to `true` when Ward resolves |
| `isTargetPlayerNeedToResponse` | `true` | Required for WardBehavior integration |

### 7.5 Lightning Ward support (future consideration)

The same pattern could be applied to Lightning in `judgePlayerShouldDelay()`. The `handleLightningJudgement()` method also draws a judgement card immediately. A future `LightningJudgementBehavior` would follow the same design. This plan does NOT include Lightning Ward -- it is out of scope. However, the implementation should be designed with Lightning extensibility in mind (e.g., the Ward check pattern in `judgePlayerShouldDelay()` should be easy to replicate for the Lightning branch).
