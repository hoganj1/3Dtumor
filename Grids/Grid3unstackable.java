package Grids;
import Tools.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Extend the Grid2unstackable class if you want a 3D lattice with one or more agents per grid voxel
 * @param <T> the AgentSQ3 or AgentPT3 extending agent class that will inhabit the grid
 */
public class Grid3unstackable<T extends AgentSQ3unstackable> extends GridBase3D implements Iterable<T>{
    AgentList<T> agents;
    T[] grid;

    /**
     * @param agentClass pass T.class, used to instantiate agent instances within the grid as needed
     */
    public Grid3unstackable(int x, int y, int z, Class<T> agentClass,boolean wrapX,boolean wrapY,boolean wrapZ){
        super(x,y,z,wrapX,wrapY,wrapZ);
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ3unstackable[length];
    }
    public Grid3unstackable(int x, int y, int z, Class<T> agentClass){
        super(x,y,z,false,false,false);
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ3unstackable[length];
    }
    /**
     * gets the index of the voxel at the specified coordinates with wrap around
     */
    public int SQwrapI(int x, int y, int z){
        //wraps Coords to proper index
        if(In(x,y,z)) { return I(x,y,z);}
        return I(Utils.ModWrap(x,xDim),Utils.ModWrap(y,yDim),Utils.ModWrap(z,zDim));
    }

    /**
     * gets the index of the voxel at the specified coordinates
     */
    public int I(int x, int y, int z){
        //gets grid index from location
        return x*yDim*zDim+y*zDim+z;
    }

    /**
     * gets the xDim component of the voxel at the specified index
     */
    public int ItoX(int i){
        return i/(yDim*zDim);
    }

    /**
     * gets the yDim component of the voxel at the specified index
     */
    public int ItoY(int i){return (i/zDim)%yDim;}

    /**
     * gets the z component of the voxel at the specified index
     */
    public int ItoZ(int i){
        return i%zDim;
    }

    /**
     * gets the index of the square that contains the specified coordinates
     */
    public int I(double x, double y, double z){
        //gets grid index from location
        return (int)Math.floor(x)*yDim*zDim+(int)Math.floor(y)*yDim+(int)Math.floor(z);
    }

    /**
     * returns whether the specified coordinates are inside the grid bounds
     */
    public boolean In(int x, int y, int z){
        if(x>=0&&x<xDim&&y>=0&&y<yDim&&z>=0&&z<zDim){
            return true;
        }
        return false;
    }

    /**
     * returns whether the specified coordinates are inside the grid bounds
     */
    public boolean In(double x, double y, double z){
        int xInt=(int)Math.floor(x);
        int yInt=(int)Math.floor(y);
        int zInt=(int)Math.floor(z);
        return In(xInt,yInt,zInt);
    }
    void AddAgentToSquare(T agent,int iGrid){
        //internal function, adds agent to grid voxel
        if(grid[iGrid]!=null) {
            throw new RuntimeException("Adding multiple agents on the same square!");
        }
        else{
            grid[iGrid]=agent;
        }
    }

    void RemAgentFromSquare(T agent,int iGrid){
        grid[iGrid]=null;
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(int x, int y, int z){
        T newAgent=agents.GetNewAgent();
        newAgent.Setup(x,y,z);
        return newAgent;
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(double x, double y, double z){
        T newAgent=agents.GetNewAgent();
        newAgent.Setup(x,y,z);
        return newAgent;
    }
    /**
     * returns an uninitialized agent at the specified index
     */
    public T NewAgent(int i){
        T newAgent=agents.GetNewAgent();
        newAgent.Setup(ItoX(i),ItoY(i),ItoZ(i));
        return newAgent;
    }
    void RemoveAgent(T agent,int iGrid){
        //internal function, removes agent from world
        RemAgentFromSquare(agent, iGrid);
        agents.RemoveAgent(agent);
    }

    /**
     * shuffles the agent list to randomize iteration
     * do not call this while in the middle of iteration
     * @param rn the Random number generator to be used
     */
    public void ShuffleAgents(Random rn){
        agents.ShuffleAgents(rn);
    }

    /**
     * cleans the list of agents, removing dead ones, may improve the efficiency of the agent iteration if many agents have died
     * do not call this while in the middle of iteration
     */
    public void CleanAgents(){
        agents.CleanAgents();
    }

    /**
     * calls CleanAgents, then SuffleAgents, then IncTick. useful to call at the end of a round of iteration
     * do not call this while in the middle of iteration
     * @param rn the Random number generator to be used
     */
    public void CleanShuffInc(Random rn){
        CleanAgents();
        ShuffleAgents(rn);
        IncTick();
    }
    public void CleanInc(){
        CleanAgents();
        IncTick();
    }

    /**
     * returns an umodifiable copy of the complete agentlist, including dead and just born agents
     */
    public ArrayList<T> AllAgents(){return (ArrayList<T>)this.agents.GetAllAgents();}

    /**
     * Gets the agent at the specified index
     */
    public T GetAgent(int i){
        return grid[i];
    }

    /**
     * Gets the agent at the specified coordinates
     */
    public T GetAgent(int x, int y, int z){
        return grid[I(x,y,z)];
    }


    /**
     * returns the number of agents that are alive in the grid
     */
    public int GetPop(){
        //gets population
        return agents.pop;
    }

    @Override
    public Iterator<T> iterator() {
        return agents.iterator();
    }
}
