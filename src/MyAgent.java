import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.board.compact.BoardCompact;
import game.board.oop.*;

import search.HeuristicProblem;
import search.Solution;

class BoxPushAction {
    Pos playerPos;
    EDirection direction;
    int cost;

    public BoxPushAction(Pos playerPos, EDirection direction) {
        this.playerPos = playerPos;
        this.cost = playerPos.value;
        this.direction = direction;
    }
}

class Pos {
    Integer x;
    Integer y;
    int value = 0;

    Pos(Integer a, Integer b) {
        x = a;
        y = b;
    }

    Pos(Integer a, Integer b, int value){
        this(a, b);
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos))
            return false;

        Pos q = (Pos) o;
        return x.equals(q.x) && y.equals(q.y);
    }

    @Override
    public int hashCode() {
        return x.hashCode() ^ y.hashCode();
    }

    @Override
    public String toString() {
        return "Pos{" +  "x=" + x +  ", y=" + y + ", value=" + value + '}';
    }

    public int ManhattanDistance(Pos b){
        return Math.abs(x - b.x) + Math.abs(y - b.y);
    }

    public double EuclideanDistance(Pos b){
        return Math.sqrt(Math.pow(b.x - x, 2) + Math.pow(b.y - y, 2));
    }
}

class Utils {

    static boolean IsWall(BoardCompact b, int x, int y) {
        return (b.tiles[x][y] & ESpace.WALL.getFlag()) != 0;
    }

    static boolean IsGoal(BoardCompact b, int x, int y) {
        return (b.tiles[x][y] & EPlace.SOME_BOX_PLACE_FLAG) != 0;
    }

    // Check if a compressed board state has a box on location x and y
    static boolean HasBox(BoardState s, int x, int y) {
        for (int[] i : s.boxes) {
            if (i[0] == x && i[1] == y) {
                return true;
            }
        }
        return false;
    }

    // Find all boxes that can be pushed from a given area on a map
    static ArrayList<BoxPushAction> FindReachableBoxes(BoardCompact template, BoardState s, int x, int y, boolean[][] deadSquares) {

//        System.out.println("\nSearching on: ");
//        s.DebugPrint(template);

        ArrayList<BoxPushAction> boxes = new ArrayList<>();
        HashSet<Pos> explored = new HashSet<>();
        Queue<Pos> frontier = new LinkedList<>();

        frontier.add(new Pos(x, y));

        while (!frontier.isEmpty()) {
            Pos tile = frontier.poll();

            if (explored.contains(tile))
                continue;

            for (EDirection dir : EDirection.arrows()){
                int dx = dir.dX;
                int dy = dir.dY;

                int check_x = tile.x + dx;
                int check_y = tile.y + dy;

                int far_x = tile.x + dx + dx;
                int far_y = tile.y + dy + dy;

                if (Utils.IsWall(template, check_x, check_y))
                    continue;

                // If we hit a box
                if (HasBox(s, check_x, check_y)) {

                    // If there is a space behind said box
                    if (    !IsWall(template, far_x, far_y) &&
                            !HasBox(s, far_x, far_y) &&
                            !deadSquares[far_x][far_y]) {

                        // Add to pushable boxes
                        boxes.add(new BoxPushAction(new Pos(tile.x, tile.y, tile.value + 1), dir));
                    }

                    continue;
                }


                frontier.add(new Pos(check_x, check_y, tile.value + 1));
            }

            explored.add(tile);
        }

//        System.out.println("Found reachable boxes:");
//        for (BoxPushAction box : boxes) {
//            System.out.print("x: " + box.playerPos.x + ", y: " + box.playerPos.y + ", with cost: " + box.playerPos.value);
//            System.out.println(" and push dir: " + box.direction);
//        }

//        if (!Validator.AreValidReachableBoxes(template, s, x, y, deadSquares, boxes)){
//            throw new RuntimeException("Reachable Boxes validation failed!");
//        }

        return boxes;
    }

    public static int[][] cloneArray(int[][] src) {
        int length = src.length;
        int[][] target = new int[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }
}

// A basic path search, that avoids walls and boxes and finds a way towards a goal
class RebuildPathProblem implements HeuristicProblem<Pos, EDirection>{

