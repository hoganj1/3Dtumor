package Testing;

import Grids.GridDiff2;
import Gui.GuiGridVis;
import Gui.GuiLabel;
import Gui.GuiWindow;
import Tools.TickRateTimer;

/**
 * Created by rafael on 7/18/17.
 */
public class DiffTest2 {
    static final int x=20;
    static final int y=10;
    static final int time=10000;
    static final int visScale=10;
    static final int tickRate=10;
    public static void main(String[] args) {
        //set up gui
        TickRateTimer trt=new TickRateTimer();
        GuiWindow win=new GuiWindow("2D Diffusion Testing",true);
        GuiGridVis v1=new GuiGridVis(x,y,visScale);
        GuiGridVis v2=new GuiGridVis(x,y,visScale);
        win.AddCol(new GuiLabel("normal"),0);
        win.AddCol(v1,0);
        win.AddCol(new GuiLabel("adi"),1);
        win.AddCol(v2,1);
        win.RunGui();

        //set up grids
        GridDiff2 g1=new GridDiff2(x,y);
        GridDiff2 g2=new GridDiff2(x,y);

        //run loop
        for (int i = 0; i < time; i++) {
            trt.TickPause(tickRate);
            //set starting condition
            for (int j = 0; j < y; j++) {
                g1.SetCurr(j,1);
                g2.SetCurr(j,1);
            }
            //do diffusion
            for (int j = 0; j < 4; j++) {
                g1.DiffSwap(0.1);
            }
            g2.ADIDoubleDiffSwap(0.4);
            //draw results
            v1.DrawGridDiff(g1,0,1,"rgb");
            v2.DrawGridDiff(g2,0,1,"rgb");
        }
    }
}
