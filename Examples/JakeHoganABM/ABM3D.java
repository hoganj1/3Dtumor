/*
default idea64.exe.vmoptions //i increased memory to 2 gigs
//go to help Edit Custom VM options to change back
# custom IntelliJ IDEA VM options
-Xms128m
-Xmx750m
rest is the same
*/
package Examples.JakeHoganABM;

import Grids.Grid3unstackable;
import Grids.AgentSQ3unstackable;
import Grids.GridDiff3;
import Tools.FileIO;
import Tools.TickRateTimer;
import Grids.GridDiff2;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import static Tools.Utils.*;

/**
 * Created by Jake Hogan on 7/13/2017.
 */

class ABMcell3d extends AgentSQ3unstackable<ABM3D> {
    int rho;
    int cellCycleTime;
    int necroticTick;
    boolean isQuiescent;
    boolean isSurrounded;

    boolean IsStem(){
        return (rho==-1||rho==-4);//-1 stem cell not only stem cell simulation, -4 stem cell, stem cell only simulation
    }
    boolean IsNecroticNonStem(){
        return (rho==-2);//rho==-2 is necrotic non stem cell
    }
    boolean IsNecroticStem(){
        return (rho==-3);//-3 is necrotic stem cell
    }
    void Init(int rho,boolean isStem){//use instead of constructor-used for when cell dissappears (stem cells are bigger, tells us to set whole nbhd to white?)
//        isSurrounded=false;
        cellCycleTime=0;
        if(isStem && !OnlyStemCellSimulation()){
            this.rho=-1;
        }
        else if(isStem && OnlyStemCellSimulation()){
            this.rho=-4;
        }
        else {
            this.rho = rho;
        }
    }

    public int FindEmptyNeighbors3d(int[] SQs, int[]ret, int centerX, int centerY, int centerZ, boolean wrapX, boolean wrapY, boolean wrapZ){
        int nIs=G().SQstoLocalIs(SQs,ret,centerX,centerY,centerZ, wrapX,wrapY, wrapZ);//ret means return
        int validCount=0;
        for (int i = 0; i < nIs; i++) {
            if(G().GetAgent(ret[i])==null){
                ret[validCount]=ret[i];
                validCount++;
            }
        }
        return validCount;
    }

