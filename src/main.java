import java.io.*;
import java.util.*;

//Justin Ruiz

/* TODO: Optimize memory to allow for #agents > 100000
 * the agent class does not need the entire graph, just the Mark array. can reference static sGraph for other calculations
 * TODO: find balance between user tweaked values to more consistently obtain ratio-optimal results
 */

public class main 
{
	//meta variables
	static String fileName = "Capital_Cities.txt";
	static String begin = "albany,ny";
	static String end = "albany,ny";
	static int prizeGoal = 1000;
	
	//static variables to be tweaked by user
	static final int TRIALS = 1;
	static final int NUM_AGENTS = 10000;
	static final int W = 10; //constant value to update the reward table
	static final double alpha = 1; //learning rate
	static final double gamma = 0.01; //discount factor
	
	//flags for graph (do not touch)
	static final int UNVISITED = 0;
	static final int VISITED = 1;
	static final int LAST_VISIT = 2;
	
	//pre-initialization parameters (do not touch)
	static LinkedList<CityNode> arrCities;
	static Graph sGraph;
	static double[][] Q;
	static int[][] R;
	static int statesCt;
	static int total_prize = 0;
	static double total_wt = 0;
	
	
	public static void main(String[] args) throws IOException
	{
		//variables for time tracking
	    long startTime, endTime;
	    double totalTime;
	    
	    //initialization functions
	    initList();
	    initGraph();
	    initStatics();
	    
	    //algo timing begins
	    startTime = System.nanoTime();
	    
	    //Q-Learning logic
	    learnQ();
	    
	    //Q-Table printout
	    printQ();
	    
	    //final traversal using completed Q-Table
	    traverse();
	    
	    //printouts and timing results
	    System.out.printf("\nTotal distance: %6.2fkm\n", total_wt);
	    System.out.printf("Prize Goal: $%d\n", prizeGoal);
	    System.out.printf("Total Prize: $%d\n", total_prize);
	    System.out.printf("Prize goal to distance ratio: $%.7f/km\n", prizeGoal/total_wt);
	    	
	    endTime = System.nanoTime();
	    totalTime = (double) (endTime - startTime) / 1000000;
	    System.out.println("Algorithm took " + totalTime + "ms to process.");
	    
	}
	
	/*
	 * Documentation for initList()
	 * (1) attempts to read in file (throws error if file not found)
	 * (2) converts text file info into cityNode object and places into array for use in creating graph
	 * (3) optional scanner functionality for user inputted begin and end points
	 * (4) restructure arrayList to make BEGIN city the first node, and END city the last node
	 */
	static void initList() 
	{
		//(1)
		File towns = new File("src/" + fileName);
		arrCities = new LinkedList<>();
		ArrayList<String> nameList = new ArrayList<>();
		
		try 
		{	
			Scanner scan = new Scanner(towns);
			//(2)
			while (scan.hasNextLine())
			{
				String name = scan.next();
				double lat = scan.nextDouble();
				double lon = scan.nextDouble();
				int pop = scan.nextInt();
				
				arrCities.add(new CityNode(name, lat, lon, pop));
				nameList.add(name.toLowerCase());
			}
			scan.close();
		}
		
		catch (FileNotFoundException p)
		{System.out.println("FILE NOT FOUND."); System.exit(0);}
		
		//(3)
		//Scanner userIn = new Scanner(System.in);
		String startCity, endCity;
		
		//optional scanner for user input
		/*
		System.out.println("Please enter the name of the starting city:");
		startCity = userIn.nextLine().toLowerCase();
		
		System.out.println("Please enter the name of the ending city:");
		endCity = userIn.nextLine().toLowerCase();
		*/
		
		startCity = begin;
		endCity = end;
		
		//(4)
		if (nameList.contains(startCity) && nameList.contains(endCity))
		{
			if (startCity.equalsIgnoreCase(endCity))
			{
				int sIndex = nameList.indexOf(startCity);
				CityNode t1 = new CityNode(arrCities.get(sIndex));
				arrCities.remove(sIndex);
				nameList.remove(sIndex);

				arrCities.add(0, t1);
				arrCities.add(t1);
			}
			else
			{
				int sIndex = nameList.indexOf(startCity);
				CityNode t1 = new CityNode(arrCities.get(sIndex));
				arrCities.remove(sIndex);
				nameList.remove(sIndex);
				
				int fIndex = nameList.indexOf(endCity);
				CityNode t2 = new CityNode(arrCities.get(fIndex));
				arrCities.remove(fIndex);
	
				arrCities.add(0, t1);
				arrCities.add(t2);
			}
		}
		
		else
		{
			System.out.println("Cannot find city.");
			System.exit(0);
		}
	}
	
