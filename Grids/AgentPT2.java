package Grids;

import static Tools.Utils.*;

/**
 * extend the AgentPT2 class if you want agents that exist on a 2D continuous lattice
 * with the possibility of stacking multiple agents on the same grid square
 * @param <T> the extended Grid2 class that the agents will live in
 * Created by rafael on 11/18/16.
 */
abstract public class AgentPT2<T extends Grid2> extends AgentSQ2<T> {
    private double ptX;
    private double ptY;
    void Setup(double xPos,double yPos){
        this.ptX =xPos;
        this.ptY =yPos;
        iSq=this.myGrid.I(xPos,yPos);
        myGrid.AddAgentToSquare(this,iSq);
    }
    void Setup(int xPos,int yPos){
        this.ptX =xPos+0.5;
        this.ptY =yPos+0.5;
        iSq=myGrid.I(xPos,yPos);
        myGrid.AddAgentToSquare(this,iSq);
    }

    /**
     * Moves the agent to the specified coordinates
     */
    public void Move(int newX, int newY){
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent");
        }
        int oldX=(int) ptX;
        int oldY=(int) ptY;
        if(oldX!=newX||oldY!=newY) {
            myGrid.RemAgentFromSquare(this, iSq);
            iSq=myGrid.I(newX,newY);
            myGrid.AddAgentToSquare(this, iSq);
        }
        ptX =newX+0.5;
        ptY =newY+0.5;
    }

    /**
     * Moves the agent to the specified coordinates
     */
    public void Move(double newX, double newY){
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent");
        }
        int xIntNew=(int)newX;
        int yIntNew=(int)newY;
        int xIntOld=(int) ptX;
        int yIntOld=(int) ptY;
        if(xIntNew!=xIntOld||yIntNew!=yIntOld) {
            myGrid.RemAgentFromSquare(this, iSq);
            iSq=myGrid.I(xIntNew,yIntNew);
            myGrid.AddAgentToSquare(this, iSq);
        }
        ptX =newX;
        ptY =newY;
    }

    /**
     * Moves the agent to the specified coordinates, and either stops or wraps around at the edges
     */
    public void MoveSafe(double newX,double newY,boolean wrapX,boolean wrapY){
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent");
        }
        if (G().In(newX, newY)) {
            Move(newX, newY);
            return;
        }
        if (wrapX) {
            newX = ModWrap(newX, G().xDim);
        } else if (!InDim(G().xDim, newX)) {
            newX = Xpt();
        }
        if (wrapY) {
            newY = ModWrap(newY, G().yDim);
        } else if (!InDim(G().yDim, newY))
            newY = Ypt();
        Move(newX,newY);
    }
    public void MoveSafe(double newX,double newY){
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent");
        }
        if (G().In(newX, newY)) {
            Move(newX, newY);
            return;
        }
        if (G().wrapX) {
            newX = ModWrap(newX, G().xDim);
        } else if (!InDim(G().xDim, newX)) {
            newX = Xpt();
        }
        if (G().wrapY) {
            newY = ModWrap(newY, G().yDim);
        } else if (!InDim(G().yDim, newY))
            newY = Ypt();
        Move(newX,newY);
    }

    /**
     * gets the xDim coordinate of the agent
     */
    public double Xpt(){
        return ptX;
    }

    /**
     * gets the yDim coordinate of the agent
     */
    public double Ypt(){
        return ptY;
    }

    /**
     * gets the xDim coordinate of the square that the agent occupies
     */
    public int Xsq(){
        return (int) ptX;
    }

    /**
     * gets the yDim coordinate of the square that the agent occupies
     */
    public int Ysq(){
        return (int) ptY;
    }
    public<T extends AgentPT2> double Xdisp(T other,boolean wrapX){
        return wrapX? DistWrap(other.Xpt(),Xpt(),G().xDim):Xpt()-other.Xpt();
    }
    public <T extends AgentPT2> double Ydisp(T other,boolean wrapY){
        return wrapY? DistWrap(other.Ypt(),Ypt(),G().yDim):Ypt()-other.Ypt();
    }
}
