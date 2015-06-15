package pso;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Settings.ContinuousDetectionMode;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Rectangle;

import windfarmapi.WindFarmLayoutEvaluator;

/**
 * Transforms the physics world into the scenario
 * by adding obstacles and walls in the right places.
 *
 */
public class WorldCreator {
	
	public static void setupWorld(World world, WindFarmLayoutEvaluator evaluator) {
		setupWalls(world, evaluator);
		setupObstacles(world, evaluator);
		world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.ALL);
	}
	
	
	/**
	 * Creates boundaries around the layout
	 */
	private static void setupWalls(World world, WindFarmLayoutEvaluator evaluator) {
		double minDistance = 3 * evaluator.getTurbineRadius();
		
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
	
	private static void setupObstacles(World world, WindFarmLayoutEvaluator evaluator) {
		double minDistance = 3 * evaluator.getTurbineRadius();
		
		//Arbitrary high value that obstacles continue into the walls
		double duzend = 1000;
		
		for (int o=0; o<evaluator.getObstacles().length; o++) {
    		double[] obs = evaluator.getObstacles()[o];

    		//Necessary to close off small gap between obstacles and walls
    		//That particles can slip through otherwise
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
	
	
}
