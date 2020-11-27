import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.CPush;
import game.board.compact.BoardCompact;
import game.board.compact.CTile;
import game.board.minimal.StateMinimal;
import game.board.oop.*;

import search.HeuristicProblem;
import search.Solution;

import javax.swing.plaf.nimbus.State;

class Position {
    Integer x;
    Integer y;
    int cost = 0; // a helper variable used in the BFS

    Position(Integer a, Integer b) {
        x = a;
        y = b;
    }

    Position(Integer a, Integer b, int cost){
        this(a, b);
        this.cost = cost;
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
        return "Pos{" +  "x=" + x +  ", y=" + y + ", value=" + cost + '}';
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
//        Utils.FindReachableBoxes(board, new StateMinimal(board), board.playerX, board.playerY, DeadSquareDetector.detect(board));
//        if ( 1 == 1) return null;
        SokobanProblem problem = new SokobanProblem(board);
        Solution<StateMinimal, BoxPushAction> solution = AStar.search(problem);
        System.out.println("BFS took " + (problem.bfstime / 1000000) + "ms");
        return SokobanProblem.Walk(solution, problem);
    }
}

class SokobanProblem implements HeuristicProblem<StateMinimal, BoxPushAction> {

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
    public double estimate(StateMinimal s) {
        double total = 0;

        for(int i = 1; i < s.positions.length; i++){
            Position box_position = new Position(s.getX(s.positions[i]), s.getY(s.positions[i]));

            double shortest = Double.POSITIVE_INFINITY;

            for(Position g : goals){
                shortest = Math.min(box_position.ManhattanDistance(g), shortest);
            }

            total += shortest;

        }

        return total;
    }

    @Override
    public StateMinimal initialState() {
        return new StateMinimal(board);
    }

    long bfstime = 0;

    @Override
    public List<BoxPushAction> actions(StateMinimal s) {
        long startTime = System.nanoTime();
        List<BoxPushAction> l = Utils.FindReachableBoxes(board, s, s.getX(s.positions[0]) , s.getY(s.positions[0]), deadSquares);
        long endTime = System.nanoTime();

        bfstime += (endTime - startTime);
        return l;
    }

    @Override
    public StateMinimal result(StateMinimal s, BoxPushAction action) {
        s.positions[0] = s.getPacked(action.player.x, action.player.y);

        BoardCompact b = board.clone();
        Utils.UniversalSetState(b, s);
        CPush push = new CPush(action.direction);
        push.perform(b);
        return new StateMinimal(b);
    }

    @Override
    public boolean isGoal(StateMinimal s) {
        boolean[] matched = new boolean[goals.length];

        for(int i = 1; i < s.positions.length; i++) {
            Position box_position = new Position(s.getX(s.positions[i]), s.getY(s.positions[i]));
            for(int goal_i = 0; goal_i < goals.length; goal_i++){

                if (!box_position.equals(goals[goal_i]))
                    continue;

                matched[goal_i] = true;
            }
        }

        return Utils.AllTrue(matched);
    }

    @Override
    public double cost(StateMinimal s, BoxPushAction push) {
        return push.cost;
    }

    // Convert BoxPushActions from solution to actual path
    static List<EDirection> Walk(Solution<StateMinimal, BoxPushAction> solution, SokobanProblem parent){
        List<EDirection> steps = new LinkedList<>();
        BoardCompact b = parent.board.clone();
        StateMinimal checkpoint = new StateMinimal(b);

        for (BoxPushAction action : solution.actions){

            // Find path from one checkpoint to the other
            RebuildPathProblem problem =
                    new RebuildPathProblem(parent.board, checkpoint,
                    new Position(checkpoint.getX(checkpoint.positions[0]), checkpoint.getY(checkpoint.positions[0])), action.player);
            Solution<Position, EDirection> s = AStar.search(problem);

            // Add all steps from the path to total path
            steps.addAll(s.actions);
            steps.add(action.direction);

            // Perform the push on our state
            checkpoint.positions[0] = checkpoint.getPacked(action.player.x, action.player.y);
            Utils.UniversalSetState(b, checkpoint);
            CPush push = new CPush(action.direction);
            push.perform(b);

            // Replace checkpoint
            checkpoint = new StateMinimal(b);
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
        this.cost = player.cost;
        this.direction = direction;
    }
}

// A basic path search, that avoids walls and boxes and finds a way towards a goal
class RebuildPathProblem implements HeuristicProblem<Position, EDirection>{

    BoardCompact template;
    StateMinimal state;
    Position start;
    Position goal;

    public RebuildPathProblem(BoardCompact template, StateMinimal state, Position start, Position goal) {
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
    static boolean HasBox(StateMinimal s, int x, int y) {
        for(int i = 1; i < s.positions.length; i++) {
            int bx = s.getX(s.positions[i]);
            int by = s.getY(s.positions[i]);

            if (bx == x && by == y)
                return true;

        }

        return false;
    }

    // Find all boxes that can be pushed from a given area on a map
    static ArrayList<BoxPushAction> FindReachableBoxes(BoardCompact template, StateMinimal s, int x, int y, boolean[][] deadSquares) {

//        template = template.clone();
//        System.out.println("\nSearching on: ");
//        Utils.UniversalSetState(template, s);

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
                        boxes.add(new BoxPushAction(new Position(tile.x, tile.y, tile.cost + 1), dir));
                    }

                    continue;
                }


                frontier.add(new Position(check_x, check_y, tile.cost + 1));
            }

            explored.add(tile);
        }

//        System.out.println("Found reachable boxes:");
//        for (BoxPushAction box : boxes) {
//            System.out.print("x: " + box.player.x + ", y: " + box.player.y + ", with cost: " + box.player.cost);
//            System.out.println(" and push dir: " + box.direction);
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

    public static void fixedUnsetState(BoardCompact b, StateMinimal state) {
        b.boxInPlaceCount = 0;
        for (int i = 1; i < state.positions.length; ++i) {
            b.tiles[state.getX(state.positions[i])][state.getY(state.positions[i])] &= EEntity.NULLIFY_ENTITY_FLAG;
        }
    }

    public static void UniversalSetState(BoardCompact b, StateMinimal s){
        fixedUnsetState(b, new StateMinimal(b));

        b.movePlayer(b.playerX, b.playerY, s.getX(s.positions[0]), s.getY(s.positions[0]));

        for (int i = 1; i < s.positions.length; ++i) {
            b.tiles[s.getX(s.positions[i])][s.getY(s.positions[i])] |= EEntity.BOX_1.getFlag();
            if (CTile.forSomeBox(b.tiles[s.getX(s.positions[i])][s.getY(s.positions[i])])) ++b.boxInPlaceCount;
        }
    }
}