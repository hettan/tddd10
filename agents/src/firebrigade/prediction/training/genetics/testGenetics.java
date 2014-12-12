package firebrigade.prediction.training.genetics;

import java.io.IOException;

public class testGenetics {
	public static void main(String[] args) throws IOException {
		GeneticsManager m = new GeneticsManager();
		Chromosome[] initial = m.createInitialGeneration(10);
		GenerationGeneticResult[] res = new GenerationGeneticResult[10];
		for(int i= 0; i < initial.length; i++)
		{
			res[i] = new GenerationGeneticResult(i, initial[i]);
		}
		printChromosome(initial);
		Chromosome[] next = m.evolve(res);
		System.out.println();
		System.out.println();
		printChromosome(next);
	}
	
	private static void printChromosome(Chromosome[] chromosomes)
	{
		int x = 1;
		for(Chromosome c : chromosomes){
			String res = "";
			for(int i = 0; i < c.getWeights().length; i++)
				res += c.getWeights()[i] + ", ";
			
			System.out.println("C(" + x + "): " + res);
			x++;
		}
	}
}
