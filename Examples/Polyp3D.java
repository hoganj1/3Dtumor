package Examples;

import Grids.AgentSQ3unstackable;
import Grids.Grid3unstackable;
import Grids.GridDiff3;
import Gui.GuiGridVis;
import Gui.GuiWindow;
import Tools.FileIO;
import Tools.TickRateTimer;

import java.util.ArrayList;
import java.util.Random;

import static Tools.Utils.*;

/**
 * Created by bravorr on 6/15/17.
 */

//18mm diameter 1mm depth
    //2x2x1
public class Polyp3D extends Grid3unstackable<Cell3D> {
    final GridDiff3 resource;
    final int[] divLocs;
    final int[] divIs;
    final Random rn;
    TickRateTimer trt;
    final double cellCycleStart =2.5;
    final double cellCycleWiggle =1;
    final double consumptionMax =0.1;
    final double consumptionHalfMaxConc=0.2;
    final double diffRate =0.2;
    final double deathConcMax =0.01;
    final double deathConcMin =0.0001;
    final double deathConcRange=deathConcMax-deathConcMin;
    final long tickRate=0;
    final int initDiffSteps=1000;
    final int timeSteps=100000;
    final boolean gui;
    final GuiWindow win;
    final ArrayList<GuiGridVis> viss;
    final double everyWhereSourceVal=0.001;
    public Polyp3D(int x, int y, int z,boolean gui) {
        super(x, y, z, Cell3D.class);
        resource=new GridDiff3(x,y,z);
        divLocs= VonNeumannHood3D(false);
        divIs=new int[divLocs.length/3];
        rn=new Random();
        trt=new TickRateTimer();
        this.gui=gui;
        if(gui){
            win=new GuiWindow("disp",true);
            viss=new ArrayList<>();
            int nCols=(int)Math.ceil(Math.sqrt(yDim/2));
            for (int i = 0; i <yDim/2  ; i++) {
                GuiGridVis vis=new GuiGridVis(xDim,zDim,1);
                viss.add(vis);
                win.AddCol(vis,i/nCols);
            }
            win.RunGui();
        }
        else{
            win=null;
            viss=null;
        }
    }
    void Init(int nCells,double includeProp){
        for (int i = 0; i < nCells; i++) {
            int x=rn.nextInt((int)(xDim*includeProp))+(int)(xDim*(1-includeProp)/2);
            int y=rn.nextInt((int)(yDim*includeProp))+(int)(yDim*(1-includeProp)/2);
            int z=rn.nextInt((int)(zDim*includeProp))+(int)(zDim*(1-includeProp)/2);
            NewAgent(x,y,z);
        }
        resource.SetAllCurr(1);
    }
    void Step(){
        trt.TickPause(tickRate);
        for (Cell3D c : this) {
            c.Step();
        }
        CleanShuffInc(rn);
        if(everyWhereSourceVal!=0){
            for (int i = 0; i < resource.length; i++) {
                resource.AddCurr(i,everyWhereSourceVal);
            }
            resource.BoundAllCurr(0,1);
            resource.DiffSwap(0.1,false,false,false);
        }
        else {
            resource.DiffSwap(0.1, 1, false, false, false);
        }
        if(GetTick()!=0&& GetTick()%200==0) {
            Record2();
        }
    }
    void Record1() {
        System.out.println("recording "+ GetTick());
        FileIO out=new FileIO("Polyp3D_"+ GetTick()+".csv","w");
        out.Write("x,y,z,cell,diffusible,cycle\n");
        for (int x = 0; x < xDim; x++) {
            for (int y = 0; y < yDim; y++) {
                for (int z = 0; z < zDim; z++) {
                    Cell3D c=GetAgent(x,y,z);
                    if(c==null) {
                        out.Write(x + "," + y + "," + z + ",0,"+resource.GetCurr(x,y,z)+",0\n");
                    }
                    else{
                        out.Write(x + "," + y + "," + z + ",1,"+resource.GetCurr(x,y,z)+","+c.cellCycle+"\n");
                    }
                }
            }
        }
        out.Close();
    }
    void Record2(){
        System.out.println("recording "+ GetTick());
        FileIO out=new FileIO("Polyp3Dcells_"+ GetTick()+".csv","w");
        out.Write("x,y,z,cell,diffusible,cycle\n");
        for (Cell3D c : this) {
            out.Write(c.Xsq() + "," + c.Ysq() + "," + c.Zsq() + ",1,"+resource.GetCurr(c.Xsq(),c.Ysq(),c.Zsq())+","+c.cellCycle+"\n");
        }
        out.Close();
        out=new FileIO("Polyp3Ddiff_"+ GetTick()+".csv","w");
        for (int x = 0; x < xDim; x++) {
            for (int z = 0; z < zDim; z++) {
                double sum=0;
                for (int y = 0; y < yDim; y++) {
                    sum+=resource.GetCurr(x,y,z);
                }
                if(z==zDim-1){
                    out.Write(sum+"");
                }
                else{
                    out.Write(sum+",");
                }
            }
            out.Write("\n");
        }
        out.Close();
    }

