package Testing;

import Gui.GuiGridVis;
import Gui.GuiWindow;

import java.awt.*;

/**
 * Created by rafael on 7/4/17.
 */
public class ColorTest {
    public static void main(String[] args) {
        GuiWindow win=new GuiWindow("test1",true);
        GuiGridVis ggv=new GuiGridVis(100,100,1);
        win.AddCol(ggv,0);
        win.RunGui();
        long start=System.currentTimeMillis();
        float j=0;
        while(j<1) {
            for (int i = 0; i < 100 * 100; i++) {
                ggv.SetColor(i, j, j, j);
            }
            j += 0.00001;
        }
        System.out.println(System.currentTimeMillis()-start);
        win.Dispose();
    }
}
