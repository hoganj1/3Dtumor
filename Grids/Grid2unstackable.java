package Grids;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Extend the Grid2unstackable class if you want a 2D lattice with at most one agent per grid square
 * @param <T> the AgentSQ2unstackable extending agent class that will inhabit the grid
 */
public class Grid2unstackable<T extends AgentSQ2unstackable> extends GridBase2D implements Iterable<T>{
    AgentList<T> agents;
    T[] grid;

    /**
     * @param agentClass pass T.class, used to instantiate agent instances within the grid as needed
     */
    public Grid2unstackable(int xDim, int yDim, Class<T> agentClass,boolean wrapX,boolean wrapY){
        super(xDim,yDim,wrapX,wrapY);
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ2unstackable[length];
    }
    public Grid2unstackable(int xDim, int yDim, Class<T> agentClass){
        super(xDim,yDim,false,false);
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ2unstackable[length];
    }

    void RemAgentFromSquare(T agent,int iGrid){
        grid[iGrid]=null;
    }
    void AddAgentToSquare(T agent,int iGrid){
        if(grid[iGrid]!=null){
            throw new RuntimeException("Adding multiple agents on the same square!");
        }
        grid[iGrid]=agent;
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(int x, int y){
        T newAgent=agents.GetNewAgent();
        newAgent.Setup(x,y);
        return newAgent;

    }

    /**
     * returns an uninitialized agent at the specified index
     */
    public T NewAgent(int i){
        T newAgent=agents.GetNewAgent();
        newAgent.Setup(ItoX(i),ItoY(i));
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
     * calls CleanAgents, then SuffleAgents, then IncTick. useful to call at the end of a tick
     * do not call this while in the middle of iteration
     * @param rn the Random number generator to be used
     */
    public void CleanShuffInc(Random rn){
        agents.CleanAgents();
        agents.ShuffleAgents(rn);
        IncTick();
    }
    public void CleanInc(Random rn){
        agents.CleanAgents();
        IncTick();
    }

    /**
     * Gets the agent at the specified coordinates
     * returns null if no agent exists
     */
    public T GetAgent(int x, int y){ return grid[I(x,y)]; }

    /**
     * Gets the agent at the specified index
     * returns null if no agent exists
     */
    public T GetAgent(int i){ return grid[i]; }

    /**
     * returns an umodifiable copy of the complete agentlist, including dead and just born agents
     */
    public ArrayList<T> AllAgents(){return (ArrayList<T>)this.agents.GetAllAgents();}

    /**
     * calls dispose on all agents in the grid
     */
    public void Reset(){
        List<T> AllAgents=this.agents.GetAllAgents();
        AllAgents.stream().filter(curr -> curr.alive).forEach(AgentSQ2unstackable::Dispose);
        tick=0;
    }

    public int FindEmptyNeighbors(int[] SQs,int[]ret,int centerX,int centerY,boolean wrapX,boolean wrapY){
        int nIs=SQsToLocalIs(SQs,ret,centerX,centerY,wrapX,wrapY);
        int validCount=0;
        for (int i = 0; i < nIs; i++) {
            if(GetAgent(ret[i])==null){
                ret[validCount]=ret[i];
                validCount++;
            }
        }
        return validCount;
    }
    public int FindEmptyNeighbors(int[] SQs,int[]ret,int centerX,int centerY){
        int nIs=SQsToLocalIs(SQs,ret,centerX,centerY,wrapX,wrapY);
        int validCount=0;
        for (int i = 0; i < nIs; i++) {
            if(GetAgent(ret[i])==null){
                ret[validCount]=ret[i];
                validCount++;
            }
        }
        return validCount;
    }
    public int FindOccupiedNeighbors(int[] SQs,int[]ret,int centerX,int centerY,boolean wrapX,boolean wrapY){
        int nIs=SQsToLocalIs(SQs,ret,centerX,centerY,wrapX,wrapY);
        int validCount=0;
        for (int i = 0; i < nIs; i++) {
            if(GetAgent(ret[i])!=null){
                ret[validCount]=ret[i];
                validCount++;
            }
        }
        return validCount;
    }
    public int FindOccupiedNeighbors(int[] SQs,int[]ret,int centerX,int centerY){
        int nIs=SQsToLocalIs(SQs,ret,centerX,centerY,wrapX,wrapY);
        int validCount=0;
        for (int i = 0; i < nIs; i++) {
            if(GetAgent(ret[i])!=null){
                ret[validCount]=ret[i];
                validCount++;
            }
        }
        return validCount;
    }

    /**
     * returns the number of agents that are alive in the grid
     */
    public int GetPop(){
        //gets population
        return agents.pop;
    }

    @Override
    public Iterator<T> iterator(){
        return agents.iterator();
    }

}
