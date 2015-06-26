package pso;

import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;

import windfarmapi.WindFarmLayoutEvaluator;




public class PSO {

	public enum Action{
		Nothing, //Do not remove or add particle
		Remove, //Remove the worst particle
		Add //Add a new particle (Attempt, may fail if no more fit)
	}
	
    private WindFarmLayoutEvaluator evaluator;
    private World world;
    private ParticleFactory particleFactory;
    private Plotter plotter;
    private double best;
    
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
		this.best = Double.MAX_VALUE;
		
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

	public void newBest(double score)
	{
		System.out.println("New best"); 
		for(Particle part : particles)
			part.newGlobalBest();
	}
	
	public void run(){
		
		System.out.println("Initializing particles") ;
		particles = findStartLayout(nParticles ,100);
		setupVelocities();
	
		//Add particles to the physics world
		for(Particle party: particles)
			world.addBody(party);
		
		System.out.println("Initializing velocities");
		
		WorldCreator.setupWorld(world, evaluator);
		
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).setLinearVelocity(velocities.get(i));
		}

		System.out.println("Starting swarm with size: " + particles.size());
		
		for(int i = 0; i < 400; i++) {

		//The score in the previous iteration
		double previousScore = Double.MAX_VALUE;
		
		Action previousAction = Action.Nothing;
		
			
			boolean validPositions = true;
			
			
			//Multiple physics updates to be able to resolve more complex collisions
			//But also enables for more movement to happen.
			for(int updateCount = 0; updateCount < 80 || !validPositions; updateCount++) {
				
				this.world.update(0.1699);
				updateVelocities();
				gui.update(particles);
				validPositions = this.evaluator.checkConstraint(particlesToLayout(particles));
				
				//After 5000 timesteps in the physics world, give up resolution
				//and start anew. This only happens very rarely.
				if(updateCount > 5000) {
					System.out.println("REMOVING ALL PARTICLES, GIVING UP RESOLUTION");
					for(Particle p: particles) {
						world.removeBody(p);
					}
					particles.clear();
					particleFactory.addParticles(particles, nParticles, world);
					validPositions = true;
				}
			}
			
			
			System.out.println("Evaluating " + i) ;
			double score = 0.0;		
			score = this.evaluate(particles);
			
			
			if (score != Double.MAX_VALUE && score != 0.0) { //Valid score?
				if(score <= this.best) {
					newBest(score);
					this.best = score;
				}

				plotter.addDataPoint(i,score*1000);
				
				//Get individual fitness from evaluator, update particles with this fitness
				double[] turbineFitnesses = evaluator.getTurbineFitnesses();
		        for(int q = 0; q < turbineFitnesses.length; q++) {
		        	if(q < particles.size())
		        	{
			        	double fit = turbineFitnesses[q];
			        	particles.get(q).newEval(fit);
		        	}
		        }
		        
		        boolean scoreImproved = score < previousScore;
		        previousScore = score;
		        
		        //Default is to repeat previous action
		        Action action = previousAction;
		        
		        //Switch action if score became worse
		        if (!scoreImproved) { 
			        switch(previousAction)  {
			        	case Nothing:
			        		//Random action
			        		action = rand.nextBoolean() ? Action.Add : Action.Remove;
			        		break;
			        	case Add:
			        		action = Action.Nothing;
			        		break;
			        	case Remove:
			        		action = Action.Nothing;
			        		break;
			        }
		        }
		        
		        //Execute action
		        switch(action)  {
		        	case Nothing: //Do nothing
		        		System.out.println("Not adding or removing particles");
		        		
		        		break;
		        		
		        	case Add: //Add new particle
		        		
		        		System.out.println("Adding new particle");
		        		
		        		particleFactory.addParticles(particles, 1, world);
		        		break;
		        		
		        	case Remove: //Remove worst particle
		        		
		        		System.out.println("Removing worst particle");
		        		
		        		//Sort the particles by score
		        		@SuppressWarnings("unchecked")
						ArrayList<Particle> sorted = (ArrayList<Particle>) particles.clone();
			        	sorted.sort(new ParticleComparator());
			        	
			        	//Remove the first (the worst)
			        	world.removeBody(sorted.get(0));
		        		particles.remove(sorted.get(0));
		        		break;
		        }
		        
		        previousAction = action;
		        
				
			}
			
		}
		
	}
	
	
	private void updateVelocities(){
		double best = -1;
		Vector2 bestPos = new Vector2(0,0);
		for(Particle party : particles)
		{
			if(party.getScore() >= best)
			{
				best = party.getScore();
				bestPos = party.getPosition();
			}
		}
		
		
		for(int i = 0 ; i < particles.size() ; i++)
			particles.get(i).updateVelocity(particles, i, bestPos);

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
	


}
