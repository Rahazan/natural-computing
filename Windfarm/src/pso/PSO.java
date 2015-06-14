package pso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Mass.Type;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import windfarmapi.CompetitionEvaluator;
import windfarmapi.KusiakLayoutEvaluator;
import windfarmapi.WindFarmLayoutEvaluator;
import windfarmapi.WindScenario;

public class PSO {

    private WindFarmLayoutEvaluator evaluator;

    private World world;
    
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
		String simulationName = System.currentTimeMillis()+"";
		
		//setupMass();
		
		//Add particles to the physics world
		for(Particle party: particles)
			world.addBody(party);
		
		System.out.println("Initializing velocities") ;
		
		setupWalls();
		setupObstacles();
		
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).setLinearVelocity(velocities.get(i));
		}

		System.out.println("Starting swarm with size: " + particles.size());
		
		XYSeries series = new XYSeries("XYGraph");
		
		for(int i = 0; i < 100; i++) {
			
			boolean validPositions = true;
			
			//Multiple physics updates to be able to resolve more complex collisions
			for(int updateCount = 0; updateCount < 80 || !validPositions; updateCount++) {
				this.world.update(800.0);
				updateVelocities();
				gui.update(particles);
				validPositions = this.evaluator.checkConstraint(particlesToLayout(particles));
			}

			//gui.update(particles);
			
			System.out.println("Evaluating " + i) ;
			double score = 0.0;		
			score = this.evaluate(particles);
			
			if (score != Double.MAX_VALUE && score != 0.0) { //Valid score?
					
				series.add(i,score*1000);
				
				double[] turbineFitnesses = evaluator.getTurbineFitnesses();
		        for(int q = 0; q < turbineFitnesses.length; q++) {
		        	double fit = turbineFitnesses[q];
		        	particles.get(q).newEval(fit);
		        }
		        
		        //Remove worst particle
		        if(particles.size() > 50 && i %15 == 0) {
		        	int index = PSO.smallestIndex(turbineFitnesses);
		        	System.out.println("Removing worst turbine with fitness " + turbineFitnesses[index]);
		        	particles.remove(index);
		        }
		        else {
		        	int index = PSO.smallestIndex(turbineFitnesses);
		        	particles.get(index).newEval(particles.get(index).getScore()/100);
		        }
				
			}
			
			
			if (i % 10 == 0) { //Plot every 10 iterations
				// Add the series to your data set
				XYSeriesCollection dataset = new XYSeriesCollection();
				dataset.addSeries(series);
				// Generate the graph
				JFreeChart chart = ChartFactory.createXYLineChart(
					"", // Title
					"time", // x-axis Label
					"fitness*1000", // y-axis Label
					dataset, // Dataset
					PlotOrientation.VERTICAL, // Plot Orientation
					false, // Show Legend
					true, // Use tooltips
					false // Configure chart to generate URLs?
				);
				XYPlot xyPlot = (XYPlot) chart.getPlot();
				((NumberAxis)xyPlot.getRangeAxis()).setAutoRangeIncludesZero(false);
				
				
				try {
					ChartUtilities.saveChartAsJPEG(new File("chart"+simulationName+".jpg"), chart, 500, 300);
				} catch (IOException e) {
					System.err.println("Problem occurred creating chart.");
				}
			}
		}
		
	}
	
	
	private void updateVelocities(){
		for(int i = 0 ; i < particles.size() ; i++) {
			
			Particle part1 = particles.get(i);
			Vector2 resultingForce = new Vector2(0.0,0.0);
			
			
			for(int j = 0 ; j < particles.size() ; j++)
			{
				if(i!=j)
				{
					Particle part2 = particles.get(j);
					Vector2 delta = part1.getPosition().subtract(part2.getPosition());
					double distance = delta.getMagnitude();
					if (distance > distanceTreshold) {
						continue;
					}
					
					//Power to make closer particles weigh much higher
					double forceScalar = Math.pow(1.0 - distance/this.maxPossibleDistance, 1.5) * 2500;
					
					delta.normalize();
					
					resultingForce.add(delta.multiply(forceScalar));
					
					
				}
				
			}
			//System.out.println(resultingForce);
			part1.applyForce(resultingForce);
		
		}
	}
	
	private ArrayList<Particle> findStartLayout(int n, int n_iter){
		double min = Double.MAX_VALUE;
		ArrayList<Particle> best = null;
		
		for(int i = 0 ; i < n_iter ; i++){
			ArrayList<Particle> current = setupParticles(n);
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
		Particle part = parts.get(0);
		
		for(int j = 0 ; j < parts.size() ; j++){
			layout[j][0] = parts.get(j).getTransform().getTranslationX();
			layout[j][1] = parts.get(j).getTransform().getTranslationY();
		}
		return layout;
	}
	
	
	private void setupWalls() {
		double minDistance = 4 * evaluator.getTurbineRadius();
		
		Body wallNorth = new Body();
		Rectangle rectS = new Rectangle(evaluator.getFarmWidth(), 100);
		wallNorth.addFixture(rectS);
		wallNorth.translate(evaluator.getFarmWidth()*0.5,-50-minDistance);
		world.addBody(wallNorth);
		
		Body wallSouth = new Body();
		Rectangle rectN = new Rectangle(evaluator.getFarmWidth(), 100);
		wallSouth.addFixture(rectN);
		wallSouth.translate(evaluator.getFarmWidth()*0.5,evaluator.getFarmHeight()+50+minDistance);
		world.addBody(wallSouth);
		
		Body wallE = new Body();
		Rectangle rectE = new Rectangle(100, evaluator.getFarmHeight());
		wallE.addFixture(rectE);
		wallE.translate(-50-minDistance,evaluator.getFarmHeight()*0.5);
		world.addBody(wallE);
		
		Body wallW = new Body();
		Rectangle rectW = new Rectangle(100, evaluator.getFarmHeight());
		wallW.addFixture(rectW);
		wallW.translate(evaluator.getFarmWidth()+50+minDistance,evaluator.getFarmHeight()*0.5);
		world.addBody(wallW);
		
	}
	
	private void setupObstacles() {
		double minDistance = 4.0000 * evaluator.getTurbineRadius();
		double duzend = 1000;
		
		for (int o=0; o<evaluator.getObstacles().length; o++) {
    		double[] obs = evaluator.getObstacles()[o];

			double[] obsClone = obs.clone();
			if (obsClone[0] < 1.0) obsClone[0] = -duzend;
			if (obsClone[1] < 1.0) obsClone[1] = -duzend;
			if (obsClone[2] > evaluator.getFarmWidth()-1) obsClone[2] = evaluator.getFarmWidth()+duzend;
			if (obsClone[3] > evaluator.getFarmHeight()-1) obsClone[3] = evaluator.getFarmHeight()+duzend;
			
			
			Body bod = new Body();
			double width = obsClone[2]-obsClone[0]-minDistance*2;
			double height = obsClone[3]-obsClone[1]-minDistance*2;

			Rectangle rect = new Rectangle(width, height);
			bod.addFixture(rect);
			bod.translate(obsClone[0]+0.5*width+minDistance ,obsClone[1]+0.5*height+minDistance );
			world.addBody(bod);
    	}
	}
	
	
	private Particle createParticle(double x, double y) {
		double minDistance = 4.035 * evaluator.getTurbineRadius();
		
		Particle party = new Particle();
    	Circle circle = new Circle(minDistance);
		party.addFixture(circle);
		party.translate(x,y);
		party.setMass(new Mass(new Vector2(), 200, 0.0));
		BodyFixture fixture = party.getFixtures().get(0);
		fixture.setRestitution(0.5);
		fixture.setFriction(0.0);
		
		return party;
	}
	
	
	private ArrayList<Particle> setupParticles(int n){
		double minDistance = 4.025 * evaluator.getTurbineRadius();
		ArrayList<Particle> layout = new ArrayList<Particle>();
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
		    		for (Body otherParticle: layout) {
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
