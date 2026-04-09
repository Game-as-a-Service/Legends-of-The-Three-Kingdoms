# 三國殺 API 文件

> 此檔案不納入 git 追蹤

## 架構概述

```
Client (前端)
  │
  ├─ HTTP POST ──→ GameController ──→ UseCase ──→ Game (Domain)
  │                                                   │
  └─ WebSocket ←── WebSocketBroadCast ←── Presenter ←─┘
      (STOMP)        /websocket/legendsOfTheThreeKingdoms/{gameId}/{playerId}
```

- **架構模式**：MVP (Model-View-Presenter)
- **WebSocket**：STOMP over `/legendsOfTheThreeKingdoms`
- **Broker prefix**：`/websocket`
- **App prefix**：`/app`
- **推播目標**：`/websocket/legendsOfTheThreeKingdoms/{gameId}/{playerId}`（per-player 個人化訊息）

---

## 1. 建立遊戲

```
POST /api/games
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| gameId | String | 遊戲 ID |
| players | List\<String\> | 玩家 ID 列表（4 人） |

**回傳**：HTTP 200 + WebSocket 推播遊戲初始化事件（角色分配、主公選將）

**流程**：建立遊戲 → 分配身份（主公/忠臣/反賊/內奸）→ 主公先選將

---

## 2. 查詢遊戲狀態

```
GET /api/games/{gameId}?playerId={playerId}
```

| 參數 | 型別 | 說明 |
|------|------|------|
| gameId | Path | 遊戲 ID |
| playerId | Query | 查詢的玩家 ID |

**回傳**：該玩家視角的遊戲狀態（只能看自己的手牌和身份）

---

## 3. 主公選將

```
POST /api/games/{gameId}/player:monarchChooseGeneral
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 主公玩家 ID |
| generalId | String | 選擇的武將 ID（如 SHU001 劉備） |

**流程**：主公選將 → 其他玩家收到可選將領列表

---

## 4. 其他玩家選將

```
POST /api/games/{gameId}/player:otherChooseGeneral
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| generalId | String | 選擇的武將 ID |

**流程**：全部選完 → 發牌（每人 4 張）→ 主公回合開始

---

## 5. 出牌

```
POST /api/games/{gameId}/player:playCard
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 出牌玩家 ID |
| targetPlayerId | String | 目標玩家 ID（無目標傳空字串） |
| cardId | String | 卡牌 ID（如 BS8008） |
| playType | String | 出牌類型 |

### playType 說明

| playType | 說明 | 使用場景 |
|----------|------|----------|
| `active` | 主動出牌 | 自己回合出殺/錦囊/裝備 |
| `inactive` | 被動出牌 | 被殺時出閃、瀕死時出桃 |
| `skip` | 跳過 | 被殺不出閃、不出桃 |

### 對應的牌與流程

#### 基本牌

| 牌名 | cardId 範例 | targetPlayerId | 流程 |
|------|------------|----------------|------|
| 殺 | BS8008 | 目標玩家 ID | 出殺 → 目標出閃(playCard inactive) 或不出(skip) → 命中扣血或未命中 |
| 閃 | BH2028 | 空字串 | 被殺後被動出閃（playType=inactive） |
| 桃 | BH3029 | 空字串 | 主動回血 或 瀕死時被動出桃（playType=inactive） |

#### 錦囊牌（透過 playCard API 出牌）

| 牌名 | cardId 範例 | targetPlayerId | 流程 |
|------|------------|----------------|------|
| 決鬥 | SSA001 | 目標玩家 ID | 出決鬥 → (Ward 詢問) → 雙方交替出殺 |
| 閃電 | SSA014 | 空字串 | 放入自己判定區 → 每回合判定流轉 |
| 桃園結義 | SHA027 | 空字串 | 全體回 1 血（已滿血無效） |
| 萬箭齊發 | SHA040 | 空字串 | (Ward 詢問) → 全體需出閃，否則扣 1 血 |
| 南蠻入侵 | SS7007 | 空字串 | (Ward 詢問) → 全體需出殺，否則扣 1 血 |
| 無中生有 | SH7046 | 空字串 | (Ward 詢問) → 摸 2 張牌 |
| 樂不思蜀 | SS6006 | 目標玩家 ID | 放入目標判定區 → 目標回合判定（Ward 詢問 → 判定紅心跳過/其他跳過出牌） |
| 五穀豐登 | SH3042 | 空字串 | (Ward Phase1 詢問) → 翻牌 → 逐人選牌（Ward Phase2 每人詢問） |
| 過河拆橋 | SS3003 | 目標玩家 ID | (Ward 詢問) → 選擇棄目標一張牌（使用 useDismantleEffect API） |
| 順手牽羊 | SS3016 | 目標玩家 ID | (Ward 詢問) → 選擇拿目標一張牌（使用 useSnatchEffect API） |
| 借刀殺人 | SCQ064 | 目標玩家 ID | (Ward 詢問) → 目標需出殺或交出武器（使用 useBorrowedSwordEffect API） |

