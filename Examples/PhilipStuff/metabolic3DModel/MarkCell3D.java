package Examples.PhilipStuff.metabolic3DModel;

import GridExtensions.CircleForceAgent3;
import Gui.Vis3DOpenGL;
import Tools.Utils;

import java.util.Random;

import static Tools.Utils.*;

class MarkCell3D extends CircleForceAgent3<MarkModel3D> {
    double cellCycle;
    double cellCycleDur;
    double forceSum;
    double consumedO2;
    double consumedGluc;
    double producedAcid;
    double glycolysisPheno = 1f;
    double acidResistPheno = 6.5f;
    boolean isMDA = false;
    double probMigration = 0;
    double atp;

    double repulsionScaleFactor = 1;
    double repulsionExp = 1;

    double maxVelocityComp = 0;

    public double attractionScaleFactor = 0;//0.4 / (MarkModel3D.TIMESTEPS_PER_DAY / 3);

    double maxGlycolysisRate = 0;

    public void ComputeConsumption() {
        int i = Isq();

        consumedO2 = -MichaelisMenten(G().oxygen.GetCurr(i), G().OXYGEN_MAX_RATE, G().OXYGEN_HALF_RATE_CONC);
        maxGlycolysisRate = glycolysisPheno * G().TARGET_ATP_PRODUCTION / 2 + 27 * consumedO2 / 10;
        consumedGluc = -Utils.MichaelisMenten(G().glucose.GetCurr(i), maxGlycolysisRate, G().GLUCOSE_HALF_RATE_CONC);

//        if (ShouldBeQuiescent() && G().GetTick() > 1) {
////            consumedO2 /= 2;
////            consumedGluc /= 2;
//        }
        producedAcid = (29.0 * G().BUFFERING_COEFFICIENT / 5.0) * (glycolysisPheno * G().OXYGEN_MAX_RATE + consumedO2);

        G().acid.AddNext(i, producedAcid);
        G().glucose.AddNext(i, consumedGluc);
        G().oxygen.AddNext(i, consumedO2);
    }

    void ComputeATPProduction() {
        atp = -(27 * consumedO2 / 5 + 2 * consumedGluc);
        if (atp > GetMaxATPPrdouction()) {
            // // System.out.println("atp greater than max");
        }
    }

    double[] velocities = new double[3];

    void Migrate() {
        if (Math.random() <= probMigration) {
            double xWeight =
                    0;
            double yWeight =
                    0;
            double zWeight =
                    0;
//            try {
//                double sum02X =
//                        G().glucose.GetCurr(Xsq() - 1, Ysq(), Zsq()) +
//                                G().glucose.GetCurr(Xsq(), Ysq(), Zsq()) +
//                                G().glucose.GetCurr(Xsq() + 1, Ysq(), Zsq());
//                xWeight = (-1 * G().glucose.GetCurr(Xsq() - 1, Ysq(), Zsq()) + 1 * G().glucose.GetCurr(Xsq() + 1, Ysq(), Zsq())) / sum02X;
//
//                double sum02Y =
//                        G().glucose.GetCurr(Xsq(), Ysq() - 1, Zsq()) +
//                                G().glucose.GetCurr(Xsq(), Ysq(), Zsq()) +
//                                G().glucose.GetCurr(Xsq(), Ysq() + 1, Zsq());
//                yWeight = (-1 * G().glucose.GetCurr(Xsq(), Ysq() - 1, Zsq()) + 1 * G().glucose.GetCurr(Xsq(), Ysq() + 1, Zsq())) / sum02Y;
//
//                double sum02Z =
//                        G().glucose.GetCurr(Xsq(), Ysq(), Zsq() - 1) +
//                                G().glucose.GetCurr(Xsq(), Ysq(), Zsq()) +
//                                G().glucose.GetCurr(Xsq(), Ysq(), Zsq() + 1);
//                zWeight = (-1 * G().glucose.GetCurr(Xsq(), Ysq(), Zsq() - 1) + 1 * G().glucose.GetCurr(Xsq(), Ysq(), Zsq() + 1)) / sum02Z;
//            } catch (IndexOutOfBoundsException e) {
//                // // System.out.println("rip migration out if bounds");
//            }


            Utils.RandomWeightedPointOnSphere(
                    G().migrationDistance,
                    0.01,
                    G().rn,
                    new double[]{xWeight, yWeight, zWeight},
                    velocities
            );

            xVel += velocities[0];
            yVel += velocities[1];
            zVel += velocities[2];
//            MoveSafe(Xpt() + velocities[0], Ypt() + velocities[1], Zpt() + velocities[2]);
        }
    }

