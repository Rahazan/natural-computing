package pso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import windfarmapi.WindFarmLayoutEvaluator;
import windfarmapi.WindScenario;

public class Test {

    private WindFarmLayoutEvaluator wfle;
    private ArrayList<double[]> particles;
    private int nParticles = 500;
    private GUI gui;
    private Random rand;
    
	public Test(WindFarmLayoutEvaluator wfle, WindScenario ws) {
		this.wfle = wfle;
		this.particles = new ArrayList<double[]>();
		rand = new Random();
		setupParticles(nParticles);
		gui = new GUI(ws);
	}
	
	public void run(){
		
		for(int i = 0; i < 10; i++) {
			setupParticles(nParticles);
			double[][] layout = particlesToLayout(particles);
			gui.update(layout);
			System.out.println("Evaluating " + i + "     " + layout.length) ;
			this.evaluate(layout);
		}
		
		
	}
	
	/**
	 * Converts list of particles to layout[][] for evaluation.
	 */
	private double[][] particlesToLayout(ArrayList<double[]> parts) {
		double[][] layout = new double[particles.size()][2];
		
		for(int j = 0 ; j < particles.size() ; j++){
			layout[j] = particles.get(j);
		}
		return layout;
	}
	
	
	private void setupParticles(int n){
		double minDistance = 8.001 * wfle.getScenario().R;
		particles.clear();
		for (int i=0; i<n; i++) {
			
		    	double[] party = null;
		    	boolean valid = false;
		    	
		    	
		    	int nFailures = 0;
		    	while(!valid) {
		    		valid = true;
		    		double x = rand.nextDouble()*wfle.getScenario().width;
			    	double y = rand.nextDouble()*wfle.getScenario().height;
		    		party = new double[]{x,y};
		    		
		    		//Check whether particle is too close to other particles
		    		for (double[] otherParticle: particles) {
		    			if(dist(party, otherParticle) < minDistance) {
		    				valid = false;
		    				break;
		    			}
		    		}
		    		
		    		for (int o=0; o<wfle.getScenario().obstacles.length; o++) {
	            		double[] obs = wfle.getScenario().obstacles[o];
	            		if (x>obs[0] && y>obs[1] && x<obs[2] && y<obs[3]) {
	            			valid = false;
	            			break;
	            		}
	            	}
		    		
		    		if (valid) {
		    			particles.add(party);
		    			nFailures++;
		    		}
		    		else {
		    			nFailures++;
		    		}
		    		
		    		if (nFailures > 200) {
				    	break;
				    }
				    	
		    	}
		    	
		    	if (nFailures > 200) {
			    	System.out.println(nFailures + " failures in randomply placing new particle,\nGiving up after " + i + " particles");
			    	break;
			    }
	    	
		}
	}
	

	private double dist(double[] a, double[] b) {
		return Math.sqrt(  Math.pow(a[0]-b[0],2.0) +  Math.pow(a[1]-b[1],2.0)   );
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
