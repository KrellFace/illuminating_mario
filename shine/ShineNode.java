package illuminating_mario.shine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import illuminating_mario.mainFunc.*;

public class ShineNode {

    private ArrayList<IllumLevelWrap> LevelWraps = new ArrayList<>();
    private Integer depth;
    private ShineTree tree;
    
    //Store whether a node is topleft (TL), topright (TR), bottomleft (BL), bottomright(BR) or root
    private ShineNodeType nodeType;
    
    //Storing the parameter ranges for node
    private float param1Min;
    private float param1Max;
    
    private float param2Min;
    private float param2Max;

    private ArrayList < ShineNode > children = new ArrayList < > ();

    private ShineNode  parent;

    //Constructor for creating child nodes
    public ShineNode(ShineTree tree, int depth, ShineNode parent, ShineNodeType nodeType,  float param1Min, float param1Max, float param2Min, float param2Max) {
        this.tree = tree;
        this.depth = depth;
        this.parent = parent;
        this.nodeType = nodeType;
        this.param1Min = param1Min;
        this.param1Max = param1Max;
        this.param2Min = param2Min;
        this.param2Max = param2Max;
    }
    
    //Constructor for building the root node
    public ShineNode(ShineTree tree, float param1Min, float param1Max, float param2Min, float param2Max) {
        this.tree = tree;
        this.parent = null;
        this.nodeType = ShineNodeType.RootNode;
        this.depth = 0;
        this.param1Min = param1Min;
        this.param1Max = param1Max;
        this.param2Min = param2Min;
        this.param2Max = param2Max;
    }

    public ShineNode addChild(ShineNode child) {
        this.children.add(child);
        return child;
    }

    public void addChildren(List <ShineNode> children) {
        this.children.addAll(children);
    }
    

    public List <ShineNode > getChildren() {
        return children;
    }

    public List<IllumLevelWrap> getLevels() {
        return LevelWraps;
    }

    //Method for adding levels to the ShineNode
    public void addLevel(IllumLevelWrap level) {
        
    	/*
    	if (checkBelongs(level)) {
    		System.out.print("Level belongs in node: " );
    		this.printNode(false);
    		//System.out.println("ShineNode- Added " + level.toString() + " to node: ");
    	}
		*/
        //System.out.println("Level inputted as param1: " + level.getParam1()+ " and param2: " +level.getParam2());
        //If node has no children, and has the appropriate parameters, add it to levelbucket
        if (children.size() == 0 && checkBelongs(level)) {
            //System.out.println("ShineNode- Added " + level.toString() + " to node: ");
            //this.printNode(false);
            this.LevelWraps.add(level);
            //System.out.println("ShineNode- Added " + level.toString());
            //If we have exceeded the max representatives at vertex, without hitting max depth create child nodes and assign
            if (LevelWraps.size() > tree.maxReps && depth < tree.maxDepth) {
                //System.out.println("Max Representatives exceeded");
                float windowParam1 = (param1Max - param1Min)/2;
                float windowParam2 = (param2Max - param2Min)/2;
                
                //GENERIC
                //BottomLeft Node
                addChild(new ShineNode(tree, depth+1, this, ShineNodeType.BottomLeft, param1Min, (param1Min+windowParam1), param2Min, (param2Min+windowParam2)));
                //TopLeft Node
                addChild(new ShineNode(tree, depth+1, this, ShineNodeType.TopLeft, param1Min, (param1Min+windowParam1), (param2Min+windowParam2), param2Max));
                //BottomRight Node
                addChild(new ShineNode(tree, depth+1, this, ShineNodeType.BottomRight, (param1Min+windowParam1), param1Max, param2Min, (param2Min+windowParam2)));
                //TopRight Node
                addChild(new ShineNode(tree, depth+1, this, ShineNodeType.TopRight, (param1Min+windowParam1), param1Max, (param2Min+windowParam2), param2Max));
                
                for (int i = 0; i<LevelWraps.size(); i++) {
                    for (int y = 0; y<this.children.size(); y++)
                        if (children.get(y).checkBelongs(LevelWraps.get(i))){
                            children.get(y).addLevel(LevelWraps.get(i));
                        }
                }
                    
                //Remove all levels from current node
                LevelWraps.clear();          
                
            }
            //If we are at the max depth, remove the weakest member
            else if (LevelWraps.size() > tree.maxReps) {
                setLevelCDscore();
                Collections.sort(LevelWraps);
                //System.out.println("Removing level with fitness " + LevelWraps.get(0).getFitness()+". Max fitness at node: " + LevelWraps.get(LevelWraps.size()-1).getFitness());
                LevelWraps.remove(0);
                //System.out.println("Bottomed out at depth " + depth + " with num reps: " + LevelWraps.size());
            }
        }
        //Reccur for all child nodes if belongs
        else if (checkBelongs(level)){  
            //System.out.println("Descending tree with level BC " + level.getBlockCount() + " and fitness " + level.getFitness() + " and JE: " + level.getJumpEntropy());
            for (int i = 0; i<children.size(); i++) {
                children.get(i).addLevel(level);
            }
        }
        else {
            //System.out.println("ShineNode- Added level does not belong");
        };
    }
    
