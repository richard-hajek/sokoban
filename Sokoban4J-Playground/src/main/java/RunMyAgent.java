import cz.sokoban4j.Sokoban;
import cz.sokoban4j.simulation.SokobanResult;

class RunMyAgent {
    public static void main(String[] args) {
		SokobanResult result;
		
		// VISUALIZED GAME
		result = Sokoban.playAgentLevel("Easy/easy.sok", 1, new MyAgent());   //  5 steps required
		// result = Sokoban.playAgentLevel("Easy/easy.sok", 2, new MyAgent());   // 13 steps required
		// result = Sokoban.playAgentLevel("Easy/easy.sok", 3, new MyAgent());   // 25 steps required
        // result = Sokoban.playAgentLevel("Easy/easy.sok", 4, new MyAgent());   // 37 steps required
        
		// HEADLESS == SIMULATED-ONLY GAME
		// result = Sokoban.simAgentLevel("Easy/easy.sok", 1, new MyAgent());
		
		System.out.println("MyAgent result: " + result.getResult());
	}
}
