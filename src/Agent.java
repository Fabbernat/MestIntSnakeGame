///Fabbernat,Fabian.Bernat@stud.u-szeged.hu
import java.util.Random;
import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.SnakeGameState;

/**
 * Az Agent osztály a kígyót vezérlő ágens, amely a játék állapota alapján 
 * dönti el a következő lépés irányát. Az ágens célja az étel felvétele és a falaktól,
 * valamint a kígyó saját testétől való elkerülés.
 */
public class Agent extends SnakePlayer {
    /**
     * Konstruktor, amely inicializálja az Agent objektumot a megadott játékkal.
     * @param gameState Az aktuális játéktér állapota.
     * @param color A kígyó színe.
     * @param random Véletlenszám-generátor az irányításhoz.
     */
    public Agent(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    /**
     * Meghatározza az aktuális játékállapot alapján a következő lépés irányát.
     * @param remainingTime A hátralévő idő a döntés meghozatalára.
     * @return Az új irány, amely felé a kígyó mozdul.
     */
    @Override
    public Direction getAction(long remainingTime) {
        // Check if gameState or snake is null
        if (gameState == null || gameState.snake == null || gameState.snake.peekFirst() == null) {
            // visszater alapertelmezett modon fel irannyal, ha headPostiton null
            return new Direction(-1, 0); // Fel irány
        }
        // Snake fej pozíciója
        var headPosition = gameState.snake.peekFirst();
        if (headPosition == null) {
            // visszater alapertelmezett modon fel irannyal, ha headPostiton null
            return new Direction(-1, 0);
        }
        int headX = headPosition.i;
        int headY = headPosition.j;

        // Étel legközelebbi pozíciója
        int targetX = -1;
        int targetY = -1;
        for (int y = 0; y < gameState.getSize(); y++) {
            for (int x = 0; x < gameState.board.length; x++) {
                if (gameState.board[y][x] == SnakeGame.FOOD) {
                    targetX = x;
                    targetY = y;
                    break;
                }
            }
        }

        // Ha megtaláljuk az ételt, a megfelelő irányban mozgunk felé
        if (targetX != -1 && targetY != -1) {
            if (targetX < headX && isSafeMove(new Direction(0, -1))) return new Direction(0, -1); // Bal
            else if (targetX > headX && isSafeMove(new Direction(0, 1))) return new Direction(0, 1); // Jobb
            else if (targetY < headY && isSafeMove(new Direction(-1, 0))) return new Direction(-1, 0); // Fel
            else if (targetY > headY && isSafeMove(new Direction(1, 0))) return new Direction(1, 0); // Le
        }

        // Alapértelmezett random irány, ha nincs étel vagy nincs biztonságos út
        Direction[] directions = { new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0) };
        return directions[random.nextInt(directions.length)];
    }

    /**
     * Ellenőrzi, hogy az adott irány biztonságos-e.
     * @param direction A vizsgálandó irány.
     * @return Igaz, ha az irány biztonságos, hamis különben.
     */
    private boolean isSafeMove(Direction direction) {
        assert gameState.snake.peekFirst() != null;
        int nextX = gameState.snake.peekFirst().i + direction.i;
        int nextY = gameState.snake.peekFirst().j + direction.j;

        // Ellenőrzi, hogy a lépés határokon belül marad-e és elkerüli-e a kígyó testét
        return (nextX >= 0 && nextX < gameState.board.length &&
                nextY >= 0 && nextY < gameState.board.length &&
                gameState.board[nextY][nextX] != SnakeGame.SNAKE);
    }
}