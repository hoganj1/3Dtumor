package Testing;

import Gui.GuiGridVis;
import Gui.GuiWindow;

/**
 * Created by bravorr on 6/2/17.
 */
public class WriteOutTest {
    public static void main(String[] args) {
        GuiWindow testGui=new GuiWindow("test",true);
        GuiGridVis ggv=new GuiGridVis(10,10,5);
        testGui.AddCol(ggv,0);
        ggv.SetColor(4,4,1,1,1);
        ggv.ToGIF("test.jpg");
    }
}