#### 裝備牌（透過 playCard API 裝備）

| 牌名 | cardId 範例 | targetPlayerId | 流程 |
|------|------------|----------------|------|
| 諸葛連弩 Chu Ko Nu | ECA066, EDA092 | 空字串 | 裝備武器（range 1） → 出殺無次數限制 |
| 麒麟弓 Qilin Bow | EH5031 | 空字串 | 裝備武器（range 5） → 殺命中後可棄對方一匹馬（useEquipmentEffect API） |
| 雌雄雙股劍 Yin-Yang Swords | ES2002 | 空字串 | 裝備武器（range 2） → 對異性出殺時目標選棄牌或讓攻擊者摸牌（AskYinYangSwordsEffectEvent） |
| 青釭劍 Black Pommel | ES6019 | 空字串 | 裝備武器（range 2） → 殺無視目標防具（觸發時發 BlackPommelEffectEvent） |
| 青龍偃月刀 Green Dragon Crescent Blade | ES5005 | 空字串 | 裝備武器（range 3） → 殺被閃抵銷時可再出一張殺對同一目標（AskGreenDragonCrescentBladeEffectEvent + GreenDragonCrescentBladeTriggerEvent） |
| 貫石斧 Stone Piercing Axe | ED5083 | 空字串 | 裝備武器（range 3） → 殺被閃抵銷時可棄兩張牌強制命中（AskStonePiercingAxeEffectEvent + StonePiercingAxeTriggerEvent） |
| 八卦陣 Eight Diagrams | ES2015, EC2067 | 空字串 | 裝備防具 → 需出閃時可判定（紅色=閃） |
| 絕影 ShadowHorse | ES5018 | 空字串 | 裝備+1馬 → 其他人對你距離+1 |
| 的盧 HexMark | EC5070 | 空字串 | 裝備+1馬 → 其他人對你距離+1 |
| 爪黃飛電 YellowFlash | EHK052 | 空字串 | 裝備+1馬 → 其他人對你距離+1 |
| 赤兔 RedRabbitHorse | EH5044 | 空字串 | 裝備-1馬 → 你對其他人距離-1 |
| 紫騂 VioletStallion | EDK104 | 空字串 | 裝備-1馬 → 你對其他人距離-1 |
| 黃爪飛電 FerghanaHorse | ESK026 | 空字串 | 裝備-1馬 → 你對其他人距離-1 |

---

## 6. 出無懈可擊

```
POST /api/games/{gameId}/player:playWardCard
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 出牌玩家 ID |
| cardId | String | 無懈可擊的 cardId（skip 時傳空字串） |
| playType | String | `active`（出牌）或 `skip`（跳過） |

**Ward cardId**：SSJ011、SCQ077、SCK078

**觸發時機**：收到 `WaitForWardEvent` / `AskPlayWardEvent` 後呼叫

**Ward 規則**：
- 奇數張 Ward → 效果取消
- 偶數張 Ward → 效果生效
- 所有有 Ward 的玩家都 skip → 效果生效

**適用的牌**：決鬥、南蠻入侵、萬箭齊發、過河拆橋、順手牽羊、借刀殺人、五穀豐登、無中生有、樂不思蜀（判定前）、閃電（判定前）

---

## 7. 結束出牌

```
POST /api/games/{gameId}/player:finishAction
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |

**流程**：結束出牌階段 → 手牌超過體力值上限時進入棄牌階段 → 否則下一位玩家回合開始（判定階段 → 摸牌 → 出牌）