    public ArrayList<IllumLevelWrap> archiveReps(){
        
        ArrayList<IllumLevelWrap> archiveReps = new ArrayList<>();
          
        //printNode(true);
        float maxReps = (float) Math.pow(( tree.maxDepth - depth + 1),tree.childNodes);
        //System.out.println("Depth: " + depth + "Max reps: " + maxReps + ". Number of reps: " + LevelWraps.size());  
        //System.out.println("Processing ArchiveNode on node:");
        //this.printNode(false);
        //If there are children nodes, reccur down and add their reps to the archive
        if (children.size()>0) {
            for (int i = 0; i<children.size();i++) {
                try {
                	//System.out.println("Attempting to add " + children.get(i).archiveReps().size() + " levels to archive");
                    archiveReps.addAll(children.get(i).archiveReps());
                }
                catch (Exception e) {
                    //System.out.println("Adding children failed");
                }
            }
        }
        //If we have more than max reps at level, add sorted (By Fitness or Novelty) levels until we are at max
        else if (LevelWraps.size() > maxReps) {
            //System.out.println("Max Reps: " + maxReps + ". Current reps at vertex: " + LevelWraps.size()+ ". Node depth: "+ depth);
            setLevelCDscore();
            Collections.sort(LevelWraps);
            int index = LevelWraps.size()-1;
            //System.out.println("MATH: tree.maxReps = "+ tree.maxReps + ", depth = " + depth + ", Node Max Reps = " + maxReps + ", (tree.maxReps*depth) = " + (tree.maxReps*depth) + ", ((tree.maxReps*depth)+maxReps= " + ((tree.maxReps*depth)+maxReps) + ", tree.maxReps/((tree.maxReps*depth)+maxReps) = " + tree.maxReps/((tree.maxReps*depth)+maxReps));
            while (archiveReps.size() < maxReps) {
                //System.out.println("Adding level with fitness" + LevelWraps.get(index).getFitness() + " over " + LevelWraps.get(0).getFitness());
                //Selection chance algorithm from Shine paper
                IllumLevelWrap clone = LevelWraps.get(index).clone();
            	//System.out.println("Adding level: " + clone.toString());
                clone.setSelectionChance(tree.maxReps/((tree.maxReps*depth)+maxReps));
                archiveReps.add(clone);
                index--;
            }
        }
        //If we have less than max reps at level, add them all
        else if (LevelWraps.size()>0){
            //System.out.println("Adding all " + LevelWraps.size() + " to archive");
        	  	
            float vPop = LevelWraps.size();
            //System.out.println("MATH: tree.maxReps = "+ tree.maxReps + ", depth = " + depth + ", vPop = " + vPop + ", (tree.maxReps*depth) = " + (tree.maxReps*depth) + ", ((tree.maxReps*depth)+maxReps= " + ((tree.maxReps*depth)+vPop) + ", tree.maxReps/((tree.maxReps*depth)+vPop) = " + tree.maxReps/((tree.maxReps*depth)+vPop));               
            for (int i = 0; i < LevelWraps.size(); i++) {
                IllumLevelWrap clone = LevelWraps.get(i).clone();
                //System.out.println("Adding level: " + clone.toString());
                clone.setSelectionChance(tree.maxReps/((tree.maxReps*depth)+vPop));
                archiveReps.add(clone);
            }
            
        }
        
        //System.out.print("Finished getting archive reps from node: " );
		//this.printNode(false);
        
        return archiveReps;
    }
    
    /*
    public void setMinMaxRepValues() {
        
        ArrayList<LevelWrap> allLevels = this.getAllChildLevels();

        for (int i = 0; i<allLevels.size(); i++) {
            if (allLevels.get(i).getBlockCount()<this.repBCMin) {
                this.repBCMin = allLevels.get(i).getBlockCount();
            }
            if (allLevels.get(i).getBlockCount()>this.repBCMax) {
                this.repBCMax = allLevels.get(i).getBlockCount();
            }
        }
        
        //System.out.println("Min BC: " + this.repBCMin + ", Max BC: " + this.repBCMax);
        
    }
    */
    
