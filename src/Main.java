import game.snake.Direction;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.LinkedList;
import java.util.Random;

// teszt
class Main{
    public static void main(String[] args) {
        int rows = 15;
        int columns = 25;
        int score = 0;
        int color = 0;
        int randomSeed = 1234567890;
        int remainingTime = 10000;
        Agent agent = new Agent(
                new SnakeGameState(new int[rows][columns], new LinkedList<Cell>(), new Direction(-1, 0), score),
                color, new Random(randomSeed));
        while (remainingTime-- > 0) {
            System.out.println(agent.getAction(remainingTime));
        }
    }
}