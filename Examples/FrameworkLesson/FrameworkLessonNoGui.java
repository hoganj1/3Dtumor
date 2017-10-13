package Examples.FrameworkLesson;

import Grids.AgentSQ2unstackable;
import Grids.Grid2unstackable;
import Grids.GridDiff2;
import Gui.*;
import Tools.FileIO;
import Tools.TickRateTimer;
import Tools.Utils;

import java.util.Random;

import static Tools.Utils.BoundVal;
import static Tools.Utils.Gaussian;
import static Tools.Utils.MooreHood;

/**
 * Created by bravorr on 6/19/17.
 */

class Polyp extends Grid2unstackable<Examples.FrameworkLesson.PolypCell> {
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
    final double consumptionRateMin=0.01;
    final double consumptionRateMax=0.02;
    final double productionRate=0.008;
    final double mutationRate=0.05;
    final double cellCycleDuration=0.5;
    final double diffusionRate=0.25;
    final double deathProb=0;
    final boolean wrap=false;
    final int tickPause=10;
    final int nSeeds=1;
    final int timeSteps=10000;

    //recording values
    int divCt;
    double avgPheno;
    int popCt;

    public static void main(String[] args) {
        int worldSideLen=200;
        int visualizerScale=5;

        GuiWindow gui=new GuiWindow("Model Run",true);
        GuiGridVis visPheno=new GuiGridVis(worldSideLen,worldSideLen,visualizerScale);
        gui.AddCol(new GuiLabel("Phenotype"),0);
        gui.AddCol(visPheno,0);
        GuiGridVis visRes=new GuiGridVis(worldSideLen,worldSideLen,visualizerScale);
        gui.AddCol(new GuiLabel("Resource"),1);
        gui.AddCol(visRes,1);
        gui.RunGui();
        FileIO outFile=new FileIO("LessonOut.csv","w");
        Polyp model=new Polyp(worldSideLen,visPheno,visRes,outFile);
        model.Run();
    }
    public Polyp(int worldSideLen,GuiGridVis visPheno, GuiGridVis visRes, FileIO outFile) {
        super(worldSideLen, worldSideLen, Examples.FrameworkLesson.PolypCell.class);
        this.visPheno=visPheno;
        this.visRes=visRes;
        this.outFile=outFile;
        resource=new GridDiff2(worldSideLen,worldSideLen);
        mooreHood =MooreHood(false);
        divIs=new int[mooreHood.length/2];
        trt=new TickRateTimer();
        rn=new Random();
    }
    public void Run(){
        Reset();
        //grid initialization
        int[] seedIs= Utils.RandomIndices(length,nSeeds,rn);
        for (int i = 0; i < nSeeds; i++) {
            Examples.FrameworkLesson.PolypCell c = NewAgent(seedIs[i]);
            c.Init(rn.nextDouble());
        }
        resource.SetAllCurr(1);
        if(outFile!=null){ outFile.Write("Population,Div Count,Avg Pheno\n"); }

        //grid step
        for (int tick = 0; tick < timeSteps; tick++) {
            divCt=0; avgPheno=0; popCt=0;
            trt.TickPause(tickPause);
            for (Examples.FrameworkLesson.PolypCell c : this) {
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

class PolypCell extends AgentSQ2unstackable<Examples.FrameworkLesson.Polyp> {
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
            Examples.FrameworkLesson.PolypCell child=G().NewAgent(G().divIs[G().rn.nextInt(nDivIs)]);
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

