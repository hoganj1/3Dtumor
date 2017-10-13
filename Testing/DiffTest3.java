package Testing;

import Grids.GridDiff3;
import Gui.GuiGridVis;
import Gui.GuiLabel;
import Gui.GuiWindow;
import Tools.TickRateTimer;

/**
 * Created by rafael on 7/18/17.
 */
public class DiffTest3 {
    static final int x=10;
    static final int y=10;
    static final int z=10;
    static final int time=10000;
    static final int visScale=10;
    static final int tickRate=100;
    public static void main(String[] args) {
        //set up gui
        TickRateTimer trt=new TickRateTimer();
        GuiWindow win=new GuiWindow("3D Diffusion Testing",true);
        GuiGridVis v1x=new GuiGridVis(y,z,visScale);
        GuiGridVis v1y=new GuiGridVis(x,z,visScale);
        GuiGridVis v1z=new GuiGridVis(x,y,visScale);
        GuiGridVis v2x=new GuiGridVis(y,z,visScale);
        GuiGridVis v2y=new GuiGridVis(x,z,visScale);
        GuiGridVis v2z=new GuiGridVis(x,y,visScale);
        win.AddCol(new GuiLabel("normal"),0);
        win.AddCol(v1x,0);
        win.AddCol(v1y,0);
        win.AddCol(v1z,0);
        win.AddCol(new GuiLabel("adi"),1);
        win.AddCol(v2x,1);
        win.AddCol(v2y,1);
        win.AddCol(v2z,1);
        win.RunGui();

        //set up grids
        GridDiff3 g1=new GridDiff3(x,y,z);
        GridDiff3 g2=new GridDiff3(x,y,z);

        //run loop
        for (int i = 0; i < time; i++) {
            trt.TickPause(tickRate);
            //set starting condition
            for (int j = 0; j < y*x; j++) {
                g1.SetCurr(j,1);
                g2.SetCurr(j,1);
            }
            //do diffusion
            for (int j = 0; j < 4; j++) {
                g1.DiffSwap(0.1);
            }
            g2.ADITripleDiffSwap(0.1);
            //draw results
            v1x.DrawGridDiffYZ(g1,0,1,"rgb");
            v1y.DrawGridDiffXZ(g1,0,1,"rgb");
            v1z.DrawGridDiffXY(g1,0,1,"rgb");

            v2x.DrawGridDiffYZ(g2,0,1,"rgb");
            v2y.DrawGridDiffXZ(g2,0,1,"rgb");
            v2z.DrawGridDiffXY(g2,0,1,"rgb");
        }
    }
}
