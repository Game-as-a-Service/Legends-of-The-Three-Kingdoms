- 詳細按照 ATDD 流程開發
- 寫法要參考既有的 WardWithDuelTest 測試方法寫法(特別是建立遊戲、以及卡牌數量要足夠)
- 最小最小迭代，測試不能改
- 處理 bug如下
- 🪱 : 
- Given : A有決鬥，B有無懈可擊，C有無懈可擊，A有無懈可擊，
  A對B出決鬥，B出無懈可擊，A C 不出無懈可擊
  => A的決鬥被B的無懈可擊取消
  此時A再進行出牌會出現
  java.lang.NullPointerException: Cannot invoke "java.util.List.isEmpty()"because "events"is null
  此時A進行結束回合會出現
  java.lang.IllegalStateException: current topBehavior is not null size[
  1
  ]
- e2e 測試寫在 /Users/scolley/Desktop/三國殺/Legends-of-The-Three-Kingdoms/LegendsOfTheThreeKingdoms/spring/src/test/java/com/gaas/threeKingdoms/e2e/scrollcard/ward/WardWithDuelTest.java
- 先分析，寫測試，然後在實作
