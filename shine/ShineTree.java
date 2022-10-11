package illuminating_mario.shine;

import java.util.ArrayList;

import illuminating_mario.mainFunc.*;

public class ShineTree {
	
	//Stores the root node for this tree	
	public ShineNode root;
	
	public Integer maxDepth;
	public Integer maxReps;
	
	//Hard coded number of child nodes. Should be 2^Number of parameters
	public final int childNodes = 4;
	
	public ShineTree(Integer maxDepth, Integer maxReps, float param1Min, float param1Max, float param2Min, float param2Max) {
		root = new ShineNode(this, param1Min, param1Max, param2Min, param2Max);
		this.maxDepth = maxDepth;
		this.maxReps = maxReps;
	}
	
	public ArrayList<IllumLevelWrap> createArchive(){
		return root.archiveReps();
	}
}
