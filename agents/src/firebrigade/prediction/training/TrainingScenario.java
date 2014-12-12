package firebrigade.prediction.training;

import firebrigade.prediction.training.genetics.Chromosome;

public class TrainingScenario {

	private int _port;
	private String _host;
	//private NeuralNetworkInstance _trainingInstance
	private Chromosome _chromosome;
	
	public TrainingScenario(String host, int port, Chromosome chromosome)
	{
		_host = host;
		_port = port;
		_chromosome = chromosome;
	}
	
	public String getHost()
	{
		return _host;
	}
	
	public int getPort()
	{
		return _port;
	}
	
	public Chromosome getChromosome()
	{
		return _chromosome;
	}
}
