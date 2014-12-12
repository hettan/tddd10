package firebrigade.prediction.training;

public class EnvironmentPaths {
	public static final String PROJECT_ROOT_PATH = "/home/jocke/git/tddd10";
	public static final String FBPREDICTION_RESOURCES_PATH = PROJECT_ROOT_PATH + "/FBPrediction-resources";
	public static final String AGENTS_PROJECT_PATH = PROJECT_ROOT_PATH + "/agents";
	public static final String RESCUE_SIMULATOR_PATH = PROJECT_ROOT_PATH + "/RescueSimulator";
	public static final String RESUCE_TRAINING_MAP_PATH = RESCUE_SIMULATOR_PATH + "/maps/training-map"; 
	public static final String SIMULATOR_START_SCRIPT = "start-fb-training-simulator.sh";
	public static final String AGENT_START_SCRIPT = "start-fb-training-agents.sh";
	public static final String NEURAL_NETWORK_SCENARIO = "chromosome.txt";
	public static final String NEURAL_NETWORK_TRAINED = FBPREDICTION_RESOURCES_PATH + "/neural_network.nn";

}
