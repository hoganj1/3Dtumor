package Grids;

import Tools.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Extend the Grid2unstackable class if you want a 2D lattice with one or more agents per grid square
 * @param <T> the AgentSQ2 or AgentPT2 extending agent class that will inhabit the grid
 */
public class Grid2<T extends AgentSQ2> extends GridBase2D implements Iterable<T>{
    AgentList<T> agents;
    T[] grid;
    int[] counts;

    public T GetAgent(int index) {
        return grid[index];
    }

    public T GetAgent(int x, int y) {
        return grid[I(x, y)];
    }
    /**
     * @param agentClass pass T.class, used to instantiate agent instances within the grid as needed
     */
    public Grid2(int x, int y, Class<T> agentClass,boolean wrapX,boolean wrapY){
        super(x,y,wrapX,wrapY);
        //creates a new grid with given dimensions
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ2[length];
        counts= new int[length];
    }
    public Grid2(int x, int y, Class<T> agentClass){
        super(x,y,false,false);
        //creates a new grid with given dimensions
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ2[length];
        counts= new int[length];
    }

    void RemAgentFromSquare(T agent,int iGrid){
        //internal function, removes agent from grid square
        if(grid[iGrid]==agent){
            grid[iGrid]=(T)agent.nextSq;
        }
        if(agent.nextSq!=null){
            agent.nextSq.prevSq=agent.prevSq;
        }
        if(agent.prevSq!=null){
            agent.prevSq.nextSq=agent.nextSq;
        }
        agent.prevSq=null;
        agent.nextSq=null;
        counts[iGrid]--;
    }
    void AddAgentToSquare(T agent,int iGrid){
        if(grid[iGrid]!=null){
            grid[iGrid].prevSq=agent;
            agent.nextSq=grid[iGrid];
        }
        grid[iGrid]=agent;
        counts[iGrid]++;
    }

    T GetNewAgent(){
        return agents.GetNewAgent();
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(int x, int y){
        T newAgent=GetNewAgent();
        newAgent.Setup(x,y);
        return newAgent;
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(double x, double y){
        T newAgent=GetNewAgent();
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

    public T NewAgentSafe(double newX,double newY,double fallbackX,double fallbackY,boolean wrapX,boolean wrapY){
        if (In(newX, newY)) {
            return NewAgent(newX, newY);
        }
        if (wrapX) {
            newX = Utils.ModWrap(newX, xDim);
        } else if (!Utils.InDim(xDim, newX)) {
            newX = fallbackX;
        }
        if (wrapY) {
            newY = Utils.ModWrap(newY, yDim);
        } else if (!Utils.InDim(yDim, newY))
            newY = fallbackY;
        return NewAgent(newX,newY);
    }
    public T NewAgentSafe(double newX,double newY,double fallbackX,double fallbackY){
        if (In(newX, newY)) {
            return NewAgent(newX, newY);
        }
        if (wrapX) {
            newX = Utils.ModWrap(newX, xDim);
        } else if (!Utils.InDim(xDim, newX)) {
            newX = fallbackX;
        }
        if (wrapY) {
            newY = Utils.ModWrap(newY, yDim);
        } else if (!Utils.InDim(yDim, newY))
            newY = fallbackY;
        return NewAgent(newX,newY);
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

    public void AgentsInRad(final ArrayList<T> putHere, final double x, final double y, final double rad, boolean wrapX, boolean wrapY){
        int nAgents;
        for (int xSq = (int)Math.floor(x-rad); xSq <(int)Math.ceil(x+rad) ; xSq++) {
            for (int ySq = (int)Math.floor(y-rad); ySq <(int)Math.ceil(y+rad) ; ySq++) {
                int retX=xSq; int retY=ySq;
                boolean inX=Utils.InDim(xDim,retX);
                boolean inY=Utils.InDim(yDim,retY);
                if((!wrapX&&!inX)||(!wrapY&&!inY)){
                    continue;
                }
                if(wrapX&&!inX){
                    retX=Utils.ModWrap(retX,xDim);
                }
                if(wrapY&&!inY){
                    retY=Utils.ModWrap(retY,yDim);
                }
                GetAgents(putHere, I(retX,retY));
            }
        }
    }
    public void AgentsInRad(final ArrayList<T> putHere, final double x, final double y, final double rad){
        int nAgents;
        for (int xSq = (int)Math.floor(x-rad); xSq <(int)Math.ceil(x+rad) ; xSq++) {
            for (int ySq = (int)Math.floor(y-rad); ySq <(int)Math.ceil(y+rad) ; ySq++) {
                int retX=xSq; int retY=ySq;
                boolean inX=Utils.InDim(xDim,retX);
                boolean inY=Utils.InDim(yDim,retY);
                if((!wrapX&&!inX)||(!wrapY&&!inY)){
                    continue;
                }
                if(wrapX&&!inX){
                    retX=Utils.ModWrap(retX,xDim);
                }
                if(wrapY&&!inY){
                    retY=Utils.ModWrap(retY,yDim);
                }
                GetAgents(putHere, I(retX,retY));
            }
        }
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
    public Iterator<T> iterator(){
        return agents.iterator();
    }
    public int PopAt(int x, int y){
        //gets population count at location
        return counts[I(x,y)];
    }
    public int PopAt(int i){
        //gets population count at location
        return counts[i];
    }
    public ArrayList<T> AllAgents(){return (ArrayList<T>)this.agents.GetAllAgents();}

    /**
     * appends to the provided arraylist all agents on the square at the specified coordinates
     * @param putHere the arraylist ot be added to
     */
    public void GetAgents(ArrayList<T>putHere, int x, int y){
        T agent= grid[I(x,y)];
        while(agent!=null){
            putHere.add(agent);
            agent=(T)agent.nextSq;
        }
    }
    public void GetAgentsCL(ArrayList<T>putHere, int x, int y){
        putHere.clear();
        T agent= grid[I(x,y)];
        while(agent!=null){
            putHere.add(agent);
            agent=(T)agent.nextSq;
        }
    }

    /**
     * appends to the provided arraylist all agents on the square at the specified index
     * @param putHere the arraylist ot be added to
     */
    public void GetAgents(ArrayList<T>putHere, int i){
        T agent= grid[i];
        while(agent!=null){
            putHere.add(agent);
            agent=(T)agent.nextSq;
        }
    }
    public T GetFirstAgent(int x,int y){
        return grid[x*yDim+y];
    }

    public void GetAgentsIs(ArrayList<T>putHere,int[] fromHere, int x, int y){
        T agent= grid[I(x,y)];
        while(agent!=null){
            putHere.add(agent);
            agent=(T)agent.nextSq;
        }
    }

    /**
     * calls dispose on all agents in the grid, resets the tick timer to 0.
     */
    public void Reset(){
        IncTick();
        for (T a : this) {
           a.Dispose();
        }
        tick=0;
    }

    /**
     * returns the number of agents that are alive in the grid
     */
    public int GetPop(){
        //gets population
        return agents.pop;
    }
}