    void Move(){
        int nEmpty= FindEmptyNeighbors3d(G().mooreHood,G().localIs,Xsq(),Ysq(),Zsq(),false, false, false);//G().calls functions from corresponding Grid class
        //1st arg-the nbhd to check, 2nd arg-place to write out indices of empty nbrs, 3rd/4rth arg-x/y coord, DONT wrap around
        //return val (nEmpty) is the # of empty nbrs (int)
        if(nEmpty == 0){
            isSurrounded = true;
            return;
        }
        else{
            isSurrounded = false;
        }
        int emptyI=-1;//emptyI is the index of the empty cell (starts at -1 because it hasn't been assigned)
        if(nEmpty==1){
            emptyI=0;
        }//there's only 1 elt of localIs, THAT's where cell will move (0th index of localIs is a position/indices on grid)
        else if(nEmpty>1) {
            emptyI=G().rn.nextInt(nEmpty);
        }//G().rn.nextInt(nEmpty) gets a random int between 0 and nEmpty (not including nEmpty)
        if(emptyI!=-1) {//if nEmpty >= 1
//            if(visCells!=null){
//                visCells.SetColor(Xsq(),Ysq(),1,1,1);
//                if(IsStem() && !OnlyStemCellSimulation()){
//                    SetNbhdColor(visCells, 1,1,1);
//                }
//              }
            //set former grid point of visCells to black (reset gridpoint)
            int moveX = G().ItoX(G().localIs[emptyI]);//FIXME-recheck if broken 7/14
            int moveY = G().ItoY(G().localIs[emptyI]);
            int moveZ = G().ItoZ(G().localIs[emptyI]);
            Move(moveX, moveY, moveZ);//move cell to location in localIs at index emptyI(emptyI came from nEmpty which was the num of empty nbrs)
        }
    }
    void Reproduce(){
        int nEmpty= FindEmptyNeighbors3d(G().mooreHood,G().localIs,Xsq(),Ysq(),Zsq(),false,false,false);
        if(nEmpty == 0){
            isSurrounded = true;
            return;
        }
        else{
            isSurrounded = false;
        }
        int emptyI=-1;
        if(nEmpty==1){ emptyI=0; }
        else if(nEmpty>1) { emptyI=G().rn.nextInt(nEmpty); }
        if(emptyI!=-1) {
            //reproducing
            cellCycleTime = 0;//was age in old model- incremented every iteration
            if(!IsStem()) { rho--; }
            if (rho == 0) {//non-stem apoptosis
//                visCells.SetColor(Xsq(),Ysq(),1,1,1);//make dead cells disappear from grid
                G().nNonStemCells--;
//                if(IsStem() && !OnlyStemCellSimulation()){//7/7 4pm commented out-not needed
//                    SetNbhdColor(visCells, 1,1,1);
//                }
                Dispose();//cell removed from grid and in essence destroyed
                return;//getting recolored green somewhere
            }
            ABMcell3d daughter = G().NewAgent(G().localIs[emptyI]);//creates new agent at emptyIth elt of localIs
            if (IsStem()) {
                if (G().rn.nextDouble() < G().STEM_SYMMETRIC_DIV) {
                    daughter.Init(G().MAX_RHO,true);
                    G().nStem++;
                } else {
                    daughter.Init(G().MAX_RHO,false);
                    G().nNonStemCells++;
                }
            }
            else {
                daughter.Init(rho,false);
                G().nNonStemCells++;
            }
        }
    }

    void SetDiffusibles(){
        if (IsNecroticNonStem()||IsNecroticStem()) {
        }else{
            double myO2 = G().o2.GetCurr(Xsq()+1,Ysq()+1, Zsq()+1);//Isq() gets the current indices position of the cell (not i,j coordinates)
//            double myO2 = G().o2.GetCurr(Xsq(),Ysq(), Zsq());//Isq() gets the current indices position of the cell (not i,j coordinates)
            G().o2.SetCurr(Xsq()+1,Ysq()+1,Zsq()+1, myO2>G().O2_CONSUMPTION_RATE?myO2-G().O2_CONSUMPTION_RATE:0);//FIXME- go back to this line of code when ADI solver is introduced
//            G().o2.SetCurr(Xsq(),Ysq(),Zsq(), myO2>G().O2_CONSUMPTION_RATE?myO2-G().O2_CONSUMPTION_RATE:0);//statement?if lacking o2, o2 set to 0:if false
        }
    }
    boolean OnlyStemCellSimulation() {
        if(G().STEM_SYMMETRIC_DIV == 1){
            return true;
        }
        return false;
    }
    void Step() {//GuiGridVis visCells removed 7/13- no 3d visualization
        isQuiescent = false;
        if (IsNecroticNonStem()|| IsNecroticStem()) {
            if(G().GetTick()-necroticTick>=G().DECOMPOSITION_TIME){//necrotic tick-time cell became necrotic
//                visCells.SetColor(Xsq(), Ysq(), 1, 1, 1);
//                if(IsNecroticStem() && !OnlyStemCellSimulation()){
//                    SetNbhdColor(visCells, 1,1,1);
//                }
                G().nNecroticCells--;//decrement as cells are disposed of
                Dispose();
                return;
            }
        }
        else {
            if(G().rn.nextDouble()<G().APOP_PROB){//random double 0-1
                if(!IsStem()){
                    G().nNonStemCells--;
                }
                if(IsStem() && !OnlyStemCellSimulation()){
                    G().nStem--;
                }
                Dispose();
                return;
            }
            double myTNFA=G().tnfa.GetCurr(Xsq()+1,Ysq()+1, Zsq()+1);
            double myO2 = G().o2.GetCurr(Xsq()+1,Ysq()+1, Zsq() + 1);//FIXME-use this line of code when ABM solver introduce
            if (myTNFA> G().NECROTIC_TNFA || myO2 < G().NECROTIC_OXYGEN) {//FIXME-add Xsq + 1 (for xyz) when ABM solver is implemented
                G().nNecroticCells++;
                necroticTick=G().GetTick();
                if(!IsStem()) {
                    rho = -2;//Necrotic non-stem cell
                    G().nNonStemCells--;
                }
                if(IsStem()){
                    rho = -3;//is necrotic stem
                    G().nStem--;
                }
                return;
            }
            if (myO2 < G().QUIESCENT_OXYGEN || myTNFA > G().QUIESCENT_TNFA ) {//FIXME- or if lvltnfa < QUIESENT_LEVEL
                G().nQuiescent++;
                isQuiescent = true;
//                visCells.SetColor(Xsq(), Ysq(), (float).5, (float).5, (float).5);
                if(IsStem() && !OnlyStemCellSimulation()) {
//                    SetNbhdColor(visCells, .5f, .5f, .5f);
                }
                return;
            }
            cellCycleTime++;
            if (cellCycleTime % G().NUM_ITER_BETWEEN_MOVEMENT == 0) {//moves 48 times a day
//                Move(visCells);
                Move();
            }
            if (cellCycleTime >= G().ITERATIONS_PER_DAY) {
//                Reproduce(visCells);//FIXME-if cell is disposed of and returns, rest of function still executed
                Reproduce();
                //ADDED 6/29 9:12
                if(!Alive()){//if cell dies because rho==0
                    return;
                }
            }
        }
    }
}
class ReturnVals{
    int[] totalCellCts;
    ReturnVals(int arraySize){
//            int[] totalCellCts;
        totalCellCts = new int[arraySize];
    }
}

