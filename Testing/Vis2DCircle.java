package Testing;

import Gui.Vis2DOpenGL;
import Tools.TickRateTimer;
import Tools.Utils;

/**
 * Created by rafael on 5/29/17.
 */
public class Vis2DCircle {
    public static void main(String[] args) {
        Vis2DOpenGL vis = new Vis2DOpenGL(1000, 1000,50,50,"Test", true);
        float[] pts= Utils.GenCirclePoints(1.0f,4);
        TickRateTimer trt=new TickRateTimer();
        float x=0;
        while(!vis.CheckClosed()){
            vis.Clear(x,0,0);
            vis.FanShape(10,10,1,pts,1,1,1);
            vis.Show();
            x+=0.000001;
        }
        vis.Dispose();
    }
}