    BoardCompact template;
    BoardState state;
    Pos start;
    Pos goal;

    public RebuildPathProblem(BoardCompact template, BoardState state, Pos start, Pos goal) {
        this.template = template;
        this.state = state;
        this.start = start;
        this.goal = goal;
    }

    @Override
    public double estimate(Pos pos) {
        return pos.ManhattanDistance(goal);
    }

    @Override
    public Pos initialState() {
        return start;
    }

    @Override
    public List<EDirection> actions(Pos pos) {
        ArrayList<EDirection> sides = new ArrayList<>();

        for(EDirection dir : EDirection.arrows()){
            int x = dir.dX + pos.x;
            int y = dir.dY + pos.y;

            if (Utils.IsWall(template, x, y))
                continue;

            if (Utils.HasBox(state, x, y))
                continue;

            sides.add(dir);
        }

        return sides;
    }

    @Override
    public Pos result(Pos pos, EDirection eDirection) {
        return new Pos(pos.x + eDirection.dX, pos.y + eDirection.dY);
    }

    @Override
    public boolean isGoal(Pos pos) {
        return pos.ManhattanDistance(goal) == 0;
    }

    @Override
    public double cost(Pos pos, EDirection eDirection) {
        return 1;
    }
}

class DeadSquareDetector {
    public static boolean[][] detect(BoardCompact board) {
        int width = board.width();
        int height = board.height();

        boolean[][] deadTiles = new boolean[width][height];

        for (boolean[] array : deadTiles)
            Arrays.fill(array, true);

        HashSet<Pos> explored = new HashSet<>();
        Queue<Pos> frontier = new LinkedList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Utils.IsGoal(board, x, y))
                    frontier.add(new Pos(x, y));
            }
        }

        while (!frontier.isEmpty()) {
            Pos tile = frontier.poll();

            if (explored.contains(tile))
                continue;

            deadTiles[tile.x][tile.y] = false;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {

                    if (dx != 0 && dy != 0)
                        continue;

                    if (Utils.IsWall(board, tile.x + dx, tile.y + dy) || Utils.IsWall(board, tile.x + dx * 2, tile.y + dy * 2)) {
                        continue;
                    }

                    frontier.add(new Pos(tile.x + dx, tile.y + dy));
                }
            }

            explored.add(tile);
        }

        return deadTiles;
    }
}

class BoardState {
    int playerX;
    int playerY;
    int boxCount;
    int[][] boxes;

    BoardState(BoardCompact initial){
        playerX = initial.playerX;
        playerY = initial.playerY;
        boxCount = initial.boxCount;
        boxes = new int[boxCount][];

        int i = 0;
        for (int x = 0; x < initial.width(); x++){
            for (int y = 0; y < initial.height(); y++){

                if ((initial.tiles[x][y] & EEntity.SOME_BOX_FLAG) == 0) // no box
                    continue;

                boxes[i++] = new int[]{x, y};
            }
        }
    }

