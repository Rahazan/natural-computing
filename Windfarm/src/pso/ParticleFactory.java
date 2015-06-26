package pso;

import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Vector2;

import windfarmapi.WindFarmLayoutEvaluator;


/**
 * Creates particles and adds these to the (physics) world
 *
 */
public class ParticleFactory {
	
	private static final int N_PLACE_RETRY = 200;
	private static final double THRESHOLD_DIAGONAL_RATIO = 0.05;
	private WindFarmLayoutEvaluator evaluator;
	private Random rand;
	
	/**
	 * The threshold above which particles do not interact with eachother.
	 * (Initialized in constructor)
	 */
	private double distanceTreshold = Double.MAX_VALUE;
	
	/**
	 * The diagonal of the scenario
	 */
	private double maxPossibleDistance;


	public ParticleFactory(WindFarmLayoutEvaluator evaluator ) {
		this.evaluator = evaluator;
		rand = new Random();
		
		// Calculate diagonal of the scenario
		maxPossibleDistance = Math.sqrt(Math.pow(evaluator.getFarmWidth(),2) +  Math.pow(evaluator.getFarmHeight(),2));
		distanceTreshold = THRESHOLD_DIAGONAL_RATIO*maxPossibleDistance;
	}
	
	public void addParticles(ArrayList<Particle> particles, int n) {
		addParticles(particles, n, null);
	}

	/**
	 * Attempt to add n particles to the world.
	 * Will attempt to randomly place particle N_PLACE_RETRY times
	 * In case of failure gives up.
	 */
	public void addParticles(ArrayList<Particle> particles, int n, World world) {
		double minDistance = 4.05 * evaluator.getTurbineRadius();
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
		    		
		    		//Check whether particle is in obstacle
		    		for (int o=0; o<evaluator.getObstacles().length; o++) {
	            		double[] obs = evaluator.getObstacles()[o];
	            		if (x>obs[0] && y>obs[1] && x<obs[2] && y<obs[3]) {
	            			valid = false;
	            			break;
	            		}
	            	}
		    		
		    		if (valid) {
		    			particles.add(party);
		    			if (world != null) world.addBody(party);
		    		}
		    		else {
		    			nFailures++;
		    		}
		    		
		    		if (nFailures > N_PLACE_RETRY) {
				    	break;
				    }
				    	
		    	}
		    	
		    	if (nFailures > N_PLACE_RETRY) {
			    	System.out.println(nFailures + " failures in randomly placing new particle,\nGiving up after " + i + " particles");
			    	break;
			    }
	    	
		}
	}
	
	/**
	 * Create particle and it's rigid body at given x, y
	 */
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