    public void SetConsumption() {
        int i = Isq();

        // assume oxygen is only ever depleted never added
        if (Math.abs(G().oxygen.GetNext(i)) >= G().oxygen.GetCurr(i)) {
            double fraction = consumedO2 / G().oxygen.GetNext(i);
            consumedO2 = fraction * G().oxygen.GetCurr(i);
        }

        if (Math.abs(G().glucose.GetNext(i)) >= G().glucose.GetCurr(i)) {
            double fraction = consumedGluc / G().glucose.GetNext(i);
            consumedGluc = fraction * G().glucose.GetCurr(i);
        }
        ComputeATPProduction();

        producedAcid = (29.0 * G().BUFFERING_COEFFICIENT / 5.0) * (glycolysisPheno * G().OXYGEN_MAX_RATE + consumedO2);

        if (!G().linearGradientMode) {
            G().acid.SetCurr(i, G().acid.GetCurr(i) + producedAcid);
            G().glucose.SetCurr(i, G().glucose.GetCurr(i) + consumedGluc);
            G().oxygen.SetCurr(i, G().oxygen.GetCurr(i) + consumedO2);
        }
    }

    void Init(boolean isMDA) {
        maxVelocityComp = Double.MAX_VALUE;//(G().xDim / (2 * MarkModel3D.TIMESTEPS_PER_DAY));

        radius = G().cellRad;
        ResetCellCycle();
        xVel = 0;
        yVel = 0;
        this.isMDA = isMDA;

        if (isMDA) {
            probMigration = G().MDAMigrationProb;
            glycolysisPheno = G().MDAGlycolysisPhenotype;
            acidResistPheno = G().MDAAcidResistance;
            cellCycleDur = 28d / 24;

            attractionScaleFactor = G().MDAadhesion;
        } else {
            probMigration = G().MCF7MigrationProb;
            glycolysisPheno = G().MCF7GlycolysisPhenotype;
            acidResistPheno = G().MCF7AcidResistance;
            cellCycleDur = 28d / 24;

            attractionScaleFactor = G().MCF7Adhesion;
        }
    }

    void ResetCellCycle() {
        cellCycle = cellCycleDur;
//        cellCycleDur = cellCycle;
        radius = G().cellRad;
    }



    void Step1() {
        ComputeConsumption();
        Migrate();

        forceSum = SumForces(G().interactionRad, G().cells, (overlap, neighbor) -> {
            double normalizedOverlap = -overlap / radius;
            if (overlap > 0) {
                // repulse
//                double ret = normalizedOverlap * attractionScaleFactor * repulsionScaleFactor;
                double ret = normalizedOverlap/* * attractionScaleFactor */* repulsionScaleFactor;

                if( Math.abs(ret) < attractionScaleFactor && overlap <= -radius) {
                    // System.out.println("overlap: " + overlap);
//                    throw new RuntimeException("repulsion weaker than attraction: " + ret + " att: " + attractionScaleFactor);
                }
                return ret;
            } else {
                MarkCell3D other = (MarkCell3D) neighbor;

                if (other.isMDA == this.isMDA) {
                    return -attractionScaleFactor;
                } else {
                    return 0d;
                }
            }
        });
    }

    static int divisionCount = 0;

    private void CapVelocityAt(double cap) {
        xVel = Math.min(Math.max(xVel, -cap), cap);
        yVel = Math.min(Math.max(yVel, -cap), cap);
        zVel = Math.min(Math.max(zVel, -cap), cap);
    }

    private double GetMaxATPPrdouction() {
        return 27 * G().OXYGEN_MAX_RATE / 5 + 2 * maxGlycolysisRate;
    }

    private boolean ShouldDie() {
        Random rn = G().rn;
        boolean randomDeath = rn.nextDouble() < G().normalDeathProb;
        if (randomDeath) {
            int b = 1;
//              System.out.println("random death");
        }
        boolean acidDeath = (ConvertHToPh(G().acid.GetCurr(Isq())) < acidResistPheno &&
                rn.nextDouble() < G().poorConditionDeathRate);
        if (acidDeath) {
            int b = 2;
//             System.out.println("acid death");
        }
        double minLivableATP = ShouldBeQuiescent() ? G().atpDeathConc / 2 : G().atpDeathConc;
        if (ShouldBeQuiescent()) {
            int b = 1;
        }
        boolean atpDeath = atp / GetMaxATPPrdouction() < minLivableATP;
        if (atpDeath) {
            int b = 1;
            if (ShouldBeQuiescent()) {
//                System.out.println("atp quisent death");
            }
        }
        return (randomDeath || acidDeath || atpDeath) /*&& G().GetPop() > 15*/;
    }