    BoardState(BoardState prev, int px, int py, EDirection pushDir){
        boxCount = prev.boxCount;
        boxes = Utils.cloneArray(prev.boxes);

        playerX = px + pushDir.dX;
        playerY = py + pushDir.dY;

        int bx = playerX;
        int by = playerY;

        int[] expected = new int[]{bx, by};

        for (int i = 0; i < boxCount; i++){
            if (Arrays.equals(boxes[i], expected)){
                boxes[i][0] += pushDir.dX;
                boxes[i][1] += pushDir.dY;
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardState that = (BoardState) o;
        return playerX == that.playerX && playerY == that.playerY && Arrays.deepEquals(that.boxes, boxes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(playerX, playerY);
        result = 31 * result + Arrays.hashCode(boxes);
        return result;
    }

    BoardCompact Apply(BoardCompact template){
        BoardCompact board = template.clone();

        int playerFlag = 1537;
        int boxFlag = 0;

        for (int x = 0; x < board.width(); x++){
            for (int y = 0; y < board.height(); y++){

                if ((board.tiles[x][y] & EEntity.SOME_BOX_FLAG) != 0){
                    boxFlag = board.tiles[x][y];
                }

                board.tiles[x][y] &= EEntity.NULLIFY_ENTITY_FLAG;
                board.tiles[x][y] |= EEntity.NONE.getFlag();
            }
        }

        board.tiles[playerX][playerY] = playerFlag;

        for(int[] b : boxes){
            board.tiles[b[0]][b[1]] = boxFlag;
        }

        return board;
    }

    void DebugPrint(BoardCompact template){
        BoardCompact b = Apply(template);
        System.out.println();
        b.debugPrint();
    }
}

class SokobanProblem implements HeuristicProblem<BoardState, BoxPushAction> {

    BoardCompact template;

    List<Pos> goals = new ArrayList<>();
    boolean[][] deadSquares;

    SokobanProblem(BoardCompact initial) {
        this.template = initial;

        this.deadSquares = DeadSquareDetector.detect(initial);

        for(int x = 0; x < template.width(); x++){
            for(int y = 0; y < template.height(); y++){
                if (Utils.IsGoal(template, x, y))
                    goals.add(new Pos(x, y));
            }
        }
    }

    @Override
    public double estimate(BoardState b) {
        double total = 0;

        for(int[] i : b.boxes){
            int x = i[0];
            int y = i[1];

            double shortest = Double.POSITIVE_INFINITY;
            for(Pos g : goals){
                if (g.ManhattanDistance(new Pos(x, y)) < shortest){
                    shortest = g.ManhattanDistance(new Pos(x, y));
                }
            }
        }

        return (int) total;
    }

    @Override
    public BoardState initialState() {
        return new BoardState(template);
    }

    long bfstime = 0;

    @Override
    public List<BoxPushAction> actions(BoardState b) {
        long startTime = System.nanoTime();
        List<BoxPushAction> l = Utils.FindReachableBoxes(template, b, b.playerX, b.playerY, deadSquares);
        long endTime = System.nanoTime();

        bfstime += (endTime - startTime);
        return l;
    }

    @Override
    public BoardState result(BoardState b, BoxPushAction action) {
        return new BoardState(b, action.playerPos.x, action.playerPos.y, action.direction);
    }

    @Override
    public boolean isGoal(BoardState b) {
        boolean[] matched = new boolean[goals.size()];

        for(int[] i : b.boxes){
            for(int goal_i = 0; goal_i < goals.size(); goal_i++){
                if (goals.get(goal_i).x == i[0] && goals.get(goal_i).y == i[1]){
                    matched[goal_i] = true;
                }
            }
        }

        for (boolean g : matched){
            if ( ! g ){
                return false;
            }
        }

        return true;
    }

    @Override
    public double cost(BoardState b, BoxPushAction push) {
        return push.playerPos.value;
    }

    static List<EDirection> Walk(Solution<BoardState, BoxPushAction> solution, SokobanProblem parent){

        List<EDirection> steps = new LinkedList<>();
        BoardState checkpoint = new BoardState(parent.template);

        for (BoxPushAction action : solution.actions){
            RebuildPathProblem problem = new RebuildPathProblem(parent.template, checkpoint, new Pos(checkpoint.playerX, checkpoint.playerY), action.playerPos );
            Solution<Pos, EDirection> s = AStar.search(problem);
            steps.addAll(s.actions);
            steps.add(action.direction);
            checkpoint = new BoardState(checkpoint, action.playerPos.x, action.playerPos.y, action.direction);
        }

        return steps;
    }
}

/**
 * The simplest Tree-DFS agent.
 *
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {

    @Override
    protected List<EDirection> think(BoardCompact board) {



//        BoardState st = new BoardState(board);
//        DeadSquareDetector.FindReachableBoxes(board, st, board.playerX, board.playerY, DeadSquareDetector.detect(board));
//
//        if ( 1 == 1) return null;

        SokobanProblem problem = new SokobanProblem(board);
        Solution<BoardState, BoxPushAction> solution = AStar.search(problem);
        System.out.println("BFS took " + (problem.bfstime / 1000000) + "ms");
        return SokobanProblem.Walk(solution, problem);
    }
}
