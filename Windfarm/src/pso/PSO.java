package pso;

import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;
import windfarmapi.WindFarmLayoutEvaluator;

public class PSO {

    private WindFarmLayoutEvaluator evaluator;
    private World world;
    private ParticleFactory particleFactory;
    private Plotter plotter;
    
    private GUI gui;

    private ArrayList<Vector2> velocities;
    private final int nParticles = 13337;
    private final double maxStartVelocity = 1000.0;
    private Random rand;
    
    private ArrayList<Particle> particles;
    
    //Diagonal of the scenario
    private double maxPossibleDistance = 0.0;
    
    //Particles further away than this treshold will not influence eachother
    private double distanceTreshold = Double.MAX_VALUE;
    
	
	public PSO(WindFarmLayoutEvaluator eval) {
		this.evaluator = eval;	
		this.velocities = new ArrayList<Vector2>();
		rand = new Random();
		gui = new GUI(eval.getFarmWidth(), eval.getFarmHeight(), eval.getObstacles(), eval.getTurbineRadius());
		particleFactory = new ParticleFactory(eval);
		plotter = new Plotter();
		
		// Physics engine
		world = new World();
		world.setGravity(new Vector2(0.0,0.0));
		particles = new ArrayList<Particle>();
		maxPossibleDistance = Math.sqrt(Math.pow(eval.getFarmWidth(),2) +  Math.pow(eval.getFarmHeight(),2));
		distanceTreshold = 0.05 * maxPossibleDistance;
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
		particles = findStartLayout(nParticles ,1);
		setupVelocities();
		
		//setupMass();
		
		//Add particles to the physics world
		for(Particle party: particles)
			world.addBody(party);
		
		System.out.println("Initializing velocities") ;
		
		WorldCreator.setupWorld(world, evaluator);
		
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).setLinearVelocity(velocities.get(i));
		}

		System.out.println("Starting swarm with size: " + particles.size());
		
		for(int i = 0; i < 200; i++) {
			
			boolean validPositions = true;
			
			//Multiple physics updates to be able to resolve more complex collisions
			for(int updateCount = 0; updateCount < 80 || !validPositions; updateCount++) {
				this.world.update(0.1699);
				updateVelocities();
				gui.update(particles);
				validPositions = this.evaluator.checkConstraint(particlesToLayout(particles));
			}
			
			
			System.out.println("Evaluating " + i) ;
			double score = 0.0;		
			score = this.evaluate(particles);
			
			if (score != Double.MAX_VALUE && score != 0.0) { //Valid score?
					
				plotter.addDataPoint(i,score*1000);
				
				double[] turbineFitnesses = evaluator.getTurbineFitnesses();
		        for(int q = 0; q < turbineFitnesses.length; q++) {
		        	if(q < particles.size())
		        	{
			        	double fit = turbineFitnesses[q];
			        	particles.get(q).newEval(fit);
		        	}
		        }
		        
		        //Remove worst particle
		        if(particles.size() > 50 && i %15 == 0) {
		        	int index = PSO.smallestIndex(turbineFitnesses);
		        	System.out.println("Removing worst turbine with fitness " + turbineFitnesses[index]);
		        	particles.remove(index);
		        	
		        }
				
			}
			
		}
		
	}
	
	
	private void updateVelocities(){
		for(int i = 0 ; i < particles.size() ; i++)
			particles.get(i).updateVelocity(particles, i);
//		for(int i = 0 ; i < particles.size() ; i++) {
//			
//			Particle part1 = particles.get(i);
//			Vector2 repulsiveForce = new Vector2(0.0,0.0);
//			Vector2 resultingForce = new Vector2(0.0,0.0);
//			
//			
//			for(int j = 0 ; j < particles.size() ; j++)
//			{
//				if(i!=j)
//				{
//					Particle part2 = particles.get(j);
//					Vector2 delta = part1.getPosition().subtract(part2.getPosition());
//					double distance = delta.getMagnitude();
//					if (distance > distanceTreshold) {
//						continue;
//					}
//					
//					//Power to make closer particles weigh much higher
//					double forceScalar = Math.pow(1.0 - distance/this.maxPossibleDistance, 1.5) * 2500;
//					
//					delta.normalize();
//					
//					repulsiveForce.add(delta.multiply(forceScalar));
//						
//				}
//				
//			}
//			//System.out.println(resultingForce);
//			resultingForce = repulsiveForce;
//			part1.applyForce(repulsiveForce);
//		
//		}
	}
	
	private ArrayList<Particle> findStartLayout(int n, int n_iter){
		double min = Double.MAX_VALUE;
		ArrayList<Particle> best = null;
		
		for(int i = 0 ; i < n_iter ; i++){
			ArrayList<Particle> current = new ArrayList<Particle>();
			particleFactory.addParticles(current, n);
			
			if (n_iter == 1) { //No need to evaluate if not iterating
				return current;
			}
			
			double currentVal = evaluate(current);
			n+=5;
			if(currentVal <= min){
				best = current;
				min = currentVal;
				gui.update(particles);
			}
		}
		
		return best;
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
	
	
	private double evaluate(ArrayList<Particle> layout)
	{
		long time = System.currentTimeMillis();
		
	    double fitness = evaluator.evaluate(particlesToLayout(layout));

        long timeTaken = System.currentTimeMillis() - time;
        System.out.println("F: " + fitness + ", time taken: " + timeTaken);
        
        
        return fitness;
	}
	
	
    public static int smallestIndex (double[] array) {
    	double currentValue = array[0]; 
    	int smallestIndex = 0;
		for (int j=1; j < array.length; j++) {
			if (array[j] < currentValue) {
				currentValue = array[j];
				smallestIndex = j;
			}
		}
		
		return smallestIndex;
	}
	


}
