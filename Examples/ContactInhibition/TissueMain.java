package Examples.ContactInhibition;

import Gui.*;
import Tools.FileIO;
import Tools.Utils;

import static Tools.Utils.*;


import java.util.ArrayList;
import java.util.Random;

public class TissueMain {
    double[] bestScoreWeights;
    double bestScore;

    public TissueMain(){
        bestScore = 0.0;
        bestScoreWeights = null;
    }

    public void KeepScore(double score,double[] weights) {
        if (score > bestScore) {
            bestScoreWeights = new double[weights.length];
            System.arraycopy(weights, 0, bestScoreWeights, 0, bestScoreWeights.length);
        }
    }

    public void GenGui(GuiWindow g, ParamSet set) {
            GuiButton viewBest = new GuiButton("ViewBestGA", true,(e)->{
                g.GreyOut(true);
                GuiGridVis visActions = new GuiGridVis(set.GetInt("runSize"), set.GetInt("runSize"), set.GetInt("viewer scale"), 1, 1, true);
                GuiGridVis visGeno = new GuiGridVis(set.GetInt("runSize"), set.GetInt("runSize"), set.GetInt("viewer scale"), 1, 1, true);
                Tissue t = new Tissue(set.GetInt("runSize"), set.GetInt("runSize"), this.bestScoreWeights, set, visActions, visGeno);
                GuiWindow disp = new GuiWindow("Best Tissue", false,(f)->{
                    t.Kill();
                    g.GreyOut(false);
                }, false);
                disp.AddCol(visActions, 0);
                disp.AddCol(visGeno, 1);
                disp.RunGui();
                t.Run(set.GetInt("runDuration"), true, set);
                disp.Dispose();
            });
            GuiButton viewCustom =new GuiButton("View Custom", true, (e)->{
                g.GreyOut(true);
                GuiGridVis visActions = new GuiGridVis(set.GetInt("runSize"), set.GetInt("runSize"), set.GetInt("viewer scale"), 1, 1, true);
                GuiGridVis visGeno = new GuiGridVis(set.GetInt("runSize"), set.GetInt("runSize"), set.GetInt("viewer scale"), 1, 1, true);
                double[] customWts;
                switch((set.GetBool("Bias")?1:0) +(set.GetBool("Local")?1:0) + (set.GetBool("Further")?1:0)){
                    case 1: customWts = new double[]{set.GetDouble("Custom Weight1")};break;
                    case 2: customWts = new double[]{set.GetDouble("Custom Weight1"), set.GetDouble("Custom Weight2")};break;
                    case 3: customWts = new double[]{set.GetDouble("Custom Weight1"), set.GetDouble("Custom Weight2"), set.GetDouble("Custom Weight3")};break;
                    default: throw new IllegalArgumentException("Must have at least one neuron on!");
                }
                Tissue t = new Tissue(set.GetInt("runSize"), set.GetInt("runSize"), customWts, set, visActions, visGeno);
                GuiWindow disp = new GuiWindow("Best Tissue", false, (ex)->{
                    t.Kill();
                    g.GreyOut(false);
                }, false);
                disp.AddCol(visActions, 0);
                disp.AddCol(visGeno, 1);
                disp.RunGui();
                double[] Score = t.Run(set.GetInt("runDuration"), true, set);
                System.out.println("Start Density: "+Score[0]+" End Density: "+Score[1]+"StartPerturb: "+Score[2]+" EndPerturb: "+Score[3]+"DeathCount: "+t.deathCt);
                disp.Dispose();
            });
//            GuiButton paramSweep = new GuiButton("ParamSweep", true, (e)->{
//                g.GreyOut(true);
//                BiasNeighborSweep(set);
//                g.GreyOut(false);
//            });
            GuiButton addCommand = new GuiButton("Add Command", false, (e)->{
                FileIO out = new FileIO(set.GetStr("Command File"), "a");
                if (out.length() == 0.0) {
                    out.WriteDelimit(set.LabelStrings(), ",");
                    out.Write("\n");
                }
                out.WriteDelimit(set.ValueStrings(), ",");
                out.Write("\n");
                out.Close();
            });
        GuiButton perturbSweep=new GuiButton("PertubSweep",true,(e)->{
                g.GreyOut(true);
                GuiWindow perturbOpts= new GuiWindow("PerturbSweep Options",false,(ex)->{
                   g.GreyOut(false);
                }, false);
            ParamSet perturbSet= new ParamSet();
                boolean[] running=new boolean[1];
                running[0]=true;
                perturbOpts.AddCol(new DoubleParam(perturbSet, "point mutation probMin", 0.001, 0.0, 1.0),0);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "point mutation probMax", 0.1, 0.0, 1.0),1);
                perturbOpts.AddCol(new GuiLabel("Log Dist",true),2);
                perturbOpts.AddCol(new BoolParam(perturbSet, "point mutation probLog", false, 1, 2),2);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "point mutation stdDevMin", 0.001, 0.0, 1.0),0);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "point mutation stdDevMax", 0.2, 0.0, 1.0),1);
                perturbOpts.AddCol(new BoolParam(perturbSet, "point mutation stdDevLog", false),2);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "WoundRadMin", -10.0, 0.0, 50.0),0);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "WoundRadMax", 9.0, 0.0, 50.0),1);
                perturbOpts.AddCol(new BoolParam(perturbSet, "WoundRadLog", false, 1, 2),2);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "WoundFreqMin", -5.0, -10.0, 50.0),0);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "WoundFreqMax", 9.0, 0.0, 50.0),1);
                perturbOpts.AddCol(new BoolParam(perturbSet, "WoundFreqLog", false, 1, 2),2);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "randDeathProbMin", -10.0, 0.0, 1.0),0);
                perturbOpts.AddCol(new DoubleParam(perturbSet, "randDeathProbMax", 1.0, 0.0, 1.0),1);
                perturbOpts.AddCol(new BoolParam(perturbSet, "randDeathProbLog", false, 1, 3),2);
                perturbOpts.AddCol(new IntParam(perturbSet, "SweepIters", 1000, 1, 10000000),0);
                perturbOpts.AddCol(new GuiButton("Start",true,(ex)->{
                    perturbOpts.GreyOut(true);
                    PerturbationSweep(set,perturbSet,running);
                    perturbOpts.GreyOut(false);
                }),2);
                perturbOpts.RunGui();
            });

            GuiButton OffLatticeButton= new GuiButton("RunOffLattice",true,(e)->{
                g.GreyOut(true);
                VTOffLattice vt= new VTOffLattice(set,set.GetDouble("Custom Weight1"),set.GetDouble("Custom Weight2"),true);
                vt.Run(set.GetInt("runDuration"));
                g.GreyOut(false);
            });

            g.AddCol(new GuiLabel("MAIN CONTROLS"), 0);
            g.AddCol(new FileChooserParam(set, "OutFile", "DefaultOut.csv"), 0);
            g.AddCol(new FileChooserParam(set, "Command File", "DefaultCommands.csv"), 0);
            g.AddCol(addCommand, 0);
            g.AddCol(new GuiLabel("MODEL PARAMS"), 0);
            g.AddCol(new IntParam(set, "runDuration", 10000, 0, 100000), 0);
            g.AddCol(new IntParam(set, "runSize", 10, 0, 10000), 0);
            g.AddCol(new BoolParam(set, "Bias", true), 0);
            g.AddCol(new BoolParam(set, "Local", false), 0);
            g.AddCol(new BoolParam(set, "Further", true), 0);
            g.AddCol(new BoolParam(set, "StochasticOutput", true), 0);
            g.AddCol(new DoubleParam(set, "SigmoidScale", 20.0, 0.0, 10000.0), 0);
            g.AddCol(new DoubleParam(set, "BiasValue", -0.7, -10000.0, 10000.0), 0);

            g.AddCol(new GuiLabel("PERTURBATIONS"), 1);
            g.AddCol(new IntParam(set, "Begin Perturb", 500, 0, 1000000), 1);
            g.AddCol(new BoolParam(set, "PerturbHomeostasis", false), 1);
            g.AddCol(new DoubleParam(set, "randDeathProb", 0.0, 0.0, 1.0), 1);
            g.AddCol(new DoubleParam(set, "WoundRad", 0.75, 0.0, 100.0), 1);
            g.AddCol(new IntParam(set, "WoundFreq", 50, 0, 100), 1);
            g.AddCol(new DoubleParam(set, "point mutation prob", 0.05, 0.0, 1.0), 1);
            g.AddCol(new DoubleParam(set, "point mutation stdDev", 0.05, 0.0, 10.0), 1);
            g.AddCol(new BoolParam(set, "RadiationMut", false), 1);
            g.AddCol(new BoolParam(set, "boundWeights", true), 1);
            g.AddCol(new DoubleParam(set, "weight min", 0.5, 0.0, 10.0), 1);
            g.AddCol(new DoubleParam(set, "weight max", 1.0, 0.0, 10.0), 1);

            g.AddCol(new GuiLabel("SWEEP PARAMS"), 2);
            //g.AddCol(new paramSweep, 2);
            g.AddCol(new IntParam(set, "Sweep Runs", 20000, 0, 100000), 2);
            g.AddCol(new GuiLabel("GA PARAMS"), 2);
            //        g.AddCol(runEvo,2);
            g.AddCol(new DoubleParam(set, "GA mutation stdDev", 0.1, 0.0, 10.0), 2);
            g.AddCol(new IntParam(set, "GA genSize", 50, 0, 1000), 2);
            g.AddCol(new IntParam(set, "GA BestKeep", 5, 0, 1000), 2);
            g.AddCol(new IntParam(set, "GA numGens", 6, 0, 100), 2);

            g.AddCol(new GuiLabel("VIEWER CONTROLS"), 3);
            g.AddCol(new IntParam(set, "viewer scale", 5, 1, 100), 3);
            g.AddCol(new DoubleParam(set, "viewer timestep", 0.0, 0.0, 10.0), 3);
            g.AddCol(perturbSweep,2);
            g.AddCol(viewBest, 3);
            g.AddCol(viewCustom, 3);
            g.AddCol(new DoubleParam(set, "Custom Weight1", 0.2, -100.0, 100.0), 3);
            g.AddCol(new DoubleParam(set, "Custom Weight2", 1.0, -100.0, 100.0), 3);
            g.AddCol(new DoubleParam(set, "Custom Weight3", 0.5, -100.0, 100.0), 3);
            g.AddCol(new GuiLabel("RESULTS CONTROLS"), 3);
            g.AddCol(new DoubleParam(set, "MinStartDensity", 0.5, 0.0, 1.0),3);
            g.AddCol(new DoubleParam(set, "MaxStartDensity", 0.9, 0.0, 1.0),3);
