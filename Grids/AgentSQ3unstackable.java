package Grids;

/**
 * extend the AgentSQ3 class if you want agents that exist on a 3D discrete lattice
 * with the possibility of stacking multiple agents on the same grid square
 * @param <T> the extended Grid3 class that the agents will live in
 * Created by rafael on 11/18/16.
 */
public abstract class AgentSQ3unstackable<T extends Grid3unstackable> extends AgentBase<T>{
    int xSq;
    int ySq;
    int zSq;
    int iSq;

    void Setup(int xSq,int ySq,int zSq){
        this.xSq=xSq;
        this.ySq=ySq;
        this.zSq=zSq;
        this.iSq=myGrid.I(xSq,ySq,zSq);
        myGrid.AddAgentToSquare(this,iSq);
    }
    void Setup(double xPos,double yPos,double zPos){
        Setup((int)xPos,(int)yPos,(int)zPos);
    }

    /**
     * Moves the agent to the specified coordinates
     */
    public void Move(int x, int y, int z){
        //moves agent discretely
        if(!alive){
            throw new RuntimeException("attempting to move dead agent");
        }
        int iNewPos=myGrid.I(x,y,z);
        myGrid.RemAgentFromSquare(this,iSq);
        myGrid.AddAgentToSquare(this,iNewPos);
        this.xSq=x;
        this.ySq=y;
        this.zSq=z;
        this.iSq=iNewPos;
    }

    /**
     * Moves the agent to the specified coordinates
     */
    public void Move(double x, double y, double z){
        Move((int)x,(int)y,(int)z);
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
     * gets the z coordinate of the square that the agent occupies
     */
    public int Zsq(){
        return zSq;
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
     * gets the z coordinate of the agent
     */
    public double Zpt(){ return zSq+0.5;}
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
}
