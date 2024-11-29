///Fabbernat_DROP_TABLE_HALLGATOK,Fabian.Bernat@stud.u-szeged.hu

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.LinkedList;
import java.util.Random;

/**
 * Nem szukseges a kodhoz, csak hogy egyszerubb legyen megtalalni es hasznalni a 4 iranyt
 */
enum Directions {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    /**
     * @param row    nem szukseges
     * @param column nem szukseges
     */
    Directions(int row, int column) {

    }
}

/**
 * Az Agent osztaly a kigyot vezerlo agens, amely a jatek allapota alapjan
 * donti el a kovetkezo lepes iranyat. Az agens celja az etel felvetele es a falak,
 * valamint a sajat teste elkerulese.
 */
public class Agent extends SnakePlayer {

    /**
     * Konstruktor, amely inicializalja az Agent objektumot a megadott jatekkal.
     *
     * @param gameState Az aktualis jatekter allapota.
     * @param color     A kigyo szine.
     * @param random    random szam generator
     */
    public Agent(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    /**
     * Meghatarozza az aktualis jatekallapot alapjan a kovetkezo lepes iranyat.
     *
     * @param remainingTime A hatralevo ido a dontes meghozatalara.
     * @return Az uj irany, amely fele a kigyo mozdul.
     */
    @Override
    public Direction getAction(long remainingTime) {
        Cell food = getFoodCell();
        if (food == null) return gameState.direction;

        Cell head = gameState.snake.peekFirst();
        LinkedList<Direction> validDirections = getValidDirections(head);

        Direction bestDirection = null;
        int bestScore = Integer.MIN_VALUE;

        for (Direction direction : validDirections) {

            // Szimulalja a kovetkezo lepest
            LinkedList<Cell> simulatedSnake = simulateMove(direction, gameState.snake);

            // Kiszámítja a kockázati pontszámot az elérhető cellák alapján.
            int accessibilityScore = calculateAccessibility(simulatedSnake);
            int holeSize = calculateHoleSize(simulatedSnake);



            // Hozzáadja súlyt az étel közelségéhez.
            Cell newHead = new Cell(head.i + direction.i, head.j + direction.j);
            int distanceToFood = food != null ? newHead.distance(food) : Integer.MAX_VALUE;

            // Penalize moves into small holes that trap the snake
            boolean foodReachable = isFoodReachable(simulatedSnake, food); // Add this line

            int moveScore = foodReachable ? (accessibilityScore - distanceToFood) : Integer.MIN_VALUE;
            if (holeSize < gameState.snake.size()) { // Compare with snake size
                moveScore -= 1000; // Penalize moves leading to small holes
            }
            // Kivalasztja az iranyta a legjobb kombinalt pontszammal
            if (moveScore > bestScore) {
                bestScore = moveScore;
                bestDirection = direction;
            }
        }

        // Ha egy valid irany sem segit a szituacion, akkor visszaesik a jelenlegi iranyba
        return bestDirection != null ? bestDirection : gameState.direction;
    }

    private boolean isFoodReachable(LinkedList<Cell> snake, Cell food) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // If food is reachable, return true
            if (current.equals(food)) {
                return true;
            }

            for (Cell neighbor : current.neighbors()) {
                if (gameState.isOnBoard(neighbor) &&
                        !visited[neighbor.i][neighbor.j] &&
                        (gameState.getValueAt(neighbor) != SnakeGame.SNAKE || neighbor.equals(snake.peekLast())) &&
                        !snake.contains(neighbor)) {
                    visited[neighbor.i][neighbor.j] = true;
                    queue.add(neighbor);
                }
            }
        }