**注意**：`topBehavior` 必須為空才能結束，否則拋 `IllegalStateException`

---

## 8. 棄牌

```
POST /api/games/{gameId}/player:discardCards
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| (body) | List\<String\> | 要棄掉的 cardId 列表 |

**流程**：棄牌至手牌數 = 體力值上限 → 下一位玩家回合開始

---

## 9. 使用裝備效果

```
POST /api/games/{gameId}/player:useEquipmentEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| targetPlayerId | String | 目標玩家 ID |
| cardId | String | 裝備 cardId |
| playType | String | 出牌類型 |

**適用**：麒麟弓（殺命中後選擇棄對方哪匹馬）

---

## 10. 選擇馬牌

```
POST /api/games/{gameId}/player:chooseHorseCard
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| cardId | String | 馬的 cardId |

**適用**：麒麟弓命中後選擇棄對方的哪匹馬

---

## 11. 借刀殺人效果

```
POST /api/games/{gameId}/player:useBorrowedSwordEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| currentPlayerId | String | 出借刀殺人的玩家 ID |
| borrowedPlayerId | String | 被借刀的玩家 ID（擁有武器的人） |
| attackTargetPlayerId | String | 被借刀者需要殺的目標 |

**流程**：出借刀殺人 → (Ward 詢問) → 被借刀者出殺或交出武器

---

## 12. 過河拆橋效果

```
POST /api/games/{gameId}/player:useDismantleEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| currentPlayerId | String | 出過河拆橋的玩家 ID |
| targetPlayerId | String | 目標玩家 ID |
| cardId | String | 過河拆橋的 cardId |
| targetCardIndex | Integer | 目標玩家的牌 index（手牌隨機 index / 裝備區 index） |

**流程**：出過河拆橋 → (Ward 詢問) → 選擇棄目標一張牌

---

## 13. 順手牽羊效果

```
POST /api/games/{gameId}/player:useSnatchEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| currentPlayerId | String | 出順手牽羊的玩家 ID |
| targetPlayerId | String | 目標玩家 ID |
| cardId | String | 順手牽羊的 cardId |
| targetCardIndex | Integer | 目標玩家的牌 index |

**流程**：出順手牽羊 → (Ward 詢問) → 選擇拿目標一張牌加入手牌

---

## 14. 五穀豐登選牌

```
POST /api/games/{gameId}/player:chooseCardFromBountifulHarvest
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 選牌的玩家 ID |
| cardId | String | 從牌池中選擇的 cardId |

**流程**：出五穀豐登 → (Ward Phase1) → 翻 N 張牌 → 逐人選牌 → (Ward Phase2 每人詢問) → 選一張加入手牌

---

## 15. 雌雄雙股劍效果選擇

```
POST /api/games/{gameId}/player:useYinYangSwordsEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 做選擇的玩家 ID（被殺的目標 B） |
| choice | String | `TARGET_DISCARDS`（目標棄一張手牌）或 `ATTACKER_DRAWS`（讓攻擊者摸牌） |
| cardId | String | 要棄的手牌 cardId（choice=TARGET_DISCARDS 時必填；ATTACKER_DRAWS 時傳空字串） |

**觸發時機**：收到 `AskYinYangSwordsEffectEvent` 後呼叫

**流程**：
```
A(男) 對 B(女) 出殺 → A 裝備雌雄雙股劍 + 異性
  → 系統發出 AskYinYangSwordsEffectEvent
  → B 呼叫本 API 做選擇：
    - TARGET_DISCARDS: B 棄一張手牌到墓地 → 發出 YinYangSwordsEffectEvent → 繼續 AskDodge
    - ATTACKER_DRAWS: A 從牌堆摸 1 張 → 發出 YinYangSwordsEffectEvent → 繼續 AskDodge
```

**特殊情況**：
- B 目標沒有手牌時，系統自動讓 A 摸牌，不會發送 `AskYinYangSwordsEffectEvent`，直接進入 AskDodge
- 同性出殺時，不觸發雌雄雙股劍，直接進入 AskDodge

**備註**：此 API 獨立於 `playCard`，不使用 `playType=active/skip` 路由

---