    private boolean ShouldBeQuiescent() {
        if ( atp / GetMaxATPPrdouction() <= G().atpQuiescent && isMDA) {
//            System.out.println("MDA quisient");
        }
        return atp / GetMaxATPPrdouction() <= G().atpQuiescent;
    }

    private boolean CanDivide() {
        return cellCycle < 0 && forceSum <= G().maxForceDiv/* && divisionCount <= 10*/;
    }

    void Step2() {
        radius = G().cellRad + 0 * G().cellRadGrowth * ((cellCycleDur - cellCycle * 1.0) / cellCycleDur);
        if (ShouldDie()) {
            Dispose();
            return;
        }


        ForceMove(0.2, maxVelocityComp);

        //not quiescent condition
        if (!ShouldBeQuiescent()) {
            cellCycle -= (atp / G().TARGET_ATP_PRODUCTION) * G().CELL_TIME_STEP / 5;
        }

        ///division condition
        if (CanDivide()) {
            ResetCellCycle();
            MarkCell3D c = Divide(G().divRad, G().coordScratch, G().rn);
            c.Init(isMDA);

            divisionCount++;
        }

        DrawCell3D(G().xyCellVis, 0);
    }

    static boolean drawMCF7 = true;
    static boolean drawMDA = true;

    public static boolean setDrawMCF7(boolean toSet) {
        drawMCF7 = toSet;
        return toSet;
    }

    public static boolean setDrawMDA(boolean toSet) {
        drawMDA = toSet;
        return toSet;
    }

    void DrawCell3D(Vis3DOpenGL visualizer, int axis) {
//        setDrawMDA(false);
        // XY
        if (visualizer != null) {
            if (axis == 0) {
                if (Zpt() > G().zDim / 2) {
                    double val = 1.0 - Zpt() / G().zDim;
                    float[] c = G().colorScratch;
                    HeatMapping(val, c);

                    if ((isMDA && drawMDA) || (!isMDA && drawMCF7)) {
                        visualizer.FanShape((float) Xpt(), (float) Ypt(), (float) Zpt(), (float) radius, G().circlePts, c[0], c[1], c[2]);
                    }

                    if (!isMDA && drawMCF7) {
                        visualizer.FanShape((float) Xpt(), (float) Ypt(), (float) Zpt() + 0.0000000000001f, (float) radius * 1.05f, G().circlePts, 0, 1f, 0);
                    } else if (drawMDA) {
                        visualizer.FanShape((float) Xpt(), (float) Ypt(), (float) Zpt() + 0.0000000000001f, (float) radius * 1.05f, G().circlePts, 0, 0, 1f);
                    }
                }
            } else if (axis == 1) { // YZ
                double val = 1.0 - Xpt() / G().xDim;
                float[] c = G().colorScratch;
                HeatMapping(val, c);
                visualizer.FanShape((float) Ypt(), (float) Zpt(), (float) Xpt(), (float) radius, G().circlePts, c[0], c[1], c[2]);
                if (!isMDA) {
                    visualizer.FanShape((float) Ypt(), (float) Zpt(), (float) Xpt() + 0.0000000000001f, (float) radius * 1.05f, G().circlePts, 0, 1f, 0);
                } else {
                    visualizer.FanShape((float) Ypt(), (float) Zpt(), (float) Xpt() + 0.0000000000001f, (float) radius * 1.05f, G().circlePts, 0, 0, 1f);
                }
            } else if (axis == 2) { // XZ
                double val = 1.0 - Ypt() / G().yDim;
                float[] c = G().colorScratch;
                HeatMapping(val, c);
                visualizer.FanShape((float) Xpt(), (float) Zpt(), (float) Ypt(), (float) radius, G().circlePts, c[0], c[1], c[2]);
                if (!isMDA) {
                    visualizer.FanShape((float) Xpt(), (float) Zpt(), (float) Ypt() + 0.0000000000001f, (float) radius * 1.05f, G().circlePts, 0, 1f, 0);
                } else {
                    visualizer.FanShape((float) Xpt(), (float) Zpt(), (float) Ypt() + 0.0000000000001f, (float) radius * 1.05f, G().circlePts, 0, 0, 1f);
                }
            } else {
                throw new IllegalArgumentException("axis must be 0, 1, or 2");
            }
        }
    }
}