# Ward (無懈可擊) + Indulgence/Contentment (樂不思蜀) Implementation Plan

## 1. Current Flow: How Contentment Judgement Works

### Turn Start Sequence

When a player's turn begins, the game calls:

1. `Game.playerTakeTurn(player)` (line 156) sets `RoundPhase.Judgement` and calls `playerTakeTurnStartInJudgement(player)`.

2. `Game.playerTakeTurnStartInJudgement(player)` (line 165) calls `judgePlayerShouldDelay()` to process all delay scroll cards.

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
                   // ... lightning handling
               }
               if (!topBehavior.isEmpty()) { // line 461: early return if behaviors are pending
                   return judgementEvents;
               }
           }
       }
       judgementEvents.add(new JudgementEvent());
       currentRound.setRoundPhase(RoundPhase.Drawing); // line 468
       return judgementEvents;
   }
   ```

4. `Game.handleContentmentJudgement(player)` (line 513):
   ```java
   private ContentmentEvent handleContentmentJudgement(Player player) {
       List<HandCard> cards = drawCardForCardEffect(1); // line 515: draws judgement card
       HandCard drawnCard = cards.get(0);
       boolean contentmentSuccess = Suit.HEART != drawnCard.getSuit(); // line 519
       return new ContentmentEvent(contentmentSuccess, player.getId(), drawnCard.getId(), drawnCard.getSuit());
   }
   ```

5. Back in `playerTakeTurnStartInJudgement` (line 165), after getting judge events:
   - If `contentmentSuccess` is true: player draws 2 cards but immediately goes to discard phase (skips Action phase).
   - If `contentmentSuccess` is false (heart drawn): player proceeds normally through Drawing -> Action.

### Key Problem

Currently, the judgement card draw happens **immediately** with NO opportunity for anyone to play Ward. In the real 三國殺 rules, **before** the judgement card is drawn, all players should be asked if they want to play Ward to cancel the Contentment effect entirely. If Ward cancels it, the judgement draw is skipped and the Contentment card is removed without effect.

### File Locations

| File | Path | Purpose |
|------|------|---------|
| Game.java | `domain/src/main/java/com/gaas/threeKingdoms/Game.java` | Contains `judgePlayerShouldDelay()` (line 445) and `handleContentmentJudgement()` (line 513) |
| ContentmentBehavior.java | `domain/src/main/java/com/gaas/threeKingdoms/behavior/behavior/ContentmentBehavior.java` | Handles playing Contentment onto a target (not the judgement phase) |
| ContentmentEvent.java | `domain/src/main/java/com/gaas/threeKingdoms/events/ContentmentEvent.java` | Event for judgement result |
| WardBehavior.java | `domain/src/main/java/com/gaas/threeKingdoms/behavior/behavior/WardBehavior.java` | Core Ward counter-chain logic |
| Round.java | `domain/src/main/java/com/gaas/threeKingdoms/round/Round.java` | Round state (phase, stage, active player) |

---

## 2. Target Flow: Where Ward Check Should Be Inserted

### Why Ward Must Go BEFORE the Judgement Card Draw

In 三國殺 rules, Ward (無懈可擊) targets the **scroll card itself**, not the judgement result. The purpose of Ward against Contentment is to prevent the entire judgement from happening -- the Contentment card is removed from the delay area without any effect. This is fundamentally different from modifying the judgement result.

If Ward were inserted after the draw, the judgement card would already have been consumed from the deck, which would incorrectly alter game state even when Ward cancels the effect. The Ward check must happen before `drawCardForCardEffect(1)` is called.

### Proposed Flow

```
Player turn starts
  -> playerTakeTurn()
    -> playerTakeTurnStartInJudgement()
      -> judgePlayerShouldDelay()
        -> For each delay scroll card:
          -> If card is Contentment:
            -> [NEW] Check if anyone has Ward
              -> YES: Push ContentmentJudgementBehavior onto topBehavior stack,
                      push WardBehavior on top, emit WaitForWardEvent, RETURN EARLY
                      (game pauses, waits for Ward responses via playWardCard API)
              -> NO:  Proceed to handleContentmentJudgement() as before (draw card, judge)
