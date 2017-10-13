package Grids;


/**
 * extend the AgentSQ2 class if you want agents that exist on a 2D discrete lattice
 * with the possibility of stacking multiple agents on the same grid square
 * @param <T> the extended Grid2 class that the agents will live in
 * Created by rafael on 11/18/16.
 */
abstract public class AgentSQ2<T extends Grid2> extends AgentBase<T>{
    private int xSq;
    private int ySq;
    AgentSQ2 nextSq;
    AgentSQ2 prevSq;
    int iSq;

    void Setup(int i){
        xSq=myGrid.ItoX(i);
        ySq=myGrid.ItoY(i);
        iSq=i;
        myGrid.AddAgentToSquare(this,i);

    }
    void Setup(int xSq,int ySq){
        this.xSq=xSq;
        this.ySq=ySq;
        iSq=myGrid.I(xSq,ySq);
        myGrid.AddAgentToSquare(this,iSq);
    }
    void Setup(double xPos,double yPos){
        Setup((int)xPos,(int)yPos);
    }

    /**
     * Moves the agent to the specified square
     */
    public void Move(int x, int y){
        //moves agent discretely
        if(!alive){
            throw new RuntimeException("attempting to move dead agent");
        }
        int iNewPos=myGrid.I(x,y);
        myGrid.RemAgentFromSquare(this,iSq);
        myGrid.AddAgentToSquare(this,iNewPos);
        this.xSq=x;
        this.ySq=y;
        iSq=iNewPos;
    }

    /**
     * Moves the agent to the specified square
     */
    public void Move(double x, double y){
       Move((int)x,(int)y);
    }

    /**
     * gets the xDim coordinate of the square that the agent occupies
     */
    public int Xsq(){
        return xSq;
    }

    /**
     * gets the yDim coordinate of the square that the agent occupies
     */
    public int Ysq(){
        return ySq;
    }

    /**
     * gets the xDim coordinate of the agent
     */
    public double Xpt(){
        return xSq+0.5;
    }

    /**
     * gets the yDim coordinate of the agent
     */
    public double Ypt(){
        return ySq+0.5;
    }


    /**
     * deletes the agent
     */
    public void Dispose(){
        //kills agent
        if(!alive){
            throw new RuntimeException("attempting to dispose already dead agent");
        }
        myGrid.RemoveAgent(this,iSq);
    }

    /**
     * Gets the index of the square that the agent occupies
     */
    public int Isq(){
        return iSq;
    }
    //addCoords
}
