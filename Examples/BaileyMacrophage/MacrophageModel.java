package Examples.BaileyMacrophage;

import GridExtensions.MarksModelCell;
import GridExtensions.MarksModelGrid;
import Grids.*;
import Gui.*;
import Tools.FileIO;
import Tools.TickRateTimer;
import static Examples.BaileyMacrophage.MacroGrid.*;

import Gui.GuiGridVis;
import Gui.GuiLabel;
import Gui.GuiWindow;
import Tools.Utils;
import jdk.nashorn.internal.runtime.regexp.joni.ScanEnvironment;
import sun.java2d.loops.ScaledBlit;

import static Tools.Utils.*;

import java.util.Random;

/**
 * Created by bravorr on 6/28/17.
 */

class MacroGrid extends Grid2<MacroCell> {
    final static int NO_CELL=0;
    final static int NECROTIC_CELL=-1;
    final static int TUMOR_CELL=1;
    final static int DIGESTING_CAP=5;
    final static double MIGRATE_SPEED=2;
    final static double DIGESTION_TIME=1;
    final static double DIGESTION_STEP=1;

    final static double CCL2_DIFF_RATE=0.25;
    final static double CCL2_MAX_DIFF=500;
    final static double CCL2_DECAY=.982;

    final static double CCL2_DISSOCIATION_CONSTANT=10000;
    final static double CCL2_HILL_CONSTANT=1;

    final static double TGFB1_DIFF_RATE=0.25;
    final static double TGFB1_MAX_DIFF=500;
    final static double TGFB1_DECAY=0.99;

    final static double TGFB1_DISSOCIATION_CONSTANT=800;
    final static double TGFBI_HILL_CONSTANT=1;

    final static int MACROPHAGE_STARTING_POP=100;
    final static int STEP_TOTAL=700;

    double[]coordScratch=new double[2];


    GridDiff2 ccl2DiffGrid;
    GridDiff2 tgfb1DiffGrid;




    MarksModelTestGrid cells;
    Random rn;
    final static float[] drawCirclePts= GenCirclePoints(1,10);
    MacroGrid(int x,int y,int nStartingPop,MarksModelTestGrid cells){
        super(x,y,MacroCell.class);
        rn=new Random();
        this.cells=cells;
        for (int i = 0; i < nStartingPop ; i++) {
            double xPos = rn.nextDouble() * xDim;
            double yPos = rn.nextDouble() * yDim;
            NewMacroCell(xPos, yPos);
        }
        ccl2DiffGrid= new GridDiff2(x, y);
        tgfb1DiffGrid= new GridDiff2(x, y);
    }
    public MacroCell NewMacroCell(double x, double y) {
        MacroCell ret = NewAgent(x, y);
        ret.Init();
        return ret;
    }

    public void MacroDraw(Vis2DOpenGL vis){
        vis.Clear(0,0,0);
        for (MacroCell c : this) {
            float gColor=(float)c.scaledArg1Express;
            float rColor=(float)c.scaledCcrl2Express;
            float bColor=(float)0;


            vis.FanShape((float)c.Xpt(),(float)c.Ypt(),1,drawCirclePts, rColor, gColor, bColor);
        }
        vis.Show();
    }
    public void Step(){
        for (MacroCell c : this) {
            c.Step();
        }
        CleanShuffInc(rn);
        Ccl2DiffLoop();
    }
    public void Ccl2DiffLoop() {
        int DiffCount = 0;
        double ccl2Diff = 0;
        double tgfbDiff = 0;
        do {
            for (MacroCell c : this) {
                if (c.firstBlood) {
                    ccl2DiffGrid.AddCurr(c.Xsq(), c.Ysq(), c.ccl2Express);
                    tgfb1DiffGrid.AddCurr(c.Xsq(), c.Ysq(), c.tgfb1Express);
                }
            }
            for (int i = 0; i < ccl2DiffGrid.length; i++) {
                ccl2DiffGrid.SetCurr(i,ccl2DiffGrid.GetCurr(i)*CCL2_DECAY);
            }
            for (int i = 0; i < tgfb1DiffGrid.length; i++) {
                tgfb1DiffGrid.SetCurr(i,tgfb1DiffGrid.GetCurr(i)*CCL2_DECAY);
            }
            ccl2Diff = ccl2DiffGrid.MaxDiff(false);
            tgfbDiff = tgfb1DiffGrid.MaxDiff(false);
            ccl2DiffGrid.DiffSwap(CCL2_DIFF_RATE);
            tgfb1DiffGrid.DiffSwap(TGFB1_DIFF_RATE);

            DiffCount++;
        }
        while (ccl2Diff > CCL2_MAX_DIFF || tgfbDiff > TGFB1_MAX_DIFF);
    }
}

