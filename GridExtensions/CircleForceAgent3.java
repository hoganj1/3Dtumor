package GridExtensions;

import Grids.AgentPT3;
import Grids.Grid3;
import Tools.Utils;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiFunction;

import static Tools.Utils.*;

/**
 * Created by bravorr on 6/26/17.
 */
public class CircleForceAgent3<T extends Grid3> extends AgentPT3<T> {
    public double radius;
    public double xVel;
    public double yVel;
    public double zVel;
    public <Q extends CircleForceAgent3> double SumForces(double interactionRad, ArrayList<Q> agentList, OverlapForceResponse OverlapFun,boolean wrapX,boolean wrapY,boolean wrapZ){
        agentList.clear();
        double sum=0;
        G().AgentsInRad(agentList,Xpt(),Ypt(),Zpt(),interactionRad,wrapX,wrapY,wrapZ);
        for (Q a : agentList) {
            if(a!=this){
                double xComp=Xdisp(a,wrapX);
                double yComp=Ydisp(a,wrapY);
                double zComp=Zdisp(a,wrapZ);
                double dist=Norm(xComp,yComp,zComp);
                if(dist<interactionRad) {
                    double touchDist = (radius + a.radius) - dist;
                    double force=OverlapFun.CalcForce(touchDist);
                    xVel+=(xComp/dist)*force;
                    yVel+=(yComp/dist)*force;
                    zVel+=(zComp/dist)*force;
                    if(force>0) {
                        sum += Math.abs(force);
                    }
                }
            }
        }
        return sum;
    }
    public <Q extends CircleForceAgent3> double SumForces(double interactionRad, ArrayList<Q> agentList, OverlapForceResponse OverlapFun){
        agentList.clear();
        double sum=0;
        G().AgentsInRad(agentList,Xpt(),Ypt(),Zpt(),interactionRad,G().wrapX,G().wrapY,G().wrapZ);
        for (Q a : agentList) {
            if(a!=this){
                double xComp=Xdisp(a,G().wrapX);
                double yComp=Ydisp(a,G().wrapY);
                double zComp=Zdisp(a,G().wrapZ);
                double dist=Norm(xComp,yComp,zComp);
                if(dist<interactionRad) {
                    double touchDist = (radius + a.radius) - dist;
                    double force=OverlapFun.CalcForce(touchDist);
                    xVel+=(xComp/dist)*force;
                    yVel+=(yComp/dist)*force;
                    zVel+=(zComp/dist)*force;
                    if(force>0){
                        sum+=Math.abs(force);
                    }
                }
            }
        }
        return sum;
    }

    public <Q extends CircleForceAgent3> double SumForces(double interactionRad, ArrayList<Q> agentList, BiFunction<Double, CircleForceAgent3, Double> OverlapFun){
        agentList.clear();
        double sum=0;
        G().AgentsInRad(agentList,Xpt(),Ypt(),Zpt(),interactionRad,G().wrapX,G().wrapY,G().wrapZ);
        for (Q a : agentList) {
            if(a!=this){
                double xComp=Xdisp(a,G().wrapX);
                double yComp=Ydisp(a,G().wrapY);
                double zComp=Zdisp(a,G().wrapZ);
                double dist=Norm(xComp,yComp,zComp);
                if(dist<interactionRad) {
                    double touchDist = (radius + a.radius) - dist;
                    // if (touchDist < 0) cells overlap => should repulse
                    // if (touchDist > 0) cells don't overlap => should attract
                    double force=OverlapFun.apply(touchDist, a);
                    xVel+=(xComp/dist)*force;
                    yVel+=(yComp/dist)*force;
                    zVel+=(zComp/dist)*force;
                    if(force>0){
                        sum+=Math.abs(force);
                    }
                }
            }
        }
        return sum;
    }

    public void ForceMove(double friction,boolean wrapX,boolean wrapY,boolean wrapZ){
        xVel*=friction;
        yVel*=friction;
        zVel*=friction;
        MoveSafe(Xpt()+xVel,Ypt()+yVel,Zpt()+zVel,wrapX,wrapY,wrapZ);
    }

    public void ForceMove(double friction, double maxVelocity) {
        xVel*=friction;
        yVel*=friction;
        zVel*=friction;

        xVel = Utils.BoundVal(xVel, -maxVelocity, maxVelocity);
        yVel = Utils.BoundVal(yVel, -maxVelocity, maxVelocity);
        zVel = Utils.BoundVal(zVel, -maxVelocity, maxVelocity);

        MoveSafe(Xpt()+xVel,Ypt()+yVel,Zpt()+zVel,G().wrapX,G().wrapY,G().wrapZ);
    }

    public void ForceMove(double friction){
        xVel*=friction;
        yVel*=friction;
        zVel*=friction;
        MoveSafe(Xpt()+xVel,Ypt()+yVel,Zpt()+zVel,G().wrapX,G().wrapY,G().wrapZ);
    }

    public <Q extends CircleForceAgent3> Q Divide(double divRadius,double[] coordStorage,Random rn,boolean wrapX,boolean wrapY,boolean wrapZ){
        if(rn!=null){
            RandomPointOnSphereEdge(divRadius,rn,coordStorage);
        }
        Q child=(Q)(G().NewAgentSafe(Xpt()+coordStorage[0],Ypt()+coordStorage[1],Zpt()+coordStorage[2],Xpt(),Ypt(),Zpt(),wrapX,wrapY,wrapZ));
        MoveSafe(Xpt()-coordStorage[0],Ypt()-coordStorage[1],Zpt()-coordStorage[2],wrapX,wrapY,wrapZ);
        return child;
    }
    public <Q extends CircleForceAgent3> Q Divide(double divRadius,double[] coordStorage,Random rn){
        if(rn!=null){
            RandomPointOnSphereEdge(divRadius,rn,coordStorage);
        }
        Q child=(Q)(G().NewAgentSafe(Xpt()+coordStorage[0],Ypt()+coordStorage[1],Zpt()+coordStorage[2],Xpt(),Ypt(),Zpt(),G().wrapX,G().wrapY,G().wrapZ));
        MoveSafe(Xpt()-coordStorage[0],Ypt()-coordStorage[1],Zpt()-coordStorage[2],G().wrapX,G().wrapY,G().wrapZ);
        return child;
    }
}
