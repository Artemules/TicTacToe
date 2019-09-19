package tictactoe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tictactoe.player.Player;
import tictactoe.player.impl.MediumBotPlayer;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class MediumLevelBotTest {
    private final String input;
    private final String expected;
    private final String description;

    public MediumLevelBotTest(String input, String expected, String description) {
        this.input = input;
        this.expected = expected;
        this.description = description;
    }

    @Parameterized.Parameters
    public static Collection fields() {
        return Arrays.asList(new Object[][]{
                {"X X" +
                 "  O" +
                 "O  ",
                 "XXX" +
                 "  O" +
                 "O  ", "Bot can win in one move" },
                {"  X" +
                 "O  " +
                 "OX ",
                 "X X" +
                 "O  " +
                 "OX ", "Opponent can win in one move" }
        });

    }

    @Test
    public void mediumBotShouldMoveWithLogic() {
        Main.Field inputField = Main.Field.fromCells(input);
        Main.Field expectedField = Main.Field.fromCells(expected);

        Player botPlayer = new MediumBotPlayer("X");
        Main.Either<String, Main.Field> nextField = botPlayer.nextMove(inputField);

        Assert.assertTrue(nextField.isRight());
        System.out.println(expectedField.getPrintableField());
        System.out.println(nextField.getRight().getPrintableField());
        Assert.assertEquals(description, expectedField, nextField.getRight());
    }

    @Test
    public void mediumBotShouldMoveWithRandom() {
        Main.Field inputField = Main.Field.fromCells("  X  OOX ");

        Player botPlayer = new MediumBotPlayer("X");
        Main.Either<String, Main.Field> nextField = botPlayer.nextMove(inputField);

        Assert.assertTrue(nextField.isRight());
        Assert.assertNotEquals("No one can win in one move, random!", inputField, nextField.getRight());
    }
}

