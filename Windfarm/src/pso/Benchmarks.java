package pso;

import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Circle;

import windfarmapi.KusiakLayoutEvaluator;
import windfarmapi.WindScenario;


/**
 * 
 * This class contains various benchmarks, please disregard.
 * It was used for ad-hoc benchmarking various approaches.
 *
 */

public class Benchmarks {

	Random rand = new Random();
	
	public ArrayList<LayoutNumberPair> randomBenchmark(WindScenario scenario)
	{
		KusiakLayoutEvaluator eval = new KusiakLayoutEvaluator();
		ArrayList<LayoutNumberPair> results = new ArrayList<LayoutNumberPair>();
		eval.initialize(scenario);
		double[][] layout;
		int n;
		double fitness;
		for(int i = 0 ; i < 2000 ; i++)
		{
			n = rand.nextInt(400) + 200;
			layout = randomLayout(n, scenario);
			fitness = eval.evaluate(layout);
			results.add(new LayoutNumberPair(n, fitness));
		}		
			return results;
	}
	
	private double[][] randomLayout(int n, WindScenario scenario)
	{
		double minDistance = 4.03 * scenario.R;
		ArrayList<Particle> layout = new ArrayList<Particle>();
		
		for (int i=0; i<n; i++) {
			
	    	Particle party = null;
	    	boolean valid = false;
	    	
	    	
	    	int nFailures = 0;
	    	while(!valid) {
	    		valid = true;
	    		double x = rand.nextDouble()*scenario.width;
		    	double y = rand.nextDouble()*scenario.height;
		    	
		    	party = new Particle(0,0); //arguments should be different but who cares, we're not using this anyway
		    	Circle circle = new Circle(minDistance);
    			party.addFixture(circle);
    			party.translate(x,y);
    			party.setMass();
    			BodyFixture fixture = party.getFixtures().get(0);
    			fixture.setRestitution(1.0);
    			fixture.setFriction(0.0);
    			
		    	
	    		//Check whether particle is too close to other particles
	    		for (Body otherParticle: layout) {
	    			if(otherParticle.getTransform().getTranslation().distance(party.getTransform().getTranslation()) < minDistance*2) {
	    				valid = false;
	    				break;
	    			}
	    		}
	    		
	    		for (int o=0; o<scenario.obstacles.length; o++) {
            		double[] obs = scenario.obstacles[o];
            		if (x>obs[0] && y>obs[1] && x<obs[2] && y<obs[3]) {
            			valid = false;
            			break;
            		}
            	}
	    		
	    		if (valid) {
//	    			world.addBody(party);
	    			layout.add(party);
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
		return particlesToLayout(layout);

	}
	
	
	public LayoutNumberPair maxFilled(WindScenario scenario){
		
		KusiakLayoutEvaluator eval = new KusiakLayoutEvaluator();
		eval.initialize(scenario);
		ArrayList<double[]> grid = new ArrayList<double[]>();
		
	      double interval = 8.001 * scenario.R;

	      for (double x=0.0; x<scenario.width; x+=interval) {
	          for (double y=0.0; y<scenario.height; y+=interval) {
	              boolean valid = true;
	              for (int o=0; o<scenario.obstacles.length; o++) {
	                  double[] obs = scenario.obstacles[o];
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
	      
	      double [][] layout = new double[grid.size()][2];
	      for(int i = 0 ; i<grid.size() ; i++)
	    	  layout[i]= grid.get(i);
	      
	      double fitness = eval.evaluate(layout);
	      
	      return new LayoutNumberPair(grid.size(), fitness);
		
		
		
		
	}
	
	/**
	 * Converts list of particles to layout[][] for evaluation.
	 */
	private double[][] particlesToLayout(ArrayList<Particle> parts) {
		double[][] layout = new double[parts.size()][2];
		
		for(int j = 0 ; j < parts.size() ; j++){
			layout[j][0] = parts.get(j).getTransform().getTranslationX();
			layout[j][1] = parts.get(j).getTransform().getTranslationY();
		}
		return layout;
	}
	
}
