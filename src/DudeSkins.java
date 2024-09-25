import java.util.Arrays;

public enum DudeSkins {

    Default("classic", new ColorReplaceEffect(
        Arrays.asList(), Arrays.asList() // no color replace cause its just Him. its Dude.
    )),
    LemonLime("lemon-lime", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(51, 51, 51), // hat
            Arrays.asList(70, 164, 133), // hair
            Arrays.asList(232, 226, 117), // sweater main
            Arrays.asList(227, 164, 121), // stripe
            Arrays.asList(22, 52, 43), // pants
            Arrays.asList(51, 51, 51) // shoes
        )
    )),
    eduD("edud", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(95, 84, 146) // pants
        ),
        Arrays.asList(
            Arrays.asList(141, 151, 194), // sweater main
            Arrays.asList(101, 54, 98) // pants
        )
    )),
    Nightmode("nightmode", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(0, 0, 0), // hat
            Arrays.asList(0, 0, 0), // hair
            Arrays.asList(0, 0, 0), // sweater main
            Arrays.asList(0, 0, 0), // stripe
            Arrays.asList(0, 0, 0), // pants
            Arrays.asList(0, 0, 0) // shoes
        )
    )),
    FreeDude("free dude", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(240, 138, 74), // hat
            Arrays.asList(102, 70, 66), // hair
            Arrays.asList(163, 199, 221), // sweater main
            Arrays.asList(35, 76, 122), // stripe
            Arrays.asList(154, 135, 120), // pants
            Arrays.asList(71, 55, 50) // shoes
        )
    )),
    ChildishPrankster("childish prankster", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(238, 214, 196), // skin
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(255, 80, 61), // hat
            Arrays.asList(255, 236, 3), // hair
            Arrays.asList(255, 236, 3), // skin
            Arrays.asList(255, 80, 61), // sweater main
            Arrays.asList(255, 80, 61), // stripe
            Arrays.asList(51, 89, 201), // pants
            Arrays.asList(76, 76, 76) // shoes
        )
    )),
    Boing("boing", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(238, 214, 196), // skin
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(67, 54, 52), // hat
            Arrays.asList(181, 134, 88), // hair
            Arrays.asList(255, 208, 166), // skin
            Arrays.asList(231, 77, 82), // sweater main
            Arrays.asList(67, 54, 52), // stripe
            Arrays.asList(67, 54, 52), // pants
            Arrays.asList(149, 137, 141) // shoes
        )
    )),
    HiBrandy("weirdo dude", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(238, 214, 196), // skin
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(131, 109, 97), // hat
            Arrays.asList(79, 75, 63), // hair
            Arrays.asList(247, 220, 229), // skin
            Arrays.asList(192, 103, 121), // sweater main
            Arrays.asList(128, 55, 61), // stripe
            Arrays.asList(113, 167, 187), // pants
            Arrays.asList(74, 44, 42) // shoes
        )
    )),
    Bubblegum("week-old bubblegum", new ColorReplaceEffect(
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(238, 214, 196), // skin
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68) // shoes
        ),
        Arrays.asList(
            Arrays.asList(0, 0, 0), // hat
            Arrays.asList(255, 121, 188), // hair
            Arrays.asList(255, 227, 176), // skin
            Arrays.asList(0, 0, 0), // sweater main
            Arrays.asList(252, 127, 255), // stripe
            Arrays.asList(252, 127, 255), // pants
            Arrays.asList(0, 0, 0) // shoes
        )
    )),
    Kira("obligatory self-insert", new ColorReplaceEffect( // i had to
        // this replaces almost literally every color possible
        // if that isnt favoritism idk what is
        Arrays.asList(
            Arrays.asList(141, 151, 194), // hat
            Arrays.asList(71, 62, 56), // hair
            Arrays.asList(238, 214, 196), // skin
            Arrays.asList(215, 121, 156), // sweater main
            Arrays.asList(101, 54, 98), // stripe
            Arrays.asList(95, 84, 146), // pants
            Arrays.asList(53, 51, 68), // shoes
            Arrays.asList(54, 51, 62), // mic top
            Arrays.asList(104, 97, 118), // mic bottom
            Arrays.asList(113, 142, 164) // miss color
        ),
        Arrays.asList(
            Arrays.asList(249, 220, 240), // hat
            Arrays.asList(239, 156, 200), // hair
            Arrays.asList(247, 221, 175), // skin
            Arrays.asList(249, 220, 240), // sweater main
            Arrays.asList(243, 188, 227), // stripe
            Arrays.asList(243, 181, 222), // pants
            Arrays.asList(142, 114, 133), // shoes
            Arrays.asList(76, 48, 73), // mic top
            Arrays.asList(106, 79, 117), // mic bottom
            Arrays.asList(165, 140, 175) // miss color
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
