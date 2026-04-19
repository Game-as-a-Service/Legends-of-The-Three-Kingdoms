# 三國殺標準版遊戲牌開發進度

> 此檔案不納入 git 追蹤

## 開發進度摘要

基本牌（殺/閃/桃）與錦囊牌（12 張）已全數完成，含無懈可擊與所有錦囊的組合互動。裝備牌完成 9/15，剩餘 6 張未開發。整體進度 80%（24/30 種牌）。

**Batch 1 完成（PR #143, #144, #145）：**
- PR #143: 4 張坐騎（HexMark 的盧、VioletStallion 紫騂、YellowFlash 爪黃飛電、FerghanaHorse 黃爪飛電）+ 八卦陣/諸葛連弩第二張 factory 登錄 ✅ merged
- PR #144: 雌雄雙股劍 YinYangSwords + Gender enum 前置工作 ✅ merged
- PR #145: 青釭劍 BlackPommel（含 BlackPommelEffectEvent 前後端通知）⏳ pending review

**Infrastructure 修復（PR #146）：** ✅ merged
- Root cause: `maven-surefire-plugin 2.22.0` 沒有 `junit-jupiter-engine` 無法偵測 JUnit 5 測試，導致 **210 個 domain tests 被靜默跳過** 多年未被執行
- 修復：加 `junit-jupiter-engine` 依賴
- 結果：domain tests 從 78 → 293 全部 pass，spring e2e 從 0 報告→ 183 tests

**已完成的 Ward 整合（無懈可擊連動）：**
決鬥、南蠻入侵、萬箭齊發、過河拆橋、順手牽羊、借刀殺人、五穀豐登（PR#129）、無中生有（PR#138）、樂不思蜀（PR#139, #140）、閃電（PR#141）。Ward 採 Behavior Stack 架構，奇數張取消效果、偶數張效果生效。

**已修復的 Bug：**
- PR#127: Ward counter-chain 卡住（trigger player 被包含在 reactionPlayers）
- PR#137: 五穀豐登 Presenter 層缺少 WaitForWardEvent → AskPlayWardEvent 轉換
- PR#138: 無中生有 Ward skip 後 Behavior 殘留（缺 isOneRound = true）
- PR#139: FinishActionPresenter 缺少 Ward per-player 轉換
- PR#142: DiscardPresenter 缺少 Ward per-player 轉換（最後一個 Presenter）
- PR#146: Maven surefire 沒有 JUnit 5 engine 導致 210 個測試被跳過
- YinYangSwords 補強（PR pending）: 新增 `useYinYangSwordsEffect` API、`YinYangSwordsEffectEvent` 前端通知、修復 `GeneralCardData` 漏序列化 gender 導致 MongoDB 往返後 gender=null 的 bug、補 4 個 e2e tests + JSON fixtures

**CI/CD 更新：**
- PR#130: 新增 NAS Docker Registry 推送
- PR#131: NAS image 加 timestamp tag
- PR#132: NAS 部署 webhook 自動觸發
- PR#134: Discord 通知拆分（PR 開啟/部署成功/部署失敗）
- PR#136: 移除 EC2 deploy，NAS 為唯一部署目標

**未開發裝備牌（6 張）：**
P2 新互動：青龍偃月刀、貫石斧、寒冰劍。
P3 較複雜：丈八蛇矛、方天畫戟、仁王盾。

## 基本牌（3/3）✅ 100%

| 牌名 | Card IDs | 狀態 |
|------|----------|------|
| 殺 | BS8008, BS9009, BS0010, BS7020, BS8021, BS9022, BS0023, BH0049, BC2054~BCJ063, BD6084~BDK091, etc. | ✅ 完成 |
| 閃 | BH2028, BH2041, BD2080, BD2093~BDJ102, etc. | ✅ 完成 |
| 桃 | BH3029, BH4030, BH6032~BH9035, BHQ038, BDQ090 | ✅ 完成 |

## 錦囊牌（12/12）✅ 100%

