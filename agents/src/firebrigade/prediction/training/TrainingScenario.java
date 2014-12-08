package firebrigade.prediction.training;

public class TrainingScenario {

	private int _port;
	private String _host;
	//private NeuralNetworkInstance _trainingInstance
	
	public TrainingScenario(String host, int port)
	{
		_host = host;
		_port = port;
	}
	
	public String getHost()
	{
		return _host;
	}
	
	public int getPort()
	{
		return _port;
	}
}
