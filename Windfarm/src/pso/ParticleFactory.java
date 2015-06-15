package pso;

import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Vector2;

import windfarmapi.WindFarmLayoutEvaluator;

public class ParticleFactory {
	
	private WindFarmLayoutEvaluator evaluator;
	private Random rand;
	private double distanceTreshold = Double.MAX_VALUE;
	private double maxPossibleDistance = 0.0;
	

	public ParticleFactory(WindFarmLayoutEvaluator evaluator) {
		this.evaluator = evaluator;
		rand = new Random();
		maxPossibleDistance = Math.sqrt(Math.pow(evaluator.getFarmWidth(),2) +  Math.pow(evaluator.getFarmHeight(),2));
		distanceTreshold = 0.05*maxPossibleDistance;
	}
	
	

	public void addParticles(ArrayList<Particle> particles, int n) {
		double minDistance = 4.025 * evaluator.getTurbineRadius();
		for (int i=0; i<n; i++) {
			
		    	Particle party = null;
		    	boolean valid = false;
		    	
		    	
		    	int nFailures = 0;
		    	while(!valid) {
		    		valid = true;
		    		double x = rand.nextDouble()*evaluator.getFarmWidth();
			    	double y = rand.nextDouble()*evaluator.getFarmHeight();
			    	
			    	party = createParticle(x,y);
			    	
		    		//Check whether particle is too close to other particles
		    		for (Body otherParticle: particles) {
		    			if(otherParticle.getTransform().getTranslation().distance(party.getTransform().getTranslation()) < minDistance*2) {
		    				valid = false;
		    				break;
		    			}
		    		}
		    		
		    		for (int o=0; o<evaluator.getObstacles().length; o++) {
	            		double[] obs = evaluator.getObstacles()[o];
	            		if (x>obs[0] && y>obs[1] && x<obs[2] && y<obs[3]) {
	            			valid = false;
	            			break;
	            		}
	            	}
		    		
		    		if (valid) {
		    			particles.add(party);
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
	
	private Particle createParticle(double x, double y) {
		double minDistance = 4.035 * evaluator.getTurbineRadius();
		
		Particle party = new Particle(distanceTreshold, maxPossibleDistance);
    	Circle circle = new Circle(minDistance);
		party.addFixture(circle);
		party.translate(x,y);
		party.setMass(new Mass(new Vector2(), 200, 0.0));
		BodyFixture fixture = party.getFixtures().get(0);
		fixture.setRestitution(0.5);
		fixture.setFriction(0.0);
		
		return party;
	}
	
	
	
}
