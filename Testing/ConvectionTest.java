package Testing;

import Grids.GridDiff2;
import Gui.GuiGridVis;
import Gui.GuiWindow;
import Tools.TickRateTimer;
import Tools.Utils;

/**
 * Created by bravorr on 7/21/17.
 */
public class ConvectionTest {
    static int x=40;
    static int y=40;
    static int steps=10000;
    static int time=2;
    public static void main(String[] args) {
        GuiWindow win=new GuiWindow("ConvectionTest",true);
        GuiGridVis ggv=new GuiGridVis(x,y,20);
        double[] xVels=new double[x*y];
        double[] yVels=new double[x*y];
        for (int xi = 0; xi < x; xi++) {
            for (int yi = 0; yi < y; yi++) {

//                xVels[xi*y+yi]=0.9;
//            }
                if(Math.abs(yi-y/2)>Math.abs(xi-x/2)){
                    //at x disp case
                    if(yi<y/2){
                        xVels[xi*y+yi]=-0.1;
                    }
                    else{
                        xVels[xi*y+yi]=0.1;
                    }
                }
                else{
                    //at y disp case
                    if(xi<x/2){
                        yVels[xi*y+yi]=0.1;
                    }
                    else{
                        yVels[xi*y+yi]=-0.1;
                    }
                }
            }
        }
        TickRateTimer trt=new TickRateTimer();
        win.AddCol(ggv,0);
        win.RunGui();
        GridDiff2 g=new GridDiff2(x,y);
        for (int i = 1; i <10 ; i++) {
            for (int j = 1; j <  10; j++) {

                g.SetCurr(i, j, 1);
            }
        }
        for (int i = 0; i < steps; i++) {
            trt.TickPause(time);
            ggv.DrawGridDiff(g,0,1);
            //g.ConvInhomogeneousSwap(xVels,yVels);
            g.ConvVolumeConservingSwap(xVels,yVels);
        }
        win.Dispose();
    }
}
