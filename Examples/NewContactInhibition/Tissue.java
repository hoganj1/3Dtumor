package Examples.NewContactInhibition;

import GridExtensions.CircleForceAgent2;
import Grids.Grid2;
import Tools.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by rafael on 7/22/17.
 */

/*

model features:
    cells must make the decision to either go through interphase and divide, or G0 phase
    cells eat resources to survive
        some amount goes to metabolism
        some amount to cell cycle (if cell is dividing)
        cell can switch to G0 in case of low resources
    model different parts of the cell div process (only if they impact the overall dynamics)
    cells can be either

    each time step is 1 hr
    grid length is 50 microns

    10A: Normal
    DCIS: Mutant

 */

class CellGenome extends GenomeInfo<CellGenome>{
    double g0scale;
    double g1scale;
    double startRad;
    double randDeathProb;
    boolean isMut;
    int cellCycle;
    void Init(double g0scale,double g1scale,double startRad, double randDeathProb,int cellCycle,boolean mut){
        this.g0scale=g0scale;
        this.g1scale=g1scale;
        this.startRad=startRad;
        this.randDeathProb=randDeathProb;
        this.cellCycle=cellCycle;
        this.isMut =mut;
    }
}

public class Tissue extends Grid2<Cell>{
    final Random rn;

    int CELL_CYCLE=5;//in hrs
    double G1_PROP=2.0/3.0;//prop of total cell cycle that cell spends in G1
    double CELL_RAD_GROWTH_EXTRA=Math.sqrt(2.0/Math.PI)/Math.sqrt(1.0/Math.PI)-1;//growth needed to double cell volume
    double CELL_RAD_NORM=0.4;//20 microns min
    double CELL_RAD_MUT=0.4;//10 microns min
    double G0_SCALE_NORM=3;
    double G0_SCALE_MUT=1;
    double G1_SCALE_NORM=G0_SCALE_NORM;
    double G1_SCALE_MUT=G0_SCALE_MUT;
    double RAND_DEATH_PROB=0.0;
    double DIV_RAD_PROP=2.0/3.0;
    double FORCE_CONST=1;
    double FORCE_EXP=2;
    double FRICTION_CONST=0.1;
    GenomeTracker<CellGenome> normalGenomes;
    GenomeTracker<CellGenome> mutantGenomes;

    double[] divCoordScratch=new double[2];
    ArrayList<Cell> agentScratch=new ArrayList<>();

    public Tissue(int sideLen){
        super(sideLen,sideLen,Cell.class,true,true);
        this.rn=new Random();
        this.normalGenomes=new GenomeTracker<>(CellGenome.class,false);
        this.mutantGenomes=new GenomeTracker<>(CellGenome.class,false);
    }
    public void Setup(int nNorm,int nMut){
        CellGenome normProg=normalGenomes.GetProgenitor();
        normProg.Init(G0_SCALE_NORM,G1_SCALE_NORM,CELL_RAD_NORM,RAND_DEATH_PROB,CELL_CYCLE,false);
        for (int i = 0; i < nNorm; i++) {
            Cell c=NewAgent(rn.nextDouble()*xDim,rn.nextDouble()*yDim);
            c.Init(normProg);
            normProg.IncPop();
        }
        CellGenome mutProg=mutantGenomes.GetProgenitor();
        mutProg.Init(G0_SCALE_MUT,G1_SCALE_MUT,CELL_RAD_MUT,RAND_DEATH_PROB,CELL_CYCLE,true);
        for (int i = 0; i < nMut; i++) {
            Cell c=NewAgent(rn.nextDouble()*xDim,rn.nextDouble()*yDim);
            c.Init(mutProg);
            mutProg.IncPop();
        }
    }
    public void Step(){
        for (Cell c : this) {
            c.Step1();
        }
        for (Cell c : this) {
            c.Step2();
        }
        CleanInc();
    }
}

class Cell extends CircleForceAgent2<Tissue>{
    boolean g0;
    int cycleCt;
    double forceSum;
    CellGenome Genome;
    void Init(CellGenome Genome){
        this.Genome=Genome;
        this.g0=false;
        this.radius=Genome.startRad;
        this.cycleCt=0;
    }
    void Shrink(){
        if(this.cycleCt!=0) {
            this.cycleCt--;
            double nextRad = G().CELL_RAD_GROWTH_EXTRA * Genome.startRad * (1.0 / Genome.cellCycle) * this.cycleCt + Genome.startRad;
            this.radius=nextRad;
        }
    }
    void Grow(){
        this.cycleCt++;
        double nextRad=G().CELL_RAD_GROWTH_EXTRA*Genome.startRad*(1.0/Genome.cellCycle)*this.cycleCt+Genome.startRad;
        this.radius=Math.max(this.radius,nextRad);
        //this.radius=nextRad;
    }
    void Split(){
        Cell daugther=this.Divide(this.radius*G().DIV_RAD_PROP,G().divCoordScratch,G().rn);
        this.Init(Genome);
        //when cell divides, reset velocity
        this.xVel=0;
        this.yVel=0;
        daugther.Init(Genome);
        daugther.xVel=0;
        daugther.yVel=0;
        Genome.IncPop();
    }
    boolean CheckG0(double forceSum){
        double prob = Math.tanh(forceSum*Genome.g0scale);
        return G().rn.nextDouble()<prob;
    }
    boolean CheckG1(double forceSum){
        double prob = 1 - Math.tanh(forceSum*Genome.g1scale);
        return G().rn.nextDouble()<prob;
    }
    double OverlapForceCalc(double overlap){
        //computes the force to overlap function
        if(overlap>0){ return Math.abs(Math.pow(overlap*G().FORCE_CONST,G().FORCE_EXP)); }
            return 0;
    }
    void Step1(){
        //"Observe" step
        forceSum=SumForces((G().CELL_RAD_NORM*(1+G().CELL_RAD_GROWTH_EXTRA))*2,G().agentScratch,this::OverlapForceCalc);

        //if in g0, possibly transition to g1
        if(g0){
            g0=!CheckG1(forceSum);
            cycleCt=0;
        }
        else if(cycleCt*1.0/Genome.cellCycle<G().G1_PROP){
            //if in first 2/3 of G1, possibly transition to g0
            g0=CheckG0(forceSum);
        }
    }
    void Step2(){
        //"Act" step
        if(G().rn.nextDouble()<Genome.randDeathProb){
            Genome.DecPop();
            Dispose();
            return;
        }
        ForceMove(G().FRICTION_CONST);
        if(!g0){
            Grow();
        }
        if(g0){
            Shrink();
        }
        if(cycleCt>=Genome.cellCycle){
            Split();
        }
    }
    boolean IsMut(){
        return Genome.isMut;
        //return startRad==G().CELL_RAD_MUT;
    }
}

