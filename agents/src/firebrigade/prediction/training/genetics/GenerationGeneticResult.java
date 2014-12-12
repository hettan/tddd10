package firebrigade.prediction.training.genetics;

public class GenerationGeneticResult {
	//private int _fitnessResult
	private double _fitness;
	private Chromosome _chromosome;
	
	public GenerationGeneticResult(double fitness, Chromosome chromosome)
	{
		_fitness = fitness;
		_chromosome = chromosome;
	}
	
	public double getFitness()
	{
		return _fitness;
	}
	
	/*
	 * Contains the weights for the neural network.
	 */
	public Chromosome getChromosome()
	{
		return _chromosome;
	}
	
	public String toPersistanceFormat()
	{
		return _fitness + "\n" + _chromosome.toString();
	}
	
	public static GenerationGeneticResult fromPersistanceFormat(String string)
	{
		String[] split = string.split("\n");
		if(split.length == 2)
		{
			double fitness = Double.parseDouble(split[0]);
			Chromosome c = Chromosome.FromString(split[1]);
			return new GenerationGeneticResult(fitness, c);
		}
		else
		{
			return new GenerationGeneticResult(0, new Chromosome());
		}
	}
}
