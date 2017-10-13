package Examples.FrameworkLesson;

import Grids.AgentSQ2unstackable;
import Grids.Grid2unstackable;
import Grids.GridDiff2;
import Gui.*;
import Tools.FileIO;
import Tools.TickRateTimer;
import Tools.Utils;

import java.util.Random;

import static Tools.Utils.*;

/**
 * Created by bravorr on 6/18/17.
 */

public class FrameworkLesson {
    public static void main(String[] args) {
        GuiWindow win=new GuiWindow("Menu",true);
        ParamSet set=new ParamSet();
        //first column of options
        win.AddCol(new DoubleParam(set,"Consumption Rate Min",0.01,0,1),0);
        win.AddCol(new DoubleParam(set,"Consumption Rate Max",0.02,0,1),0);
        win.AddCol(new DoubleParam(set,"Replenishment Rate",0.008,0,1),0);
        win.AddCol(new DoubleParam(set,"Mutation Rate",0.05,0,1),0);
        win.AddCol(new DoubleParam(set,"Diffusion Rate",0.25,0,1),0);
        win.AddCol(new DoubleParam(set,"Cell Cycle Duration",0.5,0.1,100),0);
        win.AddCol(new BoolParam(set,"Wrap Around",false),0);
        win.AddCol(new IntParam(set,"World Side Length",200,10,10000),0);
        win.AddCol(new IntParam(set,"Seed Cells",1,1,100),0);
        win.AddCol(new DoubleParam(set,"Death Prob",0,0,1),0);

        //second column of options
        win.AddCol(new IntParam(set,"Time Steps",10000,10,1000000),1);
        win.AddCol(new IntParam(set,"Step Rate (ms)",10,0,1000),1);
        win.AddCol(new IntParam(set,"Visualizer Scale",5,1,10),1);
        win.AddCol(new BoolParam(set,"Show Phenotype",true),1);
        win.AddCol(new BoolParam(set,"Show Resource",true),1);
        win.AddCol(new BoolParam(set,"Record Population",true),1);
        win.AddCol(new FileChooserParam(set,"Output File","LessonOut.csv"),1);
        win.AddCol(new GuiButton("RunModel",true,(e) -> {
            win.GreyOut(true);
            FileIO outFile=null;
            GuiGridVis visPheno=null;
            GuiGridVis visRes=null;
            GuiWindow visWin=null;
            boolean bVisPheno=set.GetBool("Show Phenotype");
            boolean bVisRes=set.GetBool("Show Resource");
            int worldSideLen=set.GetInt("World Side Length");
            int viewerScale=set.GetInt("Visualizer Scale");
            final boolean[] running = {true};
            if(set.GetBool("Record Population")){
                outFile=new FileIO(set.GetStr("Output File"),"w");
            }
            if(bVisPheno||bVisRes){
                visWin=new GuiWindow("Model Run",false,(ex)->{
                  running[0] =false;
                }, false);
                if(bVisPheno){
                    visPheno=new GuiGridVis(worldSideLen,worldSideLen,viewerScale);
                    visWin.AddCol(new GuiLabel("Phenotype"),0);
                    visWin.AddCol(visPheno,0);
                }
                if(bVisRes){
                    visRes=new GuiGridVis(worldSideLen,worldSideLen,viewerScale);
                    visWin.AddCol(new GuiLabel("Resource"),1);
                    visWin.AddCol(visRes,1);
                }
                visWin.RunGui();
            }
            Polyp2D polyp=new Polyp2D(worldSideLen,set.GetDouble("Consumption Rate Min"),set.GetDouble("Consumption Rate Max"),set.GetDouble("Replenishment Rate"),set.GetDouble("Mutation Rate"),set.GetDouble("Cell Cycle Duration"),set.GetDouble("Diffusion Rate"),set.GetInt("Step Rate (ms)"),set.GetBool("Wrap Around"),visPheno,visRes,set.GetDouble("Death Prob"),outFile, running);
            polyp.Run(set.GetInt("Seed Cells"),set.GetInt("Time Steps"));
            if(visWin!=null){ visWin.Dispose(); }
            win.GreyOut(false);
            }),1);
        win.RunGui();
    }
}

