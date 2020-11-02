package game;

import java.io.*;

public class SokobanResult {
	
	private String id = null;
	
	private IAgent agent = null;
	
	private String level = null;
	
	private SokobanResultType result = null;
	
	private Throwable execption;
	
	private int steps = 0;
	
	private long simStartMillis = 0;
	
	private long simEndMillis = 0;
	
	public SokobanResult() {		
	}

	/**
	 * Assigned ID given to this simulation.
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Agent that was running in simulation.
	 * @return
	 */
	public IAgent getAgent() {
		return agent;
	}

	public void setAgent(IAgent agent) {
		this.agent = agent;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * Result of the simulation.
	 * @return
	 */
	public SokobanResultType getResult() {
		return result;
	}

	public void setResult(SokobanResultType result) {
		this.result = result;
	}

	/**
	 * How many steps an agent performed.
	 * @return
	 */
	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	/**
	 * Time the simulation started in milliseconds (obtained via {@link System#currentTimeMillis()}.
	 * @return
	 */
	public long getSimStartMillis() {
		return simStartMillis;
	}

	public void setSimStartMillis(long simStartMillis) {
		this.simStartMillis = simStartMillis;
	}

	/**
	 * Time the simulation ended in milliseconds (obtained via {@link System#currentTimeMillis()}.
	 * @return
	 */
	public long getSimEndMillis() {
		return simEndMillis;
	}

	public void setSimEndMillis(long simEndMillis) {
		this.simEndMillis = simEndMillis;
	}
	
	/**
	 * How long the simulation run in milliseconds.
	 * @return
	 */
	public long getSimDurationMillis() {
		return simEndMillis - simStartMillis;
	}

	/**
	 * Exception caught during the simulation; 
	 * filled in case of {@link #getResult()} == {@link SokobanResultType#AGENT_EXCEPTION} or {@link SokobanResultType#SIMULATION_EXCEPTION}.  
	 * @return
	 */
	public Throwable getException() {
		return execption;
	}

	public void setException(Throwable execption) {
		this.execption = execption;
	}
	
	@Override
	public String toString() {
		return "SokobanResult[" + getResult() + "]";
	}
    
	public void outputResult(File resultFile, String levelFile, int level, String agentClassString) {
		System.out.println("Outputting result: " + toString());
		FileOutputStream output = null;		
		boolean header = !resultFile.exists();
		try {
			output = new FileOutputStream(resultFile, true);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Failed to append to the result file: " + resultFile.getAbsolutePath());
		}
		try {
			PrintWriter writer = new PrintWriter(output);
		
			if (header) {
				writer.println("id;levelFile;levelNumber;agent;result;steps;playTimeMillis");
			}
			writer.println(getId() + ";" + levelFile + ";" + level + ";" + agentClassString + ";" + getResult() + ";" + getSteps() + ";" + getSimDurationMillis());
			
			writer.flush();
			writer.close();
			
		} finally {
			try {
				output.close();
			} catch (IOException e) {
			}
		}		
	}
}
