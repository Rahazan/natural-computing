package pso;

import java.util.ArrayList;

import windfarmapi.WindFarmLayoutEvaluator;

public class Test {

    private WindFarmLayoutEvaluator wfle;
    private ArrayList<double[]> grid;
    
	public Test(WindFarmLayoutEvaluator wfle) {
		this.wfle = wfle;
		this.grid = new ArrayList<double[]>();
		setupGrid();
	}
	
	public void run(){
		double[][] layout = new double[grid.size()][2];
		
		for(int i = 0 ; i < grid.size() ; i++){
			layout[i] = grid.get(i);
		}
		System.out.println("Evaluating");
		
		
		this.evaluate(layout);
	}
	
	private void setupGrid()
	{
		double interval = 8.001 * wfle.getScenario().R;

	    for (double x=0.0; x<wfle.getScenario().width; x+=interval) {
	    	for (double y=0.0; y<wfle.getScenario().height; y+=interval) {
	    		boolean valid = true;
	            	for (int o=0; o<wfle.getScenario().obstacles.length; o++) {
	            		double[] obs = wfle.getScenario().obstacles[o];
	            		if (x>obs[0] && y>obs[1] && x<obs[2] && y<obs[3]) {
	            			valid = false;
	            		}
	            	}

	            	if (valid) {
	            		double[] point = {x, y};
	            		grid.add(point);
	            	}
	          	}
	      }
	}
	
	
	private double evaluate(double[][] layout)
	{
		long time = System.currentTimeMillis();
	    double fitness = wfle.evaluate(layout);
        long timeTaken = System.currentTimeMillis() - time;
        System.out.println("F: " + fitness + ", time taken: " + timeTaken);
        
        return fitness;
	}

}