class MacroCell extends AgentPT2<MacroGrid> {
    int[] cellsBeingDigested=new int[DIGESTING_CAP];
    double[] digestionTimes=new double[DIGESTING_CAP];
    boolean firstBlood;
    double phenotypeId;
    double reversePheno;
    double ccl2Express;
    double ccrl2Express;
    double fcer1gExpress;
    double scaledFcer1gExpress;
    double mrc1Express;
    double scaledMrc1Express;
    double tgfb1Express;
    double tgfbr1Express;
    double vegfaExpress;
    double arg1Express;
    double scaledInflam;
    double scaledAntiInflam;
    double scaledArg1Express;
    double scaledCcrl2Express;


    double ph;
    double inflam;
    double antiInflam;
    int tumor;
    double scaledTumor;
    int necrotic;
    double scaledNecrotic;
    double envCond;

    int iOpen=0;
    public void Init() {
        for (int i = 0; i < DIGESTING_CAP; i++) {
            firstBlood = false;
            cellsBeingDigested[i] = 0;
            digestionTimes[i] = 0;
            iOpen = 0;
        }
    }


    public void SetExpression(){
        ph= ConvertHToPh(G().cells.protons.GetCurr(Isq()));
        inflam= ScaleData(inflam, 0, 4000, 0, 1);
        antiInflam= ScaleData(antiInflam, 0, 3500, 0, 1);
        scaledTumor= tumor/5d;
        scaledNecrotic= necrotic/5d;
        double envCond= (-0.5*inflam)+(0.5*antiInflam)+(-0.5*(tumor))+(0.5*(necrotic));

        CalcEnvCond(inflam, antiInflam);

        ccl2Express= PredictExpression(-140893, 22274, 121063, -18943 );
        ccrl2Express= PredictExpression(-383.1, 225.3, 449.0, -228.5 );
        tgfb1Express= PredictExpression(3465.40, -62.42, 6138.16, -796.27 );
        tgfbr1Express= PredictExpression(-6041.1, 1246.0, 1788.3, -210.3 );
        fcer1gExpress= PredictExpression(11402.7, -702.9, -4451.6, 290.0);
        mrc1Express= PredictExpression(22419, -2523, 19382, -2195);
        arg1Express= PredictExpression(126004, -15936, 120034, -15150);

        scaledCcrl2Express= ScaleData(ccrl2Express, 0, 2500, 0, 1);
        scaledArg1Express= ScaleData(arg1Express, 0, 50000, 0, 1);
        scaledFcer1gExpress= ScaleData(fcer1gExpress, 0, 10000, 0, 5);
        scaledMrc1Express= ScaleData(mrc1Express, 0, 10000, 0, 5);
    }

    public double PredictExpression(double intercept, double phCoef, double envCondCoef, double interactionCoef){
        return intercept+(phCoef*ph)+(envCondCoef*envCond)+(interactionCoef*ph*envCond);

    }

    public double CalcEnvCond(double inflam, double antiInflam){
        envCond= (-0.5*inflam)+(0.5*antiInflam)+(-0.5*(scaledTumor))+(0.5*(scaledNecrotic));
        ph=7.4;
        return envCond;
    }

    public double ScaleData(double x, double inMin, double inMax, double outMin, double outMax){
        return ((outMax - outMin)*(x-inMin))/(inMax - inMin) + outMin;
    }

    public boolean StartDigesting(MarksModelTestCell c){
        if(digestionTimes[iOpen]<=0){
            if (c.isCancer) {
                this.tumor++;
                cellsBeingDigested[iOpen] = TUMOR_CELL;
                if(firstBlood==false) {
                    firstBlood = true;
                }
            }
            else {
                this.necrotic++;
                cellsBeingDigested[iOpen] = NECROTIC_CELL;
            }
            digestionTimes[iOpen]=DIGESTION_TIME;
            iOpen=(iOpen+1)%DIGESTING_CAP;
            c.Dispose();
            return true;
        }
        return false;
    }
    public void Step(){
            Chemotaxis(G().ccl2DiffGrid.GradientX(Xsq(), Ysq()), G().ccl2DiffGrid.GradientY(Xsq(), Ysq()), G().coordScratch, 10);
            for (int i = 0; i < DIGESTING_CAP; i++) {
                if (digestionTimes[i] != 0) {
                    digestionTimes[i] -= DIGESTION_STEP;
                } else {
                    if (digestionTimes[i] == 0) {
                        int cellType;
                        cellType = cellsBeingDigested[i];
                        if (cellType == TUMOR_CELL) {
                            this.tumor = this.tumor - 1;
                            cellsBeingDigested[i] = 0;
                        } else if (cellType == NECROTIC_CELL) {
                            this.necrotic = this.necrotic - 1;
                            cellsBeingDigested[i] = 0;
                        }
                    }
                }
            }

            Digest();
            SetExpression();
            CalcInflam(G().ccl2DiffGrid.GetCurr(Isq()));
            CalcAntiInflam(G().tgfb1DiffGrid.GetCurr(Isq()));
    }