public class ABM3D extends Grid3unstackable <ABMcell3d>{
    //globals
    final int TIMESTEP_MS=0;
    static final int GRID_SIZE=150;//switched name from GRID_SIZE to GRID_SIZE
    static final int ITERATIONS_PER_DAY=24 * 60 * 60;//once every 6 min 96 was original val
    static final int NUM_DAYS = 60;
    static final int TIMESTEPS=ITERATIONS_PER_DAY*NUM_DAYS +1;//+1 ensures we get the last cell data written to a file
    static final int PIXEL_SIZE=2;
    static final double STEM_SYMMETRIC_DIV= 1;

    //CA CONSTANTS
    static final int DIFF_TIMESTEP= 1;
    final double O2_INCREASE_RATIO = 1;
    final double DEC_RATE_DIFFUSION = 22;//Diffusion is slow, must dec defusion rate by 22 to keep equation steady and run diffusion 22 times more often
    final int MAX_RHO=3;
    final int CELL_WIDHT=20;
    //CHANGE THESE
    final double NECROTIC_TNFA=0.00125/DEC_RATE_DIFFUSION;//OK
    final double QUIESCENT_TNFA = .000525/DEC_RATE_DIFFUSION;//HALF NECROTIC LEVEL FOR TNFA
    double TNFA_PRODUCTION_RATE=0.0001/DEC_RATE_DIFFUSION;
    final double TNFA_BOUNDARY_CONDITION=0/DEC_RATE_DIFFUSION;
    final double NECROTIC_OXYGEN=0.0001/DEC_RATE_DIFFUSION;
    final double QUIESCENT_OXYGEN=0.0005/DEC_RATE_DIFFUSION;//mM(milliMolar)
    final double O2_CONSUMPTION_RATE=.012/DEC_RATE_DIFFUSION;//mM/s(milliMoles/second)
    final double O2_BOUNDARY_CONDITION=0.056/DEC_RATE_DIFFUSION;