	/*
	 * Documentation for initGraph()
	 * This function simply initializes the graph with requisite flags,
	 * prize and weight values for each node,
	 * and a name for each node (debug purposes only)
	 */
	static void initGraph()
	{
		sGraph = new Graph();
		int size = arrCities.size();
		sGraph.Init(size);
		
		for(int i = 0; i < size; i++)
		{
			sGraph.setMark(i, UNVISITED);
			sGraph.setName(i, arrCities.get(i).name);
			sGraph.setPrize(i, arrCities.get(i).pop);
			for(int j = 0 ; j < size; j++)
				sGraph.setEdge(i, j, CityNode.getDistance(arrCities.get(i), arrCities.get(j)));
		}
		sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
	}
	
	/*
	 * Documentation for initStatics()
	 * This function simply initializes the 2d arrays used for Q-Learning
	 * Reward of edge (i, j), initially -w(i,j)/pv
	 */
	static void initStatics() 
	{
		statesCt = sGraph.n();
		Q = new double[statesCt][statesCt];
		R = new int[statesCt][statesCt];
		
		for (int i = 0; i < statesCt; i++)
			for (int j = 0; j < statesCt; j++)
					R[i][j] = (int) (sGraph.weight(i, j)/sGraph.getPrize(j) * -1);
	}
	
	/*
	 * Documentation for learnQ()
	 * learnQ() is the primary function for the program
	 * 
	 * (1) Random() is called to provide a random number within bounds of the possibleActionsFromState array.
	 * 
	 * (2) Will run as many trials as indicated from the static variables at the top of the program.
	 * 
	 * (3) Initialize the appropriate number of agents and place them into an array of agents (aList)
	 * 
	 * (4) Checks the allQuotaMet function to decide if all agents have reached their prize goal
	 * if not, all agents will step through to a node based on the Q-Learning function until they do
	 * 
	 * (5) Once all agents meet their goal, they will go to the last node in the graph
	 * once there, they will add those weight and prize values to the agent,
	 * calculate the prize/distance ratio for each agent, and set pertinent flags.
	 * 
	 * (6) mostFitIndex() will find the agent with the highest ratio and set them to jStar.
	 * Reward table will then be updated
	 * (NEEDS INSIGHT: a W = 10, and a weight > 10, will result in a value of 0, as per integer division)
	 * 
	 * TODO: Implement line 27 of the research paper.
	 * (NEEDS INSIGHT: Q-Table)
	 */
	
	static void learnQ() 
	{
		//(1)
		Random rand = new Random();
		
		//(2)
		for (int i = 0; i < TRIALS; i++)
		{
			//(3)
			Agent[] aList = new Agent[NUM_AGENTS];
			for (int j = 0; j < NUM_AGENTS; j++)
			{
				Graph newGraph = new Graph(sGraph);
				Agent a = new Agent(statesCt, prizeGoal, newGraph);
				aList[j] = a;
			}
			
			//(4)
			while (!allQuotaMet(aList))
			{
				for (int j = 0; j < NUM_AGENTS; j++)
				{
					Agent aj = aList[j];
					
					aj.actionsFromCurrentState = possibleActionsFromState(aj, aj.curState);
					
					if (aj.total_prize < aj.modGoal)
					{
						aj.index = rand.nextInt(aj.actionsFromCurrentState.length);
						aj.nextState = aj.actionsFromCurrentState[aj.index];
						
						double q = Q[aj.curState][aj.nextState];
						double maxQ = maxQ(aj, aj.nextState);
						int r = R[aj.curState][aj.nextState];
						
						
						double value = q + alpha * (r + gamma * maxQ - q);
						Q[aj.curState][aj.nextState] += value;
						
						aj.indexPath.add(aj.curState);
						aj.total_wt += aj.weight(aj.curState, aj.nextState);
						aj.total_prize += aj.getPrize(aj.nextState);
						
						aj.setAgentMark(aj.curState, VISITED);
						aj.curState = aj.nextState;
					}
				}
			}
			
			//(5)
			for (int j = 0; j < NUM_AGENTS; j++)
			{
				Agent aj = aList[j];
				
				double q = Q[aj.curState][aj.getLastNode()];
				double maxQ = maxQ(aj, aj.getLastNode());
				int r = R[aj.curState][aj.getLastNode()];
				
				double value = q + alpha * (r + gamma * maxQ - q);
				Q[aj.curState][aj.getLastNode()] += value;
				
				aj.indexPath.add(aj.getLastNode());
				aj.total_wt += aj.weight(aj.curState, aj.getLastNode());
				aj.total_prize += aj.getPrize(aj.getLastNode());
				aj.calcRatio();
				
				aj.setAgentMark(aj.getLastNode(), VISITED);
				aj.curState = aj.getLastNode();
			}
			
			//(6)
			int mostFitIndex = findBestRatio(aList);
			
			Agent jStar = aList[mostFitIndex];
			ArrayList<Integer> path = jStar.indexPath;
			
			for (int v = 0; v < path.size()-1; v++)
				R[path.get(v)][path.get(v+1)] += (W/jStar.total_wt);
		}
	}

