package pso;

import windfarmapi.KusiakLayoutEvaluator;
import windfarmapi.WindScenario;

public class Main {

	public static void main(String[] args) {
        WindScenario ws;
		try {
			ws = new WindScenario("Scenarios/obs_00.xml");		
		    Test test = new Test(ws);
		    test.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        

	}

}