| 牌名 | Card IDs | Behavior | Handler | Ward 整合 | 狀態 |
|------|----------|----------|---------|-----------|------|
| 決鬥 | SSA001, SCA053, SDA079 | DuelBehavior | DuelBehaviorHandler | ✅ | ✅ 完成 |
| 閃電 | SSA014 | LightningBehavior, LightningJudgementBehavior | LightningBehaviorHandler | ✅ PR#141 | ✅ 完成 |
| 桃園結義 | SHA027 | PeachGardenBehavior | PeachGardenBehaviorHandler | — | ✅ 完成 |
| 萬箭齊發 | SHA040 | ArrowBarrageBehavior | ArrowBarrageBehaviorHandler | ✅ | ✅ 完成 |
| 過河拆橋 | SS3003, SS4004, SSQ012, SHQ051, SC3068, SC4069 | DismantleBehavior | DismantleBehaviorHandler | ✅ | ✅ 完成 |
| 順手牽羊 | SS3016, SS4017, SSJ024, SD3081, SD4082 | SnatchBehavior | SnatchBehaviorHandler | ✅ | ✅ 完成 |
| 五穀豐登 | SH3042, SH4043 | BountifulHarvestBehavior | BountifulHarvestHandler | ✅ PR#129 | ✅ 完成 |
| 南蠻入侵 | SS7007, SSK013, SC7072 | BarbarianInvasionBehavior | BarbarianInvasionBehaviorHandler | ✅ | ✅ 完成 |
| 無中生有 | SH7046, SH8047, SH9048, SHJ050 | SomethingForNothingBehavior | SomethingForNothingHandler | ✅ PR#138 | ✅ 完成 |
| 樂不思蜀 | SS6006, SH6045, SC6071 | ContentmentBehavior, ContentmentJudgementBehavior | ContentmentBehaviorHandler | ✅ PR#139 | ✅ 完成 |
| 借刀殺人 | SCQ064, SCK065 | BorrowedSwordBehavior | BorrowedSwordBehaviorHandler | ✅ | ✅ 完成 |
| 無懈可擊 | SSJ011, SCQ077, SCK078 | WardBehavior | — | — | ✅ 完成 |

## 裝備牌 — 武器（4/8）

| 牌名 | Card ID | 攻擊範圍 | 效果 | 狀態 |
|------|---------|----------|------|------|
| 諸葛連弩 | ECA066, EDA092 | 1 | 出殺無次數限制 | ✅ 完成 |
| 麒麟弓 | EH5031 | 5 | 殺命中可棄對方一匹馬 | ✅ 完成 |
| 雌雄雙股劍 YinYangSwords | ES2002 | 2 | 攻擊異性角色時，令其棄一牌或你摸一牌（含 AskYinYangSwordsEffectEvent + YinYangSwordsEffectEvent 前端通知 + useYinYangSwordsEffect API） | ✅ PR#144 merged + PR pending |
| 青釭劍 BlackPommel | ES6019 | 2 | 殺無視防具（含 BlackPommelEffectEvent 通知前端） | ✅ 完成 PR#145 |
| 青龍偃月刀 GreenDragonCrescentBlade | ES5005 | 3 | 殺被閃後可再出一張殺（含 AskGreenDragonCrescentBladeEffectEvent + GreenDragonCrescentBladeTriggerEvent + useGreenDragonCrescentBladeEffect API） | ✅ 完成 PR#148 |
| 貫石斧 StonePiercingAxe | ED5083 | 3 | 殺被閃後可棄兩張牌強制命中（含 AskStonePiercingAxeEffectEvent + StonePiercingAxeTriggerEvent + useStonePiercingAxeEffect API） | ✅ 完成 |
| 方天畫戟 HeavenlyDoubleHalberd | EDQ103 | 4 | 最後一張手牌為殺時可額外指定兩個目標 | ❌ 未開發 |
| 丈八蛇矛 EighteenSpanViperSpear | ESQ025 | 3 | 棄兩張手牌當殺使用（含 VirtualKill + ViperSpearKillBehavior + ViperSpearKillTriggerEvent + useViperSpearKill API，Phase 2 被動場景為 TODO） | ⏳ PR pending |

