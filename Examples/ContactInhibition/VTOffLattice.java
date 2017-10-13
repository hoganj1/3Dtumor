package Examples.ContactInhibition;

import GridExtensions.CircleForceAgent2;
import Grids.Grid2;
import Gui.ParamSet;
import Gui.Vis2DOpenGL;
import Tools.GenomeInfo;
import Tools.GenomeTracker;
import Tools.TickRateTimer;

import static Tools.Utils.*;

import java.util.ArrayList;
import java.util.Random;

class OLGenome extends GenomeInfo<OLGenome>{
    double[]weights=new double[2];
    double mutDist;
    VTOffLattice myTissue;
    void Init(double w1,double w2,VTOffLattice myTissue){
        weights[0]=w1;
        weights[1]=w2;
        this.myTissue=myTissue;
    }
    public OLGenome RunPossibleMutation() {
        Random rn=myTissue.rn;
        if(myTissue.perturbing&&rn.nextDouble()<myTissue.pointMutProb){
            int iWeight=rn.nextInt(2);
            OLGenome mutantGenome=NewMutantGenome();
            mutantGenome.Init(weights[0],weights[1],myTissue);
            mutantGenome.weights[iWeight]=Gaussian(mutantGenome.weights[iWeight],myTissue.mutStdDev,rn);
            mutantGenome.mutDist=Math.sqrt(DistSqND(weights,MyTracker().GetProgenitor().weights));
            return mutantGenome;
        }
        return this;
    }

}

class OLCell extends CircleForceAgent2<VTOffLattice> {
    OLGenome myGenome;
    double forceSum;
    double xVel;
    double yVel;
    void Init(OLGenome myGenome){
        this.myGenome=myGenome;
        myGenome.IncPop();
        this.forceSum=0;
        this.radius=G().cellRad;
        this.xVel=0;
        this.yVel=0;
    }
    void Think(){
        double[]ns=G().neurons;
        NNset(ns,G().nIn,ns.length,0.0);
        ns[G().iBias]=G().biasVal;
        ns[G().iHood]=forceSum*G().thinkForceScale;
        NNfullyConnectedLayer(ns,myGenome.weights,0,G().nIn,G().nIn,ns.length,0);
        if(G().stochasticOutput){
            double exp=Math.exp(ns[G().nIn]*G().sigmoidScale);
            ns[G().nIn]=exp/(1+exp);
        }
    }
    void Wait(){ }
    void Apoptosis(){
        myGenome.DecPop();
        Dispose();
    }
    void CellDiv(){
        OLCell child=Divide(G().divRad,G().divCoords,G().rn);
        child.Init(myGenome);
        myGenome=myGenome.RunPossibleMutation();
        child.myGenome=child.myGenome.RunPossibleMutation();
    }
    void Act(){
//        if(forceSum==0){
//            Divide(G().divRad);
//        }
//        else{Wait();}
        double nnOut=G().neurons[G().nIn];
        if (G().stochasticOutput) {
            if (G().rn.nextDouble() > nnOut) { CellDiv(); }
            else { Wait(); }
        }
        else {
            if (nnOut < 0) { CellDiv(); }
            else { Wait(); }
        }
        ForceMove(G().friction,true,true);
    }
}


//@FunctionalInterface
//interface RadToForceMap{
//    double DistToForce(double rad);
//}

public class VTOffLattice extends Grid2<OLCell> {

    TickRateTimer trt;
    Random rn;
    final GenomeTracker<OLGenome> gt;

    int mutantCount;
    boolean running;
    boolean perturbing;
    final double[] divCoords;

    final boolean radiationMut;
    final int nIn;
    final int nOut;
    final int deathCt;
    final double mutStdDev;
    final double randDeathProb;
    final double pointMutProb;
    final int beginPerturb;
    final boolean stochasticOutput;
    final int iBias;
    final int iHood;
    final int woundFreq;
    final double woundRad;
    final double woundRadSq;
    final double[] neurons;
    final double[] progenitorWeights;
    final double sigmoidScale;
    final double biasVal;
    final String recMutOutPath;
    final ArrayList<OLCell> cellList;
    final double tickRate;
    final double interactionRad;

    final double cellRad;
    final double divRad;
    final Vis2DOpenGL vis;
    final float[] circleCoords;
    final double friction;
    final double forceExp;
    final double forceMul;
    final double thinkForceScale;
    final int visColor;

