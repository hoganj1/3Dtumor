package Grids;

import Tools.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Extend the Grid2unstackable class if you want a 3D lattice with one or more agents per grid voxel
 * @param <T> the AgentSQ3 or AgentPT3 extending agent class that will inhabit the grid
 */
public class Grid3 <T extends AgentSQ3> extends GridBase3D implements Iterable<T>{
    AgentList<T> agents;
    T[] grid;
    int[] counts;


    /**
     * @param agentClass pass T.class, used to instantiate agent instances within the grid as needed
     */
    public Grid3(int x,int y,int z, Class<T> agentClass,boolean wrapX,boolean wrapY,boolean wrapZ){
        super(x,y,z,wrapX,wrapY,wrapZ);
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ3[length];
        counts= new int[length];
    }
    public Grid3(int x,int y,int z, Class<T> agentClass){
        super(x,y,z,false,false,false);
        agents=new AgentList<T>(agentClass,this);
        grid=(T[])new AgentSQ3[length];
        counts= new int[length];
    }
    void AddAgentToSquare(T agent,int iGrid){
        //internal function, adds agent to grid voxel
        if(grid[iGrid]==null) {
            grid[iGrid]=agent;
        }
        else{
            grid[iGrid].prevSq=agent;
            agent.nextSq=grid[iGrid];
            grid[iGrid]=agent;
        }
        counts[iGrid]++;
    }
    void RemAgentFromSquare(T agent,int iGrid){
        //internal function, removes agent from grid voxel
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
    T GetNewAgent(){
        return agents.GetNewAgent();
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(int x, int y, int z){
        T newAgent=GetNewAgent();
        newAgent.Setup(x,y,z);
        return newAgent;
    }

    /**
     * returns an uninitialized agent at the specified coordinates
     */
    public T NewAgent(double x, double y, double z){
        T newAgent=GetNewAgent();
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
     * returns the number of agents on the voxel at the specified coordinates
     */
    public int PopAt(int x, int y, int z){
        //gets population count at location
        return counts[I(x,y,z)];
    }
    /**
     * returns the number of agents on the voxel at the specified index
     */
    public int PopAt(int i){
        //gets population count at location
        return counts[i];
    }
    public T NewAgentSafe(double newX,double newY,double newZ,double fallbackX,double fallbackY,double fallbackZ,boolean wrapX,boolean wrapY,boolean wrapZ){
        if (In(newX, newY,newZ)) {
            return NewAgent(newX, newY,newZ);
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
        if (wrapZ) {
            newZ = Utils.ModWrap(newZ, yDim);
        } else if (!Utils.InDim(yDim, newZ))
            newZ = fallbackZ;
        return NewAgent(newX,newY,newZ);
    }
    public T NewAgentSafe(double newX,double newY,double newZ,double fallbackX,double fallbackY,double fallbackZ){
        if (In(newX, newY,newZ)) {
            return NewAgent(newX, newY,newZ);
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
        if (wrapZ) {
            newZ = Utils.ModWrap(newZ, yDim);
        } else if (!Utils.InDim(yDim, newZ))
            newZ = fallbackZ;
        return NewAgent(newX,newY,newZ);
    }
    /**
     * returns an umodifiable copy of the complete agentlist, including dead and just born agents
     */
    public ArrayList<T> AllAgents(){
        return new ArrayList<>(this.agents.GetAllAgents());
    }

    /**
     * Gets the last agent to move to the specified coordinates
     * not recommended if the model calls for multiple agents on the same square, as it will only return one of these
     * returns null if no agent exists
     */
    public T GetFirstAgent(int x, int y, int z){
        return grid[I(x,y,z)];
    }

    public void AgentsInRad(final ArrayList<T> putHere, final double x, final double y, final double z, final double rad, boolean wrapX, boolean wrapY, boolean wrapZ){
        int nAgents;
        for (int xSq = (int)Math.floor(x-rad); xSq <(int)Math.ceil(x+rad) ; xSq++) {
            for (int ySq = (int)Math.floor(y-rad); ySq <(int)Math.ceil(y+rad) ; ySq++) {
                for (int zSq = (int)Math.floor(z-rad); zSq <(int)Math.ceil(z+rad) ; zSq++) {
                    int retX = xSq;
                    int retY = ySq;
                    int retZ = zSq;
                    boolean inX = Utils.InDim(xDim, retX);
                    boolean inY = Utils.InDim(yDim, retY);
                    boolean inZ = Utils.InDim(zDim, retZ);
                    if ((!wrapX && !inX) || (!wrapY && !inY) || (!wrapZ && !inZ)) {
                        continue;
                    }
                    if (wrapX && !inX) {
                        retX = Utils.ModWrap(retX, xDim);
                    }
                    if (wrapY && !inY) {
                        retY = Utils.ModWrap(retY, yDim);
                    }
                    if (wrapZ && !inZ) {
                        retZ = Utils.ModWrap(retZ, zDim);
                    }
                    GetAgents(putHere, I(retX, retY, retZ));
                }
            }
        }
    }
    public void AgentsInRad(final ArrayList<T> putHere, final double x, final double y, final double z, final double rad){
        int nAgents;
        for (int xSq = (int)Math.floor(x-rad); xSq <(int)Math.ceil(x+rad) ; xSq++) {
            for (int ySq = (int)Math.floor(y-rad); ySq <(int)Math.ceil(y+rad) ; ySq++) {
                for (int zSq = (int)Math.floor(z-rad); zSq <(int)Math.ceil(z+rad) ; zSq++) {
                    int retX = xSq;
                    int retY = ySq;
                    int retZ = zSq;
                    boolean inX = Utils.InDim(xDim, retX);
                    boolean inY = Utils.InDim(yDim, retY);
                    boolean inZ = Utils.InDim(zDim, retZ);
                    if ((!wrapX && !inX) || (!wrapY && !inY) || (!wrapZ && !inZ)) {
                        continue;
                    }
                    if (wrapX && !inX) {
                        retX = Utils.ModWrap(retX, xDim);
                    }
                    if (wrapY && !inY) {
                        retY = Utils.ModWrap(retY, yDim);
                    }
                    if (wrapZ && !inZ) {
                        retZ = Utils.ModWrap(retZ, zDim);
                    }
                    GetAgents(putHere, I(retX, retY, retZ));
                }
            }
        }
    }
    /**
     * Gets the last agent to move to the specified index
     * not recommended if the model calls for multiple agents on the same square, as it will only return one of these
     * returns null if no agent exists
     */
    public T GetFirstAgent(int i){
        return grid[i];
    }

    /**
     * appends to the provided arraylist all agents on the square at the specified coordinates
     * @param putHere the arraylist ot be added to
     */
    public void GetAgents(ArrayList<T>putHere, int x, int y, int z){
        T agent= GetFirstAgent(x,y,z);
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
