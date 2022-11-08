import java.io.*;
import java.util.*;

//Justin Ruiz

/* TODO: Confirm the algorithm is functioning as expected */

public class main 
{
	static String fileName = "Capital_Cities.txt";
	static String begin = "albany,ny";
	static String end = "albany,ny";
	
	static final int UNVISITED = 0;
	static final int VISITED = 1;
	static final int LAST_VISIT = 2;
	
	static final int TRIALS = 500_000;
	static final int NUM_AGENTS = 1;
	static final double alpha = 1;
	static final double gamma = 0.01;
	
	
	static LinkedList<CityNode> arrCities;
	static Graph sGraph;
	static double[][] Q;
	static int[][] R;
	static int statesCt;
	static int prizeGoal = 1500;
	static int total_prize = 0;
	static double total_wt = 0;
	
	
	public static void main(String[] args) throws IOException
	{
	    long startTime, endTime;
	    double totalTime;
	    initList();
	    initGraph();
	    initStatics();
	    	
	    startTime = System.nanoTime();
	    
	    learnQ();
	    
	    printQ();
	    
	    traverse();
	    
	    System.out.printf("\nTotal distance: %6.2fkm\n", total_wt);
	    System.out.printf("Prize Goal: $%d\n", prizeGoal);
	    System.out.printf("Total Prize: $%d\n", total_prize);
	    System.out.printf("Prize goal to distance ratio: $%.7f/km\n", prizeGoal/total_wt);
	    	
	    endTime = System.nanoTime();
	    totalTime = (double) (endTime - startTime) / 1000000;
	    System.out.println("Algorithm took " + totalTime + "ms to process.");
	    
	}

	
	static void initList() 
	{
		File towns = new File("src/" + fileName);
		arrCities = new LinkedList<>();
		ArrayList<String> nameList = new ArrayList<>();
		
		try 
		{	
			Scanner scan = new Scanner(towns);
			
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
		
		Scanner userIn = new Scanner(System.in);
		String startCity, endCity;
		
		/*
		System.out.println("Please enter the name of the starting city:");
		startCity = userIn.nextLine().toLowerCase();
		
		System.out.println("Please enter the name of the ending city:");
		endCity = userIn.nextLine().toLowerCase();
		*/
		
		startCity = begin;
		endCity = end;
		
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
	
	static void initStatics() 
	{
		statesCt = sGraph.n();
		Q = new double[statesCt][statesCt];
		R = new int[statesCt][statesCt];
		
		resetR();
	}
	
	static void resetR()
	{
		for (int i = 0; i < statesCt; i++)
			for (int j = 0; j < statesCt; j++)
					R[i][j] = (int) (sGraph.weight(i, j)/sGraph.getPrize(j) * -3);
	}
	static void learnQ() 
	{
		Random rand = new Random();
		int curState = 0; //rand.nextInt(statesCt);
		
		for (int i = 0; i < TRIALS; i++)
		{
			for (int k = 0; k < NUM_AGENTS; k++)
			{
				int mPrizeGoal = reset();
				int j = 0;
				ArrayList<Integer> indexPath = new ArrayList<>();
				while (j < statesCt)
				{
					int[] actionsFromCurrentState = possibleActionsFromState(curState, false);
					if (actionsFromCurrentState.length == 0 || total_prize >= mPrizeGoal)
					{
						actionsFromCurrentState = possibleActionsFromState(curState, true);
						int index = rand.nextInt(actionsFromCurrentState.length);
						int nextState = actionsFromCurrentState[index];
						
						double q = Q[curState][nextState];
						double maxQ = maxQ(nextState);
						int r = R[curState][nextState];
						
						double value = q + alpha * (r + gamma * maxQ - q);
						Q[curState][nextState] += (value)/10;
						
						indexPath.add(curState);
						total_wt += sGraph.weight(curState, nextState);
						total_prize += sGraph.getPrize(nextState);
						sGraph.setMark(curState, VISITED);
						curState = nextState;
						j++;
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
	}

	static int reset() 
	{
		total_wt = 0;
		total_prize = 0;
		int moddedPrizeGoal = prizeGoal - sGraph.getPrize(sGraph.getLastNode());
		for (int i = 0; i < statesCt; i++)
			sGraph.setMark(i, UNVISITED);
		sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
		
		return moddedPrizeGoal;
	}

	static int[] possibleActionsFromState(int curState, boolean lastVisit) 
	{
		if (!lastVisit)
		{
			ArrayList<Integer> result = new ArrayList<>();
			for (int i = 0; i < statesCt; i++)
	            if (R[curState][i] != 0 && sGraph.getMark(i) == UNVISITED)
	                result.add(i);
	
	        return result.stream().mapToInt(i -> i).toArray();
		}
		else
		{
			ArrayList<Integer> result = new ArrayList<>();
			result.add(sGraph.getLastNode());
			
			return result.stream().mapToInt(i -> i).toArray();
		}
	}
	
	static double maxQ(int nextState) 
	{
        int[] actionsFromState = possibleActionsFromState(nextState, false);
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