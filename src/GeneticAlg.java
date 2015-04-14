import java.util.*;

public class GeneticAlg {
	private static double mutationRate = 0.015;
	private static double geneMutationRate = 0.05;
	private static double min_average = 0.8;
	private static int max_iteration = 100;
	private static int population_size = 16;
	private static Population population;
	private static String database1;
	private static String database2;
	
	public static void randomizePopulation() {
		population = new Population(population_size, true);
		FitnessCal.setDatabases(database1, database2);
	}
	
	public static Individual roundWheelSelection() {
		double sum = 0.0;
		double sum_prob = 0.0;
		double[] prob = new double[population.getSize()];
		for (int i=0; i<population.getSize(); i++) {
			sum += population.getIndividual(i).getFitness();
		}
		for (int i=0; i<population.getSize(); i++) {
			prob[i] = sum_prob + population.getIndividual(i).getFitness()/sum;
			sum_prob += prob[i];
		}
		double number = Math.random();
		for (int i=0; i<population.getSize()-1; i++) {
			if (number>=prob[i] && (number<prob[i+1]))
				return population.getIndividual(i);
		}
		return population.getIndividual(population.getSize()-1);
	}
	
	public static Individual crossover(Individual parent1, Individual parent2) {
		int split_index = (int) (Math.random() * parent1.getSize());
		Individual offspring = new Individual();
		for (int i=0; i<parent1.getSize(); i++) {
			if (i<split_index) {
				offspring.setWeight(i, parent1.getWeight(i));
			} else {
				offspring.setWeight(i, parent2.getWeight(i));
			}
		}
		return offspring;
	}
	
	public static Individual mutate(Individual individual) {
		boolean will_mutate = Math.random() < mutationRate;
		if (will_mutate) {
			Individual new_individual = new Individual();
			for (int i=0; i<individual.getSize(); i++) {
				boolean mutate_gene = Math.random() < geneMutationRate;
				if (mutate_gene) {
					new_individual.setWeight(i, Math.random());
				} else {
					new_individual.setWeight(i, individual.getWeight(i));
				}
			}
		}
		return individual;
	}
	
	public static boolean isSatisfied() {
		return population.getNormalizedFitnessScore() >= min_average;
	}
	
	public static void evolvePopulation() {
		int count = 0;
		do {
			count ++;
			Population new_population = new Population(population.getSize(), false);
			// keep the best individual of previous generation
			new_population.setIndividual(0, population.getFitness());
			// crossover			
			for (int i=1; i<new_population.getSize(); i++) {
				// choose parents randomly and perform crossover
				Individual parent1 = roundWheelSelection();
				Individual parent2 = roundWheelSelection();
				Individual offspring = crossover(parent1, parent2);
				new_population.setIndividual(i, offspring);
			}
			// mutate some new offsprings
			for (int i=0; i<new_population.getSize(); i++) {
				Individual individual = new_population.getIndividual(i);
				new_population.setIndividual(i, mutate(individual));
			}
			population = new_population;
		} while (!isSatisfied() && count <max_iteration);
		
		System.out.println(population.getAverageFitnessScore());
		// record best finest data
		Individual best = population.getFitness();
		System.out.println(	best.getWeight(0) + " " + best.getWeight(1) + " " + 
							best.getWeight(2) + " " + best.getWeight(3));
		FitnessCal.writeToCSV(best);
	}
	
	public static void main (String[] args) {
		try {
			database1 = args[0];
			database2 = args[1];
			// initialize a population and randomize their genes
			randomizePopulation();
			evolvePopulation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Population {
	private Individual[] individuals;
	private int size;
	
	public Population(int _size, boolean init) {
		size = _size;
		individuals = new Individual[size];
		if (init) {
			//randomize individuals
			for (int i=0; i<size; i++) {
				Individual new_i = new Individual();
				new_i.generateIndividual();
				individuals[i] = new_i;
			}
		}
	}
	
	public Individual getIndividual(int index) {
		return individuals[index];
	}
	
	public double getAverageFitnessScore() {
		double sum = 0.0;
		for (int i=0; i<getSize(); i++) {
			sum += getIndividual(i).getFitness();
		}
		return sum/getSize();
	}
	
	public double getNormalizedFitnessScore() {
		return getAverageFitnessScore()/getFitness().getFitness();
	}
	
	public int getSize() {
		return size;
	}
	
	public void setIndividual(int index, Individual _individual) {
		individuals[index] = _individual;
	} 
	
	// get best fitness 
	public Individual getFitness() {
		Individual result = getIndividual(0);
		for (int i=0; i<getSize(); i++) {
			if (result.getFitness() <= getIndividual(i).getFitness())
				result = getIndividual(i);
		}
		return result;
	}
}

class Individual {
	static int defaultLength = 4; // number of weights
	private double[] weights = new double[defaultLength];
	private double fitness = 0; // fitness, i.e. average score of top 100 matched pairs
	
	// create random individual
	public void generateIndividual() {
		for (int i=0; i<defaultLength; i++) {
			double weight = Math.random();
			weights[i] = weight;
		}
	}
	
	// getter and setter
	public int getSize() {return defaultLength; }
	
	public double getWeight(int index) { return weights[index]; }
	
	public void setWeight(int index, double val) { weights[index] = val; }
	
	public double getFitness() {
		if (fitness == 0)
			fitness = FitnessCal.getFitness(this);
		return fitness;
	}
}

class FitnessCal {
	private static String database1, database2;
	private static Validation validation;
	
	public static void setDatabases(String _db1, String _db2) {
		database1 = _db1;
		database2 = _db2;
		validation = new Validation(database1, database2);
	}
	
	public static double getFitness(Individual individual) {
		List<Double> weightSet = new ArrayList<Double>();
		for (int i=0; i<individual.getSize(); i++) {
			weightSet.add(individual.getWeight(i));
		}
		// calculate score given weight set
		// and extract top 100 highest scores
		List<Double> topHundred = validation.getTopHundred(weightSet);
		// calculate average score of topHundred
		double sum = 0.0;
		for (double val : topHundred) {
			sum += val;
		}
		return sum/topHundred.size();
	} 
	
	public static void writeToCSV(Individual individual) {
		List<Double> weightSet = new ArrayList<Double>();
		for (int i=0; i<individual.getSize(); i++) {
			weightSet.add(individual.getWeight(i));
		}
		validation.writeToCSV(database1, database2, weightSet);
	}
	
	public static void writeTopMatchToCSV(Individual individual) {
		List<Double> weightSet = new ArrayList<Double>();
		for (int i=0; i<individual.getSize(); i++) {
			weightSet.add(individual.getWeight(i));
		}
		validation.writeTopMatchToCSV(database1, database2, weightSet);
	}
}