    final int DAYS_BEFORE_DECOMPOSITION = 50;//?
    final int DECOMPOSITION_TIME=ITERATIONS_PER_DAY * DAYS_BEFORE_DECOMPOSITION;
    final double APOP_PROB=ProbScale(0,ITERATIONS_PER_DAY);//PROB SCALE ACCOUNTS FOR MULTIPLE ITERATIONS IN A DAY
    final int SEC_PER_DAY = 24 *3600;
    final int DIFFUSION_RUNS_PER_ITER = (int) ((SEC_PER_DAY*DEC_RATE_DIFFUSION)/(ITERATIONS_PER_DAY));//num seconds per iteration

    //DIFFUSIBLE CONSTANTS
    //FIXME- DIFFUSION IS UNSTABLE EVEN WITH VERY LOW DIFFUSION IF O2 IS CONSUMED
    //PARAMETER THAT CHANGES OUTPUT FILENAME

    //    final double O2_BOUNDARY_CONDITION=0.07;//mM changed 303 pm to .056
//    final double O2_DIFF_RATE=(1460.0/(CELL_WIDHT*CELL_WIDHT))*DIFF_TIMESTEP*O2_INCREASE_RATIO;//micrometers squared/sec um^2/sec. needs to be float/double
    final double O2_DIFF_RATE=(1460/(DEC_RATE_DIFFUSION*CELL_WIDHT*CELL_WIDHT));//micrometers squared/sec um^2/sec. needs to be float/double
    final double TNFA_DIFF_RATE=(100.0/(DEC_RATE_DIFFUSION*CELL_WIDHT*CELL_WIDHT));
    //1460/22 comes out around .1666 (1/6) a diffusion rate any higher would become unsteady
//    final double TNFA_DEGREDATION_RATE=ProbScale((.01), SEC_PER_DAY / ITERATIONS_PER_DAY);//.00001 hz
    final double O2_MAX_DIFF_THRESH=0.0001;
    final double TNFA_MAX_DIFF_THRESH=0.0001;
    final double GRID_SQUARE_SIZE = 400;//um^2
    final double CELL_MOVEMENT_SPEED = .2;//uM/min-avgs out to every 30-60 min (micrometers is uM)
    final int CELL_MOVEMENTS_PER_DAY = 6;
    final int NUM_ITER_BETWEEN_MOVEMENT = ITERATIONS_PER_DAY/CELL_MOVEMENTS_PER_DAY;//cell moves every 30 min
    final int SEC_PER_ITER = SEC_PER_DAY/ITERATIONS_PER_DAY;

    final TickRateTimer trt=new TickRateTimer();
    final int[] mooreHood=MooreHood3d(false);
    final int[] localIs=new int[mooreHood.length/3];//FIXME-check length of localis if not working

    //grid components
    final GridDiff3 o2;
    final GridDiff3 tnfa;
    final double[] steadyCheckO2;
    //    final double[] steadyCheckTnfa;
    final Random rn=new Random();
    FileIO out;//changed to final

    int nStem;
    int nNecroticCells;
    int nNonStemCells;
    int nQuiescent;
    int numViable = nStem + nNonStemCells;
    String outPath;