//            g.AddCol(DoubleParam(set, "divHeuristicValue", -0.5, -10.0, 10.0), 3)
//            g.AddCol(DoubleParam(set, "waitHeuristicValue", 1.0, 0.0, 10.0), 3)
//            g.AddCol(DoubleParam(set, "waitHeuristicExp", 10.0, 0.0, 20.0), 3)
            g.AddCol(new BoolParam(set, "RecMutations", false), 3);
            g.AddCol(new FileChooserParam(set, "MutationsOutFile", "DefaultMut.csv"), 3);
            g.AddCol(OffLatticeButton,4);
            g.AddCol(new DoubleParam(set,"cellRad",0.2,0.01,1),4);
            g.AddCol(new DoubleParam(set,"divRad",2.0/3.0,0,1),4);
            g.AddCol(new DoubleParam(set,"friction",0.5,0,1),4);
            g.AddCol(new DoubleParam(set,"forceExp",2,0,10),4);
            g.AddCol(new DoubleParam(set,"forceMul",1,0,10),4);
            g.AddCol(new DoubleParam(set,"thinkForceScale",100,0,10000),4);
            g.AddCol(new ComboBoxParam(set,"VisColor",0,new String[]{"MutationColor","ForceColor","BothColor"}),4);
        }

        void PerturbationSweep(ParamSet set, ParamSet perturbSet, boolean[] running){
            String[] varStrings={"point mutation prob","point mutation stdDev","WoundRad","WoundFreq","randDeathProb"};
            ParamSet sweepSet=new ParamSet(set.LabelStrings(), set.ValueStrings());
            ArrayList<String> sweepOut= ParallelSweep(perturbSet.GetInt("SweepIters"),4,(iThread)->{
                if(running[0]) {
                    Random rn =new Random();
                    ParamSet runSet = new ParamSet(sweepSet.LabelStrings(), sweepSet.ValueStrings());
                    double[] sweepParams = new double[5];
                    for (int i=0;i<varStrings.length;i++) {
                        String s = varStrings[i];
                        double min = perturbSet.GetDouble(s+"Min");
                        double max = perturbSet.GetDouble(s+"Max");
                        double sweepParam = perturbSet.GetBool(s+"Log")?Math.max(LogDist(min, max, rn),0.0):Math.max(rn.nextDouble() * (max - min) + min,0.0);
                        sweepParams[i] = sweepParam;
                        runSet.Set(s, Double.toString(sweepParam));
                    }
                    Tissue t = new Tissue(runSet.GetInt("runSize"), runSet.GetInt("runSize"), new double[]{runSet.GetDouble("Custom Weight1"), runSet.GetDouble("Custom Weight2")}, runSet, null, null);

                    double[] out = t.Run(runSet.GetInt("runDuration"), false, runSet);
                    double[] mutrec=t.MutRecordAverage();
                    return ArrToString(sweepParams, ",") + ArrToString(out, ",") + Utils.ArrToString(t.progenitorWeights,",") + ArrToString(mutrec,",");
                }
                else { return "";}
            });
            FileIO out=new FileIO(set.GetStr("OutFile"),"w");
            out.Write(ArrToString(varStrings,",")+"StartDensity,EndDensity,StartTick,EndTick,w1i,w2i,w1f,w2f,\n");
            out.WriteStrings(sweepOut,"\n");
            out.Close();
            System.out.println("wrote results");
        }

        public void BiasNeighborSweep(ParamSet set) {
            int nNeurons = (set.GetBool("Bias")?1:0) +(set.GetBool("Local")?1:0) + (set.GetBool("Further")?1:0);
            String weightNames = "";
            Random rn = new Random();
            ArrayList<String> sweepOut =ParallelSweep(set.GetInt("Sweep Runs"),4,(iThread)->{
                double[] params = new double[nNeurons];
                double weightMin = set.GetDouble("weight min");
                double weightMax = set.GetDouble("weight max");
                for (int i=0;i<nNeurons;i++) {
                    params[i] = rn.nextDouble() * (weightMax - weightMin) + weightMin;
                }
                Tissue t = new Tissue(set.GetInt("runSize"), set.GetInt("runSize"), params, set, null, null);
                double[] res = t.Run(set.GetInt("runDuration"), false, set);
                String ret="";
                ret= ArrToString(params,",");
                double[] avgs = t.MutRecordAverage();
                ret+= ArrToString(res,",");
                ret+=t.deathCt+","+avgs[0]+","+avgs[1]+"\n";
                return ret;
            });
            FileIO out = new FileIO(set.GetStr("OutFile"), "w");
            if (set.GetBool("Bias")) {
                out.Write("Bias,");
            }
            if (set.GetBool("Local")) {
                out.Write("Local,");
            }
            if (set.GetBool("Further")) {
                out.Write("Further,");
            }
            out.Write(weightNames + "StartDensity,EndDensity,StartTick,EndTick,DeathCount,w1Avg,w2Avg\n");
            for (String s : sweepOut) {
                out.Write(s);
            }
            out.Close();
        }

        void RunCommandSet(String commandFilePath, ParamSet set) {
            FileIO commandFile = new FileIO(commandFilePath, "r");
            ArrayList<String[]> commands = commandFile.ReadDelimit(",");
            set.SetLabels(commands.get(0));
            int nCommands = commands.size();
            for (int i=1;i<=nCommands;i++) {
                set.SetVals(commands.get(i));
                BiasNeighborSweep(set);
                System.out.println("Commpleted command " + i + " out of " + (nCommands - 1));
            }
        }

    public static void main(String[] args) {
        ParamSet set = new ParamSet();
        TissueMain tm = new TissueMain();
        GuiWindow g = new GuiWindow("Virtual Tissue II", true);
        tm.GenGui(g, set);
        g.RunGui();
        //tm.RunCommandSet("PosterData/Commands.csv",set);
    }
}
