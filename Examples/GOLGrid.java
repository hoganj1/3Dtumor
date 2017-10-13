package Examples;

import Grids.AgentSQ2unstackable;
import Grids.Grid2unstackable;

import static Tools.Utils.*;
import Gui.*;

import java.util.Random;

/**
 * Created by rafael on 4/16/17.
 */

class GOLAgent extends AgentSQ2unstackable<GOLGrid>{
    final boolean[] status=new boolean[2];
    boolean GetCurrStatus(){
        return status[G().GetTick()%2];
    }
    boolean GetNextStatus(){
        return status[(G().GetTick()+1)%2];
    }
    void SetNextStatus(boolean alive){
        status[(G().GetTick()+1)%2]=alive;
    }
    public void Step(){
        G().SQsToLocalIs(G().mooreHood,G().neighborIs,Xsq(),Ysq());
        int countNeighbors=0;
        for (int i : G().neighborIs) {
                if(G().GetAgent(i).GetCurrStatus()){
                    countNeighbors++;
                }
        }
        if((GetCurrStatus()&&(countNeighbors==2||countNeighbors==3))||(!GetCurrStatus()&&countNeighbors==3)){
            SetNextStatus(true);
        }
        else{
            SetNextStatus(false);
        }
    }
}

class GOLGrid extends Grid2unstackable<GOLAgent>{
    final GuiGridVis vis;
    final int[] neighborIs;
    final int[] mooreHood;
    final int runTicks;
    final int refreshRateMS;
    final int red=ColorInt(1,0,0);
    final int black=ColorInt(0,0,0);
    GOLGrid(int x, int y, double livingProb, int runTicks, int refreshRateMS, GuiGridVis vis){
        super(x,y,GOLAgent.class,true,true);
        this.vis=vis;
        Random rn=new Random();
        for (int i = 0; i < length; i++) {
            GOLAgent a=NewAgent(i);
            a.SetNextStatus(rn.nextDouble() < livingProb);
        }
        mooreHood=MooreHood(false);
        neighborIs=new int[mooreHood.length/2];
        this.runTicks=runTicks;
        this.refreshRateMS=refreshRateMS;
    }
    public int CountAlive(){
        int count=0;
        for (GOLAgent a : this) {
            if(a.GetCurrStatus()){
                count++;
            }
        }
        return count;
    }
    public void Step(){
        for (GOLAgent a : this) {
            a.Step();
        };
        IncTick();
    }
    public void Run(GuiLabel tickCt){
        for (int i = 0; i < runTicks; i++) {
            Step();
            tickCt.SetText("Tick "+GetTick());
            vis.DrawAgents(this, (GOLAgent a) -> {
                if(a.GetCurrStatus()){ return red; }
                return black;
            });
        }
    }
    public static void main(String[] args){
        int xDim=71;
        int yDim=71;
        double livingProb=0.35;
        int runTicks=10000000;
        int refreshRate=0;
        GuiWindow gui=new GuiWindow("GOL with Agents",true);
        GuiLabel tickCt=new GuiLabel("Tick: ");
        GuiGridVis vis=new GuiGridVis(xDim,yDim,5,true);
        gui.AddCol(tickCt,0);
        gui.AddCol(vis,0);
        GOLGrid gol=new GOLGrid(xDim,yDim,livingProb,runTicks,refreshRate,vis);
        gui.RunGui();
        vis.SetActive(true);
        gol.Run(tickCt);
        gui.Dispose();
    }
}