    public VTOffLattice(ParamSet set, double w1, double w2, boolean vis) {

        super(set.GetInt("runSize"),set.GetInt("runSize"),OLCell.class,true,true);
        divCoords=new double[2];
        radiationMut = set.GetBool("RadiationMut");
        trt = new TickRateTimer();
        running = false;
        perturbing = false;
        rn = new Random();
        nIn = 2;
        nOut = 1;
        deathCt = 0;
        mutStdDev = set.GetDouble("point mutation stdDev");
        randDeathProb = set.GetDouble("randDeathProb");
        pointMutProb = set.GetDouble("point mutation prob");
        beginPerturb = set.GetInt("Begin Perturb");
        stochasticOutput = set.GetBool("StochasticOutput");
        mutantCount = 0;
        iBias = 0;
        iHood = 1;
        sigmoidScale = set.GetDouble("SigmoidScale");
        recMutOutPath = set.GetStr("MutationsOutFile");
        neurons = new double[nIn + nOut];
        woundFreq = set.GetInt("WoundFreq");
        woundRad = set.GetDouble("WoundRad");
        woundRadSq=woundRad*woundRad;
        progenitorWeights = new double[]{w1, w2};
        biasVal = set.GetDouble("BiasValue");
        gt=new GenomeTracker<OLGenome>(OLGenome.class,true);
        gt.GetProgenitor().Init(w1,w2,this);
        cellList=new ArrayList<>();
        cellRad=set.GetDouble("cellRad");
        interactionRad=cellRad*2;
        divRad=cellRad*set.GetDouble("divRad");
        friction=set.GetDouble("friction");
        circleCoords=GenCirclePoints((float)cellRad,10);
        this.vis=vis?new Vis2DOpenGL(1000,1000,xDim,yDim,"Tissue", true):null;
        tickRate=set.GetDouble("viewer timestep")*1000;
        forceExp=set.GetDouble("forceExp");
        forceMul=set.GetDouble("forceMul");
        thinkForceScale=set.GetDouble("thinkForceScale");
        visColor=set.GetInt("VisColor");
    }
    void Step(){
        trt.TickPause((long)tickRate);
        if(vis!=null){
            vis.Clear(0,0,0);
        }
        if(perturbing&&woundFreq!=0&& GetTick()%woundFreq==0){
            Wound();
        }
        for (OLCell c : this) {
            c.forceSum=c.SumForces(interactionRad,cellList,(overlap)->{return Math.abs(Math.pow(overlap/(interactionRad),forceExp))*forceMul;});
        }
        for(OLCell c:this){
            if(perturbing&&rn.nextDouble()<randDeathProb){
                c.Apoptosis();
                continue;
            }
            c.Think();
            c.Act();
            if(vis!=null) {
                float mut=(float)c.myGenome.mutDist;
                float force=(float)(c.forceSum*thinkForceScale);
                //vis.FanShape((float) c.Xpt(), (float) c.Ypt(), 1f, circleCoords, mut, force,0.2f);
                switch (visColor){
                    //Mutation Color
                    case 0:
                        float dw1=(float)Math.abs(c.myGenome.weights[0]-gt.GetProgenitor().weights[0]);
                        float dw2=(float)Math.abs(c.myGenome.weights[1]-gt.GetProgenitor().weights[1]);
                        vis.FanShape((float) c.Xpt(), (float) c.Ypt(),1, circleCoords, dw1, dw2,0.2f);
                        break;
                    //Force Color
                    case 1:
                        vis.FanShape((float) c.Xpt(), (float) c.Ypt(),1, circleCoords, force/10, 0.2f,0.2f);
                        break;
                    //Both Color
                    case 2:
                        vis.FanShape((float) c.Xpt(), (float) c.Ypt(),1, circleCoords, force/10, mut,0.2f);
                        break;
                }
            }
        }
        if(vis!=null) {
            vis.Show();
        }
        IncTick();
    }
    void Wound(){
        cellList.clear();
        double x=rn.nextDouble()*xDim;
        double y=rn.nextDouble()*yDim;
        AgentsInRad(cellList,x,y,woundRad);
        for (OLCell c : cellList) {
            if(DistSq(c.Xpt(),c.Ypt(),x,y)<woundRadSq){
                c.Apoptosis();
            }
        }
    }
    void Run(int duration){
        Reset();
        OLCell first=NewAgent(xDim/2.0,yDim/2.0);
        first.Init(gt.GetProgenitor());
       running=true;
        for (int i = 0; i < duration; i++) {
            if(i==beginPerturb){
                perturbing=true;
            }
            if(vis!=null&&vis.CheckClosed()){
                break;
            }
            Step();
        }
        if(vis!=null){
            vis.Dispose();
        }
    }
}
