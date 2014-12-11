package firebrigade.prediction.training.genetics;

import java.util.Random;

import firebrigade.prediction.NeuralNetwork;

public class Chromosome {
	private double[] _weights;
	private static Random random = new Random();
	
	public Chromosome()
	{
		setDefault();
	}
	
	public Chromosome(double[] weights)
	{
		_weights = weights;
	}
	
	public void random()
	{
		for(int i = 0; i < _weights.length; i++)
		{
			_weights[i] = GetRandomGene();
		}
	}
	
	public static double GetRandomGene()
	{
		return  random.nextDouble() * (NeuralNetwork.WEIGHT_UPPERBOUND - NeuralNetwork.WEIGHT_LOWERBOUND) + NeuralNetwork.WEIGHT_LOWERBOUND;
	}
	
	public void setDefault()
	{
		_weights = new double[NeuralNetwork.AMOUNT_OF_WEIGHTS];
		for(int i = 0; i < _weights.length; i++)
			_weights[i] = NeuralNetwork.DEFAULT_WEIGHT;
	}
	
	public double[] getWeights()
	{
		return _weights;
	}
	
	@Override
	public String toString()
	{
		return Chromosome.ToString(this);
	}
	
	public static Chromosome FromString(String encoded)
	{
		if(encoded.contains(","))
		{
			String[] split = encoded.split(",");
			double[] weights = new double[split.length];
			for(int i = 0; i < split.length; i++)
			{
				weights[i] = Double.parseDouble(split[i]);
			}
			return new Chromosome(weights);
		}
		
		return new Chromosome();
	}
	
	public static String ToString(Chromosome c)
	{
		if(c.getWeights().length == 0)
		{
			return "0";
		}
		else
		{
			double[] w = c.getWeights();
			String ret = String.valueOf(w[0]);
			for(int i = 1; i < w.length; i++)
				ret += "," + String.valueOf(w[i]);
			return ret;
		}
	}
}