    ABM3D(int x, int y, int z, int iThread) {
        super(x,y,z,ABMcell3d.class);
        int gridSize = x + 2;//assume grid is a square
        o2=new GridDiff3(x+2,y+2, z + 2);//FIXME-replace this and next line when ABM solver implemented
        tnfa=new GridDiff3(x+2,y+2, z + 2);//define 2 new grids in the "master" ABM3D class
        //initialize grid
        ABMcell3d first=NewAgent(gridSize/2,gridSize/2, gridSize/2);//xDim yDim passed in as x,y I think? From parent class
        first.Init(MAX_RHO,true);//create Stem cell
        //define above with constants
        nNecroticCells=0;
        nStem=1;
        nNonStemCells=0;
        nQuiescent=0;

        Arrays.fill(o2.GetCurrField(),O2_BOUNDARY_CONDITION);//FIXME- does o2.currField get the correct value?
//        Arrays.fill(tnfa.GetCurrField(),TNFA_BOUNDARY_CONDITION);//
        o2.SetAllCurr(O2_BOUNDARY_CONDITION);//FIXME-reinstate when ABM solver is implemented
//            tnfa.SetAllCurr(TNFA_BOUNDARY_CONDITION);
        this.steadyCheckO2=new double[o2.length];
        Random rand = new Random();
        int n = rand.nextInt(50) + 1;//rand num between 1 and 1000 gives unique ofname
        final String fileName = "outFile.realisticParamTest"+n+".csv";//Systime just generates a number that's different each time so files don't overwrite eachother
        this.outPath = fileName;
        if(outPath!=""){
            out=new FileIO(outPath,"w");
            out.Write("X Val,Y Val,Z Val,Rho,NecroticTick\n");
        } else{ out=null; }
    }

