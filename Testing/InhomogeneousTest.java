package Testing;

import Grids.GridDiff2inhomogeneous;
import Gui.GuiGridVis;
import Gui.GuiWindow;
import Tools.TickRateTimer;

/**
 * Created by bravorr on 6/9/17.
 */
public class InhomogeneousTest {
    public static void main(String[] args) {
        int xDim=100;
        int yDim=100;
        TickRateTimer trt=new TickRateTimer();
        GuiWindow win=new GuiWindow("ex",true);
        GuiGridVis vis=new GuiGridVis(xDim,yDim,10);
        win.AddCol(vis,0);
        win.RunGui();
        GridDiff2inhomogeneous g = new GridDiff2inhomogeneous(xDim, yDim,0.1);
        for (int i = 0; i < g.length; i++) {
            g.SetDiff(i,0.1);
        }
        for (int x = 0; x < xDim; x++) {
                g.SetDiff(x,yDim/2,0);
            g.SetDiff(x,yDim/2+1,0);

        }
        while(true) {
            trt.TickPause(0);
            g.SetCurr(xDim/2,yDim/3,1);
            for (int x = 0; x < xDim; x++) {
                for (int y = 0; y < yDim; y++) {
                    vis.SetColorHeatBound(x,y,g.GetCurr(x,y));
                }
            }
            //g.DiffuseInhomogeneous(false,0.0,false,false);
            //g.Diffuse(0.1,false,0.0,false,false);
            g.SwapInc();
        }
    }
}
