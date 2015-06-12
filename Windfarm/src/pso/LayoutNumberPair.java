package pso;

public class LayoutNumberPair {
	
	private int nr_turbines;
	private double fitness;
	
	public  LayoutNumberPair(int nr_turbines, double fitness){
		this.nr_turbines = nr_turbines;
		this.fitness = fitness;
	}

	public int getNr_turbines() {
		return nr_turbines;
	}

	public double getFitness() {
		return fitness;
	}


	
}
