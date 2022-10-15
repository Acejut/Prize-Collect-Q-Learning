import java.io.*;
import java.util.*;

//Justin Ruiz

/* TODO: Confirm the algorithm is functioning as expected */

public class main 
{
	static String fileName = "Capital_Cities.txt";
	
	static final int UNVISITED = 0;
	static final int VISITED = 1;
	
	static final int TRIALS = 100000;
	static final double alpha = 1;
	static final double gamma = 0.01;
	
	static CityNode[] arrCities = null;
	static Graph sGraph;
	static double[][] Q;
	static int[][] R;
	static int statesCt;
	static int prizeGoal = 2518;
	static int total_prize = 0;
	static double total_wt = 0;
	
	
	public static void main(String[] args) throws IOException
	{
		Graph G = new Graph();
		
		arrCities = createCityArr(arrCities);
	    
	    long startTime, endTime;
	    double totalTime;
	    	
	    sGraph = createGraph(G, arrCities);
	    initStatics();
	    	
	    startTime = System.nanoTime();
	    
	    learnQ();
	    
	    printQ();
	    
	    traverse();
	    
	    System.out.printf("Total distance: %6.2fkm\n", total_wt);
	    System.out.printf("Prize Goal: $%6d\n", prizeGoal);
	    System.out.printf("Total Prize: $%6d\n", total_prize);
	    System.out.printf("Prize to distance ratio: $%.7f/km\n", total_prize/total_wt);
	    	
	    endTime = System.nanoTime();
	    totalTime = (double) (endTime - startTime) / 1000000;
	    System.out.println("Algorithm took " + totalTime + "ms to process.");
	    
	}

	static CityNode[] createCityArr(CityNode[] arr) 
	{
		int i = 0;
		File towns = new File("src/" + fileName);
		
		try 
		{
			Scanner firstScan = new Scanner(towns);
			
			firstScan.nextLine();
			while (firstScan.hasNextLine())
			{
				firstScan.nextLine();
				i++;
			}
			firstScan.close();
			
			arr = new CityNode[i+1];
			i = 0;
			
			Scanner scan = new Scanner(towns);
			
			while (scan.hasNextLine())
			{
				String name = scan.next();
				double lat = scan.nextDouble();
				double lon = scan.nextDouble();
				int pop = scan.nextInt();
				
				arr[i] = new CityNode(name, lat, lon, pop);
				i++;
			}
			scan.close();
			
			Scanner userIn = new Scanner(System.in);
			String startCity = "";
			boolean found = false;
			
			System.out.println("Please enter the name of the starting city:");
			startCity = userIn.nextLine();
			
			for (int j = 0; j < arr.length; j++)
			{
				if (startCity.equalsIgnoreCase(arr[j].name))
				{
					found = true;
					CityNode temp1 = arr[j];
					CityNode temp2 = arr[0];
					
					arr[0] = temp1;
					arr[j] = temp2;
				}
			}
			
			if (!found)
			{
				System.out.println("City not found.");
				System.exit(0);
			}
		}
		catch (FileNotFoundException p)
		{
			System.out.println("FILE NOT FOUND.");
			System.exit(0);
		}
		
		return arr;
	}
	
	static Graph createGraph(Graph G, CityNode[] arrCities)
    {
		int numNodes = arrCities.length;
		
		G.Init(numNodes);
		
		for (int i = 0; i < numNodes; i++)
		{
			G.setMark(i, UNVISITED);
			G.setName(i, arrCities[i].name);
			G.setPrize(i, arrCities[i].pop);
		}
		
		for(int i = 0; i < numNodes; i++)
			for(int j = 0 ; j < numNodes; j++)
			{
				double weight = CityNode.getDistance(arrCities[i], arrCities[j]);
				G.setEdge(i, j, weight);
			}
		return G;
    }
	
	static void initStatics() 
	{
		statesCt = sGraph.n();
		Q = new double[statesCt][statesCt];
		R = new int[statesCt][statesCt];
		
		for (int i = 0; i < statesCt; i++)
			for (int j = 0; j < statesCt; j++)
				sGraph.setEdge(j, j, 0);
		
		resetR();
	}
	