        return false; // Food is not reachable
    }

    private int calculateHoleSize(LinkedList<Cell> snake) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        int regionSize = 0;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            regionSize++;

            for (Cell neighbor : current.neighbors()) {
                if (gameState.isOnBoard(neighbor) &&
                        !visited[neighbor.i][neighbor.j] &&
                        (gameState.getValueAt(neighbor) != SnakeGame.SNAKE || neighbor.equals(snake.peekLast())) &&
                        !snake.contains(neighbor)) {
                    visited[neighbor.i][neighbor.j] = true;
                    queue.add(neighbor);
                }
            }
        }

        return regionSize; // Size of the reachable region from the snake's head
    }

    /**
     * A kigyo mozgasanak szimulalasa egy adott iranyba.
     *
     * @param direction A mozgasi irany, amely fele a kigyo mozogni fog.
     * @param snake     A kigyo jelenlegi allapota, a kigyo testet alkoto cellak listaja.
     * @return A kigyo szimulalt allapota az adott mozgas utan.
     */
    private LinkedList<Cell> simulateMove(Direction direction, LinkedList<Cell> snake) {
        LinkedList<Cell> simulatedSnake = new LinkedList<>(snake);
        Cell newHead = new Cell(snake.peekFirst().i + direction.i, snake.peekFirst().j + direction.j);
        simulatedSnake.addFirst(newHead);

        if (!newHead.equals(getFoodCell())) {
            // A farok tunjon el, ha nem tudunk almat enni.
            simulatedSnake.removeLast();
        }
        return simulatedSnake;
    }

    /**
     * Az elerheto cellak szamanak kiszamitasa egy adott kigyoallapotbol.
     *
     * @param snake A kigyo jelenlegi allapota, a kigyo testet alkoto cellak listaja.
     * @return Az elerheto cellak szama a kigyo feje alapjan.
     */
    private int calculateAccessibility(LinkedList<Cell> snake) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        int accessibleCells = 0;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            accessibleCells++;

            for (Cell neighbor : current.neighbors()) {
                if (gameState.isOnBoard(neighbor) &&
                        !visited[neighbor.i][neighbor.j] &&
                        (gameState.getValueAt(neighbor) != SnakeGame.SNAKE || neighbor.equals(snake.peekLast())) &&
                        !snake.contains(neighbor)) {
                    visited[neighbor.i][neighbor.j] = true;
                    queue.add(neighbor);
                }
            }
        }

        // Visszater az elerheto cellak szamaval a jelenlegi allapotbol.
        return accessibleCells;
    }

    /**
     * Az ervenyes iranyok meghatarozasa a kigyo feje alapjan.
     *
     * @param head A kigyo feje, amelytol az ervenyes iranyokat meghatarozzuk.
     * @return Az ervenyes iranyok listaja.
     */
    private LinkedList<Direction> getValidDirections(Cell head) {
        LinkedList<Direction> directions = new LinkedList<>();
        for (Cell neighbor : head.neighbors()) {
            if (gameState.isOnBoard(neighbor) && gameState.getValueAt(neighbor) != SnakeGame.SNAKE) {
                directions.add(head.directionTo(neighbor));
            }
        }
        return directions;
    }

    /**
     * Annak meghatarozasa, hogy egy adott iranyba mozgas a kigyo halalat okozna-e.
     *
     * @param d     A kivant irany, amerre a kigyo mozogni szeretne.
     * @param snake A kigyo jelenlegi allapota, a kigyo testet alkoto cellak listaja.
     * @return True, ha a mozgas a kigyo halalat okozna, kulonben False.
     */
    boolean wouldDie(Direction d, LinkedList<Cell> snake) {
        Cell head = snake.peekFirst();
        assert (head != null);

        Cell newHead = new Cell(head.i + d.i, head.j + d.j);

        // Ha a mozgas palyan kivulre vezetne vagy utkozest okozna, visszater True-val.
        if (!gameState.isOnBoard(newHead) || snake.contains(newHead)) {
            return true;
        }

        LinkedList<Cell> simulatedSnake = new LinkedList<>(snake);
        simulatedSnake.addFirst(newHead);

        if (!newHead.equals(getFoodCell())) {
            simulatedSnake.removeLast();
        }

        return !hasEscapeRoute(simulatedSnake);
    }

    /**
     * Ellenorzi, hogy van-e menekulesi lehetoseg a kigyo jelenlegi allapotabol.
     *
     * @param snake A kigyo jelenlegi allapota, a kigyo testet alkoto cellak listaja.
     * @return True, ha a kigyo eletben maradhat, kulonben False.
     */
    private boolean hasEscapeRoute(LinkedList<Cell> snake) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        int accessibleCells = 0;
        int requiredCells = snake.size();

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            accessibleCells++;

            // Korai kilepes, ha a kigyonak eleg cellaja van az eletben maradashoz.
            if (accessibleCells >= requiredCells) {
                return true;
            }

            for (Cell neighbor : current.neighbors()) {
                if (gameState.isOnBoard(neighbor) &&
                        !visited[neighbor.i][neighbor.j] &&
                        (gameState.getValueAt(neighbor) != SnakeGame.SNAKE || neighbor.equals(snake.peekLast())) &&
                        !snake.contains(neighbor)) {
                    visited[neighbor.i][neighbor.j] = true;
                    queue.add(neighbor);
                }
            }
        }

        return false;
    }

    /**
     * Megkeresi azt a cellat, amelyen a food talalhato.
     *
     * @return A cella, amelyen az etel van, vagy null, ha nincs etel a palyan.
     */
    private Cell getFoodCell() {
        Cell food = null;
        outer:
        for (int row = 0; row < gameState.board.length; row++) {
            for (int column = 0; column < gameState.board[row].length; column++) {
                if (gameState.board[row][column] == SnakeGame.FOOD) {
                    food = new Cell(row, column);

                    // A dupla for ciklusnak nem kell vegigiteralnia a tombon, ha mar megtalalta az etelt. Ez idopocsekolas lenne.
                    break outer;
                }
            }
        }
        return food;
    }

}