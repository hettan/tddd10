package firebrigade.prediction.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import firebrigade.prediction.training.Logger.Level;
import firebrigade.prediction.training.genetics.Chromosome;
import firebrigade.prediction.training.genetics.GenerationGeneticResult;
import firebrigade.prediction.training.genetics.GeneticsManager;

public class TrainingConductor {

	private final int AMOUNT_OF_WORKERS_GENERATION = 4;
	private final int BASE_PORT = 8000;
	private final String HOST = "localhost";
	private int getPort(int offset)
	{
		return BASE_PORT + offset;
	}
	
	private TrainingConductorWorker[] _workers;
	//Used to store the results of all generations.
	private TrainingPersistor _persistor;
	
	//Used to handle the genetic evolution.
	private GeneticsManager _geneticsManager;
	
	
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
		_geneticsManager = new GeneticsManager();
		
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
		
		
		for(int i = 0; i < AMOUNT_OF_WORKERS_GENERATION; i++)
		{
			int index = i;
			_workers[index].runSimulationAsync(scenarios[index]);
			
		}
		
		for(int i = 0; i < AMOUNT_OF_WORKERS_GENERATION; i++)
		{
			int index = i;
			if(!_workers[index].isDone()){
				Logger.Write("Waiting for " + _workers[index].getID() + ".");
				_workers[index].waitFor();
				_workers[index].saveResults();
				Logger.Write("Done waiting!");
			}
		}
		
		for(int i = 0; i < AMOUNT_OF_WORKERS_GENERATION; i++)
		{
			int index = i;
			_workers[index].releaseResources();
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
		GenerationGeneticResult[] results = new GenerationGeneticResult[AMOUNT_OF_WORKERS_GENERATION];
		for(int i = 0; i < _workers.length; i++)
			results[i] = _workers[i].getResult();
		int id = _persistor.allocateStorage();
		_persistor.storeData(id, results);
	}
	
	private TrainingScenario[] getTrainingScenarios()
	{
		int newest = _persistor.getNewestStorage();
		Chromosome[] chromosomes;
		if(newest == 0)
		{
			//Create a default instance
			chromosomes = _geneticsManager.createInitialGeneration(AMOUNT_OF_WORKERS_GENERATION);
		}
		else
		{
			GenerationGeneticResult[] previousResults = _persistor.fetchData(newest);
			//Evolve it and create new instances.
			chromosomes = _geneticsManager.evolve(previousResults);
			
		}
		TrainingScenario[] scenario = new TrainingScenario[AMOUNT_OF_WORKERS_GENERATION];
		for(int i = 0; i < scenario.length; i++)
		{
			//Add the instance
			scenario[i] = new TrainingScenario("localhost", getPort(i), chromosomes[i]);
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
	
	/*
	 * Takes the best output from the generations and saves it for future use.
	 */
	public void saveBestNetwork() throws IOException
	{
		Logger.Write("Saving the best trained network...");
		int generations = _persistor.getStorageCount();
		GenerationGeneticResult best = null;
		double bestFitness = 0;
		for(int i = 0; i < generations; i++)
		{
			int storageIndex = _persistor.getStorageIdentifier(i);
			GenerationGeneticResult[] results = _persistor.fetchData(storageIndex);
			for(int x = 0; x < results.length; x++)
			{
				if(results[x].getFitness() > bestFitness)
				{
					best = results[x];
					bestFitness = results[x].getFitness();
				}
			}
		}
		Chromosome toSave;
		if(best == null)
		{
			toSave = new Chromosome();
			Logger.Write("Did not find a best network. Saving a default one.");
		}
		else
		{
			toSave = best.getChromosome();
		}
		File file = new File(EnvironmentPaths.NEURAL_NETWORK_TRAINED);
		if(!file.exists())
			file.createNewFile();
		
		String resultStr = toSave.toString();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(resultStr);
		bw.close();
		Logger.Write("Saved.");
	}
}
