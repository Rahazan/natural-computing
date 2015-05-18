package pso;

import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Vector2;

import windfarmapi.WindFarmLayoutEvaluator;
import windfarmapi.WindScenario;

public class Test {

    private WindFarmLayoutEvaluator wfle;
    private WindScenario scenario;
    
    private World world;
    
    private GUI gui;

    private ArrayList<Vector2> velocities;
    private final int nParticles = 40;
    private final double maxStartVelocity = 10.0;
    private Random rand;
    
    private ArrayList<Particle> particles;
    
    
	public Test(WindFarmLayoutEvaluator wfle, WindScenario ws) {
		this.wfle = wfle;
		this.scenario = wfle.getScenario();
		this.velocities = new ArrayList<Vector2>();
		rand = new Random();
		gui = new GUI(ws);
		
		// Physics engine
		world = new World();
		particles = new ArrayList<Particle>();
		
		
	}
	
	private void setupVelocities() {
		
		for(int i = 0; i < particles.size(); i++) {
			double vx = rand.nextDouble() * maxStartVelocity * 2 - maxStartVelocity;
			double vy = rand.nextDouble() * maxStartVelocity * 2 - maxStartVelocity;
			
			Vector2 vel = new Vector2(vx,vy);
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
		
	}
	
	
	
	
	
	/**
	 * Converts list of particles to layout[][] for evaluation.
	 */
	private double[][] particlesToLayout(ArrayList<Particle> parts) {
		double[][] layout = new double[particles.size()][2];
		
		for(int j = 0 ; j < particles.size() ; j++){
			layout[j][0] = particles.get(j).getTransform().getTranslationX();
			layout[j][1] = particles.get(j).getTransform().getTranslationY();
		}
		return layout;
	}
	
	
	private void setupParticles(int n){
		double minDistance = 8.001 * scenario.R;
		particles.clear();
		for (int i=0; i<n; i++) {
			
		    	Particle party = null;
		    	boolean valid = false;
		    	
		    	
		    	int nFailures = 0;
		    	while(!valid) {
		    		valid = true;
		    		double x = rand.nextDouble()*wfle.getScenario().width;
			    	double y = rand.nextDouble()*wfle.getScenario().height;
			    	
			    	party = new Particle();
			    	Circle circle = new Circle(8.00* scenario.R);
	    			party.addFixture(circle);
	    			party.translate(x,y);
			    	
		    		//Check whether particle is too close to other particles
		    		for (Body otherParticle: particles) {
		    			if(otherParticle.getTransform().getTranslation().distance(party.getTransform().getTranslation()) < minDistance) {
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
		    			world.addBody(party);
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
