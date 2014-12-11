package firebrigade.prediction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.neuroph.nnet.MultiLayerPerceptron;

import firebrigade.prediction.training.Logger;
import firebrigade.prediction.training.Logger.Level;
import firebrigade.prediction.training.genetics.Chromosome;

public class NeuralNetwork {
	public static final double DEFAULT_WEIGHT = 1.0;
	public static final double WEIGHT_UPPERBOUND = 10;
	public static final double WEIGHT_LOWERBOUND = -10;
	
	public static final int INPUT_NODES_COUNT = 6;
	public static final int OUTPUT_NODES_COUNT = 1;
	public static final int HIDDEN_NODES_COUNT = 4;
	//This is derived from the size of the network.
	//the + 1 comes from the implementation of MultiLayerPerceptron, where they magically add 1 node for all layers except output (perhaps something with their training).
	//public static final int AMOUNT_OF_WEIGHTS = (INPUT_NODES_COUNT) * (HIDDEN_NODES_COUNT) + (HIDDEN_NODES_COUNT ) * OUTPUT_NODES_COUNT;
	//derived from the size of the network ( i really can't find the logic for it right now )
	public static final int AMOUNT_OF_WEIGHTS = 33;
	
	private org.neuroph.core.NeuralNetwork _neuralNetwork;
	private boolean _isLoaded = false;
	
	public NeuralNetwork()
	{
		initialize();
	}
	
	private void initialize()
	{
		_neuralNetwork = new MultiLayerPerceptron(INPUT_NODES_COUNT, HIDDEN_NODES_COUNT, OUTPUT_NODES_COUNT);
	}
	
	/*
	 * Loads a neural network from a chromosome file.
	 */
	public void load(String path)
	{
		_isLoaded = false;
		File file = new File(path);
		if(file.exists())
		{
			try {
				String chromosomeStr = IOUtils.toString(new FileInputStream(file));
				Chromosome chromosome = Chromosome.FromString(chromosomeStr);
				if(chromosome.getWeights().length == _neuralNetwork.getWeights().length) //same type of network.
				{
					_neuralNetwork.setWeights(chromosome.getWeights());
					_isLoaded = true;
				}
			} catch (FileNotFoundException e) {
				Logger.Write(Level.ERROR, "File not found: " + e.getMessage()); 
			} catch (IOException e) {
				Logger.Write(Level.ERROR, "IOException: " + e.getMessage()); 
			}
		}
	}
	
	public boolean isLoaded()
	{
		return _isLoaded;
	}
	
	public double[] getPredictions(double... args)	
	{
		if(args.length != INPUT_NODES_COUNT)
			return null;
		_neuralNetwork.setInput(args);
		_neuralNetwork.calculate();
		return _neuralNetwork.getOutput();
	}
	
}