```

When Ward responses complete (all players skip or Ward counter-chain resolves):

- **Ward count is ODD (effect cancelled):** Contentment is removed from delay area. The `doWardCancelledAction()` on ContentmentJudgementBehavior resumes `judgePlayerShouldDelay()` flow -- checks for more delay cards, then proceeds to Drawing phase.
- **Ward count is EVEN (effect proceeds):** The `doBehaviorAction()` on ContentmentJudgementBehavior calls `handleContentmentJudgement()` to draw the judgement card and determine the outcome, then resumes the normal flow.

### Why a New Behavior Class Is Needed

The existing `ContentmentBehavior` handles **playing** the Contentment card onto a target during the Action phase. The judgement-phase Ward check is a completely different game moment -- it happens at turn start, is system-initiated (not player-initiated), and needs `doBehaviorAction()` and `doWardCancelledAction()` hooks. A new `ContentmentJudgementBehavior` (or similar name) is needed to:

1. Hold the Contentment card reference and the affected player.
2. Implement `doBehaviorAction()` to execute the judgement when Ward resolves to "even" (effect proceeds).
3. Implement `doWardCancelledAction()` to skip the judgement when Ward resolves to "odd" (effect cancelled), then continue processing remaining delay scrolls.

This follows the same pattern used by other Behaviors that interact with Ward (e.g., `DuelBehavior`, `BountifulHarvestBehavior`).

---

## 3. Files That Need Modification

### 3.1 New File: `ContentmentJudgementBehavior.java`

**Location:** `domain/src/main/java/com/gaas/threeKingdoms/behavior/behavior/ContentmentJudgementBehavior.java`

**Why:** This is the core of the feature. It represents the system-initiated moment when a Contentment card is about to be judged at turn start. It needs to:
- Push a WardBehavior onto the stack if any player has Ward.
- Implement `doBehaviorAction()` for when Ward resolves to "even" (proceed with judgement).
- Implement `doWardCancelledAction()` for when Ward resolves to "odd" (skip judgement, continue to next delay card or Drawing phase).

**Pattern reference:** Similar to how `BountifulHarvestBehavior` (line 40-77) checks for Ward in `playerAction()` and implements both hooks.

### 3.2 Modify: `Game.java`

**Location:** `domain/src/main/java/com/gaas/threeKingdoms/Game.java`

**Why:** The `judgePlayerShouldDelay()` method (line 445) needs to be modified to check for Ward before calling `handleContentmentJudgement()`. Specifically:

- **Line 453-455:** Instead of immediately calling `handleContentmentJudgement(player)`, first check `doesAnyPlayerHaveWard()`. If true, create a `ContentmentJudgementBehavior`, push it and a `WardBehavior` onto `topBehavior`, and return early with `WaitForWardEvent`.
- **`handleContentmentJudgement()`** (line 513): This method should become `public` (or package-private) so that `ContentmentJudgementBehavior.doBehaviorAction()` can call it.
- **`playerTakeTurnStartInJudgement()`** (line 165): The early return when `!topBehavior.isEmpty()` at line 461 already handles the case where Ward is pending, so this method needs minimal changes. However, we need a way for the game to resume judgement processing after Ward resolves. This likely requires a new method or a way for `ContentmentJudgementBehavior` to trigger continuation of `judgePlayerShouldDelay()`.

**Key design consideration:** After Ward resolves (either cancelling or allowing Contentment), the game needs to:
1. Continue processing remaining delay scroll cards (e.g., if player has both Contentment and Lightning).
2. Eventually transition to Drawing phase.

The existing `if (!topBehavior.isEmpty()) return judgementEvents;` at line 461 provides the early-exit mechanism. The `ContentmentJudgementBehavior`'s `doBehaviorAction()` / `doWardCancelledAction()` must handle resuming the judgement flow. This may require making parts of `judgePlayerShouldDelay()` accessible or refactoring it slightly.

### 3.3 Modify: `BehaviorData.java`

**Location:** `spring/src/main/java/com/gaas/threeKingdoms/repository/data/BehaviorData.java`

**Why:** The persistence layer needs to know how to serialize/deserialize the new `ContentmentJudgementBehavior`. A new `case "ContentmentJudgementBehavior"` must be added to the `createBehavior()` switch (around line 41). This follows the same pattern as every other Behavior in the project.

### 3.4 Potentially Modify: `DomainEventToViewModelMapper.java` and/or `FinishActionPresenter.java`

**Location:** `spring/src/main/java/com/gaas/threeKingdoms/presenter/mapper/DomainEventToViewModelMapper.java` and `spring/src/main/java/com/gaas/threeKingdoms/presenter/FinishActionPresenter.java`

**Why:** If the Ward-cancelled path for Contentment needs a new event type (e.g., `ContentmentWardCancelledEvent`), the presenter layer needs to map it to a view model. However, this may not be necessary if we reuse existing events (`WardEvent`, `ContentmentEvent`). The existing `WardEvent` and `WaitForWardEvent` are already mapped, so those paths should work. We need to verify that the `ContentmentEvent` continues to be emitted correctly in the "Ward even, proceed with judgement" path.

### 3.5 Potentially Modify: `ContentmentEvent.java`

**Location:** `domain/src/main/java/com/gaas/threeKingdoms/events/ContentmentEvent.java`

**Why:** We may need a way to represent "Contentment was cancelled by Ward" (no judgement card drawn). The current `ContentmentEvent` always includes a `drawCardId` and `suit`, which assume a judgement card was drawn. If Ward cancels the effect, there is no drawn card. Options:
- Add a new `ContentmentWardCancelledEvent` (cleaner separation).
- Allow `drawCardId` to be null in `ContentmentEvent` (less clean but simpler).
- Rely solely on `WardEvent` to communicate the cancellation (simplest, may be sufficient).

The simplest approach is likely to rely on `WardEvent` for the cancellation notification, and only emit `ContentmentEvent` when the judgement actually occurs (Ward count even or no Ward at all). This means no changes to `ContentmentEvent.java`.

---

## 4. Domain Test Cases

All domain tests go in: `domain/src/test/java/com/gaas/threeKingdoms/Ward/WardWithContentmentTest.java`

### Test 1: Ward Trigger on Contentment Judgement

**What:** When a player's turn starts and they have Contentment in their delay area, and another player has Ward, a `WaitForWardEvent` should be emitted with the correct eligible player IDs.

**Why:** Verifies the entry point -- that the Ward check is properly inserted before the judgement card draw. This is the most fundamental test: without it, Ward would never be offered.

**Setup:** Player A's turn. Player B has Contentment in delay area. Player C has Ward. Deck has enough cards. Previous player (A) finishes action, triggering B's turn.

**Assertions:**
- `WaitForWardEvent` is present in the returned events.
- `WaitForWardEvent.playerIds` contains player C (the Ward holder).
- No `ContentmentEvent` is emitted (judgement hasn't happened yet).
- `topBehavior` stack contains a `WardBehavior` on top of a `ContentmentJudgementBehavior`.

### Test 2: Ward Cancels Contentment (Odd Ward Count)

**What:** Player C plays Ward, no one counter-Wards. Ward count is 1 (odd) -> Contentment is cancelled. Player B proceeds to normal turn (Drawing -> Action).

**Why:** Verifies the core cancel path. The judgement card must NOT be drawn, and the player should enter their turn normally.

**Setup:** Same as Test 1. After WaitForWardEvent, player C plays Ward. No other players have Ward.

**Assertions:**
- `WardEvent` is emitted (showing C's Ward cancelling the Contentment).
- No `ContentmentEvent` is emitted (judgement was skipped).
- Player B draws 2 cards (normal Drawing phase).
- `RoundPhase` is `Action` after drawing.
- Player B's delay scroll area is empty (Contentment was removed).

### Test 3: Double Ward, Contentment Proceeds (Even Ward Count)

**What:** Player C plays Ward (count=1), then Player D counter-Wards (count=2, even) -> Contentment effect proceeds. Judgement card is drawn.

**Why:** Verifies the counter-chain resolution. Even Ward count means the original scroll effect proceeds, so the judgement must occur.

**Setup:** Player B has Contentment in delay area. Player C has Ward. Player D also has Ward. Deck top card is a Spade (Contentment succeeds -- player skips Action).

**Assertions:**
- Two `WardEvent`s are emitted.
- `ContentmentEvent` is emitted with `isSuccess=true` (Spade drawn).
- Player B enters Discard phase (skips Action).

### Test 4: All Players Skip Ward

**What:** No player plays Ward (all skip). Contentment judgement proceeds normally.

**Why:** Verifies that when all Ward-eligible players decline, the game correctly falls through to the existing judgement logic without any behavioral changes.

**Setup:** Player B has Contentment in delay area. Players C and D both have Ward. Both skip.

**Assertions:**
- `ContentmentEvent` is emitted (judgement card drawn).
- Normal Contentment effect applies based on drawn card suit.
- No `WardEvent` is emitted.

### Test 5: Ward Cancels Contentment, Player Still Has Lightning

**What:** Player B has both Contentment and Lightning in delay area. Ward cancels Contentment. Lightning judgement still processes.

**Why:** Verifies that after Ward cancels one delay scroll, the game correctly continues processing remaining delay scrolls. This is a critical edge case since `judgePlayerShouldDelay()` loops through all delay cards.

**Setup:** Player B has Contentment AND Lightning in delay area. Player C has Ward. Player C plays Ward to cancel Contentment.

**Assertions:**
- Contentment is cancelled (no `ContentmentEvent`).
- Lightning judgement still occurs (`LightningEvent` emitted).
- Delay scroll processing completes correctly.

### Test 6: No Player Has Ward, Contentment Proceeds Immediately

**What:** When no player has Ward, the Contentment judgement happens immediately (no `WaitForWardEvent`).

**Why:** Verifies backward compatibility. The Ward check should be a no-op when no Ward cards exist.

**Setup:** Player B has Contentment in delay area. No player has Ward cards.

**Assertions:**
- No `WaitForWardEvent` emitted.
- `ContentmentEvent` is emitted immediately.
- Behavior identical to current implementation.

### Test 7: Ward on Contentment, Player Who Has Contentment Also Has Ward

**What:** Player B has Contentment in their delay area AND Ward in hand. Player B should be eligible to Ward their own Contentment.

**Why:** In 三國殺 rules, any player can Ward any scroll card, including the affected player themselves. This tests that the player with the delay scroll is correctly included in Ward-eligible players (they are NOT the "caster" -- the caster was whoever played Contentment on them in a previous turn).

**Setup:** Player B has Contentment in delay area AND Ward in hand. No other player has Ward.

**Assertions:**
- `WaitForWardEvent.playerIds` includes player B.
- Player B can play Ward to cancel their own Contentment.

---

## 5. E2E Test Cases

All E2E tests go in: `spring/src/test/java/com/gaas/threeKingdoms/e2e/scrollcard/ward/WardWithContentmentTest.java`

JSON fixtures go in: `spring/src/test/resources/TestJsonFile/ScrollTest/Ward/Contentment/`

### E2E Test 1: Ward Trigger When Turn Starts with Contentment

**What:** Player A plays Contentment on Player B. Player A finishes action. When B's turn starts, if Player C has Ward, all four players should receive a websocket message containing `WaitForWardEvent`.

**Why:** End-to-end verification that the Ward prompt is sent to the frontend at the right time (turn start, during judgement phase). This tests the full stack: domain logic -> repository persistence -> websocket broadcast.

**Flow:**
1. Given: Player A has Contentment. Player C has Ward. Deck is set up.
2. Player A plays Contentment targeting Player B.
3. Player A finishes action (triggers B's turn).
4. Assert: All players receive JSON containing `WaitForWardEvent` with Player C as eligible.

### E2E Test 2: Ward Cancels Contentment

**What:** Following Test 1, Player C plays Ward. Contentment is cancelled. Player B proceeds to normal turn.

**Why:** End-to-end verification of the cancel path. Verifies that the websocket messages contain the correct state transitions: Ward resolution -> normal turn start for Player B.

**Flow:**
1. Given: Same setup as E2E Test 1.
2. Player A plays Contentment on B, finishes action.
3. Pop all messages.
4. Player C plays Ward (via `mockMvcUtil.playWardCard`).
5. Assert: All players receive JSON containing `WardEvent` and Player B's normal turn (Drawing phase, draw cards, Action phase).

### E2E Test 3: All Skip Ward, Contentment Succeeds (Non-Heart Drawn)

**What:** Player C has Ward but skips. Contentment judgement proceeds, non-heart card drawn, Player B's turn is skipped.

**Why:** End-to-end verification of the skip-then-judge path. Verifies correct JSON output when Ward is offered but declined and Contentment succeeds.

**Flow:**
1. Given: Player B has Contentment in delay area. Player C has Ward. Deck top is a Spade card.
2. Player A finishes action (triggers B's turn).
3. Pop messages (WaitForWardEvent).
4. Player C skips Ward (via `mockMvcUtil.playWardCard` with SKIP play type).
5. Assert: All players receive JSON with `ContentmentEvent(isSuccess=true)` and Player B entering Discard phase.

### E2E Test 4: All Skip Ward, Contentment Fails (Heart Drawn)

**What:** Player C has Ward but skips. Contentment judgement proceeds, heart card drawn, Player B gets a normal turn.

**Why:** End-to-end verification of the skip-then-judge path with Contentment failing. Ensures the full flow from Ward prompt to normal turn works correctly.

**Flow:**
1. Given: Same as E2E Test 3 but deck top is a Heart card.
2. Player A finishes action (triggers B's turn).
3. Pop messages.
4. Player C skips Ward.
5. Assert: All players receive JSON with `ContentmentEvent(isSuccess=false)` and Player B entering Action phase normally.

### E2E Test 5: Ward Counter-Chain (Two Wards, Even = Effect Proceeds)

**What:** Player C plays Ward, Player D counter-Wards. Ward count is even, so Contentment proceeds.

**Why:** End-to-end verification that the Ward counter-chain works correctly in the delay scroll context, including proper persistence of the behavior stack across multiple API calls.

**Flow:**
1. Given: Player B has Contentment. Players C and D both have Ward. Deck top is Spade.
2. Player A finishes action.
3. Pop messages.
4. Player C plays Ward -> Pop messages -> New WaitForWardEvent for counter-Ward.
5. Player D counter-Wards -> Contentment proceeds -> Judgement happens.
6. Assert: `ContentmentEvent(isSuccess=true)` and Player B enters Discard phase.

---

## 6. Implementation Order

1. **Create `ContentmentJudgementBehavior.java`** -- the new Behavior class with Ward integration hooks.
2. **Modify `Game.judgePlayerShouldDelay()`** -- insert Ward check before `handleContentmentJudgement()`.
3. **Modify `Game.handleContentmentJudgement()`** -- make it accessible from the new Behavior.
4. **Modify `BehaviorData.java`** -- add serialization/deserialization for the new Behavior.
5. **Write domain tests** (`WardWithContentmentTest.java`).
6. **Write E2E tests** (`WardWithContentmentTest.java` in spring).
7. **Generate JSON fixtures** using `JsonFileWriterUtil.writeJsonToFile()`, then switch to `websocketUtil.getValue()`.

---

## 7. Key Design Decisions

### Who is the "caster" for Ward targeting?

For Contentment judgement, the Contentment card was placed by a different player (e.g., Player A placed it on Player B in a previous turn). At judgement time, there is no "caster" in the current turn. For the Ward check:
- `WARD_TRIGGER_PLAYER_ID` should be `null` or a special sentinel, since this is a system-initiated trigger, not a player action. (Looking at the pattern in `BountifulHarvestBehavior` line 57, the WardBehavior is created with `behaviorPlayer = null` for system-initiated Ward challenges.)
- `WARD_TARGET_PLAYER_IDS` should contain the player who has the Contentment (the one whose turn it is).
- All players (including the affected player) should be eligible to play Ward. The `doesAnyPlayerHaveWard()` without excluding anyone (or pass `null`) should be used.

### How to resume judgement after Ward resolves?

The `ContentmentJudgementBehavior` must handle two outcomes:
- **`doBehaviorAction()`** (Ward even/no Ward): Call the judgement logic, then check for more delay scrolls.
- **`doWardCancelledAction()`** (Ward odd): Skip judgement, then check for more delay scrolls.

In both cases, the Behavior needs to trigger continuation of the delay scroll processing. This could be done by:
- Calling `game.playerTakeTurnStartInJudgement()` from the Behavior (which re-enters the judgement flow).
- Or by having the Behavior directly call a refactored version of the remaining loop in `judgePlayerShouldDelay()`.

The cleanest approach is to have `ContentmentJudgementBehavior` handle just the single Contentment card, and after it resolves (whether cancelled or judged), call back into `game.playerTakeTurnStartInJudgement()` to continue processing any remaining delay scrolls and eventually transition to Drawing phase.

### State that ContentmentJudgementBehavior needs to store

- The `Player` who has the Contentment (the current round player).
- The `ScrollCard` reference (the Contentment card that was popped from the delay area).
- These can be stored via the existing Behavior fields (`behaviorPlayer`, `card`) or via the `params` map.
