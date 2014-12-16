package firebrigade.prediction.training.genetics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import firebrigade.prediction.training.TrainingFitnessObserver;
import firebrigade.prediction.training.TrainingPersistor;

public class GeneticsManager {
	public static Random random = new Random();
	private double crossover_rate = 0.7;
	private double mutation_rate = 0.1;
	
	/*
	 * Takes a generation, evolves it, and creates a new equally large generation.
	 */
	public Chromosome[] evolve(GenerationGeneticResult[] previousGeneration)
	{
		if(previousGeneration.length == 0)
			return new Chromosome[] { new Chromosome() };
		
		//Puts best fitness first in list.
		Arrays.sort(previousGeneration, new Comparator<GenerationGeneticResult>() {
			@Override
			public int compare(GenerationGeneticResult o1,
					GenerationGeneticResult o2) {
				// TODO Auto-generated method stub
				return (int) (o2.getFitness() - o1.getFitness());
			}
		});
		
		Chromosome[] children = new Chromosome[previousGeneration.length];
		int geneLength = previousGeneration[0].getChromosome().getWeights().length;
		
		//generate crossovers for each new child from two parents.
		for(int i = 0; i < children.length; i++)
		{
			Chromosome[] parents = selectParentPair(previousGeneration);
			double[] genes = new double[geneLength];
			int crossOverIndex = 0;
			do {
				//Select a random crossover gene.
				crossOverIndex = random.nextInt(geneLength);
			} while(random.nextDouble() < crossover_rate);
			
			for(int x = 0; x < crossOverIndex; x++)
				genes[x] = parents[0].getWeights()[x];
			for(int x = crossOverIndex; x < geneLength; x++)
				genes[x] = parents[1].getWeights()[x];
			
			children[i] = new Chromosome(genes);
		}
		
		//Create some random mutations.
		children = createMutations(children);
		
		return children;
	}
	
	/*
	 * Just pick the two best parents now, better with roulette wheel in the future.
	 */
	public Chromosome[] selectParentPair(GenerationGeneticResult[] previousGeneration)
	{
		double maxFitness = TrainingFitnessObserver.MAX_FITNESS;
		
		Chromosome[] parents = new Chromosome[2];
		while(parents[0] == null || parents[1] == null)
		{
			for(GenerationGeneticResult r : previousGeneration)
			{
				if(random.nextDouble() < (r.getFitness()/maxFitness)) //The better the fitness, the greater the chance it is selected.
				{
					if(parents[0] == null)
					{
						parents[0] = r.getChromosome();
						break;
					}
					else
					{
						if(previousGeneration.length == 1 || 
								r.getChromosome() != parents[0])
							parents[1] = r.getChromosome();
					}
				}
			}
		}
		
		return parents;
	}
	
	public Chromosome[] createMutations(Chromosome[] children)
	{
		for(Chromosome child : children)
		{
			for(int i = 0; i < child.getWeights().length; i++)
			{
				if(random.nextDouble() < mutation_rate)
					child.getWeights()[i] = Chromosome.GetRandomGene();
			}
		}
		return children;	
	}
	

	public Chromosome[] createInitialGeneration(int size) {
		// TODO Auto-generated method stub
		Chromosome[] ret = new Chromosome[size];
		for(int i = 0; i < size; i++)
		{
			ret[i] = new Chromosome();
			ret[i].random();
		}
		return ret;
	}
}
