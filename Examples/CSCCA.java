package Examples;
import Grids.AgentSQ2unstackable;
import Grids.Grid2unstackable;
import Gui.*;
import Tools.TickRateTimer;
import Tools.Utils;

import java.awt.*;
import java.util.Random;

class CSCCACell extends AgentSQ2unstackable<CSCCAGrid> {
    //cell specific properties
    int divs;
    boolean stem;

    void Init(int divs, boolean stem){
        //set agent properties
        this.divs=divs;
        this.stem=stem;

        //draw agent on visActions if it exists
        if(G().vis!=null) {
            if (stem) {
                G().vis.SetColor(Xsq(), Ysq(), 0, 0, 1);
            }
            else{G().vis.SetColor(Xsq(),Ysq(),((divs+G().maxDivs)*1.0f)/(G().maxDivs*2),0,0); }
        }
    }

    void Die(){
        //set visActions square to black if visActions exists
        if(G().vis!=null) {
            G().vis.SetColor(Xsq(), Ysq(), 0, 0, 0);
        }
        Dispose();
    }

    //requires location to place the child into
    void Divide(int iChildLoc) {
        boolean stemChild=false;
        int divsChild=divs;
        if(stem&&G().rn.nextDouble()<G().stemDivProb){ stemChild=true; }
        else{ divsChild--; }
        CSCCACell child=G().NewAgent(iChildLoc);
        child.Init(divsChild,stemChild);
        if(!stem) {
            divs--;
        }
    }

    void Step(){
        //random death
        if(G().rn.nextDouble()<G().deathProb){
            Die();
            return;
        }
        //check if division event will occur
        if(G().rn.nextDouble()<G().divProb){
            //get moore neighborhood around cell, ignores indices that fall outside the bounds of the model
            int checkLen=G().SQsToLocalIs(G().mooreHood,G().localIs,Xsq(),Ysq(),false,false);
            int openings=0;

            //get indices of all moore neighborhood locations that do not have cells occupying them
            for(int i=0;i<checkLen;i++){
                int iLoc=G().localIs[i];
                if(G().GetAgent(iLoc)==null){
                    G().divIs[openings]=iLoc;
                    openings++;
                }
            }

            //can only divide if there is space
            if(openings>0){
                //die if out of divisions
                if(divs==0){
                    Die();
                    return;
                }
                //choose a random location to divide into
                int divI=G().divIs[G().rn.nextInt(openings)];
                Divide(divI);
            }
        }
    }
}

class CSCCAGrid extends Grid2unstackable<CSCCACell> {
    //model specific params
    final double divProb;
    final double deathProb;
    final double stemDivProb;
    final int maxDivs;

    //value containers used in cell division function
    final int[] divIs=new int[8];
    final int[] localIs=new int[8];
    final int[] mooreHood= Utils.MooreHood(false);

    final GuiGridVis vis;
    final Random rn;

    CSCCAGrid(int xDim, int yDim, double divProb, double deathProb, double stemDivProb, int maxDivs, GuiGridVis vis){
        //grid constructor, passes the agent class which is used to make agents when NewAgent is called
        super(xDim,yDim,CSCCACell.class);
        this.vis=vis;
        this.divProb=divProb;
        this.deathProb=deathProb;
        this.stemDivProb=stemDivProb;
        this.maxDivs=maxDivs;
        rn=new Random();

        //add a first agent to the middle of the world
        CSCCACell c=NewAgent(xDim/2,yDim/2);
        c.Init(maxDivs,true);
    }

}

public class CSCCA {
    public static void main(String[] args){
        //main menu gui defined
        GuiWindow gui=new GuiWindow("CSCCA menu",true);
        //ParamSet stores all menu options
        ParamSet set=new ParamSet();
        gui.AddCol(new DoubleParam(set,"divProb",1.0/24,0,1).SetColor(Color.white,Color.black),0);
        gui.AddCol(new DoubleParam(set,"deathProb",1.0/1000,0,1).SetColor(Color.white,Color.black),0);
        gui.AddCol(new DoubleParam(set,"stemDivProb",7.0/10,0,1).SetColor(Color.white,Color.black),0);
        gui.AddCol(new IntParam(set,"maxDivs",11,1,100).SetColor(Color.white,Color.black),0);
        gui.AddCol(new IntParam(set,"visScale",5,0,10).SetColor(Color.white,Color.black),0);
        gui.AddCol(new IntParam(set,"runTicks",20000,0,1000000).SetColor(Color.white,Color.black),0);
        gui.AddCol(new IntParam(set,"TimeStep",0,0,1000).SetColor(Color.white,Color.black),0);
        gui.AddCol(new IntParam(set,"worldDims",200,0,1000).SetColor(Color.white,Color.black),0);

        //Run button definition, includes run button action
        gui.SetColor(Color.black);
        gui.AddCol(new GuiButton("Run",true,(clickEvent)->{

            //greys out the gui while the model is running
            gui.GreyOut(true);
            final int[] runDuration = {set.GetInt("runTicks")};//array is used so the value can be reset on close

            //visualization gui defined, with close event that causes the model to stop execution
            GuiWindow visGui=new GuiWindow("CSCCA visActions",false,(closeEvent)->{
                runDuration[0]=0;
                gui.GreyOut(false);
            }, true);
            //defines visualization window, sets dimensions and how many gui squares the visualization window will take up
            GuiGridVis vis=new GuiGridVis(set.GetInt("worldDims"),set.GetInt("worldDims"),set.GetInt("visScale"),2,1, true);
            GuiLabel tickLabel=new GuiLabel("GetTick 0");
            GuiLabel popLabel=new GuiLabel("Population 1");
            //visGui contains a label that displays the tick
            visGui.AddCol(tickLabel,0);
            visGui.AddCol(popLabel,1);
            visGui.AddCol(vis,0);
            //defines the grid that will be run, pulling grid initialization values from the menuset
            CSCCAGrid runGrid=new CSCCAGrid(set.GetInt("worldDims"),set.GetInt("worldDims"),set.GetDouble("divProb"),
                    set.GetDouble("deathProb"),set.GetDouble("stemDivProb"),set.GetInt("maxDivs"),vis);
            long timeStep=set.GetInt("TimeStep");
            TickRateTimer timer=new TickRateTimer();
            //starts the visualization gui
            visGui.RunGui();
            while(runGrid.GetTick()< runDuration[0]){
                //iterates over all cells and calls their step function
                for(CSCCACell c:runGrid){
                    c.Step();
                }
                //increments tick, cleans and shuffles the agentlist
                runGrid.CleanShuffInc(runGrid.rn);
                //imposes a tick rate
                timer.TickPause(timeStep);
                //displays the current tick
                tickLabel.SetText("GetTick "+runGrid.GetTick());
                popLabel.SetText("Population "+runGrid.GetPop());
            }
            //destroys visGui when run is complete, calling close event
            visGui.Dispose();
        }),0);
        //starts the main gui
        gui.RunGui();
    }
}
