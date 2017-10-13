package Examples;
import Tools.FileIO;

import java.util.ArrayList;
import java.util.Random;

import static Tools.Utils.*;

/**
 * Created by mark on 7/7/17.
 */
class ReturnVals{
    //this object will hold the results of each run
    int[] pops;
    double startLivingProb;
    ReturnVals(int timeSteps,double startLivingProb){
        pops=new int[timeSteps];
        this.startLivingProb=startLivingProb;
    }
}

public class ParallelSweepEx {
    public static void main(String[] args) {
        ArrayList<ReturnVals> runOutputs=ParallelSweep(10,8,(iThread)->{
            Random rn=new Random();
            //the function that will run in parallel is specified here
            System.out.println("Started Thread:"+iThread);
            double startLivingProb=Math.random();
            int runTicks=1000;
            GOLGrid model=new GOLGrid(100,100,startLivingProb,runTicks,0,null);
            ReturnVals ret=new ReturnVals(runTicks,startLivingProb);
            for (int i = 0; i < runTicks; i++) {
                model.Step();
                ret.pops[i]=model.CountAlive();
            }
            System.out.println("Finished Thread:"+iThread);
            return ret;
        });
        //after all runs finish, we loop through the array of ReturnVals objects and write out their data
        FileIO out=new FileIO("SweepResults.csv","w");
        for (ReturnVals ret : runOutputs) {
            out.Write(ret.startLivingProb+",");
            out.WriteDelimit(ret.pops,",");
            out.Write("\n");
        }
        out.Close();
    }
}