	static void resetR()
	{
		for (int i = 0; i < statesCt; i++)
			for (int j = 0; j < statesCt; j++)
					R[i][j] = (int) sGraph.weight(i, j)/sGraph.getPrize(j) * -1;
					//R[i][j] = (int) sGraph.weight(i, j) * -1;
	}
	static void learnQ() 
	{
		Random rand = new Random();
		int curState = rand.nextInt(statesCt);
		
		for (int i = 0; i < TRIALS; i++)
		{
			reset();
			int j = 0;
			ArrayList<Integer> indexPath = new ArrayList<>();
			while (j < statesCt)
			{
				resetR();
				int[] actionsFromCurrentState = possibleActionsFromState(curState);
				if (actionsFromCurrentState.length == 0 || total_prize >= prizeGoal)
				{
					indexPath.add(curState);
					/*
					double tripWeight = total_prize/(total_wt*j);
					for (int k = 0; k < j; k++) //statesCt-1
						Q[indexPath.get(k)][indexPath.get(k+1)] -= tripWeight;
					*/
					break;
				}
				else
				{
					int index = rand.nextInt(actionsFromCurrentState.length);
					int nextState = actionsFromCurrentState[index];
					
					double q = Q[curState][nextState];
					double maxQ = maxQ(nextState);
					int r = R[curState][nextState];
					
					double value = q + alpha * (r + gamma * maxQ - q);
					Q[curState][nextState] += value;
					
					indexPath.add(curState);
					total_wt += sGraph.weight(curState, nextState);
					total_prize += sGraph.getPrize(nextState);
					sGraph.setMark(curState, VISITED);
					curState = nextState;
					j++;
				}
				
			}
		}
	}

	static void reset() 
	{
		total_wt = 0;
		total_prize = 0;
		for (int i = 0; i < statesCt; i++)
			sGraph.setMark(i, UNVISITED);
	}

	static int[] possibleActionsFromState(int curState) 
	{
		ArrayList<Integer> result = new ArrayList<>();
		for (int i = 0; i < statesCt; i++)
            if (R[curState][i] != 0 && sGraph.getMark(i) != VISITED)
                result.add(i);

        return result.stream().mapToInt(i -> i).toArray();
	}
	
	static double maxQ(int nextState) 
	{
        int[] actionsFromState = possibleActionsFromState(nextState);
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
	
	private static void traverse() 
	{
		reset();
		DFSGreed_P(0);
	}
	
	static void DFSGreed(int v) 
	{
		sGraph.setMark(v, VISITED);
		int index = getHighestQ(v);
		int x = sGraph.first(v);
		
		while (x < sGraph.n())
		{
			if (sGraph.getMark(index) == UNVISITED)
			{
				System.out.printf("Going from %-18s to %-18s was %-2.2fkm\n", sGraph.getName(v), sGraph.getName(index), sGraph.weight(v, index));
				total_wt += sGraph.weight(v, index);
				DFSGreed(index);
			}
			else if (sGraph.getMark(x) == UNVISITED)
			{
				System.out.printf("Going from %-18s to %-18s was %-2.2fkm\n", sGraph.getName(v), sGraph.getName(index), sGraph.weight(v, x));
				DFSGreed(x);
				total_wt += sGraph.weight(v, x);
			}
			/*else if (x == sGraph.getLastNode() && sGraph.getMark(sGraph.getLastNode()) != LAST_VISIT)
			{
				sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
				System.out.printf("Going from %-18s to %-18s was %-2.2fkm\n", arr[v].name, arr[x].name, sGraph.weight(v, x));
				total_wt += sGraph.weight(v, x);
			}*/
			
			x = sGraph.next(v, x);
		}
	}
	
	static void DFSGreed_P(int v) 
	{
		sGraph.setMark(v, VISITED);
		int index = getHighestQ(v);
		int x = sGraph.first(v);
		
		while (x < sGraph.n())
		{
			if ((sGraph.getMark(index) == UNVISITED) && (total_prize < prizeGoal))
			{
				System.out.printf("Going from %-18s to %-18s was %-5.2fkm collecting $%-5d with a ratio of $%.7f/km\n", sGraph.getName(v), sGraph.getName(index), sGraph.weight(v, index), sGraph.getPrize(index), sGraph.getPrize(index)/sGraph.weight(v, index));
				total_wt += sGraph.weight(v, index);
				total_prize += sGraph.getPrize(index);
				DFSGreed_P(index);
			}
			else
				break;
			
			x = sGraph.next(v, x);
		}
	}

	private static int getHighestQ(int v) 
	{
		int runningHigh = Integer.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < Q[v].length; i++)
			if (Q[v][i] > runningHigh && sGraph.getMark(i) == 0)
			{
				runningHigh = (int) Q[v][i];
				index = i;
			}
		
		return index;
	}
}