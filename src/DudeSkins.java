import static java.util.Arrays.asList;

public enum DudeSkins {

    Default("classic", new ColorReplaceEffect(
        asList(), asList() // no color replace cause its just Him. its Dude.
    )),
    LemonLime("lemon-lime", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(51, 51, 51), // hat
            asList(70, 164, 133), // hair
            asList(232, 226, 117), // sweater main
            asList(227, 164, 121), // stripe
            asList(22, 52, 43), // pants
            asList(51, 51, 51) // shoes
        )
    )),
    eduD("edud", new ColorReplaceEffect(
        asList(
            asList(215, 121, 156), // sweater main
            asList(95, 84, 146) // pants
        ),
        asList(
            asList(141, 151, 194), // sweater main
            asList(101, 54, 98) // pants
        )
    )),
    Nightmode("nightmode", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(0, 0, 0), // hat
            asList(0, 0, 0), // hair
            asList(0, 0, 0), // sweater main
            asList(0, 0, 0), // stripe
            asList(0, 0, 0), // pants
            asList(0, 0, 0) // shoes
        )
    )),
    FreeDude("free dude", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(240, 138, 74), // hat
            asList(102, 70, 66), // hair
            asList(163, 199, 221), // sweater main
            asList(35, 76, 122), // stripe
            asList(154, 135, 120), // pants
            asList(71, 55, 50) // shoes
        )
    )),
    ChildishPrankster("childish prankster", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(238, 214, 196), // skin
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(255, 80, 61), // hat
            asList(255, 236, 3), // hair
            asList(255, 236, 3), // skin
            asList(255, 80, 61), // sweater main
            asList(255, 80, 61), // stripe
            asList(51, 89, 201), // pants
            asList(76, 76, 76) // shoes
        )
    )),
    Boing("boing", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(238, 214, 196), // skin
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(67, 54, 52), // hat
            asList(181, 134, 88), // hair
            asList(255, 208, 166), // skin
            asList(231, 77, 82), // sweater main
            asList(67, 54, 52), // stripe
            asList(67, 54, 52), // pants
            asList(149, 137, 141) // shoes
        )
    )),
    HiBrandy("weirdo dude", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(238, 214, 196), // skin
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(131, 109, 97), // hat
            asList(79, 75, 63), // hair
            asList(247, 220, 229), // skin
            asList(192, 103, 121), // sweater main
            asList(128, 55, 61), // stripe
            asList(113, 167, 187), // pants
            asList(74, 44, 42) // shoes
        )
    )),
    Bubblegum("week-old bubblegum", new ColorReplaceEffect(
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(238, 214, 196), // skin
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68) // shoes
        ),
        asList(
            asList(0, 0, 0), // hat
            asList(255, 121, 188), // hair
            asList(255, 227, 176), // skin
            asList(0, 0, 0), // sweater main
            asList(252, 127, 255), // stripe
            asList(252, 127, 255), // pants
            asList(0, 0, 0) // shoes
        )
    )),
    Kira("obligatory self-insert", new ColorReplaceEffect( // i had to
        // this replaces almost literally every color possible
        // if that isnt favoritism idk what is
        asList(
            asList(141, 151, 194), // hat
            asList(71, 62, 56), // hair
            asList(238, 214, 196), // skin
            asList(215, 121, 156), // sweater main
            asList(101, 54, 98), // stripe
            asList(95, 84, 146), // pants
            asList(53, 51, 68), // shoes
            asList(54, 51, 62), // mic top
            asList(104, 97, 118), // mic bottom
            asList(113, 142, 164) // miss color
        ),
        asList(
            asList(249, 220, 240), // hat
            asList(239, 156, 200), // hair
            asList(247, 221, 175), // skin
            asList(249, 220, 240), // sweater main
            asList(243, 188, 227), // stripe
            asList(243, 181, 222), // pants
            asList(142, 114, 133), // shoes
            asList(76, 48, 73), // mic top
            asList(106, 79, 117), // mic bottom
            asList(165, 140, 175) // miss color
        )
    )),
    // this is a dummy just meant to denote that the player is using a custom dude skin.
    // it does not hold any color data itself, that is handled in Stage (and dude select menu).
    // 
    Custom("CUSTOM", null); 

    public ColorReplaceEffect skin;
    public String name;

    DudeSkins(String name, ColorReplaceEffect cre){
        this.skin = cre;
        this.name = name;
    }
}