class PolypCell2D extends AgentSQ2unstackable<Polyp2D> {
    double phenotype;
    double consumptionRate;
    double cellCycleTime;
    public void Init(double phenotype){
        //always called after NewAgent
        this.phenotype=phenotype;
        this.consumptionRate=this.phenotype*(G().consumptionRateMax-G().consumptionRateMin)+G().consumptionRateMin;
        cellCycleTime=G().cellCycleDuration;
        if(G().visPheno!=null){
            G().visPheno.SetColor(Xsq(),Ysq(),1,(float)phenotype,0);
        }
    }
    public void Step(){
        double myResource=G().resource.GetCurr(Isq());
        if(G().rn.nextDouble()<G().deathProb||myResource<consumptionRate){
            //cell death
            if(G().visPheno!=null){G().visPheno.SetColor(Xsq(),Ysq(),0,0,0);}
            Dispose();
            return;
        }
        G().resource.AddCurr(Isq(),-consumptionRate);
        cellCycleTime-=consumptionRate;
        if(cellCycleTime<=0){
            //cell division
            if(Divide()){
                cellCycleTime=G().cellCycleDuration;
                G().divCt++;
            }
        }
        //pass values to grid for output writing
        G().avgPheno+=phenotype;
        G().popCt++;
    }
    public boolean Divide() {
        int nIs = G().SQsToLocalIs(G().mooreHood, G().divIs, Xsq(), Ysq(), G().wrap, G().wrap);
        int nDivIs = 0;
        for (int i = 0; i < nIs; i++) {
            //find open squares
            if (G().GetAgent(G().divIs[i]) == null) {
                G().divIs[nDivIs] = G().divIs[i];
                nDivIs++;
            }
        }
        if (nDivIs > 0) {
            //create daugther cell if an empty square exists
            PolypCell2D child=G().NewAgent(G().divIs[G().rn.nextInt(nDivIs)]);
            child.Init(phenotype);
            child.Mutate();
            Mutate();
            phenotype=BoundVal(Gaussian(phenotype,G().mutationRate,G().rn),0,1);
            return true;
        }
        return false;
    }
    public void Mutate(){
        phenotype=BoundVal(Gaussian(phenotype,G().mutationRate,G().rn),0,1);
    }
}

class Polyp2D extends Grid2unstackable<PolypCell2D>{
    //Other class members
    final GridDiff2 resource;
    final int[] mooreHood;
    final int[] divIs;
    final Random rn;
    final TickRateTimer trt;
    final GuiGridVis visPheno;
    final GuiGridVis visRes;
    final FileIO outFile;

    //Initialization parameters
    final double consumptionRateMin;
    final double consumptionRateMax;
    final double productionRate;
    final double mutationRate;
    final double cellCycleDuration;
    final double diffusionRate;
    final double deathProb;
    final boolean wrap;
    final int tickPause;

    //recording values
    int divCt;
    double avgPheno;
    int popCt;

    final boolean[] running;
    public Polyp2D(int worldSideLen, double consumptionRateMin, double consumptionRateMax, double productionRate, double mutationRate, double cellCycleDuration,double diffusionRate,int tickPause,boolean wrap, GuiGridVis visPheno, GuiGridVis visRes,double deathProb, FileIO outFile,boolean[] running) {
        super(worldSideLen, worldSideLen, PolypCell2D.class);
        this.consumptionRateMin=consumptionRateMin;
        this.consumptionRateMax=consumptionRateMax;
        this.productionRate=productionRate;
        this.mutationRate=mutationRate;
        this.cellCycleDuration=cellCycleDuration;
        this.wrap=wrap;
        this.visPheno=visPheno;
        this.visRes=visRes;
        this.outFile=outFile;
        this.diffusionRate=diffusionRate;
        this.tickPause=tickPause;
        this.deathProb=deathProb;
        resource=new GridDiff2(worldSideLen,worldSideLen);
        mooreHood =MooreHood(false);
        divIs=new int[mooreHood.length/2];
        trt=new TickRateTimer();
        rn=new Random();
        this.running=running;
    }
    public void Run(int nSeeds,int duration){
        Reset();
        //grid initialization
        int[] seedIs= Utils.RandomIndices(length,nSeeds,rn);
        for (int i = 0; i < nSeeds; i++) {
            PolypCell2D c = NewAgent(seedIs[i]);
            c.Init(rn.nextDouble());
        }
        resource.SetAllCurr(1);
        if(outFile!=null){ outFile.Write("Population,Div Count,Avg Pheno\n"); }

        //grid step
        for (int tick = 0; tick < duration; tick++) {
            divCt=0; avgPheno=0; popCt=0;
            if(!running[0]){ break; }
            trt.TickPause(tickPause);
            for (PolypCell2D c : this) {
                c.Step();
            }
            avgPheno=popCt>0?avgPheno/popCt:0;
            if(outFile!=null){ outFile.Write(popCt+","+divCt+","+avgPheno+"\n"); }
            CleanShuffInc(rn);
            resource.AddAllCurr(productionRate);
            resource.DiffSwap(diffusionRate,wrap,wrap);
            resource.BoundAllCurr(0,1);
            if(visRes!=null){
                for (int x = 0; x < xDim; x++) {
                    for (int y = 0; y < yDim; y++) {
                        visRes.SetColorHeat(x,y,resource.GetCurr(x,y));
                    }
                }
            }
        }
        if(outFile!=null){ outFile.Close(); }
    }
}
