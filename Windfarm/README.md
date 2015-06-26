##Running the Windfarm PSO
To run the code perform the following steps

- Unzip
- This unzipped folder is an Eclipse project, import this into your Eclipse workspace. 

Alternatively, to compile and run without Eclipse, add `dyn4j-3.1.11.jar`,`jcommon-1.0.23.jar`,`jfreechart-1.0.19.jar` and `json-20141113.jar` to your classpath when compiling.
- Edit the `userToken` variable in `src/windfarmapi/main.java` to reflect your user token for the competition. This is necessary to identify yourself for the online evaluator and (automatic) submission to leaderboard. Our own user token is currently present, but please provide your own (windflo.com/register).
- Run the main class (`src/windfarmapi/main.java`).

Plots are created in the `./plots/` folder and are updated every 10 iterations.

### Troubleshooting
- Eclipse version: A 64-bit version of Eclipse was used. This should not matter too much.
- Java version: We compiled our code against the JavaSE-1.8 execution environment (Java 8 I believe).
