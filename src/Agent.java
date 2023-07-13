import java.util.*;

class Agent 
{
	public int total_prize;
	public int statesCt;
	public int curState;
	public int index;
	public int nextState;
	public int prizeGoal;
	public int modGoal;
	
	public double total_wt;
	public double ratio;
	
	public ArrayList<Integer> indexPath;
	private Graph newGraph;
	public int[] actionsFromCurrentState;
	
	public Agent(int statesCt, int prizeGoal, Graph newGraph)
	{
		this.statesCt = statesCt;
		this.prizeGoal = prizeGoal;
		this.newGraph = newGraph;
		modGoal = prizeGoal - newGraph.getPrize(newGraph.getLastNode());
		total_prize = 0;
		curState = 0;
		total_wt = 0;
		indexPath = new ArrayList<Integer>();
	}
	
	public double calcRatio()
	{
		return (ratio = prizeGoal/total_wt);
	}
	
	public void setAgentMark(int v, int val)
	{
		this.newGraph.Mark[v] = val;
	}
	
	public double weight(int i, int v)
	{
		return this.newGraph.matrix[i][v];
	}
	
	public int getMark(int v)
	{
		return this.newGraph.Mark[v];
	}
	
	public int getLastNode()
	{
		return (this.newGraph.n()-1);
	}
	
	public int getPrize(int v)
	{
		return this.newGraph.prize[v];
	}
	
	
}