## 16. 青龍偃月刀效果選擇

```
POST /api/games/{gameId}/player:useGreenDragonCrescentBladeEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID |
| choice | String | `KILL`（再出一張殺）或 `SKIP`（不發動） |
| killCardId | String | 要追加的殺 cardId（choice=KILL 時必填；SKIP 時傳空字串） |

**觸發時機**：收到 `AskGreenDragonCrescentBladeEffectEvent` 後呼叫

**流程**：
```
A (裝備青龍偃月刀) 對 B 出殺 → B 出閃抵銷
  → 系統發出 AskGreenDragonCrescentBladeEffectEvent（詢問 A）
  → A 呼叫本 API 做選擇：
    - SKIP: 殺被抵銷結束，流程正常結束
    - KILL: A 棄第二張殺到墓地 → 發出 GreenDragonCrescentBladeTriggerEvent
            → B 收到 AskDodgeEvent（若有防具先 AskPlayEquipmentEffectEvent）
            → B 再出閃 → 回到青龍偃月刀詢問（可多次循環）
            → B 不出閃 → 扣血，流程結束
```

**特殊情況**：
- A 沒有殺或提供非殺 cardId → 拋例外
- 青龍偃月刀追加的殺**不計入**每回合殺次數限制
- 可與八卦陣並存：B 出閃若是八卦陣判定，觸發青龍偃月刀後，A 再出殺，B 再判定八卦陣

**備註**：此 API 獨立於 `playCard`

---

## 17. 貫石斧效果選擇

```
POST /api/games/{gameId}/player:useStonePiercingAxeEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID |
| choice | String | `DISCARD_TWO`（棄兩張牌強制命中）或 `SKIP`（不發動） |
| discardCardIds | List\<String\> | 要棄的兩張 cardId（DISCARD_TWO 時必填，長度必須為 2；SKIP 時傳空陣列） |

**觸發時機**：收到 `AskStonePiercingAxeEffectEvent` 後呼叫

**流程**：
```
A (裝備貫石斧) 對 B 出殺 → B 出閃抵銷
  → 系統檢查 A 是否有 ≥2 張可棄牌（手牌 + 裝備區）
  → 有：發出 AskStonePiercingAxeEffectEvent（詢問 A）
  → A 呼叫本 API 做選擇：
    - SKIP: 殺被抵銷結束
    - DISCARD_TWO: A 棄兩張牌到墓地 → 發出 StonePiercingAxeTriggerEvent
                   → B 強制扣血（跳過 AskDodge）
                   → 若 B HP=0 進入瀕死流程
```

**特殊情況**：
- 可棄的牌可以是手牌或裝備區的牌（包括貫石斧本身）
- A 可棄牌總數 < 2 時，不會觸發貫石斧，殺直接被閃抵銷
- 棄牌不足 2 張、傳入非 A 擁有的 cardId → 拋例外

**備註**：此 API 獨立於 `playCard`

---

## 18. 丈八蛇矛出殺（棄兩張牌當殺）

```
POST /api/games/{gameId}/player:useViperSpearKill
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID（裝備丈八蛇矛） |
| targetPlayerId | String | 被攻擊玩家 ID |
| discardCardIds | List\<String\> | 作為殺的兩張手牌 cardId（長度必須為 2） |

**觸發時機**：裝備丈八蛇矛的玩家主動使用（不透過 `playCard` API）

**流程**：
```
A (裝備丈八蛇矛) 呼叫本 API → 棄兩張手牌到墓地 → 視為出殺
  → 系統發出 ViperSpearKillTriggerEvent（通知前端丈八蛇矛發動）
  → 進入殺流程：B 收到 AskDodgeEvent
    - B 出閃 → 殺被抵銷
    - B 不出閃 → B 扣血（HP=0 進入瀕死流程）
