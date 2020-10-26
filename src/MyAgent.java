import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;

import cz.sokoban4j.agents.ArtificialAgent;
import cz.sokoban4j.simulation.actions.EDirection;
import cz.sokoban4j.simulation.actions.compact.*;
import cz.sokoban4j.simulation.board.compact.BoardCompact;

class DeadSquareDetector {
	public static boolean[][] detect(BoardCompact board) {
		// Write your dead square detector here.

		return null;
	}
}

/**
 * The simplest Tree-DFS agent. Feel free to fool around here! You're in the PLAYGROUND after all!
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;
	
	@Override
	protected List<EDirection> think(BoardCompact board) {
		// INIT SEARCH
		this.board = board;
		
		// DEBUG
		out.println("===== BOARD =====");
		board.debugPrint();
		out.println("=================");
		
		// FIRE THE SEARCH
		
		searchedNodes = 0;
		
		long searchStartMillis = System.currentTimeMillis();
		
		List<EDirection> result = new ArrayList<EDirection>();
		dfs(5, result); // the number marks how deep we will search (the longest plan we will consider)

		long searchTime = System.currentTimeMillis() - searchStartMillis;
		
		out.println("SEARCH TOOK:   " + searchTime + " ms");
		out.println("NODES VISITED: " + searchedNodes);
		out.println("PERFORMANCE:   " + ((double)searchedNodes / (double)searchTime * 1000) + " nodes/sec");
		out.println("SOLUTION:      " + (result.size() == 0 ? "NOT FOUND" : "FOUND in " + result.size() + " steps"));
		
		if (result.size() > 0) {
			out.print("STEPS:         ");
			for (EDirection winDirection : result) {
				out.print(winDirection + " -> ");
			}
			out.println("BOARD SOLVED!");
		}
		out.println("=================");
		
		if (result.size() == 0) {
            out.println("FAILED TO SOLVE THE BOARD...");
            return null;
		}
				
		return result;
	}

	private boolean dfs(int level, List<EDirection> result) {
		if (level <= 0) return false; // DEPTH-LIMITED
		
		++searchedNodes;
		
		// COLLECT POSSIBLE ACTIONS
		
		List<CAction> actions = new ArrayList<CAction>(4);
		
		for (CMove move : CMove.getActions()) {
			if (move.isPossible(board)) {
				actions.add(move);
			}
		}
		for (CPush push : CPush.getActions()) {
			if (push.isPossible(board)) {
				actions.add(push);
			}
		}
		
		// TRY ACTIONS
		for (CAction action : actions) {
			// PERFORM THE ACTION
			result.add(action.getDirection());
			action.perform(board);
			
			// CHECK VICTORY
			if (board.isVictory()) {
				// SOLUTION FOUND!
				return true;
			}
			
			// CONTINUE THE SEARCH
			if (dfs(level - 1, result)) {
				// SOLUTION FOUND!
				return true;
			}
			
			// REVERSE ACTION
			result.remove(result.size()-1);
			action.reverse(board);
		}
		
		return false;
	}
}
