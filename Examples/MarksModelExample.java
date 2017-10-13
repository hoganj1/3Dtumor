package Examples;

import GridExtensions.MarksModelCell;
import GridExtensions.MarksModelGrid;
import Gui.GuiGridVis;
import Gui.GuiLabel;
import Gui.GuiWindow;
import Tools.TickRateTimer;

/**
 * Created by bravorr on 6/28/17.
 */

class MarksModelTestGrid extends MarksModelGrid<MarksModelTestCell>{

    public MarksModelTestGrid(int x, int y) {
        super(x, y, MarksModelTestCell.class);
    }
    public void Step(){
    }
}

class MarksModelTestCell extends MarksModelCell<MarksModelTestGrid>{

}

public class MarksModelExample {
    public static void main(String[] args) {
        int x=200;
        int y=200;
        boolean visOn=true;
        GuiWindow win=new GuiWindow("testDisp",true,visOn);
        GuiGridVis visCells=new GuiGridVis(x,y,2,visOn);
        GuiGridVis visO2=new GuiGridVis(x,y,2,visOn);
        GuiGridVis visAcid=new GuiGridVis(x,y,2,visOn);
        GuiGridVis visGlu=new GuiGridVis(x,y,2,visOn);
        GuiGridVis visPheno=new GuiGridVis(x,y,4,3,3, visOn);
        win.AddCol(new GuiLabel("Cells",visOn),0);
        win.AddCol(visCells,0);
        win.AddCol(new GuiLabel("Oxygen",visOn),0);
        win.AddCol(visO2,0);
        win.AddCol(new GuiLabel("pH",visOn),1);
        win.AddCol(visAcid,1);
        win.AddCol(new GuiLabel("Glucose",visOn),1);
        win.AddCol(visGlu,1);
        win.AddCol(new GuiLabel("red: acid resist green: glycolytic",visOn),2);
        win.AddCol(visPheno,2);
        win.RunGui();
        MarksModelTestGrid a=new MarksModelTestGrid(x,y);
        a.FillGrid(0.8);
        a.CreateTumor(5,a.xDim/2,a.yDim/2);
        a.InitDiffusibles();
        TickRateTimer trt=new TickRateTimer();
        for (int i = 0; i < 10000; i++) {
            trt.TickPause(0);
            a.DefaultStep();
            a.DrawCells(visCells);
            a.DrawMicroEnvHeat(visO2,false,false,true);
            a.DrawMicroEnvHeat(visAcid,false,true,false);
            a.DrawMicroEnvHeat(visGlu,true,false,false);
            a.DrawPheno(visPheno);
        }
        win.Dispose();
    }
}
