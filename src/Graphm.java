/** Source code example for "A Practical Introduction to Data
    Structures and Algorithm Analysis, 3rd Edition (Java)" 
    by Clifford A. Shaffer
    Copyright 2008-2011 by Clifford A. Shaffer
*/
import java.util.*;
/** Graph: Adjacency matrix */
class Graph {
  public double[][] matrix;                // The edge matrix
  public int numEdge;                   // Number of edges
  public int[] Mark;                     // The mark array
  public String[] nodeName;
  public int[] prize;

  public Graph() {}                     // Constructors
  public Graph(int n) {
    Init(n);
  }
  public Graph(Graph G) //hard copy constructor
  {
	  int n = G.n();
	  
	  this.Mark = new int[n];
	  this.matrix = new double[n][n];
	  this.prize = new int[n];
	  
	  for (int i = 0; i < n; i++)
	  {
		  this.Mark[i] = G.Mark[i];
		  this.prize[i] = G.prize[i];
		  for (int j = 0; j < n; j++)
			  this.matrix[i][j] = G.matrix[i][j];
	  }
	  
	  int k = G.numEdge;
	  this.numEdge = k;
  }

  public void Init(int n) {
    Mark = new int[n];
    matrix = new double[n][n];
    numEdge = 0;
    nodeName = new String[n];
    prize = new int[n];
    Arrays.fill(nodeName, 0, n, "");
  }

  public int n() { return Mark.length; } // # of vertices
  public int e() { return numEdge; }     // # of edges

  /** @return v's first neighbor */
  public int first(int v) {
    for (int i=0; i<Mark.length; i++)
      if (matrix[v][i] != 0) return i;
    return Mark.length;  // No edge for this vertex
  }

 /** @return v's next neighbor after w */
  public int next(int v, int w) {
    for (int i=w+1; i<Mark.length; i++)
      if (matrix[v][i] != 0)
        return i;
    return Mark.length;  // No next edge;
  }

  /** Set the weight for an edge */
  public void setEdge(int i, int j, double wt) {
    assert wt!=0 : "Cannot set weight to 0";
    if (matrix[i][j] == 0) numEdge++;
    matrix[i][j] = wt;
  }

  /** Delete an edge */
  public void delEdge(int i, int j) { // Delete edge (i, j)
    if (matrix[i][j] != 0) numEdge--;
    matrix[i][j] = 0;
  }

  /** Determine if an edge is in the graph */
  public boolean isEdge(int i, int j)
    { return matrix[i][j] != 0; }
  
  /** @return an edge's weight */
  public double weight(int i, int j) {
    return matrix[i][j];
  }

  /** Set/Get the mark value for a vertex */
  public void setMark(int v, int val) { this.Mark[v] = val; }
  public int getMark(int v) { return Mark[v]; }
  
  /** Set/Get the name value for a vertex */
  public void setName(int v, String val) { this.nodeName[v] = val; }
  public String getName(int v) { return nodeName[v]; }
  
  /** Set/Get the prize for a vertex */
  public void setPrize(int v, int val) { this.prize[v] = val;}
  public int getPrize(int v) { return prize[v];}
  
  /** @return the index of the city node whose weight is the least for that city and unvisited*/
  public int getLeast(int i)
  {
	  double min = Double.MAX_VALUE;
	  int index = 0;
	  for (int j = 0; j < Mark.length; j++)
	  {
		  if ((matrix[i][j] < min) && (matrix[i][j] != 0) && (getMark(j) == 0))
		  {
			  min = matrix[i][j];
			  index = j;
		  }
	  }
	  return index;
  }
  
  public int getGreatestRatio(int i)
  {
	  double max = 0;
	  int index = 0;
	  for (int j = 0; j < Mark.length; j++)
	  {
		  if ((getPrize(j)/matrix[i][j] > max) && (matrix[i][j] != 0) && (getMark(j) == 0))
		  {
			  max = getPrize(j)/matrix[i][j];
			  index = j;
		  }
	  }
	  return index;
  }
  
  public int getGreatestPrize(int i)
  {
	  int max = 0;
	  int index = 0;
	  for (int j = 0; j < Mark.length; j++)
	  {
		  if ((getPrize(j) > max) && (matrix[i][j] != 0) && (getMark(j) == 0))
		  {
			  max = getPrize(j);
			  index = j;
		  }
	  }
	  return index;
  }
  
  /** @return last node of the graph */
  public int getLastNode()
  {return (this.n()-1);}
  
}