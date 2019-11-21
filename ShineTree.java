package illumsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ShineTree {
	
	//Stores the root node for this tree	
	public ShineNode root;
	
	
	public Integer maxDepth;
	public Integer maxReps;
	
	//Hard coded number of child nodes. Should be 2^Number of parameters
	public final int childNodes = 4;
	
	//This is where we store the size of the map 
	
	public ShineTree(Integer maxDepth, Integer maxReps, float param1Min, float param1Max, float param2Min, float param2Max) {
		root = new ShineNode(this, param1Min, param1Max, param2Min, param2Max);
		this.maxDepth = maxDepth;
		this.maxReps = maxReps;
	}
	
	public void printTree() {
		
		if (root.getChildren().size()==0) {
			
		}
		
	}
	
	public ArrayList<LevelWrap> createArchive(){
			
		return root.archiveReps();
		
	}
	

}