    private void setLevelCDscore() {
        
        //System.out.println("NodeType: " + this.nodeType);
        
        //Calculate ratio between two parameters in real terms, feed this into th e corner distance calculation
        float normalisationFactor = (this.param1Max-this.param1Min)/(this.param2Max-this.param2Min);
        
        for (IllumLevelWrap level : this.LevelWraps) {
            
            //System.out.println(level.toString());
            switch(this.nodeType) {
            
                case TopLeft:
                    
                    /*
                    System.out.println("Cell is in top left with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((this.param1Max-level.getParam1()),(level.getParam2()-this.param2Min)));
                    System.out.println("This levels normalised novelty is: " + getPythagC((this.param1Max-level.getParam1()),(level.getParam2()-this.param2Min)*normalisationFactor));
                    System.out.println("");
                    */
                    level.setCDscore(getPythagC((this.param1Max-level.getParam1()),(level.getParam2()-this.param2Min)*normalisationFactor));
                    break;
                case TopRight:
                    
                    /*
                    System.out.println("Cell is in top right with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((level.getParam1()-this.param1Min),(level.getParam2()-this.param2Min)));
                    System.out.println("This levels normalised novelty is: " + getPythagC((level.getParam1()-this.param1Min),(level.getParam2()-this.param2Min)*normalisationFactor));
                    System.out.println("");
                    */
                    level.setCDscore(getPythagC((level.getParam1()-this.param1Min),(level.getParam2()-this.param2Min)*normalisationFactor));
                    break;
                case BottomLeft:
                    /*
                    System.out.println("Cell is in bottom left with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((this.param1Max-level.getParam1()),this.param2Max-level.getParam2()));
                    System.out.println("This levels normalised novelty is: " + getPythagC((this.param1Max-level.getParam1()),(this.param2Max-level.getParam2())*normalisationFactor));
                    System.out.println("");
                    */
                    level.setCDscore(getPythagC((this.param1Max-level.getParam1()),this.param2Max-level.getParam2())*normalisationFactor);
                    break;
                case BottomRight:
                    /*
                    System.out.println("Cell is in bottom right with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((level.getParam1()-this.param1Min),(this.param2Max-level.getParam2())));
                    System.out.println("This levels normalised novelty is: " + getPythagC((level.getParam1()-this.param1Min),(this.param2Max-level.getParam2())*normalisationFactor));
                    System.out.println("");
					*/
                    level.setCDscore(getPythagC((level.getParam1()-this.param1Min),(this.param2Max-level.getParam2())*normalisationFactor));
                    break;
                default:
                    level.setCDscore(0);
            
            }
        }
        
    }
    
    private double getPythagC(float sideA, float sideB) {
        
        return Math.sqrt(Math.pow(sideA,2)+Math.pow(sideB,2));
        
    }
    
    public ArrayList<IllumLevelWrap> getAllChildLevels(){
        
        ArrayList<IllumLevelWrap> allLevels = new ArrayList<IllumLevelWrap>();
        if (children.size() == 0) {
            allLevels= this.LevelWraps;
        }
        else {
            for (int i = 0; i<this.getChildren().size();i++) {
                allLevels.addAll(this.getChildren().get(i).getAllChildLevels());
            }
        }
        
        return allLevels;


    }
    
    public boolean checkBelongs(IllumLevelWrap level) {
        
        float param1Val = level.getParam1();
        float param2Val = level.getParam2();
        
        //System.out.println("Comparing input param 1 " + level.getParam1() + ", param2: " + level.getParam2() + " with param1 min/max " + param1Min + "/" + param1Max+ " and param2 min/max " + param2Min + "/" + param2Max);
        
        if ( param1Val<=param1Max && param1Val>param1Min&&param2Val<=this.param2Max&&param2Val>this.param2Min) {
            return true;
        }
        else {
            return false;
        }
        
    }
    
    public void printLeaves(boolean noisy) {
        if (children.size()<1) {
            printNode(noisy);
        }
        else {
            for (int i = 0; i<children.size();i++) {
                children.get(i).printLeaves(noisy);
            }
        }
    }
    
    public int countLeaves() {
        int count = 0;
        if (children.size()<1) {
            count+=1;
        }
        else {
            for (int i = 0; i<children.size();i++) {
                count += children.get(i).countLeaves();
            }
        }
        return count;
    }

    public ShineNode getParent() {
        return parent;
    }
    
    public void printNode(boolean noisy) {
        System.out.print("ShineNode- Position: " + this.nodeType + " Depth: " + depth + ". Param1 Min/Max = " + param1Min + "/" + param1Max +". Num reps: " + LevelWraps.size()+". Param2 Min/Max = " + param2Min+"/"+param2Max );
        if (parent != null){
        	System.out.print(". Parent position: " + this.parent.nodeType);
        	System.out.println("");
        }
        if(noisy) {
            for (int i = 0; i<LevelWraps.size(); i++) {
                
                System.out.println(LevelWraps.get(i).toString());
            }
            System.out.println("");
        }
    }

}