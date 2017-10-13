package Examples;

import Grids.GridDiff2;
import Gui.*;
import Tools.TickRateTimer;

import static Tools.Utils.*;

import java.util.Random;

/**
 * Created by rafael on 4/16/17.
 */
public class GOLGridDiff extends GridDiff2{
    final int[]mooreHood;
    final int[]neighborIs;
    final GuiGridVis vis;
    final GuiLabel popLbl;
    final GuiLabel tickLbl;
    final int runTicks;
    final int refreshRateMS;
    final TickRateTimer trt;
    GOLGridDiff(int x, int y, double livingProb, int runTicks, int refreshRateMS, GuiGridVis vis, GuiLabel popLbl, GuiLabel tickLbl){
        super(x,y);
        this.vis=vis;
        mooreHood=MooreHood(false);
        neighborIs=new int[mooreHood.length/2];
        Random rn=new Random();
        trt=new TickRateTimer();
        this.runTicks=runTicks;
        this.refreshRateMS=refreshRateMS;
        this.popLbl=popLbl;
        this.tickLbl=tickLbl;
        for (int i = 0; i < length; i++) {
            SetCurr(i,rn.nextDouble()<livingProb?1:0);
        }
    }
    public void Run(GuiWindow win){
        for (int t = 0; t < runTicks; t++) {
            int totalPop=0;
            for (int x = 0; x < xDim; x++) {
                for (int y = 0; y < yDim; y++) {
                    SQsToLocalIs(mooreHood, neighborIs, x, y, true, true);
                    int countNeighbors = 0;
                    for (int i : neighborIs) {
                        countNeighbors += GetCurr(i);
                    }
                    int status = (int) GetCurr(x, y);
                    if ((status == 1 && (countNeighbors == 2 || countNeighbors == 3)) || (status == 0 && countNeighbors == 3)) {
                        SetNext(x, y, 1);
                    } else {
                        SetNext(x, y, 0);
                    }
                    if (vis != null) {
                        double nextPop=GetNext(x, y);
                        vis.SetColor(x, y, (float) nextPop, 0, 0);
                        totalPop+=nextPop;
                    }
                }
            }
            SwapInc();
            if(popLbl!=null){ popLbl.SetText("Population: "+totalPop); }
            if(tickLbl!=null){ tickLbl.SetText("GetTick: "+ GetTick()); }
        }
        win.Dispose();
    }
    public static void main(String[] args){
        int xDim=100;
        int yDim=100;
        double livingProb=0.1;
        int scaleFactor=5;
        int runTicks=10000000;
        int refreshRate=0;
        GuiWindow gui=new GuiWindow("GOL with GridDiff",true);
        GuiGridVis vis=new GuiGridVis(xDim,yDim,5,2,1, true);
        GuiLabel popLbl=new GuiLabel("Population: 0",1,1);
        GuiLabel tickLbl=new GuiLabel("GetTick: 0",1,1);

        gui.AddCol(popLbl,0);
        gui.AddCol(tickLbl,1);
        gui.AddCol(vis,0);
        gui.RunGui();
        vis.SetActive(false);
        GOLGridDiff g=new GOLGridDiff(xDim,yDim,livingProb,runTicks,refreshRate,vis,popLbl,tickLbl);
        g.Run(gui);
    }
}
