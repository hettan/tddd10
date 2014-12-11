package firebrigade.prediction.training;

import java.io.IOException;

import firebrigade.prediction.NeuralNetwork;
import firebrigade.prediction.NeuralNetworkPrediction;
import firebrigade.prediction.RREFPrediction;
import firebrigade.prediction.training.Logger.Level;

public class LaunchTestNeuralNetwork {
	public static void main(String[] args) throws IOException {
		NeuralNetwork nn = new NeuralNetwork();
		nn.load(EnvironmentPaths.NEURAL_NETWORK_TRAINED);
		System.out.println("Could load network: " + nn.isLoaded());
		double[] test = new double[nn.INPUT_NODES_COUNT];
		for(int i = 0; i < test.length; i++)
			test[i] = 0.0;
		double[] result = nn.getPredictions(test);
		
		for(int i = 0; i < result.length; i++)
			System.out.println("Result (" + i + "): " + result[i]);
	}
}
