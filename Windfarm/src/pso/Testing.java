package pso;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import windfarmapi.GA;
import windfarmapi.KusiakLayoutEvaluator;
import windfarmapi.WindScenario;

public class Testing {
	

	public void testGA() throws Exception
	{
		String filepath = "./Scenarios/";
		KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
		GA algorithm = new GA(wfle);
		ArrayList<Double> fitnesses;
		for(int i = 0 ; i < 10 ; i++)
		{
			WindScenario ws = new WindScenario(filepath + "0" + i + ".xml");
			wfle.initialize(ws);
			System.out.println("Running GA on scenario 0" + i);
			long time = System.currentTimeMillis();
			fitnesses = algorithm.run();
			long timeTaken = System.currentTimeMillis() - time;
			writeFitness("./data/ga/", i, false, fitnesses, timeTaken);
			
			//with obstacles
			ws = new WindScenario(filepath + "obs_0" + i + ".xml");
			wfle.initialize(ws);
			System.out.println("Running GA on scenario obs_0" + i);
			time = System.currentTimeMillis();
			fitnesses = algorithm.run();
			timeTaken = System.currentTimeMillis() - time;
			writeFitness("./data/ga/", i, true, fitnesses, timeTaken);
		}	
	}
	
	public void testMax() throws Exception
	{
		String filepath = "./Scenarios/";
		Benchmarks maxBench = new Benchmarks();
		WindScenario ws;
		ArrayList<LayoutNumberPair> results = new ArrayList<LayoutNumberPair>();

		for(int i = 0 ; i < 10 ; i++)
		{
			ws = new WindScenario(filepath + "0" + i + ".xml");
			System.out.println("Running max on scenario 0" + i);
			results.add(maxBench.maxFilled(ws));
		}
		writeFitness("./data/max/all_no_obs.csv", results, 0);
		
		
		results = new ArrayList<LayoutNumberPair>();
		for(int i = 0 ; i < 10 ; i++)
		{
			ws = new WindScenario(filepath + "obs_0" + i + ".xml");
			System.out.println("Running max on scenario obs_0" + i);
			results.add(maxBench.maxFilled(ws));
		}
		writeFitness("./data/max/all_obs.csv", results, 0);
	}
	
	
	public void testRandom() throws Exception
	{
		String filepath = "./Scenarios/";
		Benchmarks randomBench = new Benchmarks();
		WindScenario ws;
		ArrayList<LayoutNumberPair> results;
		long time;
		long timeTaken;
		for(int i = 0 ; i < 10 ; i++)
		{
				ws = new WindScenario(filepath + "0" + i + ".xml");
				System.out.println("Running random on scenario 0" + i);
				time = System.currentTimeMillis();
				results = randomBench.randomBenchmark(ws);
				timeTaken = System.currentTimeMillis() - time;
				
				writeFitness("./data/random/scenario0" + i + ".csv", results, timeTaken);
			
		}
		
		for(int i = 0 ; i < 10 ; i++)
		{
				ws = new WindScenario(filepath + "obs_0" + i + ".xml");
				System.out.println("Running random on scenario obs_0" + i);
				time = System.currentTimeMillis();
				results = randomBench.randomBenchmark(ws);
				timeTaken = System.currentTimeMillis() - time;
				
				writeFitness("./data/random/scenario_obs_0" + i + ".csv", results, timeTaken);
			
		}
		
	}
	
	public void writeFitness(String filepath, ArrayList<LayoutNumberPair> results, long time)
	{
		System.out.println("Writing fitness to file");

		try {
			FileWriter writer = new FileWriter(filepath);
			for(LayoutNumberPair pair : results){
				writer.append(Integer.toString(pair.getNr_turbines()));
				writer.append(',');
				writer.append(Double.toString(pair.getFitness()));
				writer.append("\n");
			}
			writer.append(Long.toString(time));
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
		
	public void writeFitness(String path, int i, boolean obstacles, ArrayList<Double> fitnesses, long time)
	{
		System.out.println("Writing fitness to file");
		String filepath = "";
		if(obstacles){
			filepath = (path + "fitness of obs_0" + i + ".csv");
		}
		else{
			filepath = (path + "fitness of 0" + i + ".csv");
		}
			
		try {
			FileWriter writer = new FileWriter(filepath);
			for(double fit : fitnesses){
				writer.append(Double.toString(fit));
				writer.append("\n");
			}
			writer.append(Long.toString(time));
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}


}
