import java.util.*;

class Agent 
{
	public int total_prize;
	public int statesCt;
	public int curState;
	public int index;
	public int nextState;
	public int r;
	public double total_wt;
	public double q;
	public double maxQ;
	public double value;
	public double ratio = 0;
	public ArrayList<Integer> indexPath;
	public int[] actionsFromCurrentState;
	public double[][] Q;
	
	public Agent(int statesCt)
	{
		this.statesCt = statesCt;
		total_prize = 0;
		curState = 0;
		total_wt = 0;
		indexPath = new ArrayList<Integer>();
		Q = new double[statesCt][statesCt];
		
	}
	
	public void calcRatio(int prizeGoal)
	{
		ratio = prizeGoal/total_wt;
	}
}