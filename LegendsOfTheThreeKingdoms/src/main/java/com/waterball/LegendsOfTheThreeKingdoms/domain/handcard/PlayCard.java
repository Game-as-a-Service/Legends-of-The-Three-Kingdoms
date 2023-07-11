package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;

public enum PlayCard {
    SSA001("SSA001","決鬥", Suit.SPADE, Rank.ACE),
    EES2002("ES2002","雌雄雙股劍", Suit.SPADE, Rank.TWO),
    SS3003("SS3003","過河拆橋", Suit.SPADE, Rank.THREE),
    SS4004("SS4004","過河拆橋", Suit.SPADE, Rank.FOUR),
    ES5005("ES5005","青龍偃月刀", Suit.SPADE, Rank.FIVE),
    SS6006("SS6006","樂不思蜀", Suit.SPADE, Rank.SIX),
    SSA007("SSA007","南蠻入侵", Suit.SPADE, Rank.SEVEN),
    BS8008("BS8008","殺", Suit.SPADE, Rank.EIGHT),
    BS8009("BS8009","殺", Suit.SPADE, Rank.NINE),
    BS8010("BS8010","殺", Suit.SPADE, Rank.TEN),
    SSJ011("SSJ011","無懈可擊", Suit.SPADE, Rank.J),
    SSQ012("SSQ012","過河拆橋", Suit.SPADE, Rank.Q),
    SSK013("SSK013","南蠻入侵", Suit.SPADE, Rank.K),

    SSA014("SSA014","閃電", Suit.SPADE, Rank.ACE),
    ES2015("ES2015","八卦陣", Suit.SPADE, Rank.TWO),
    SS3016("SS3016","順手牽羊", Suit.SPADE, Rank.THREE),
    SS4017("SS4017","順手牽羊", Suit.SPADE, Rank.FOUR),
    ES5018("ES5018","赤兔", Suit.SPADE, Rank.FIVE),
    ES6019("ES6019","青虹劍", Suit.SPADE, Rank.SIX),
    SS7020("SS7020","殺", Suit.SPADE, Rank.SEVEN),
    BS8021("BS8021","殺", Suit.SPADE, Rank.EIGHT),
    BS9022("BS9022","殺", Suit.SPADE, Rank.NINE),
    BS0023("BS0023","殺", Suit.SPADE, Rank.TEN),
    SSJ024("SSJ024","順手牽羊", Suit.SPADE, Rank.J),
    ESQ025("ESQ025","丈八蛇矛", Suit.SPADE, Rank.Q),
    ESK026("ESK026","黃爪飛電", Suit.SPADE, Rank.K),

    SHA027("SHA027","桃源結義", Suit.HEART, Rank.ACE),
    BH2028("BH2028","閃", Suit.HEART, Rank.TWO),
    BH3029("BH3029","桃", Suit.HEART, Rank.THREE),
    BH4030("BH4030","桃", Suit.HEART, Rank.FOUR),
    EH5031("EH5031","麒麟弓", Suit.HEART, Rank.FIVE),
    BH6032("BH6032","桃", Suit.HEART, Rank.SIX),
    BH7033("BH7033","桃", Suit.HEART, Rank.SEVEN),
    BH8034("BH8034","桃", Suit.HEART, Rank.EIGHT),
    BH9035("BH9035","桃", Suit.HEART, Rank.NINE),
    BH0036("BH0036","殺", Suit.HEART, Rank.TEN),
    BHJ037("BHJ037","殺", Suit.HEART, Rank.J),
    BHQ038("BHQ038","桃", Suit.HEART, Rank.Q),
    BHK039("BHK039","閃", Suit.HEART, Rank.K),

    // TODO

    SCA053("SCA053","決鬥", Suit.CLUB, Rank.ACE),
    SCA054("SC2054","殺", Suit.CLUB, Rank.TWO),
    SCA055("SC3055","殺", Suit.CLUB, Rank.THREE),
    SCA056("SC4056","殺", Suit.CLUB, Rank.FOUR),
    SCA057("SC5057","殺", Suit.CLUB, Rank.FIVE),
    SCA058("SC6058","殺", Suit.CLUB, Rank.SIX),
    SCA059("SC7059","殺", Suit.CLUB, Rank.SEVEN),
    SCA060("SC8060","殺", Suit.CLUB, Rank.EIGHT),
    SCA061("SC9061","殺", Suit.CLUB, Rank.NINE),
    SCA062("SC0062","殺", Suit.CLUB, Rank.TEN),
    SCA063("SCJ063","殺", Suit.CLUB, Rank.J),
    SCA064("SCQ064","借刀殺人", Suit.CLUB, Rank.Q),
    SCA065("SCK065","借刀殺人", Suit.CLUB, Rank.K),

    ;

    private final String cardId;
    private final String cardName;
    private final Suit suit;
    private final Rank rank;


    PlayCard(String cardId, String cardName, Suit suit, Rank rank) {
        this.cardId = cardId;
        this.cardName = cardName;
        this.suit = suit;
        this.rank = rank;
    }

    public String getCardId() {
        return cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }
}
