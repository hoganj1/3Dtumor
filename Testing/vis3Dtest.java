package Testing;

import Gui.Vis3DOpenGL;
import Tools.TickRateTimer;

import static Tools.Utils.*;

/**
 * Created by bravorr on 6/16/17.
 */
class Vis3Dtest {
    public static void main(String[] args) {
        Vis3DOpenGL v3d=new Vis3DOpenGL(640,480,10,10,10,"testing", true);
        float[] circ=GenCirclePoints(0.5f,100);
        TickRateTimer trt=new TickRateTimer();
        int i=0;
        while(!v3d.CheckClosed()) {
            trt.TickPause(100);
            v3d.Clear(0,0,0);
//            v3d.FanShape(5,5,5,1,circ,1,0,0);
            v3d.FanShape(0,0,10,1,circ,0,1,0);
            v3d.FanShape(10,10,0,1,circ,1,0,0);
            i+=1;
            v3d.Show();
            trt.TickPause(10);
        }
    }
}