	/*
	 * Documentation for allQuotaMet()
	 * This function checks if every agent has reached their prelim goal (prizeGoal - prize of last node)
	 * if every agent in aList does, return true, and break out of while loop. else, return false, while loop continues
	 */
	static boolean allQuotaMet(Agent[] aList)
	{
		int trueCounter = 0;
		
		for (int i = 0; i < aList.length; i++)
			if (aList[i].total_prize >= aList[i].modGoal)
				trueCounter++;
		
		if (trueCounter == aList.length)
			return true;
		else
			return false;
	}
	
	/*
	 * Documentation for findBestRatio
	 * This function find the agent in the list with the best prizeGoal/distance ratio
	 * it then returns its index, and becomes agent jStar
	 */
	static int findBestRatio(Agent[] aList)
	{
		double runningHigh = 0;
		int index = 0;
		for (int j = 0; j < aList.length; j++)
		{
			if (aList[j].ratio > runningHigh)
			{
				runningHigh = aList[j].ratio;
				index = j;
			}
		}
		return index;
	}
	
	/*
	 * Documentation for reset()
	 * resets pertinent variables and graph flags for use in final traversal (see traverse())
	 */
	static void reset() 
	{
		total_wt = 0;
		total_prize = 0;
		for (int i = 0; i < statesCt; i++)
			sGraph.setMark(i, UNVISITED);
		sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
	}

	/*
	 * Documentation for possibleActionsFromState()
	 * This function returns an arraylist of all nodes an agent can currently visit from the current node
	 * This does not include its own node, or previously visited nodes
	 */
	static int[] possibleActionsFromState(Agent aj, int curState) 
	{
			ArrayList<Integer> result = new ArrayList<>();
			for (int i = 0; i < statesCt; i++)
	            if (aj.weight(curState, i) != 0 && aj.getMark(i) == UNVISITED)
	                result.add(i);
	
	        return result.stream().mapToInt(i -> i).toArray();
	}
	
	/*
	 * Documentation for maxQ
	 * This function should limit the growth of Q-Values, unsure if working properly but no issues thus far
	 */
	static double maxQ(Agent aj, int nextState)
	{
        int[] actionsFromState = possibleActionsFromState(aj, nextState);
        //the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = -10;
        for (int nextAction : actionsFromState) 
        {
            double value = Q[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }
	
	/*
	 * Documentation for printQ
	 * Simply prints the completed Q-Table values
	 */
	static void printQ() {
        System.out.println("Q matrix");
        for (int i = 0; i < Q.length; i++) {
            System.out.print("From state " + i + ":  ");
            for (int j = 0; j < Q[i].length; j++) {
                System.out.printf("%-15.2f ", (Q[i][j]));
            }
            System.out.println("\n");
        }
    }
	
	/*
	 * Documentation for traverse() and DFSGreed_P()
	 * This function handles the final traversal using the Q-Table as reference
	 * DFSGreed_P finds the index of the largest Q-Value in the table for the current node
	 * then visits it, and repeats until the prizeGoal is met.
	 * Includes printouts for debugging
	 */
	private static void traverse() 
	{
		reset();
		DFSGreed_P(0);
	}
	
	static void DFSGreed_P(int v) 
	{
		sGraph.setMark(v, VISITED);
		int index = getHighestQ(v);
		int x = sGraph.first(v);
		
		while (x < sGraph.n())
		{
			if (total_prize < (prizeGoal - sGraph.getPrize(sGraph.getLastNode()))  && sGraph.getMark(index) == UNVISITED)
			{
				System.out.printf("Going from %-18s to %-18s was %-5.2fkm collecting $%-5d with a ratio of $%.7f/km\n", sGraph.getName(v), sGraph.getName(index), sGraph.weight(v, index), sGraph.getPrize(index), sGraph.getPrize(index)/sGraph.weight(v, index));
				total_wt += sGraph.weight(v, index);
				total_prize += sGraph.getPrize(index);
				DFSGreed_P(index);
			}
			else if (x == sGraph.getLastNode() && sGraph.getMark(sGraph.getLastNode()) == LAST_VISIT)
			{
				sGraph.setMark(sGraph.getLastNode(), VISITED);
				System.out.printf("Going from %-18s to %-18s was %-5.2fkm collecting $%-5d with a ratio of $%.7f/km\n", sGraph.getName(v), sGraph.getName(x), sGraph.weight(v, x), sGraph.getPrize(x), sGraph.getPrize(x)/sGraph.weight(v, x));
				total_wt += sGraph.weight(v, x);
				total_prize += sGraph.getPrize(x);
				break;
			}
			
			x = sGraph.next(v, x);
		}
	}

	/*
	 * Documentation for getHighestQ()
	 * This function finds the index of the largest Q-Table value for the current node (v) and returns it.
	 * It makes sure to exclude VISITED nodes, and its own node.
	 */
	private static int getHighestQ(int v) 
	{
		int runningHigh = Integer.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < Q[v].length; i++)
			if (Q[v][i] > runningHigh && sGraph.getMark(i) == UNVISITED && Q[v][i] != 0)
			{
				runningHigh = (int) Q[v][i];
				index = i;
			}
		
		return index;
	}
}