    public MarksModelTestGrid M(){
        return G().cells;
    }

    public void Digest(){
        MarksModelTestCell c=M().GetAgent(Xsq(),Ysq());
        if(c!=null){
            if((!c.IsAlive()&&necrotic<scaledMrc1Express)||(c.isCancer&&tumor<scaledFcer1gExpress)){
                StartDigesting(c);
            }
        }
    }

    public void Migrate(){
        double angle=G().rn.nextDouble()*Math.PI*2;
        double y=Math.sin(angle)*MIGRATE_SPEED;
        double x=Math.cos(angle)*MIGRATE_SPEED;
        MoveSafe(Xpt()+x,Ypt()+y,false,false);
    }

    double[] xAndY= new double[2];
    double[] weights= new double[2];
//    public void Ccl2Chemtaxis(){
//        try {
//            double xWeight=G().ccl2DiffGrid.GradientX(Xsq(),Ysq());
//            double yWeight=G().ccl2DiffGrid.GradientY(Xsq(),Ysq());
//
////            double sumX=(G().ccl2DiffGrid.GetCurr(Xsq()-1, Ysq())+G().ccl2DiffGrid.GetCurr(Xsq()+1,Ysq())+G().ccl2DiffGrid.GetCurr(Xsq(),Ysq()));
////            double sumY=(G().ccl2DiffGrid.GetCurr(Xsq(), Ysq()-1)+G().ccl2DiffGrid.GetCurr(Xsq(),Ysq()+1)+G().ccl2DiffGrid.GetCurr(Xsq(),Ysq()));
////
////            if (sumX!=0&&sumY!=0) {
////                xWeight= xWeight/sumX;
////                yWeight= yWeight/sumY;
////            } else {
////                xWeight=0;
////                yWeight=0;
////            }
//
//            weights[0] = xWeight;
//            weights[1] = yWeight;
//        } catch (Exception e) {
//            System.out.println("un weighted migration because on edge");
//            weights[0] = 0;
//            weights[1] = 0;
//        }

//        weights[0]=0;
//        weights[1]=0;
//        RandomWeightedPointOnCircle(MIGRATE_SPEED, 0.01, G().rn, weights,xAndY);
//        if (!Double.isNaN(xAndY[0]) && !Double.isNaN(xAndY[1])) {
//            MoveSafe(Xpt()+xAndY[0],Ypt()+xAndY[1],false,false);
//        }
//    }

    public void Chemotaxis(double gradX,double gradY,double[] out,double moveRad){
        gradX=gradX/1000;
        gradY=gradY/1000;
        double gradMag=Norm(gradX,gradY);
//        System.out.println(gradMag);
        double maxCenterDisp=moveRad/4;
        if(gradMag>maxCenterDisp){
            //forcing movement circle center to be at most maxCenterDisp away
            gradX=gradX*maxCenterDisp/gradMag;
            gradY=gradY*maxCenterDisp/gradMag;
            gradMag=maxCenterDisp;
        }
        double moveCircleRad=moveRad-gradMag;
        RandomPointInCircle(moveCircleRad,G().rn,G().coordScratch);
        //adding center of circle and point coordinates to x and y position to compute destination
        double nextX=Xpt()+gradX+G().coordScratch[0];
        double nextY=Ypt()+gradY+G().coordScratch[1];
        MoveSafe(nextX,nextY);
    }

    public void CalcInflam(double ccl2Concentration){
        double ccl2Bound= 0;
        ccl2Concentration= G().ccl2DiffGrid.GetCurr(Isq());
        ccl2Bound= HillEqn(ccl2Concentration, CCL2_DISSOCIATION_CONSTANT, CCL2_HILL_CONSTANT)*ccrl2Express;
        if (ccl2Bound > ccl2Concentration){
            ccl2Bound= ccl2Concentration;
        }
        G().ccl2DiffGrid.AddCurr(Isq(), -ccl2Bound);
        this.inflam= ccl2Bound;
    }

    public void CalcAntiInflam(double tgfb1Concentration){
        double tgfb1Bound= 0;
        tgfb1Concentration= G().tgfb1DiffGrid.GetCurr(Isq());
        tgfb1Bound= (HillEqn(tgfb1Concentration, TGFB1_DISSOCIATION_CONSTANT, TGFBI_HILL_CONSTANT))*tgfbr1Express;
        if (tgfb1Bound > tgfb1Concentration){
            tgfb1Bound= tgfb1Concentration;
        }
        G().tgfb1DiffGrid.AddCurr(Isq(), -tgfb1Bound);
        this.antiInflam= tgfb1Bound;
    }


}

