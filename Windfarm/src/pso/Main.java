package pso;

import windfarmapi.KusiakLayoutEvaluator;
import windfarmapi.WindScenario;

public class Main {

	public static void main(String[] args) {
        WindScenario ws;
		try {
			ws = new WindScenario("Scenarios/obs_00.xml");
			 KusiakLayoutEvaluator wfle = new KusiakLayoutEvaluator();
		     wfle.initialize(ws);
		     Test test = new Test(wfle);
		     test.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        

	}

}
