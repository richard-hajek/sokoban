package game;

import java.io.File;

import utils.Sanitize;

public class SokobanConfig {
	
	/**
	 * Can be used to mark unique name of the simulation.
	 */
	public String id = "Sokoban";
	
	/**
	 * Sokoban level file or directory to play.
	 * 
	 * FILE == play from file.
	 * DIRECTORY == play all {@link ELevelFormat} recognized files in alphabetic order found in this directory (no directory recursion).
	 */
	public File level;
	
	/**
	 * If {@link #level} is FILE, than this contains what number of the level to load; 1-based.
	 * 0 == run all levels sequentially.
	 */
	public int levelNumber;
	
	/**
	 * Expected format of the level(s) in file {@link #level} file. Ignored if {@link #level} points to directory. In such a case,
	 * {@link Sokoban} will play through all supported level files supported in alphabetic order.
	 */
	public ELevelFormat levelFormat;
	
	/**
	 * Timeout for the game; positive number == timeout in effect; otherwise no timeout.
	 */
	public long timeoutMillis = 0;
	
	/**
	 * TRUE == start Sokoban visualized using {@link SokobanVis}; FALSE == start Sokoban headless using {@link SokobanSim}.
	 */
	public boolean visualization = false;
	
	/**
	 * Instance of the agent that should play the game.
	 */
	public IAgent agent;
    
    public boolean verbose = false;

    public boolean requireOptimal = false;

	/**
	 * Validates the configuration; throws {@link RuntimeException} if config is found invalid. 
	 */
	public void validate() {
		if (id == null) throw new RuntimeException("ID is null.");
		if (id.length() == 0) throw new RuntimeException("ID is of zero length.");
		id = Sanitize.idify(id);
		if (agent == null) throw new RuntimeException("Agent is null.");
		if (level == null) throw new RuntimeException("Level is null.");
		if (levelNumber < 0) throw new RuntimeException("LevelNumber < 0");
		if (!level.exists()) throw new RuntimeException("Level '" + level.getAbsolutePath() + "' does not exist.");
		if (!level.isFile() && !level.isDirectory()) throw new RuntimeException("Level '" + level.getAbsolutePath() + "' is neither a file nor a directory.");
		if (level.isFile() && levelFormat == null) throw new RuntimeException("LevelFormat is null but Level points to a file '" + level.getAbsolutePath() + "'.");
	}

}
