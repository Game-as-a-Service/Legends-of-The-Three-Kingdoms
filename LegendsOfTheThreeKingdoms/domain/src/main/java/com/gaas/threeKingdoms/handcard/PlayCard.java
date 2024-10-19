package com.gaas.threeKingdoms.handcard;

import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public enum PlayCard {
    SSA001("SSA001", "決鬥", Suit.SPADE, Rank.ACE),
    ES2002("ES2002", "雌雄雙股劍", Suit.SPADE, Rank.TWO),
    SS3003("SS3003", "過河拆橋", Suit.SPADE, Rank.THREE),
    SS4004("SS4004", "過河拆橋", Suit.SPADE, Rank.FOUR),
    ES5005("ES5005", "青龍偃月刀", Suit.SPADE, Rank.FIVE),
    SS6006("SS6006", "樂不思蜀", Suit.SPADE, Rank.SIX),
    SS7007("SS7007", "南蠻入侵", Suit.SPADE, Rank.SEVEN),
    BS8008("BS8008", "殺", Suit.SPADE, Rank.EIGHT),
    BS8009("BS8009", "殺", Suit.SPADE, Rank.NINE),
    BS8010("BS8010", "殺", Suit.SPADE, Rank.TEN),
    SSJ011("SSJ011", "無懈可擊", Suit.SPADE, Rank.J),
    SSQ012("SSQ012", "過河拆橋", Suit.SPADE, Rank.Q),
    SSK013("SSK013", "南蠻入侵", Suit.SPADE, Rank.K),
    SSA014("SSA014", "閃電", Suit.SPADE, Rank.ACE),
    ES2015("ES2015", "八卦陣", Suit.SPADE, Rank.TWO),
    SS3016("SS3016", "順手牽羊", Suit.SPADE, Rank.THREE),
    SS4017("SS4017", "順手牽羊", Suit.SPADE, Rank.FOUR),
    ES5018("ES5018", "絕影", Suit.SPADE, Rank.FIVE),
    ES6019("ES6019", "青虹劍", Suit.SPADE, Rank.SIX),
    BS7020("BS7020", "殺", Suit.SPADE, Rank.SEVEN),
    BS8021("BS8021", "殺", Suit.SPADE, Rank.EIGHT),
    BS9022("BS9022", "殺", Suit.SPADE, Rank.NINE),
    BS0023("BS0023", "殺", Suit.SPADE, Rank.TEN),
    SSJ024("SSJ024", "順手牽羊", Suit.SPADE, Rank.J),
    ESQ025("ESQ025", "丈八蛇矛", Suit.SPADE, Rank.Q),
    ESK026("ESK026", "黃爪飛電", Suit.SPADE, Rank.K),

    SHA027("SHA027", "桃源結義", Suit.HEART, Rank.ACE),
    BH2028("BH2028", "閃", Suit.HEART, Rank.TWO),
    BH3029("BH3029", "桃", Suit.HEART, Rank.THREE),
    BH4030("BH4030", "桃", Suit.HEART, Rank.FOUR),
    EH5031("EH5031", "麒麟弓", Suit.HEART, Rank.FIVE),
    BH6032("BH6032", "桃", Suit.HEART, Rank.SIX),
    BH7033("BH7033", "桃", Suit.HEART, Rank.SEVEN),
    BH8034("BH8034", "桃", Suit.HEART, Rank.EIGHT),
    BH9035("BH9035", "桃", Suit.HEART, Rank.NINE),
    BH0036("BH0036", "殺", Suit.HEART, Rank.TEN),
    BHJ037("BHJ037", "殺", Suit.HEART, Rank.J),
    BHQ038("BHQ038", "桃", Suit.HEART, Rank.Q),
    BHK039("BHK039", "閃", Suit.HEART, Rank.K),

    BHK040("BHK040", "萬箭齊發", Suit.HEART, Rank.ACE),
    BH2041("BH2041", "閃", Suit.HEART, Rank.TWO),
    SH3042("SH3042", "五穀豐登", Suit.HEART, Rank.THREE),
    SH4043("SH4043", "五穀豐登", Suit.HEART, Rank.FOUR),
    EH5044("EH5044", "赤兔", Suit.HEART, Rank.FIVE),
    SH6045("SH6045", "樂不思蜀", Suit.HEART, Rank.SIX),
    SH7046("SH7046", "無中生有", Suit.HEART, Rank.SEVEN),
    SH8047("SH8047", "無中生有", Suit.HEART, Rank.EIGHT),
    SH9048("SH9048", "無中生有", Suit.HEART, Rank.NINE),
    BH0049("BH0049", "殺", Suit.HEART, Rank.TEN),
    SHJ050("SHJ050", "無中生有", Suit.HEART, Rank.J),
    SHQ051("SHQ051", "過河拆橋", Suit.HEART, Rank.Q),
    EHK052("EHK052", "爪黃飛電", Suit.HEART, Rank.K),

    SCA053("SCA053", "決鬥", Suit.CLUB, Rank.ACE),
    BC2054("BC2054", "殺", Suit.CLUB, Rank.TWO),
    BC3055("BC3055", "殺", Suit.CLUB, Rank.THREE),
    BC4056("BC4056", "殺", Suit.CLUB, Rank.FOUR),
    BC5057("BC5057", "殺", Suit.CLUB, Rank.FIVE),
    BC6058("BC6058", "殺", Suit.CLUB, Rank.SIX),
    BC7059("BC7059", "殺", Suit.CLUB, Rank.SEVEN),
    BC8060("BC8060", "殺", Suit.CLUB, Rank.EIGHT),
    BC9061("BC9061", "殺", Suit.CLUB, Rank.NINE),
    BC0062("BC0062", "殺", Suit.CLUB, Rank.TEN),
    BCJ063("BCJ063", "殺", Suit.CLUB, Rank.J),
    SCQ064("SCQ064", "借刀殺人", Suit.CLUB, Rank.Q),
    SCK065("SCK065", "借刀殺人", Suit.CLUB, Rank.K),

    ECA066("ECA066", "諸葛連弩", Suit.CLUB, Rank.ACE),
    EC2067("EC2067", "八卦陣", Suit.CLUB, Rank.TWO),
    SC3068("SC3068", "過河拆橋", Suit.CLUB, Rank.THREE),
    SC4069("SC4069", "過河拆橋", Suit.CLUB, Rank.FOUR),
    EC5070("EC5070", "的盧", Suit.CLUB, Rank.FIVE),
    SC6071("SC6071", "樂不思蜀", Suit.CLUB, Rank.SIX),
    SC7072("SC7072", "南蠻入侵", Suit.CLUB, Rank.SEVEN),
    BC8073("BC8073", "殺", Suit.CLUB, Rank.EIGHT),
    BC9074("BC9074", "殺", Suit.CLUB, Rank.NINE),
    BC0075("BC0075", "殺", Suit.CLUB, Rank.TEN),
    BCJ076("BCJ076", "殺", Suit.CLUB, Rank.J),
    SCQ077("SCQ077", "無懈可擊", Suit.CLUB, Rank.Q),
    SCK078("SCK078", "無懈可擊", Suit.CLUB, Rank.K),

    SDA079("SDA079", "決鬥", Suit.DIAMOND, Rank.ACE),
    BD2080("BD2080", "閃", Suit.DIAMOND, Rank.TWO),
    SD3081("SD3081", "順手牽羊", Suit.DIAMOND, Rank.THREE),
    SD4082("SD4082", "順手牽羊", Suit.DIAMOND, Rank.FOUR),
    ED5083("ED5083", "貫石斧", Suit.DIAMOND, Rank.FIVE),
    BD6084("BD6084", "殺", Suit.DIAMOND, Rank.SIX),
    BD7085("BD7085", "殺", Suit.DIAMOND, Rank.SEVEN),
    BD8086("BD8086", "殺", Suit.DIAMOND, Rank.EIGHT),
    BD9087("BD9087", "殺", Suit.DIAMOND, Rank.NINE),
    BD0088("BD0088", "殺", Suit.DIAMOND, Rank.TEN),
    BDJ089("BDJ089", "閃", Suit.DIAMOND, Rank.J),
    BDQ090("BDQ090", "桃", Suit.DIAMOND, Rank.Q),
    BDK091("BDK091", "殺", Suit.DIAMOND, Rank.K),

    EDA092("EDA092", "諸葛連弩", Suit.DIAMOND, Rank.ACE),
    BD2093("BD2093", "閃", Suit.DIAMOND, Rank.TWO),
    BD3094("BD3094", "閃", Suit.DIAMOND, Rank.THREE),
    BD4095("BD4095", "閃", Suit.DIAMOND, Rank.FOUR),
    BD5096("BD5096", "閃", Suit.DIAMOND, Rank.FIVE),
    BD6097("BD6097", "閃", Suit.DIAMOND, Rank.SIX),
    BD7098("BD7098", "閃", Suit.DIAMOND, Rank.SEVEN),
    BD8099("BD8099", "閃", Suit.DIAMOND, Rank.EIGHT),
    BD9100("BD9100", "閃", Suit.DIAMOND, Rank.NINE),
    BD0101("BD0101", "閃", Suit.DIAMOND, Rank.TEN),
    BDJ102("BDJ102", "閃", Suit.DIAMOND, Rank.J),
    EDQ103("EDQ103", "方天畫戟", Suit.DIAMOND, Rank.Q),
    EDK104("EDK104", "紫騂", Suit.DIAMOND, Rank.K),
    ;

    private static final Map<String, HandCard> CARD_FACTORY_MAP = new HashMap<>();

    static {
        CARD_FACTORY_MAP.put("BS8008", new Kill(BS8008));
        CARD_FACTORY_MAP.put("BS8009", new Kill(BS8009));
        CARD_FACTORY_MAP.put("BS8010", new Kill(BS8010));
        CARD_FACTORY_MAP.put("BH0049", new Kill(BH0049));
        CARD_FACTORY_MAP.put("BH0036", new Kill(BH0036));
        CARD_FACTORY_MAP.put("BHJ037", new Kill(BHJ037));
        CARD_FACTORY_MAP.put("SSA001", new Kill(SSA001));
        CARD_FACTORY_MAP.put("BS9022", new Kill(BS9022));
        CARD_FACTORY_MAP.put("BS8021", new Kill(BS8021));
        CARD_FACTORY_MAP.put("BS0023", new Kill(BS0023));
        CARD_FACTORY_MAP.put("BC2054", new Kill(BC2054));
        CARD_FACTORY_MAP.put("BC3055", new Kill(BC3055));
        CARD_FACTORY_MAP.put("BC4056", new Kill(BC4056));
        CARD_FACTORY_MAP.put("BC5057", new Kill(BC5057));
        CARD_FACTORY_MAP.put("BC6058", new Kill(BC6058));
        CARD_FACTORY_MAP.put("BC7059", new Kill(BC7059));
        CARD_FACTORY_MAP.put("BC8060", new Kill(BC8060));
        CARD_FACTORY_MAP.put("BC9061", new Kill(BC9061));
        CARD_FACTORY_MAP.put("BC0062", new Kill(BC0062));
        CARD_FACTORY_MAP.put("BCJ063", new Kill(BCJ063));
        CARD_FACTORY_MAP.put("BD6084", new Kill(BD6084));
        CARD_FACTORY_MAP.put("BD7085", new Kill(BD7085));
        CARD_FACTORY_MAP.put("BD8086", new Kill(BD8086));
        CARD_FACTORY_MAP.put("BD9087", new Kill(BD9087));
        CARD_FACTORY_MAP.put("BD0088", new Kill(BD0088));
        CARD_FACTORY_MAP.put("BDK091", new Kill(BDK091));
        CARD_FACTORY_MAP.put("BC8073", new Kill(BC8073));
        CARD_FACTORY_MAP.put("BC9074", new Kill(BC9074));
        CARD_FACTORY_MAP.put("BC0075", new Kill(BC0075));
        CARD_FACTORY_MAP.put("BCJ076", new Kill(BCJ076));
        CARD_FACTORY_MAP.put("BH4030", new Peach(BH4030));
        CARD_FACTORY_MAP.put("BH3029", new Peach(BH3029));
        CARD_FACTORY_MAP.put("BH6032", new Peach(BH6032));
        CARD_FACTORY_MAP.put("BH7033", new Peach(BH7033));
        CARD_FACTORY_MAP.put("BH8034", new Peach(BH8034));
        CARD_FACTORY_MAP.put("BH9035", new Peach(BH9035));
        CARD_FACTORY_MAP.put("BHQ038", new Peach(BHQ038));
        CARD_FACTORY_MAP.put("BDJ102", new Dodge(BDJ102));
        CARD_FACTORY_MAP.put("BH2041", new Dodge(BH2041));
        CARD_FACTORY_MAP.put("BH2028", new Dodge(BH2028));
        CARD_FACTORY_MAP.put("BHK039", new Dodge(BHK039));
        CARD_FACTORY_MAP.put("BD2093", new Dodge(BD2093));
        CARD_FACTORY_MAP.put("BD3094", new Dodge(BD3094));
        CARD_FACTORY_MAP.put("BD4095", new Dodge(BD4095));
        CARD_FACTORY_MAP.put("BD5096", new Dodge(BD5096));
        CARD_FACTORY_MAP.put("BD6097", new Dodge(BD6097));
        CARD_FACTORY_MAP.put("BD7098", new Dodge(BD7098));
        CARD_FACTORY_MAP.put("BD8099", new Dodge(BD8099));
        CARD_FACTORY_MAP.put("BD9100", new Dodge(BD9100));
        CARD_FACTORY_MAP.put("BD0101", new Dodge(BD0101));
        CARD_FACTORY_MAP.put("BD2080", new Dodge(BD2080));
        CARD_FACTORY_MAP.put("BDJ089", new Dodge(BDJ089));
        CARD_FACTORY_MAP.put("EH5044", new RedRabbitHorse(EH5044));
        CARD_FACTORY_MAP.put("ES5018", new ShadowHorse(ES5018));
        CARD_FACTORY_MAP.put("EH5031", new QilinBowCard(EH5031));
        CARD_FACTORY_MAP.put("SSK013", new BarbarianInvasion(SSK013));
        CARD_FACTORY_MAP.put("SC7072", new BarbarianInvasion(SC7072));
        CARD_FACTORY_MAP.put("SS7007", new BarbarianInvasion(SS7007));
        CARD_FACTORY_MAP.put("SCK065", new BorrowedSword(SCK065));
        CARD_FACTORY_MAP.put("SCQ064", new BorrowedSword(SCQ064));
        CARD_FACTORY_MAP.put("ECA066", new RepeatingCrossbowCard(ECA066));
        CARD_FACTORY_MAP.put("ES2015", new EightDiagramTactic(ES2015));
    }

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

    public static boolean isDodgeCard(String cardId) {
        return Arrays.stream(PlayCard.values())
                .filter(c -> c.getCardName().equals("閃"))
                .anyMatch(c -> c.getCardId().equals(cardId));
    }

    public static boolean isPeachCard(String cardId){
        return Arrays.stream(PlayCard.values())
                .filter(c->c.getCardName().equals("桃"))
                .anyMatch(c->c.getCardId().equals(cardId));
    }

    public static boolean isKillCard(String cardId){
        return Arrays.stream(PlayCard.values())
                .filter(c->c.getCardName().equals("殺"))
                .anyMatch(c->c.getCardId().equals(cardId));
    }

    public static boolean isEightDiagramTacticCard(String cardId){
        return Arrays.stream(PlayCard.values())
                .filter(c->c.getCardName().equals("八卦陣"))
                .anyMatch(c->c.getCardId().equals(cardId));
    }
    public boolean isMountsCard() {return Arrays.stream(PlayCard.values()).filter(c -> "赤兔".equals(c.getCardName()) || "絕影".equals(c.getCardName())).anyMatch(c->c.getCardId().equals(this.cardId));
    }

    public static boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }

    public static String getCardName(String cardId) {
        return Arrays.stream(PlayCard.values())
                .filter(c -> c.getCardId().equals(cardId))
                .map(PlayCard::getCardName)
                .findFirst()
                .orElseThrow();
    }

    public static HandCard findById(String cardId) {
        return CARD_FACTORY_MAP.get(cardId);
    }

}
