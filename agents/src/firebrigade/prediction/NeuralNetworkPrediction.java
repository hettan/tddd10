package firebrigade.prediction;

import java.io.IOException;

import firebrigade.FireArea;
import firebrigade.prediction.training.EnvironmentPaths;

public class NeuralNetworkPrediction implements RREFPrediction {
	public static boolean IS_TRAINING = true;
	
	private NeuralNetwork _neuralNetwork;
	
	public NeuralNetworkPrediction() throws IOException
	{
		_neuralNetwork = new NeuralNetwork();
		if(IS_TRAINING)
		{
			//load from scenario chromosome
			_neuralNetwork.load(EnvironmentPaths.NEURAL_NETWORK_SCENARIO);
		}
		else
		{
			//load trained
			_neuralNetwork.load(EnvironmentPaths.NEURAL_NETWORK_TRAINED);
		}
		
		if(!_neuralNetwork.isLoaded()) //Failed to load. Throw exception.
		{
			if(IS_TRAINING)
				throw new IOException("Failed to load NN from: " + EnvironmentPaths.NEURAL_NETWORK_SCENARIO);
			throw new IOException("Failed to load NN from file: " + EnvironmentPaths.NEURAL_NETWORK_TRAINED);
		}
	}
	
	@Override
	public int getPrediction(FireArea fireArea) {
		// TODO Auto-generated method stub
		double[] predictions = _neuralNetwork.getPredictions(getFactors(fireArea));
		return extractNeuralNetworkResult(predictions);
	}
	
	private double[] getFactors(FireArea fireArea)
	{
		return new double[] { 0.0 };
	}
	
	private int extractNeuralNetworkResult(double[] predictions)
	{
		return 2;
	}

}
