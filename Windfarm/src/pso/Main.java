package pso;

import windfarmapi.WindScenario;

public class Main {

	public static void main(String[] args) {
		
		//Testing testert = new Testing();

		//try {
			//testert.testMax();
		//} catch (Exception e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
			
        WindScenario ws;
		try {
			ws = new WindScenario("Scenarios/obs_00.xml");		
		    PSO test = new PSO(ws);
		    test.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
      
	}

}
