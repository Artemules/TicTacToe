package tictactoe.player.impl;

import tictactoe.Main;
import tictactoe.player.Player;

import java.util.List;
import java.util.Random;

public class EasyBotPlayer implements Player {

    private final Random random;
    private final String symbol;

    public EasyBotPlayer(String symbol) {
        this.symbol = symbol;
        random = new Random();
    }

    @Override
    public Main.Either<String, Main.Field> nextMove(Main.Field field) {
        List<Main.Coordinates> possibleMoves = field.getPossibleMoves();
        Main.Coordinates nextCoordinates = possibleMoves.get(random.nextInt(possibleMoves.size()));
        Main.Either<String, Main.Field> nextMove = field.nextMove(nextCoordinates.getCoordinates(), symbol);
        if (nextMove.isRight()) {
            return nextMove;
        }
        return Main.Either.left("Something went wrong with bot player");
    }

    @Override
    public String moveMessage() {
        return "Making move level \"easy\"";
    }
}
