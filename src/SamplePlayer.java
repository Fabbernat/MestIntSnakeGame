import java.util.Random;

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.SnakeGameState;

public class SamplePlayer extends SnakePlayer {

    public SamplePlayer(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    @Override
    public Direction getAction(long remainingTime) {
        Direction action = SnakeGame.DIRECTIONS[random.nextInt(SnakeGame.DIRECTIONS.length)];
        return action;
    }
}