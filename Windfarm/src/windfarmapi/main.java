package windfarmapi;

import pso.PSO;

public class main {

  public static void main(String argv[]) {
      try {
	  String userToken = new String("6YSEODYBB4XBTY332MARA0PUCZ0JG3");
	  String runToken = new String("OCUAYFGN7Q78WFF7MPQVBF157WITFY");
	  for (int sc = 0; sc < 5; sc++) {
	      // Create the competition evaluator
	      CompetitionEvaluator eval = new CompetitionEvaluator();
	      // initialize the evalutor with the scenario id (from 0 to 4), with you user token (see you account online) and the run token. For the first run, ommit the run token, the server will send you (and display) a new one. This run token can be recovered with the getRunToken() method from CompetitionEvaluator
	      if (sc == 0 && runToken == null) {
			  eval.initialize(sc, userToken);
			  runToken = eval.getRunToken();
	      } else {
	    	  eval.initialize(sc, userToken, runToken);
	      }
	      for(int i = 0; i < 1; i++) {
	    	  PSO algorithm = new PSO(eval);
	      	algorithm.run();
	      }
	  }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
