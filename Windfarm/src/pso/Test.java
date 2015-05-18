package pso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import windfarmapi.WindFarmLayoutEvaluator;
import windfarmapi.WindScenario;

public class Test {

    private WindFarmLayoutEvaluator wfle;
    private WindScenario scenario;
    
    private ArrayList<double[]> particles;
    private GUI gui;

    private ArrayList<double[]> velocities;
    private final int nParticles = 40;
    private final double maxStartVelocity = 10.0;
    private Random rand;
    
	public Test(WindFarmLayoutEvaluator wfle, WindScenario ws) {
		this.wfle = wfle;
		this.scenario = wfle.getScenario();
		this.particles = new ArrayList<double[]>();
		this.velocities = new ArrayList<double[]>();
		rand = new Random();
		gui = new GUI(ws);
	}
	
	private void setupVelocities() {
		
		for(int i = 0; i < particles.size(); i++) {
			double vx = rand.nextDouble() * maxStartVelocity * 2 - maxStartVelocity;
			double vy = rand.nextDouble() * maxStartVelocity * 2 - maxStartVelocity;
			
			double[] vel = {vx,vy};
			velocities.add(vel);
		}
	}

	public void run(){
		
		System.out.println("Initializing particles") ;
		setupParticles(nParticles);
		System.out.println("Initializing velocities") ;
		setupVelocities();
		
		for(int i = 0; i < 10000; i++) {
			updatePositions();
			double[][] layout = particlesToLayout(particles);
			gui.update(layout);
			System.out.println("Update!");
			System.out.println("Evaluating " + i + "     " + layout.length) ;
			this.evaluate(layout);
		}
		
	}
	
	private void updatePositions() {
		for(int i = 0; i < particles.size(); i++) {
			double[] particle = particles.get(i);
			double[] v = velocities.get(i);
			
			//Todo maybe change the velocity
			
			particle[0] = particle[0]+v[0];
			particle[1] = particle[1]+v[1];
			
			if (particle[0] >= scenario.width) {
				particle[0] = scenario.width - (particle[0] - scenario.width);
				v[0] = -v[0];
			}
			
			if (particle[1] >= scenario.height) {
				particle[1] = scenario.height - (particle[1] - scenario.height);
				v[1] = -v[1];
			}
			
			if (particle[0] < scenario.width) {
				particle[0] = -particle[0];
				v[0] = -v[0];
			}
			
			if (particle[1] < scenario.height) {
				particle[1] = -particle[1];
				v[1] = -v[1];
			}
			
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
        wfle.evaluate(layout);
        
        return fitness;
	}

}