    void Draw(){
        for (int y = 0; y < yDim/2; y++) {
            GuiGridVis vis=viss.get(y);
            for (int x = 0; x < xDim; x++) {
                for (int z = 0; z < zDim; z++) {
                    float val=(float)BoundVal(resource.GetCurr(x,y*2,z),0,1);
                    vis.SetColor(x,z,GetAgent(x,y*2,z)==null?0:1,val,0);
                    //vis.SetColor(x,z,GetAgent(x,y,z)==null?0:1,0,0);
                }
            }
        }
    }
    void AddCell(int iSq){
        Cell3D child=NewAgent(iSq);
        child.ResetCellCycle();
    }
    void AddCell(int x,int y,int z){
        AddCell(I(x,y,z));
    }
    public static void main(String[] args){
        //dimensions of vat
        Polyp3D p=new Polyp3D(100,30,100,true);
        p.Init(5,0.8);
        for (int i = 0; i < p.timeSteps; i++) {
            p.Step();
            if(p.gui) {
                p.Draw();
            }
        }
        if(p.gui){
            p.win.Dispose();
        }
    }
}
class Cell3D extends AgentSQ3unstackable<Polyp3D> {
    double cellCycle;
    void ResetCellCycle(){
        cellCycle=G().cellCycleStart +G().rn.nextDouble()*G().cellCycleWiggle;
    }
    void Step(){
        if(UseResource()){
            return;
        }
        if(cellCycle<0){
            Divide();
        }
    }
    boolean UseResource(){
        //returns true if death from lack of nutrients occurs
        double currRes=G().resource.GetCurr(Isq());
        double resVal=MichaelisMenten(G().resource.GetCurr(Isq()),G().consumptionMax,G().consumptionHalfMaxConc);
        G().resource.AddCurr(Isq(),-resVal);
        if(resVal<G().deathConcMax){
            if(resVal<G().deathConcMin||G().rn.nextDouble()<(resVal-G().deathConcMin)/G().deathConcRange){
                if(G().gui){
                    G().viss.get(Ysq()/2).SetColor(Xsq(),Zsq(),0,0,0);
                }
                Dispose();
                return true;
            }
        }
        cellCycle-=resVal;
        return false;
    }
    boolean Divide(){
        //returns true if division occurs
        //cell cycle gets reset whether cell divides or not
        int nIs=G().SQstoLocalIs(G().divLocs,G().divIs,Xsq(),Ysq(),Zsq(),false,false,false);
        int nHits=0;
        for (int i = 0; i < nIs; i++) {
            if(G().GetAgent(G().divIs[i])==null){
                G().divIs[nHits]=G().divIs[i];
                nHits++;
            }
        }
        if(nHits==0){
            return false;
        }
        ResetCellCycle();
        int iDiv=G().divIs[G().rn.nextInt(nHits)];
        G().AddCell(iDiv);
        return true;
    }
}

