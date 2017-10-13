package Examples;

import GridExtensions.CircleForceAgent3;
import Grids.Grid3;
import Grids.GridDiff3;
import Gui.Vis3DOpenGL;
import Tools.TickRateTimer;
import Tools.Utils;

import java.util.ArrayList;
import java.util.Random;

import static Tools.Utils.*;

/**
 * Created by bravorr on 6/15/17.
 */


//18mm diameter 1mm depth
//2x2x1
public class Polyp3DOL extends Grid3<Cell3DOL> {
    final GridDiff3 resource;
    final Random rn;
    TickRateTimer trt;
    final double cellCycleStart =1.5;
    final double cellCycleWiggle =1;
    final double consumptionMax =0.1;
    final double consumptionHalfMaxConc=0.2;
    final double diffRate =0.2;
    final double deathConcMax =0.01;
    final double deathConcMin =0.0001;
    final double deathConcRange=deathConcMax-deathConcMin;
    final double quiescentConc=0.08;
    final long tickRate=7;
    final int initDiffSteps=1000;
    final int timeSteps=100000;
    final Vis3DOpenGL vis;
    final double everyWhereSourceVal=0.1;

    final double cellRad=0.3;
    final double cellRadGrowth=0.1;
    final float[]circlePts=GenCirclePoints(1,20);
    final double interactionRad=(cellRad+cellRadGrowth)*3;//cell attraction
    final double divRad=cellRad*(2.0/3.0);
    final double maxForceDiv=1;

    final double[] coordScratch=new double[3];
    final float[] colorScratch=new float[3];
    final int[] colorShuff=new int[]{0,1,2};
    final ArrayList<Cell3DOL> cells;
    public Polyp3DOL(int x, int y, int z,boolean gui) {
        super(x, y, z, Cell3DOL.class,false,false,false);
        resource=new GridDiff3(x,y,z);
        rn=new Random();
        trt=new TickRateTimer();
        cells=new ArrayList<>();
        if(gui){
            vis=new Vis3DOpenGL(1000,1000,xDim,yDim,zDim,"3D Cells", true);
        } else{ vis=null; }
    }
    void Init(int nCells,double includeProp){
        for (int i = 0; i < nCells; i++) {
            double x=rn.nextDouble()*xDim*includeProp+xDim*(1-includeProp)/2;
            double y=rn.nextDouble()*yDim*includeProp+yDim*(1-includeProp)/2;
            double z=rn.nextDouble()*zDim*includeProp+zDim*(1-includeProp)/2;
            Cell3DOL c=NewAgent(x,y,z);
            c.Init();
        }
        resource.SetAllCurr(1);
    }
    void Step(){
        trt.TickPause(tickRate);
        for (Cell3DOL c : this) {
            c.Step1();
        }
        if(vis!=null){
            vis.Clear(0,0,0);
        }
        for (Cell3DOL c : this) {
            c.Step2();
        }
        if(vis!=null){
            vis.Show();
        }
        CleanAgents();
        IncTick();
            for (int i = 0; i < resource.length; i++) {
                resource.AddCurr(i,everyWhereSourceVal);
            resource.BoundAllCurr(0,1);
            resource.DiffSwap(0.1,false,false,false);
        }
    }
    void AddCell(double x,double y,double z){
        Cell3DOL child=NewAgent(x,y,z);
        child.Init();
    }
    public static void main(String[] args){
        //dimensions of vat
        Polyp3DOL p=new Polyp3DOL(10,10,10,true);
        p.Init(5,0.8);
        for (int i = 0; i < p.timeSteps; i++) {
            p.Step();
        }
        if(p.vis!=null){
            p.vis.Dispose();
        }
    }
}
class Cell3DOL extends CircleForceAgent3<Polyp3DOL> {
    double cellCycle;
    double cellCycleDur;
    double forceSum;
    double resVal;
    void Init(){
        radius=G().cellRad;
        ResetCellCycle();
        xVel=0;
        yVel=0;
    }
    void ResetCellCycle(){
        cellCycle=G().cellCycleStart +G().rn.nextDouble()*G().cellCycleWiggle;
        cellCycleDur=cellCycle;
        radius=G().cellRad;
    }
    void Step1(){
        forceSum=SumForces(G().interactionRad,G().cells,(overlap)-> {
                if (overlap > 0) {
                    return -Math.pow(overlap * 0.5,3);
                }
                else{
                    //return -Math.pow(overlap * 0.3,4);
                    return 0;
                }
        });
        resVal=-MichaelisMenten(G().resource.GetCurr(Isq()),G().consumptionMax,G().consumptionHalfMaxConc);
    }
    void Step2(){
        radius=G().cellRad+G().cellRadGrowth*((cellCycleDur-cellCycle*1.0)/cellCycleDur);
        if(resVal<G().deathConcMax) {
            if (resVal < G().deathConcMin || G().rn.nextDouble() < (resVal - G().deathConcMin) / G().deathConcRange) {
                Dispose();
                return;
            }
        }
        ForceMove(0.5);
        if(resVal>G().quiescentConc) {
            cellCycle -= resVal;
            G().resource.AddCurr(Isq(), -resVal);
        }
        if(cellCycle<0&&forceSum<=G().maxForceDiv) {
            ResetCellCycle();
            Cell3DOL c = Divide(G().divRad, G().coordScratch, G().rn);
            c.Init();
        }
        double val=1.0-Zpt()/G().zDim;
        float[]c=G().colorScratch;
        int[]cIs=G().colorShuff;
        Utils.Shuffle(G().colorShuff,3,3,G().rn);
        HeatMapping(val,c);
        G().vis.FanShape((float)Xpt(),(float)Ypt(),(float)Zpt(),(float)radius,G().circlePts,c[cIs[0]],c[cIs[1]],c[cIs[2]]);
        G().vis.FanShape((float)Xpt(),(float)Ypt(),(float)Zpt()+0.0000000000001f,(float)radius*1.05f,G().circlePts,0,0,0);
    }
}

