import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.board.compact.BoardCompact;
import game.board.oop.*;

import search.HeuristicProblem;
import search.Solution;

class Position {
    Integer x;
    Integer y;
    int value = 0;

    Position(Integer a, Integer b) {
        x = a;
        y = b;
    }

    Position(Integer a, Integer b, int value){
        this(a, b);
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position))
            return false;

        Position q = (Position) o;
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

    public int ManhattanDistance(Position b){
        return Math.abs(x - b.x) + Math.abs(y - b.y);
    }
}

class DeadSquareDetector {
    public static boolean[][] detect(BoardCompact board) {
        int width = board.width();
        int height = board.height();

        boolean[][] deadTiles = new boolean[width][height];

        for (boolean[] array : deadTiles)
            Arrays.fill(array, true);

        HashSet<Position> explored = new HashSet<>();
        Queue<Position> frontier = new LinkedList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Utils.IsGoal(board, x, y))
                    frontier.add(new Position(x, y));
            }
        }

        while (!frontier.isEmpty()) {
            Position tile = frontier.poll();

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

                    frontier.add(new Position(tile.x + dx, tile.y + dy));
                }
            }

            explored.add(tile);
        }

        return deadTiles;
    }
}

public class MyAgent extends ArtificialAgent {

    @Override
    protected List<EDirection> think(BoardCompact board) {
//        BoardState st = new BoardState(board);
//        DeadSquareDetector.FindReachableBoxes(board, st, board.playerX, board.playerY, DeadSquareDetector.detect(board));
//        if ( 1 == 1) return null;
        SokobanProblem problem = new SokobanProblem(board);
        Solution<BoardState, BoxPushAction> solution = AStar.search(problem);
        System.out.println("BFS took " + (problem.bfstime / 1000000) + "ms");
        return SokobanProblem.Walk(solution, problem);
    }
}

class SokobanProblem implements HeuristicProblem<BoardState, BoxPushAction> {

    BoardCompact board;
    Position[] goals;
    boolean[][] deadSquares;

    SokobanProblem(BoardCompact initial) {
        this.board = initial;
        this.deadSquares = DeadSquareDetector.detect(initial);
        this.goals = new Position[board.boxCount];
        FindGoals();
    }

    void FindGoals(){
        int i = 0;
        for(int x = 0; x < board.width(); x++){
            for(int y = 0; y < board.height(); y++){

                if (! Utils.IsGoal(board, x, y))
                    continue;

                this.goals[i++] = new Position(x, y);
            }
        }
    }

    @Override
    public double estimate(BoardState b) {
        double total = 0;

        for(int[] i : b.boxes){
            Position box_position = new Position(i[0], i[1]);

            double shortest = Double.POSITIVE_INFINITY;

            for(Position g : goals){
                shortest = Math.min(box_position.ManhattanDistance(g), shortest);
            }

            total += shortest;
        }

        return total;
    }

    @Override
    public BoardState initialState() {
        return new BoardState(board);
    }

    long bfstime = 0;

    @Override
    public List<BoxPushAction> actions(BoardState b) {
        long startTime = System.nanoTime();
        List<BoxPushAction> l = Utils.FindReachableBoxes(board, b, b.playerX, b.playerY, deadSquares);
        long endTime = System.nanoTime();

        bfstime += (endTime - startTime);
        return l;
    }

    @Override
    public BoardState result(BoardState b, BoxPushAction action) {
        return new BoardState(b, action.player.x, action.player.y, action.direction);
    }

    @Override
    public boolean isGoal(BoardState b) {
        boolean[] matched = new boolean[goals.length];

        for(int[] i : b.boxes){
            for(int goal_i = 0; goal_i < goals.length; goal_i++){

                // Check if box overlaps with a goal
                if (goals[goal_i].x != i[0])
                    continue;

                if (goals[goal_i].y != i[1])
                    continue;

                matched[goal_i] = true;
            }
        }

        return Utils.AllTrue(matched);
    }

    @Override
    public double cost(BoardState b, BoxPushAction push) {
        return push.cost;
    }

    // Convert BoxPushActions from solution to actual path
    static List<EDirection> Walk(Solution<BoardState, BoxPushAction> solution, SokobanProblem parent){
        List<EDirection> steps = new LinkedList<>();
        BoardState checkpoint = new BoardState(parent.board);

        for (BoxPushAction action : solution.actions){
            RebuildPathProblem problem = new RebuildPathProblem(parent.board, checkpoint, new Position(checkpoint.playerX, checkpoint.playerY), action.player);
            Solution<Position, EDirection> s = AStar.search(problem);
            steps.addAll(s.actions);
            steps.add(action.direction);
            checkpoint = new BoardState(checkpoint, action.player.x, action.player.y, action.direction);
        }

        return steps;
    }
}

class BoxPushAction {
    Position player;
    EDirection direction;
    int cost;

    public BoxPushAction(Position player, EDirection direction) {
        this.player = player;
        this.cost = player.value;
        this.direction = direction;
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
// A basic path search, that avoids walls and boxes and finds a way towards a goal
class RebuildPathProblem implements HeuristicProblem<Position, EDirection>{

    BoardCompact template;
    BoardState state;
    Position start;
    Position goal;

    public RebuildPathProblem(BoardCompact template, BoardState state, Position start, Position goal) {
        this.template = template;
        this.state = state;
        this.start = start;
        this.goal = goal;
    }

    @Override
    public double estimate(Position pos) {
        return pos.ManhattanDistance(goal);
    }

    @Override
    public Position initialState() {
        return start;
    }

    @Override
    public List<EDirection> actions(Position pos) {
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
    public Position result(Position pos, EDirection eDirection) {
        return new Position(pos.x + eDirection.dX, pos.y + eDirection.dY);
    }

    @Override
    public boolean isGoal(Position pos) {
        return pos.ManhattanDistance(goal) == 0;
    }

    @Override
    public double cost(Position pos, EDirection eDirection) {
        return 1;
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
        HashSet<Position> explored = new HashSet<>();
        Queue<Position> frontier = new LinkedList<>();

        frontier.add(new Position(x, y));

        while (!frontier.isEmpty()) {
            Position tile = frontier.poll();

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
                        boxes.add(new BoxPushAction(new Position(tile.x, tile.y, tile.value + 1), dir));
                    }

                    continue;
                }


                frontier.add(new Position(check_x, check_y, tile.value + 1));
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

    public static boolean AllTrue(boolean[] arr){
        for(boolean b : arr) {
            if (!b) {
                return false;
            }
        }
        return true;
    }
}