import static java.lang.System.out;

import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;
import game.board.oop.EPlace;
import game.board.oop.ESpace;

class Pair<A, B>{
	A x;
	B y;

	Pair(A a, B b){
		x = a;
		y = b;
	}


	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair))
			return false;

		Pair q = (Pair) o;
		return x == q.x && y == q.y;
	}

	@Override
	public int hashCode() { return x.hashCode() ^ y.hashCode(); }
}

class DeadSquareDetector{

	private static boolean IsSpace(BoardCompact b, int x, int y){
		int wall_mask = ESpace.WALL.getFlag();
		return (b.tiles[x][y] & wall_mask) == 0;
	}

	public static boolean[][] detect(BoardCompact board){
		int width = board.width();
		int height = board.height();

		boolean[][] deadTiles = new boolean[width][height];

		for (boolean[] array : deadTiles)
			Arrays.fill(array, true);

		HashSet<Pair<Integer, Integer>> explored = new HashSet<>();
		Queue<Pair<Integer, Integer>> frontier = new LinkedList<Pair<Integer, Integer>>();

		for(int x = 0; x < width; x++){
			for (int y = 0; y < width; y++){
				int mask = EPlace.SOME_BOX_PLACE_FLAG;
				if ((board.tiles[x][y] & mask) != 0)
					frontier.add(new Pair<>(x, y));
			}
		}

		while ( ! frontier.isEmpty()) {
			Pair<Integer, Integer> tile = frontier.poll();

			if (explored.contains(tile))
				continue;

			deadTiles[tile.x][tile.y] = false;

			for (int dx = -1; dx <= 1; dx++){
				for (int dy = -1; dy <= 1; dy++){

					if (dx != 0 && dy != 0)
						continue;

					if (! IsSpace(board, tile.x + dx, tile.y + dy) || ! IsSpace(board, tile.x + dx*2, tile.y + dy*2)){
						continue;
					}

					frontier.add(new Pair<>(tile.x + dx, tile.y + dy));
				}
			}

			explored.add(tile);
		}

		return deadTiles;
	}
}

/**
 * The simplest Tree-DFS agent.
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;
	
	@Override
	protected List<EDirection> think(BoardCompact board) {
		this.board = board.clone();

		boolean[][] dead = DeadSquareDetector.detect(board);

		for(int x = 0; x < board.width(); x ++){
			for(int y = 0; y < board.width(); y++){
				System.out.print(dead[x][y] ? '0' : '1');
			}
			System.out.println();
		}

		return null;
	}
}