class MarksModelTestGrid extends MarksModelGrid<MarksModelTestCell> {

    MacroGrid macros;
    public MarksModelTestGrid(int x, int y) {
        super(x, y, MarksModelTestCell.class);
        macros=new MacroGrid(x,y,MACROPHAGE_STARTING_POP,this);
        //OXYGEN_DIFF_RATE =500f*DIFF_TIME_STEP*1.0f/(GRID_SIZE*GRID_SIZE);

    }

    public void MyStep(){
        DiffLoop(false);
        for (MarksModelCell c : this) {
            c.DefaultStep();
        }

        CleanShuffInc(rn);
        Angiogenesis();

    }

}

class MarksModelTestCell extends MarksModelCell<MarksModelTestGrid> {

    public void MyNewStep(){}

}

public class MacrophageModel {
    public static void main(String[] args) {
        int x=100;
        int y=100;
        GuiWindow win=new GuiWindow("testDisp",true);
        GuiGridVis visCells=new GuiGridVis(x,y,2);
        GuiGridVis visO2=new GuiGridVis(x,y,2);
        GuiGridVis visAcid=new GuiGridVis(x,y,2);
        GuiGridVis visGlu=new GuiGridVis(x,y,2);
        GuiGridVis visPheno=new GuiGridVis(x,y,4,1,3);
        GuiGridVis visCcl2=new GuiGridVis(x,y,2);
        GuiGridVis visTGFb=new GuiGridVis(x,y,2);
        GuiGridVis visActive=new GuiGridVis(x,y,2);
        Vis2DOpenGL visMacro=new Vis2DOpenGL(200,200,x,y,"Macrophages", true);
        win.AddCol(new GuiLabel("Cells"),0);
        win.AddCol(visCells,0);
        win.AddCol(new GuiLabel("Oxygen"),0);
        win.AddCol(visO2,0);
        win.AddCol(new GuiLabel("pH"),1);
        win.AddCol(visAcid,1);
        win.AddCol(new GuiLabel("Glucose"),1);
        win.AddCol(visGlu,1);
        win.AddCol(new GuiLabel("red: acid resist green: glycolytic"),2);
        win.AddCol(visPheno,2);
        win.AddCol(new GuiLabel("CCL2"),3);
        win.AddCol(visCcl2,3);
        win.AddCol(new GuiLabel("TGFb"),3);
        win.AddCol(visTGFb,3);
        win.AddCol(visActive,4);
        win.RunGui();
        MarksModelTestGrid g=new MarksModelTestGrid(x,y);
        g.FillGrid(0.8);
        g.CreateTumor(5,g.xDim/2,g.yDim/2);
        g.InitDiffusibles();
        TickRateTimer trt=new TickRateTimer();
        FileIO out=new FileIO("testOut.csv","w");
        out.Write("AvgPH,AvgArg1,AvgCCRL2,tumorCt,necroCt\n");
        for (int i = 0; i < STEP_TOTAL; i++) {
            trt.TickPause(100);
            g.MyStep();
            g.macros.Step();
            System.out.println(g.GetTick());
            g.DrawCells(visCells);
            g.DrawMicroEnvHeat(visO2,false,false,true);
            g.DrawMicroEnvHeat(visAcid,false,true,false);
            g.DrawMicroEnvHeat(visGlu,true,false,false);
            g.DrawPheno(visPheno);
            g.macros.MacroDraw(visMacro);
            visCcl2.DrawGridDiffBound(g.macros.ccl2DiffGrid,0,450000,"gbr");
            visTGFb.DrawGridDiffBound(g.macros.tgfb1DiffGrid,0,40000,"bgr");
            visActive.DrawAgents(g.macros,(MacroCell c)->{
                if(c.firstBlood){
                    return Utils.ColorInt(1,1,0);
                }
                else{
                    return Utils.ColorInt(1,0,0);
                }
            },0,0,0);

            //Getting Values of interest for the timestep
            int tumorCt=0;
            int necroCt=0;
            for (MarksModelTestCell c : g) {
                if(c.IsAlive()&&c.isCancer){
                    tumorCt++;
                }
                if(!c.IsAlive()){
                    necroCt++;
                }
            }
            double avgPH=Utils.ConvertHToPh(g.protons.AvgCurr());
            double arg1Sum=0;
            double ccrl2Sum=0;
            for(MacroCell c:g.macros){
                arg1Sum+=c.arg1Express;
                ccrl2Sum+=c.ccrl2Express;
            }
            out.Write(avgPH+","+arg1Sum+","+ccrl2Sum+","+tumorCt+","+necroCt+"\n");
        }
        out.Close();
        win.Dispose();
    }

}



