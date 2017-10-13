package Examples.KroghModel;

import Grids.AgentSQ2unstackable;
import Grids.Grid2unstackable;
import Grids.GridDiff2;
import Gui.GuiGridVis;
import Gui.GuiWindow;
import Tools.TickRateTimer;

import java.util.Random;

import static Examples.KroghModel.Tissue.*;
import static Tools.Utils.*;

/**
 * Created by Rafael on 8/1/2017.
 */
class Cell extends AgentSQ2unstackable<Tissue>{
    double vesselO2;
    double O2Consumed;
    void Init(boolean isVessel){
        if(isVessel){
            this.vesselO2 =G().VESSEL_O2;
        }
        else{
            this.vesselO2 =-1;
        }
    }
    boolean IsVessel(){
        return vesselO2 !=-1;
    }
    void DiffStep(){
        //vessel production
        if(IsVessel()) {
            G().O2.SetCurr(Isq(),vesselO2);
        }
        //cell consumption
        else{
            double O2Conc=G().O2.GetCurr(Isq());
            O2Consumed=-MichaelisMenten(O2Conc,G().CELL_CONSUMPTION_MAX_RATE,G().CELL_CONSUMPTION_HALF_MAX_CONC);
            G().O2.AddCurr(Isq(),O2Consumed);
        }
    }
    void Step(){
        //cell step function
        if(!IsVessel()) {
            if (-O2Consumed < G().NECROSIS_O2) {
                Dispose();
            }
        }
    }

}

public class Tissue extends Grid2unstackable<Cell>{
    static int SIDE_LEN=15;
    static int TIMESTEPS=1000;
    static int STEP_PAUSE=100;
    static int VIS_SCALE=10;
    TickRateTimer trt;
    double CELL_CONSUMPTION_MAX_RATE=0.05;
    double CELL_CONSUMPTION_HALF_MAX_CONC=0.3;
    double NECROSIS_O2 =0.001;
    double VESSEL_O2=1;
    GridDiff2 O2;
    Random rn;
    TissueVis vis;
    public Tissue(int x,int y){
        super(x,y,Cell.class);
        int[] startPopSquares=CircleCentered(false,5);
        int[] startPopIs=new int[startPopSquares.length/2];
        this.SQsToLocalIs(startPopSquares,startPopIs,xDim/2,yDim/2);
        O2=new GridDiff2(xDim,yDim);
        rn=new Random();
        vis =new TissueVis(this);
        trt=new TickRateTimer();
        Cell firstVessel=NewAgent(xDim/2,yDim/2);
        firstVessel.Init(true);
        IncTick();
        for (int i = 0; i < 100; i++) {
            DiffStep();
        }
        for (int i : startPopIs) {
            Cell c=NewAgent(i);
            c.Init(false);
        }
    }
    void DiffStep(){
        O2.DiffSwap(0.25,0);
        for (Cell c : this) {
            c.DiffStep();
        }
    }
    void Step(){
        trt.TickPause(STEP_PAUSE);
        DiffStep();
        for (Cell c : this) {
            c.Step();
        }
        CleanShuffInc(rn);
        vis.Draw();
    }

    public static void main(String[] args) {
        Tissue t=new Tissue(SIDE_LEN,SIDE_LEN);
        for (int i = 0; i < TIMESTEPS; i++) {
            t.Step();
        }
    }
}

class TissueVis{
    Tissue drawMe;
    GuiWindow win;
    GuiGridVis visCells;
    GuiGridVis visO2;
    public TissueVis(Tissue drawMe){
        this.drawMe=drawMe;
        win=new GuiWindow("KroghModel",true);
        visCells=new GuiGridVis(drawMe.xDim,drawMe.yDim,VIS_SCALE);
        visO2=new GuiGridVis(drawMe.xDim,drawMe.yDim,VIS_SCALE);
        win.AddCol(visCells,0);
        win.AddCol(visO2,1);
        win.RunGui();
    }
    public void Draw(){
        //draw cells
        visCells.DrawAgents(drawMe,(Cell drawMe)->{
            if(drawMe.IsVessel()){
                return ColorInt(1,0,0);
            }
            return ColorInt(1,1,1);
        },0,0,0);
        //draw diffusible
        visO2.DrawGridDiffBound(drawMe.O2,0,1);
    }
}