## 裝備牌 — 防具（1/3）

| 牌名 | Card ID | 效果 | 狀態 |
|------|---------|------|------|
| 八卦陣 | ES2015, EC2067 | 需出閃時可判定，紅色則視為出閃 | ✅ 完成 |
| 仁王盾 | — | 黑色殺無效 | ❌ 未開發 |
| 寒冰劍 | — | 殺命中後可改為翻看對方兩張牌各棄一張 | ❌ 未開發 |

## 裝備牌 — 坐騎（6/6）✅ 100%

| 牌名 | Card ID | 類型 | 效果 | 狀態 |
|------|---------|------|------|------|
| 絕影 ShadowHorse | ES5018 | +1馬 | 其他角色計算與你的距離+1 | ✅ 完成 |
| 赤兔 RedRabbitHorse | EH5044 | -1馬 | 你計算與其他角色的距離-1 | ✅ 完成 |
| 的盧 HexMark | EC5070 | +1馬 | 其他角色計算與你的距離+1 | ✅ 完成 PR#143 |
| 紫騂 VioletStallion | EDK104 | -1馬 | 你計算與其他角色的距離-1 | ✅ 完成 PR#143 |
| 爪黃飛電 YellowFlash | EHK052 | +1馬 | 其他角色計算與你的距離+1 | ✅ 完成 PR#143 |
| 黃爪飛電 FerghanaHorse | ESK026 | -1馬 | 你計算與其他角色的距離-1 | ✅ 完成 PR#143 |

## 總結

| 類別 | 完成 | 總數 | 進度 |
|------|------|------|------|
| 基本牌 | 3 | 3 | 100% |
| 錦囊牌 | 12 | 12 | 100% |
| 武器 | 6 | 8 | 75% |
| 防具 | 1 | 3 | 33% |
| 坐騎 | 6 | 6 | 100% |
| **總計** | **28** | **32** | **88%** |

## PlayCard enum 未開發清單（5 個 Card ID）

### P2 — 需要新武器效果機制

| Card ID | 牌名 | 花色 | 點數 | 攻擊範圍 | 效果 |
|---------|------|------|------|----------|------|
| ES5005 | 青龍偃月刀 GreenDragonCrescentBlade | 黑桃 | 5 | 3 | 殺被閃後可繼續出殺 |
| ED5083 | 貫石斧 StonePiercingAxe | 方塊 | 5 | 3 | 殺被閃後可棄兩張牌強制命中 |

### P3 — 較複雜

| Card ID | 牌名 | 花色 | 點數 | 攻擊範圍 | 效果 |
|---------|------|------|------|----------|------|
| ESQ025 | 丈八蛇矛 EighteenSpanViperSpear | 黑桃 | Q | 3 | 棄兩張手牌當殺使用 |
| EDQ103 | 方天畫戟 HeavenlyDoubleHalberd | 方塊 | Q | 4 | 最後一張手牌為殺時可額外指定兩個目標 |

### 擴充牌（EX 系列，enum 尚無定義）

- 仁王盾（防具）— 黑色殺無效
- 寒冰劍（武器）— 殺命中後可改為翻看對方兩張牌各棄一張

中英對照表
武器牌
WeaponCard
諸葛連弩
Chu Ko Nu
青鋼劍
Black Pommel
雌雄雙股劍
Yin-Yang Swords
貫石斧
Stone Piercing Axe
青龍偃月刀
Green Dragon Crescent Blade
丈八蛇矛
Eighteen-span Viper Spear
方天畫戟
Heavenly Double Halberd
麒麟弓
Qilin Bow
坐騎牌
MountsCard
赤兔
Red Hare
大宛
Ferghana Horse
紫騂
Violet Stallion
绝影
Shadowrunner
爪黃飛電
Yellow Flash
的盧
Hex Mark
