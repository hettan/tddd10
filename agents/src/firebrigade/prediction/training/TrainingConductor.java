package firebrigade.prediction.training;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import firebrigade.prediction.training.Logger.Level;

public class TrainingConductor {

	private final int AMOUNT_OF_WORKERS_GENERATION = 3;
	private final int AMOUNT_PARALLELL_RUNS = 1;
	private final int AMOUNT_RUN_IN_PARALLEL = AMOUNT_OF_WORKERS_GENERATION / AMOUNT_PARALLELL_RUNS;
	private final int BASE_PORT = 8000;
	private final String HOST = "localhost";
	private int getPort(int offset)
	{
		return BASE_PORT + offset;
	}
	
	private TrainingConductorWorker[] _workers;
	//Used to store the results of all generations.
	private TrainingPersistor _persistor;
	
	
	/*
	 * Sets up the training conductor, with all the storage
	 * required to save data about each individual, and generation
	 * of neural networks.
	 * 
	 * Injects the necessary resources into RescueSimulator as well.
	 */
	public void initialize() throws IOException
	{	
		//Set up the storage.
		_persistor = new TrainingPersistor();
		_persistor.initialize();
		
		//Copies all temporary files.
		initializeTemporaryFiles();
		
		//Sets up the workers
		initializeWorkers();
		
		Logger.Write("TrainingConductor initialized.");
	}
	
	private void initializeTemporaryFiles() throws IOException
	{
		File agentsProjectJar = new File(EnvironmentPaths.FBPREDICTION_RESOURCES_PATH + "/agents.jar");
		File RSJarDirectory = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/jars");
		if(agentsProjectJar.exists())
		{
			FileUtils.copyFileToDirectory(agentsProjectJar, RSJarDirectory);
		}
		else
		{
			Logger.Write(Level.ERROR, "Did not find the agents.jar. Please compile it and place it into: " + EnvironmentPaths.AGENTS_PROJECT_PATH);
			throw new IOException("Did not find agents.jar");
		}

		File agentStartScript = new File(EnvironmentPaths.FBPREDICTION_RESOURCES_PATH + "/" + EnvironmentPaths.AGENT_START_SCRIPT);
		File simulationStartScript = new File(EnvironmentPaths.FBPREDICTION_RESOURCES_PATH + "/" + EnvironmentPaths.SIMULATOR_START_SCRIPT);
		File RSBootDirectory = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/boot");
		if(agentStartScript.exists() && simulationStartScript.exists())
		{
			FileUtils.copyFileToDirectory(simulationStartScript, RSBootDirectory);
			FileUtils.copyFileToDirectory(agentStartScript, RSBootDirectory);
		}
		else
		{
			Logger.Write(Level.ERROR, "could not copy training start scripts, they don't exist. Please add them.");
			throw new IOException("Did not find training start scripts");
		}
		

		File trainingMap = new File(EnvironmentPaths.FBPREDICTION_RESOURCES_PATH + "/training-map");
		File RSmapsDirectory = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/maps/training-map");
		try
		{
			FileUtils.copyDirectory(trainingMap, RSmapsDirectory);
		}
		catch(Exception e)
		{
			Logger.Write(Level.ERROR, "Could not copy training map.");
			throw e;
		}

	}
	
	private void initializeWorkers()
	{
		_workers = new TrainingConductorWorker[AMOUNT_OF_WORKERS_GENERATION];
		for(int i = 0; i < AMOUNT_OF_WORKERS_GENERATION; i++)
		{
			_workers[i] = new TrainingConductorWorker();
			_workers[i].initialize();
		}
	}
	
	/*
	 * Runs one generation of simulations.
	 * Halts the thread until all generations are done.
	 */
	public void runGeneration()
	{
		//Prepare a training scenario.
		TrainingScenario [] scenarios = getTrainingScenarios();
		
		
		for(int y = 0; y < AMOUNT_PARALLELL_RUNS; y++)
		{

			for(int i = 0; i < AMOUNT_RUN_IN_PARALLEL; i++)
			{
				int index = i + y*AMOUNT_RUN_IN_PARALLEL;
				_workers[index].runSimulationAsync(scenarios[index]);
			}
			
			for(int i = 0; i < AMOUNT_RUN_IN_PARALLEL; i++)
			{
				int index = i + y*AMOUNT_RUN_IN_PARALLEL;
				if(!_workers[index].isDone()){
					Logger.Write("Waiting for " + _workers[index].getID() + ".");
					_workers[index].waitFor();
					Logger.Write("Done waiting!");
				}
			}
			
			for(int i = 0; i < AMOUNT_RUN_IN_PARALLEL; i++)
			{
				int index = i + y*AMOUNT_RUN_IN_PARALLEL;
				_workers[index].releaseResources();
			}
		}
		try {
			Thread.sleep(5000);//The waiting time for this to remove the processes.
		} catch (InterruptedException e) {
		} 
		
		//Save the fetched data.
		saveGeneration();
	}
	
	private void saveGeneration()
	{
		TrainingResult[] results = new TrainingResult[AMOUNT_OF_WORKERS_GENERATION];
		for(int i = 0; i < _workers.length; i++)
			results[i] = _workers[i].getResult();
		int id = _persistor.allocateStorage();
		_persistor.storeData(id, results);
	}
	
	private TrainingScenario[] getTrainingScenarios()
	{
		int newest = _persistor.getNewestStorage();
		if(newest == 0)
		{
			//Create a default instance
		}
		else
		{
			TrainingResult[] previousResults = _persistor.fetchData(newest);
			//Evolve it and create new instances.
		}
		TrainingScenario[] scenario = new TrainingScenario[AMOUNT_OF_WORKERS_GENERATION];
		for(int i = 0; i < scenario.length; i++)
		{
			//Add the instance
			scenario[i] = new TrainingScenario("localhost", getPort(i));
		}
		return scenario;
	}
	
	/*
	 * Cleans up after itself, removing any temporary files created.
	 */
	public void cleanup()
	{
		for(int i = 0; i < AMOUNT_OF_WORKERS_GENERATION; i++)
			_workers[i].cleanup();
		
		File agentsProjectJar = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/jars/agents.jar");
		File agentScript = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/boot/" + EnvironmentPaths.AGENT_START_SCRIPT);
		File simulationScript = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/boot/" + EnvironmentPaths.SIMULATOR_START_SCRIPT);
		File trainingMapDirectory = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/maps/training-map");
		try {
			FileUtils.deleteDirectory(trainingMapDirectory);
		} catch (IOException e) {
			Logger.Write(Level.ERROR, "Failed at deleting training-map.");
		}
		agentsProjectJar.delete();
		agentScript.delete();
		simulationScript.delete();
		
		Logger.Write("TrainingConductor has cleaned up.");;
	}
	
	
}
