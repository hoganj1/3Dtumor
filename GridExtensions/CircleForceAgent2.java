package GridExtensions;

import Grids.AgentPT2;
import Grids.Grid2;

import java.util.ArrayList;
import java.util.Random;

import static Tools.Utils.*;

/**
 * Created by bravorr on 6/26/17.
 */
public class CircleForceAgent2<T extends Grid2> extends AgentPT2<T> {
    public double radius;
    public double xVel;
    public double yVel;
    public <Q extends CircleForceAgent2> double SumForces(double interactionRad, ArrayList<Q> agentList, OverlapForceResponse OverlapFun, boolean wrapX, boolean wrapY){
        agentList.clear();
        double sum=0;
        G().AgentsInRad(agentList,Xpt(),Ypt(),interactionRad,wrapX,wrapY);
        for (Q a : agentList) {
            if(a!=this){
                double xComp=Xdisp(a,wrapX);
                double yComp=Ydisp(a,wrapY);
                double dist=Norm(xComp,yComp);
                if(dist<interactionRad) {
                    double touchDist = (radius + a.radius) - dist;
                    double force=OverlapFun.CalcForce(touchDist);
                    xVel+=(xComp/dist)*force;
                    yVel+=(yComp/dist)*force;
                    if(force>0) {
                        sum += Math.abs(force);
                    }
                }
            }
        }
        return sum;
    }
    public <Q extends CircleForceAgent2> double SumForces(double interactionRad, ArrayList<Q> agentList, OverlapForceResponse OverlapFun){
        return SumForces(interactionRad, agentList, OverlapFun, G().wrapX, G().wrapY);
    }

    public void ForceMove(double friction,boolean wrapX,boolean wrapY){
        xVel*=friction;
        yVel*=friction;
        MoveSafe(Xpt()+xVel,Ypt()+yVel,wrapX,wrapY);
    }
    public void ForceMove(double friction){
        xVel*=friction;
        yVel*=friction;
        MoveSafe(Xpt()+xVel,Ypt()+yVel,G().wrapX,G().wrapY);
    }
    public <Q extends CircleForceAgent2> Q Divide(double divRadius, double[] coordStorage, Random rn, boolean wrapX, boolean wrapY){
        if(rn!=null){
            RandomPointOnCircleEdge(divRadius,rn,coordStorage);
        }
        Q child=(Q)(G().NewAgentSafe(Xpt()+coordStorage[0],Ypt()+coordStorage[1],Xpt(),Ypt(),wrapX,wrapY));
        MoveSafe(Xpt()-coordStorage[0],Ypt()-coordStorage[1],wrapX,wrapY);
        return child;
    }
    public <Q extends CircleForceAgent2> Q Divide(double divRadius, double[] coordStorage, Random rn){
        if(rn!=null){
            RandomPointOnCircleEdge(divRadius,rn,coordStorage);
        }
        Q child=(Q)(G().NewAgentSafe(Xpt()+coordStorage[0],Ypt()+coordStorage[1],Xpt(),Ypt(),G().wrapX,G().wrapY));
        MoveSafe(Xpt()-coordStorage[0],Ypt()-coordStorage[1],G().wrapX,G().wrapY);
        return child;
    }
}
