package illumsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShineNode {

    private ArrayList<LevelWrap> LevelWraps = new ArrayList<>();
    private Integer depth;
    private ShineTree tree;
    
    //Store whether a node is topleft (TL), topright (TR), bottomleft (BL), bottomright(BR) or root
    private String nodeType;
    
    //Storing the parameter ranges for node
    private float param1Min;
    private float param1Max;
    
    private float param2Min;
    private float param2Max;
    
    //Store the min and max values of current representative levels at node
    private float repBCMin = 10000f;
    private float repBCMax = 0f;

    private ArrayList < ShineNode > children = new ArrayList < > ();

    private ShineNode  parent;

    //Constructor for creating child nodes
    public ShineNode(ShineTree tree, int depth, ShineNode parent, String nodeType,  float param1Min, float param1Max, float param2Min, float param2Max) {
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
        this.nodeType = "root";
        this.depth = 0;
        this.param1Min = param1Min;
        this.param1Max = param1Max;
        this.param2Min = param2Min;
        this.param2Max = param2Max;
    }

    public ShineNode addChild(ShineNode child) {
        //child.setParent(this);
        this.children.add(child);
        return child;
    }

    public void addChildren(List <ShineNode> children) {
        //children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }
    

    public List <ShineNode > getChildren() {
        return children;
    }

    public List<LevelWrap> getLevels() {
        return LevelWraps;
    }

    //Method for adding levels to the ShineNode
    public void addLevel(LevelWrap level) {
        
        //System.out.println("ShineNode- Param1 min/max = " + this.param1Min+"/"+this.param1Max+ ". Param2 min/max"+ this.param2Min+"/"+this.param2Max);
        //System.out.println("ShineNode- Added " + level.toString());   
        //System.out.println("Level inputted as param1: " + level.getParam1()+ " and param2: " +level.getParam2());
        //If node has no children, and has the appropriate parameters, add it to levelbucket
        if (children.size() == 0 && checkBelongs(level)) {
            this.LevelWraps.add(level);
            //System.out.println("ShineNode- Added " + level.toString());
            //If we have exceeded the max representatives at vertex, without hitting max depth create child nodes and assign
            if (LevelWraps.size() > tree.maxReps && depth < tree.maxDepth) {
                //System.out.println("Max Representatives exceeded");
                float windowParam1 = (param1Max - param1Min)/2;
                float windowParam2 = (param2Max - param2Min)/2;
                
                //GENERIC
                ShineNode BL = addChild(new ShineNode(tree, depth+1, this, "BL", param1Min, (param1Min+windowParam1), param2Min, (param2Min+windowParam2)));
                ShineNode TL = addChild(new ShineNode(tree, depth+1, this, "TL", param1Min, (param1Min+windowParam1), (param2Min+windowParam2), param2Max));
                ShineNode BR = addChild(new ShineNode(tree, depth+1, this, "BR", (param1Min+windowParam1), param1Max, param2Min, (param2Min+windowParam2)));
                ShineNode TR = addChild(new ShineNode(tree, depth+1, this, "TR", (param1Min+windowParam1), param1Max, (param2Min+windowParam2), param2Max));
                
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
                setLevelsNovelty();
                Collections.sort(LevelWraps);
                //System.out.println("Removing level with fitness " + LevelWraps.get(0).getFitness()+". Max fitness at node: " + LevelWraps.get(LevelWraps.size()-1).getFitness());
                LevelWraps.remove(0);
                //System.out.println("Bottomed out at depth " + depth + " with num reps: " + LevelWraps.size());
            }
            //Printing after every actual addition
            //this.printNode();
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
    
    public ArrayList<LevelWrap> archiveReps(){
        
        ArrayList<LevelWrap> archiveReps = new ArrayList<>();
        
        //printNode(true);
        float maxReps = (float) Math.pow(( tree.maxDepth - depth + 1),tree.childNodes);
        //System.out.println("Depth: " + depth + "Max reps: " + maxReps + ". Number of reps: " + LevelWraps.size());
        
        //System.out.println("Processing ArchiveNode on node:");
        //this.printNode();
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
            setLevelsNovelty();
            Collections.sort(LevelWraps);
            int index = LevelWraps.size()-1;
            while (archiveReps.size() < maxReps) {
                //System.out.println("Adding level with fitness" + LevelWraps.get(index).getFitness() + " over " + LevelWraps.get(0).getFitness());
                //Selection chance algorithm from Shine paper
                //System.out.println("MATH: tree.maxReps = "+ tree.maxReps + ", depth = " + depth + ", maxReps = " + maxReps + ", (tree.maxReps*depth) = " + (tree.maxReps*depth) + ", ((tree.maxReps*depth)+maxReps= " + ((tree.maxReps*depth)+maxReps) + ", tree.maxReps/((tree.maxReps*depth)+maxReps) = " + tree.maxReps/((tree.maxReps*depth)+maxReps));
                LevelWrap clone = LevelWraps.get(index).clone();
                clone.setSelectionChance(tree.maxReps/((tree.maxReps*depth)+maxReps));
                archiveReps.add(clone);
                index--;
            }
        }
        //If we have less than max reps at level, add them all
        else if (LevelWraps.size()>0){
            //System.out.println("Adding all " + LevelWraps.size() + " to archive");
            for (int i = 0; i < LevelWraps.size(); i++) {
                LevelWrap clone = LevelWraps.get(i).clone();
                float vPop = LevelWraps.size();
                //System.out.println("MATH: tree.maxReps = "+ tree.maxReps + ", depth = " + depth + ", maxReps = " + vPop + ", (tree.maxReps*depth) = " + (tree.maxReps*depth) + ", ((tree.maxReps*depth)+maxReps= " + ((tree.maxReps*depth)+vPop) + ", tree.maxReps/((tree.maxReps*depth)+vPop) = " + tree.maxReps/((tree.maxReps*depth)+vPop));               
                clone.setSelectionChance(tree.maxReps/((tree.maxReps*depth)+vPop));
                archiveReps.add(clone);
            }
            
        }
        return archiveReps;
    }
    
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
    
    private void setLevelsNovelty() {
        
        //System.out.println("NodeType: " + this.nodeType);
        
        //Calculate ratio between two parameters in real terms, feed this into th e corner distance calculation
        float normalisationFactor = (this.param1Max-this.param1Min)/(this.param2Max-this.param2Min);
        
        for (LevelWrap level : this.LevelWraps) {
            
            //System.out.println(level.toString());
            switch(this.nodeType) {
            
                case "TL":
                    
                    /*
                    System.out.println("Cell is in top left with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((this.param1Max-level.getParam1()),(level.getParam2()-this.param2Min)));
                    System.out.println("This levels normalised novelty is: " + getPythagC((level.getParam1()-this.param1Min),(this.param2Min-level.getParam2())*normalisationFactor));
                    System.out.println("");
                    */
                    level.setNovelty(getPythagC((level.getParam1()-this.param1Min),(this.param2Min-level.getParam2())*normalisationFactor));
                    break;
                case "TR":
                    
                    /*
                    System.out.println("Cell is in top right with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((level.getParam1()-this.param1Min),(level.getParam2()-this.param2Min)));
                    System.out.println("This levels normalised novelty is: " + getPythagC((this.param1Max-level.getParam1()),(this.param2Max-level.getParam2())*normalisationFactor));
                    System.out.println("");
                    */
                    level.setNovelty(getPythagC((level.getParam1()-this.param1Min),(level.getParam2()-this.param2Min)*normalisationFactor));
                    break;
                case "BL":
                    /*
                    System.out.println("Cell is in bottom left with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((this.param1Max-level.getParam1()),this.param2Max-level.getParam2()));
                    System.out.println("This levels normalised novelty is: " + getPythagC((this.param1Max-level.getParam1()),(this.param2Max-level.getParam2())*normalisationFactor));
                    System.out.println("");
                    */
                    
                    level.setNovelty(getPythagC((this.param1Max-level.getParam1()),this.param2Max-level.getParam2())*normalisationFactor);
                    break;
                case "BR":
                    /*
                    System.out.println("Cell is in bottom right with P1Min/Max/P2Min/Max " + this.param1Min + "/"+this.param1Max + "/" + this.param2Min + "/" + this.param2Max);
                    System.out.println("This level has param1: " + level.getParam1()+ " and param2: " + level.getParam2());
                    System.out.println("This levels novelty is : " + getPythagC((level.getParam1()-this.param1Min),(this.param2Max-level.getParam2())));
                    System.out.println("This levels normalised novelty is: " + getPythagC((level.getParam1()-this.param1Min),(this.param2Max-level.getParam2())*normalisationFactor));
                    */
                    level.setNovelty(getPythagC((level.getParam1()-this.param1Min),(this.param2Max-level.getParam2())*normalisationFactor));
                    break;
                default:
                    level.setNovelty(0);
            
            }
        }
        
    }
    
    private double getPythagC(float sideA, float sideB) {
        
        return Math.sqrt(Math.pow(sideA,2)+Math.pow(sideB,2));
        
    }
    
    public ArrayList<LevelWrap> getAllChildLevels(){
        
        ArrayList<LevelWrap> allLevels = new ArrayList<LevelWrap>();
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
    
    public boolean checkBelongs(LevelWrap level) {
        
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
        System.out.println("ShineNode- Depth: " + depth + ". Param1 Min/Max = " + param1Min + "/" + param1Max +". Num reps: " + LevelWraps.size()+". Param2 Min/Max = " + param2Min+"/"+param2Max);
        if(noisy) {
            for (int i = 0; i<LevelWraps.size(); i++) {
                
                System.out.println(LevelWraps.get(i).toString());
            }
            System.out.println("");
        }
    }

}