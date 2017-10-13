package Grids;

import static Tools.Utils.*;

/**
 * extend the AgentPT3 class if you want agents that exist on a 3D continuous lattice
 * with the possibility of stacking multiple agents on the same grid square
 * @param <T> the extended Grid3 class that the agents will live in
 * Created by rafael on 11/18/16.
 */
public abstract class AgentPT3<T extends Grid3> extends AgentSQ3<T> {
    private double xPos;
    private double yPos;
    private double zPos;
    protected void Setup(double xPos,double yPos,double zPos){
        this.xPos=xPos;
        this.yPos=yPos;
        this.zPos=zPos;
        iSq=myGrid.I(xPos,yPos,zPos);
        myGrid.AddAgentToSquare(this,iSq);
    }
    protected void Setup(int xPos,int yPos,int zPos){
        this.xPos=xPos+0.5;
        this.yPos=yPos+0.5;
        this.zPos=zPos+0.5;
        iSq=myGrid.I(xPos,yPos,zPos);
        myGrid.AddAgentToSquare(this,iSq);
    }

    /**
     * Moves the agent to the specified coordinates
     */
    public void Move(int newX, int newY,int newZ){
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent!");
        }
        int oldX=(int)xPos;
        int oldY=(int)yPos;
        int oldZ=(int)zPos;
        if(oldX!=newX||oldY!=newY||oldZ!=newZ) {
            myGrid.RemAgentFromSquare(this, iSq);
            iSq=myGrid.I(newX,newY,newZ);
            myGrid.AddAgentToSquare(this, iSq);
        }
        this.xPos=newX+0.5;
        this.yPos=newY+0.5;
        this.zPos=newZ+0.5;
    }

    /**
     * Moves the agent to the specified coordinates
     */
    public void Move(double newX, double newY,double newZ){
        int xIntNew=(int)newX;
        int yIntNew=(int)newY;
        int zIntNew=(int)newZ;
        int xIntOld=(int)xPos;
        int yIntOld=(int)yPos;
        int zIntOld=(int)zPos;
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent!");
        }
        if(xIntNew!=xIntOld||yIntNew!=yIntOld||zIntNew!=zIntOld) {
            myGrid.RemAgentFromSquare(this, iSq);
            iSq=myGrid.I(xIntNew,yIntNew,zIntNew);
            myGrid.AddAgentToSquare(this, iSq);
        }
        xPos=newX;
        yPos=newY;
        zPos=newZ;
    }


    public void MoveSafe(double newX,double newY,double newZ,boolean wrapX,boolean wrapY,boolean wrapZ) {
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent!");
        }
        if (G().In(newX, newY, newZ)) {
            Move(newX, newY, newZ);
            return;
        }
        if (wrapX) {
            newX = ModWrap(newX, G().xDim);
        } else if (!InDim(G().xDim, newX)) {
            newX = Xpt();
        }
        if (wrapY) {
            newY = ModWrap(newY, G().yDim);
        } else if (!InDim(G().yDim, newY)) {
            newY = Ypt();
        }
        if (wrapZ) {
            newZ = ModWrap(newZ, G().zDim);
        } else if (!InDim(G().zDim, newZ)) {
            newZ = Zpt();
        }
        Move(newX,newY,newZ);
    }
    public void MoveSafe(double newX,double newY,double newZ) {
        if(!alive){
            throw new RuntimeException("Attempting to move dead agent!");
        }
        if (G().In(newX, newY, newZ)) {
            Move(newX, newY, newZ);
            return;
        }
        if (G().wrapX) {
            newX = ModWrap(newX, G().xDim);
        } else if (!InDim(G().xDim, newX)) {
            newX = Xpt();
        }
        if (G().wrapY) {
            newY = ModWrap(newY, G().yDim);
        } else if (!InDim(G().yDim, newY)) {
            newY = Ypt();
        }
        if (G().wrapZ) {
            newZ = ModWrap(newZ, G().zDim);
        } else if (!InDim(G().zDim, newZ)) {
            newZ = Zpt();
        }
        Move(newX,newY,newZ);
    }
    /**
     * gets the xDim coordinate of the agent
     */
    public double Xpt(){
        return xPos;
    }

    /**
     * gets the yDim coordinate of the agent
     */
    public double Ypt(){
        return yPos;
    }

    /**
     * gets the z coordinate of the agent
     */
    public double Zpt(){
        return zPos;
    }

    /**
     * gets the xDim coordinate of the square that the agent occupies
     */
    public int Xsq(){
        return (int)xPos;
    }

    /**
     * gets the yDim coordinate of the square that the agent occupies
     */
    public int Ysq(){
        return (int)yPos;
    }

    /**
     * gets the z coordinate of the square that the agent occupies
     */
    public int Zsq(){ return (int)zPos; }

    public void Dispose(){
        if(!alive){
            throw new RuntimeException("attepting to dispose already dead agent");
        }
        myGrid.RemoveAgent(this,iSq);
    }

    public<T extends AgentPT3> double Xdisp(T other,boolean wrapX){
        return wrapX? DistWrap(other.Xpt(),Xpt(),G().xDim):Xpt()-other.Xpt();
    }
    public <T extends AgentPT3> double Ydisp(T other,boolean wrapY){
        return wrapY? DistWrap(other.Ypt(),Ypt(),G().yDim):Ypt()-other.Ypt();
    }
    public <T extends AgentPT3> double Zdisp(T other,boolean wrapY){
        return wrapY? DistWrap(other.Zpt(),Zpt(),G().zDim):Zpt()-other.Zpt();
    }

    public <T extends AgentPT3> double disp(T other,boolean wrap){
        double dx = Xdisp(other, wrap);
        double dy = Ydisp(other, wrap);
        double dz = Zdisp(other, wrap);
        return Norm(dx, dy, dz);
    }
}