    double GetMaxDiff(double[] a,double[] b){
        double max=Double.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            max=Math.max(Math.abs(a[i]-b[i]),max);
        }
        return max;
    }
    String GetModelData(int i){//changed to public MODIFIED 7/13 late night//FIXME- reimplement if GUI is implemented
        numViable= nStem + nNonStemCells- nQuiescent;
        return "Day: "+(int)((i / ITERATIONS_PER_DAY))+" Via: "+numViable+" Nec:"+nNecroticCells+" Qui:"+nQuiescent+" Stem: "+nStem+" Non-Stem:"+nNonStemCells;
//        String returnVal;
//        return returnVal;
    }
    int[] GetModelDataSweep(int i){//changed to public MODIFIED 7/13 late night//FIXME- reimplement if GUI is implemented
        numViable= nStem + nNonStemCells- nQuiescent;
        int[] returnVal = {(int)(-(i / ITERATIONS_PER_DAY)),numViable,nNecroticCells,nQuiescent,nStem,nNonStemCells};
        return returnVal;
    }

    public void SetOuterLayerCurr(GridDiff3 grid, double val){
        for (int x = 0; x < xDim; x++) {
            for (int y = 0; y < xDim; y++) {//ASK RAFAEL- why in Dif2d is it (y < yDim -1) not y < yDim?
                grid.SetCurr(x, y, 0, val);
                grid.SetCurr(x, y, yDim - 1, val);
            }
        }
        for (int x = 0; x < xDim; x++) {
            for (int z = 0; z < xDim; z++) {
                grid.SetCurr(x, 0, z, val);
                grid.SetCurr(x, yDim - 1, z, val);
            }
        }
        for (int y = 0; y < xDim; y++) {
            for (int z = 0; z < xDim; z++) {
                grid.SetCurr(0, y, z, val);
                grid.SetCurr(xDim - 1, y, z, val);
            }
        }
    }
    void WriteModelDataToFile(int currentDay){
        numViable= nStem + nNonStemCells;
//        out.Write(numViable+","+nStem+","+nNonStemCells+","+nNecroticCells+","+nHypoxic+"\n");
        //write out coordinates
        if(out != null){
            out.Write("Day "+currentDay+",New Day,,,\n");
            for(ABMcell3d c: this) {
                int outputRho = c.rho + 100;//R won't accept negative color vals
                if (c.isQuiescent) {
                    int rho = -5;//next lowest number after -4 for stem cells stem cell only simulation
                    outputRho = rho + 100;
                }
                //System.out.println(outputRho);
                out.Write(c.Xsq() + "," + c.Ysq() + "," + c.Zsq() + "," + outputRho + "," + c.necroticTick + "\n");
            }
        }
    }

    void OneDiffusionStep(){
        //store current values
        System.arraycopy(o2.GetCurrField(),0,steadyCheckO2,0,o2.length);
//        System.arraycopy(tnfa.GetCurrField(),0,steadyCheckTnfa,0,o2.length);
        for (ABMcell3d c : this) {
            c.SetDiffusibles();
        }
        SetOuterLayerCurr(o2, O2_BOUNDARY_CONDITION);//FIXME-when ADI3 is implemented, uncomment this, also change code back to grid.size +2 and x + 1 (when initializing and setting vals)
        o2.DiffSwap(O2_DIFF_RATE);
        double maxO2diff=GetMaxDiff(o2.GetCurrField(),steadyCheckO2);
        double maxTnfadiff = 0;
        if(maxO2diff > 1 || maxTnfadiff > 1){
            System.out.println("ERROR-DIFFUSION UNSTEADY(max_diff>1)");
        }
    }
    public int[] MooreHood3d(boolean includeOrigin) {
        if (includeOrigin) {
            return new int[]{0, 0, 0,
                    0, 0, 1,
                    0, 0, -1,
                    1, 0, 0,
                    1, 0, 1,
                    1, 0, -1,
                    1, 1, 0,
                    1, 1, 1,
                    1, 1, -1,
                    0, 1, 0,
                    0, 1, 1,
                    0, 1, -1,
                    -1, 0, 0,
                    -1, 0, 1,
                    -1, 0, -1,
                    -1, 1, 0,
                    -1, 1, 1,
                    -1, 1, -1,
                    -1, -1, 0,
                    -1, -1, 1,
                    -1, -1, -1,
                    0, -1, 0,
                    0, -1, 1,
                    0, -1, -1,
                    1, -1, 0,
                    1, -1, 1,
                    1, -1, -1,
            };
        } else {
            return new int[]{
                    0, 0, 1,
                    0, 0, -1,
                    1, 0, 0,
                    1, 0, 1,
                    1, 0, -1,
                    1, 1, 0,
                    1, 1, 1,
                    1, 1, -1,
                    0, 1, 0,
                    0, 1, 1,
                    0, 1, -1,
                    -1, 0, 0,
                    -1, 0, 1,
                    -1, 0, -1,
                    -1, 1, 0,
                    -1, 1, 1,
                    -1, 1, -1,
                    -1, -1, 0,
                    -1, -1, 1,
                    -1, -1, -1,
                    0, -1, 0,
                    0, -1, 1,
                    0, -1, -1,
                    1, -1, 0,
                    1, -1, 1,
                    1, -1, -1,
            };
        }
    }

    void Step(int i){
        trt.TickPause(TIMESTEP_MS);//increase TIMESTEP_MS if program is too fast
        nQuiescent=0;//get the per-step number of quiescent cells rather than a running total
        for (ABMcell3d c : this) {//this refers to all members of ABMcell3d class
            c.Step();//LOOKUP c.Step function
        }//set a return bool value and plug return bool val into next for loop (necrotic cells don't consume O2)
        CleanShuffInc(rn);//Clean moves unoccupied agents to end of the array, inc increments timer
        int ct=0;
//        for(int j = 0; j < this.DIFFUSION_RUNS_PER_ITER; j++){//DIFF_ADJUSTMENT_CONSTANT;j++){//FIXME- reimplement diffusion here
        for(int j = 0; j < DIFFUSION_RUNS_PER_ITER; j++){//DIFF_ADJUSTMENT_CONSTANT;j++){//FIXME- reimplement diffusion here
            OneDiffusionStep();
        }
    }

    void Update2dDiffGrid(GridDiff2 updateMe, GridDiff3 getDataFromMe){
        updateMe.SetAllCurr(0);
        for (int x = 0; x < xDim; x++) {
            for (int y = 0; y < yDim; y++) {
                for (int z = 0; z < zDim; z++) {
                    updateMe.AddCurr(x,y,(getDataFromMe.GetCurr(x,y,z)/zDim));
//                    double avgRowVal = 0;
//                    avgRowVal += getDataFromMe.GetCurr(x,y,z);//gets avg val of diffusion constants for respective row
//                    avgRowVal = avgRowVal / zDim;
                }
//                updateMe.SetCurr(x,y,avgRowVal);
            }
        }
    }


    public static void oneSec() {
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ReturnVals Run() {
        final int CELL_CT_VAR = 6;//NumDays, nViable, nNecrotic, nQuiescent, nStem, nNonstem
        //hold results of runs
        ReturnVals ret = new ReturnVals(NUM_DAYS * CELL_CT_VAR);
//        int[] totalCellCts = [NUM_DAYS * CELL_CT_VAR];//total cell cts
        int currentIndex;

//        for (int i = 0; i < ABM3D.TIMESTEPS; i++) {
        int i = 0;
        while(i < TIMESTEPS){
            this.Step(i);
            //ADDED 7/7
            if ((i % (ITERATIONS_PER_DAY)) == 0 && i > 0) {
                int[] cellCts = GetModelDataSweep(i);
//                System.out.println(cellCts);
                int currentDay = i / ITERATIONS_PER_DAY;
                //write coordinates to output file
                if (this.out != null) {
                    this.WriteModelDataToFile(currentDay);
                    this.out.Close();
                    this.out = new FileIO(this.outPath, "a");
                }
                System.out.println("Day" + currentDay);
//                totalCellCts = AssignVariables(ret.totalCellCts);
                //store cell cts to be written to file
                for(int j = 0; j < CELL_CT_VAR; j++) {//i*CELL_CT_VAR+j gets index of line
                    currentIndex = (currentDay-1)*CELL_CT_VAR + j;
                    ret.totalCellCts[currentIndex] = cellCts[j];//create var cellCts, get data for cellCts
//                    System.out.println(totalCellCts[currentIndex]);
                }
            }

            if ((i == (ITERATIONS_PER_DAY * 5))) {//
                int currentDay = i / ITERATIONS_PER_DAY;
            }
            i++;
        }
        if(this.out!=null){ this.out.Close();}
        for(int j = 0;j < ret.totalCellCts.length; j++) {System.out.print(j);}
        return ret;//total cell cts
    }

    public static void main(String[] args) {
        int nRuns = 1;
//        int nThreads = 4;
        int nThreads = 20;

        ArrayList<ModelOut> runMultipleSimulations= ParallelSweep(nRuns, nThreads,(iThread) -> {
            System.out.println("Started Thread:"+iThread);


            ABM3D model=new ABM3D(ABM3D.GRID_SIZE,ABM3D.GRID_SIZE,ABM3D.GRID_SIZE, iThread);//deleted 7/13 until visualization works out: viso2,visTNFA,cellCts,
            double TNFA_MIN=1e-5;
            double TNFA_MAX=1e-3;

            int[] TotalCellCts = model.Run().totalCellCts;//creates object w/ string array inside, updates and returns it
            ModelOut output=new ModelOut();
            output.TotalCellCts=TotalCellCts;
            output.TNFA_PROD_RATE=model.TNFA_PRODUCTION_RATE;
            System.out.println("Finished Thread:"+iThread);
            return output;
        });
        //
        FileIO out2=new FileIO("Results"+nRuns+"Runs"+ "realisticParamTest.csv","w");
        for(ModelOut modelOut:runMultipleSimulations){
            out2.WriteDelimit(modelOut.TotalCellCts, ",");//each row is 1 run, new days are negative numbers,
            // values in between new days are the viable, necrotic, quiescent, stem and nonstem counts respectively
            out2.Write("\n");
        }
        out2.Close();
    }
}
class ModelOut {
    int[] TotalCellCts;
    double TNFA_PROD_RATE;
}