```

**特殊情況**：
- 攻擊者必須裝備丈八蛇矛
- 攻擊者本回合若已出過殺（無諸葛連弩）仍受殺次數限制
- discardCardIds 必須剛好為 2 張，且必須是攻擊者手牌中的 cardId
- 不可與其他 Behavior 同時進行（topBehavior 必須為空）

**備註**：
- 此 API 獨立於 `playCard`
- 內部使用虛擬殺（VirtualKill，cardId = `VIPER_SPEAR_VIRTUAL_KILL`），不在手牌也不在 PlayCard enum 中
- Phase 2 TODO：被動場景（被決鬥/南蠻入侵/借刀殺人要求出殺時改用丈八蛇矛回應）尚未支援

---

## WebSocket 事件類型

前端透過 WebSocket 接收以下事件，根據事件類型決定 UI 行為：

### 遊戲流程事件

| 事件 | 說明 | 觸發 API |
|------|------|----------|
| `PlayCardEvent` | 有人出牌 | playCard |
| `PlayWardCardEvent` | 有人出無懈可擊 | playWardCard |
| `GameStatusEvent` | 遊戲狀態更新（每次操作都附帶） | 所有 API |
| `DrawCardEvent` | 玩家摸牌 | finishAction / playCard |
| `DiscardEvent` | 玩家棄牌 | discardCards |

### 需要玩家回應的事件

| 事件 | 說明 | 玩家需呼叫的 API |
|------|------|------------------|
| `AskPlayWardEvent` | 詢問是否出無懈可擊（有 Ward 的玩家收到） | playWardCard |
| `WaitForWardEvent` | 等待其他人出無懈可擊（沒 Ward 的玩家收到） | （等待） |
| `AskKillEvent` | 需要出殺（南蠻入侵/決鬥/借刀殺人） | playCard |
| `AskDodgeEvent` | 需要出閃（萬箭齊發/被殺） | playCard |
| `AskPeachEvent` | 瀕死需要出桃 | playCard |
| `BountifulHarvestEvent` | 五穀豐登輪到你選牌 | chooseCardFromBountifulHarvest |
| `AskYinYangSwordsEffectEvent` | 雌雄雙股劍效果：目標選擇棄牌或讓攻擊者摸牌 | useYinYangSwordsEffect |
| `AskGreenDragonCrescentBladeEffectEvent` | 青龍偃月刀效果：攻擊者選擇是否再出一張殺 | useGreenDragonCrescentBladeEffect |
| `AskStonePiercingAxeEffectEvent` | 貫石斧效果：攻擊者選擇是否棄兩張牌強制命中 | useStonePiercingAxeEffect |
| `ViperSpearKillTriggerEvent` | 丈八蛇矛發動：攻擊者棄兩張牌作為虛擬殺使用（通知事件，無需回應） | useViperSpearKill |

### 效果事件

| 事件 | 說明 |
|------|------|
| `WardEvent` | Ward 結算結果 |
| `BarbarianInvasionEvent` | 南蠻入侵效果 |
| `ArrowBarrageEvent` | 萬箭齊發效果 |
| `DuelEvent` | 決鬥效果 |
| `BountifulHarvestChooseCardEvent` | 玩家選了五穀豐登的牌 |
| `ContentmentEvent` | 樂不思蜀判定結果 |
| `LightningEvent` | 閃電判定結果 |
| `LightningTransferredEvent` | 閃電轉移到下家 |
| `SomethingForNothingEvent` | 無中生有效果 |
| `PeachGardenEvent` | 桃園結義效果 |
| `BorrowedSwordEvent` | 借刀殺人效果 |
| `EquipmentEvent` | 裝備效果 |
| `JudgementEvent` | 判定結果（八卦陣/樂不思蜀/閃電） |
| `YinYangSwordsEffectEvent` | 雌雄雙股劍效果結算：含 attackerPlayerId、targetPlayerId、choice（TARGET_DISCARDS/ATTACKER_DRAWS）、discardedCardId |
| `BlackPommelEffectEvent` | 青釭劍發動，殺無視目標防具（含 attackerPlayerId、targetPlayerId） |
| `GreenDragonCrescentBladeTriggerEvent` | 青龍偃月刀發動，追加一張殺（含 attackerPlayerId、targetPlayerId、killCardId） |
| `StonePiercingAxeTriggerEvent` | 貫石斧發動，棄兩張牌強制命中（含 attackerPlayerId、targetPlayerId、discardedCardIds） |
| `ViperSpearKillTriggerEvent` | 丈八蛇矛發動，棄兩張牌當殺使用（含 attackerPlayerId、targetPlayerId、discardedCardIds） |
