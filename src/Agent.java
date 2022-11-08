import java.util.*;

class Agent 
{
	public int reward;
	public int dist;
	public ArrayList<Integer> statePath;
	public double[][] agentQ;
	
	public Agent(int statesCt)
	{
		reward = dist = 0;
		agentQ = new double[statesCt][statesCt];
	}
	
	public void addQ(int curState, int nextState, double value)
	{
		agentQ[curState][nextState] += value;
	}
}