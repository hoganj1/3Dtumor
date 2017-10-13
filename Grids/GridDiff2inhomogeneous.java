package Grids;

import Tools.Utils;

import java.util.Arrays;

/**
 * Created by bravorr on 6/8/17.
 */
public class GridDiff2inhomogeneous extends GridDiff2 {
    double[] diffRates;
    public GridDiff2inhomogeneous(int xDim,int yDim,double diffDefault,boolean wrapX,boolean wrapY){
        super(xDim,yDim,wrapX,wrapY);
        diffRates=new double[xDim*yDim];
        Arrays.fill(diffRates,diffDefault);
    }
    public GridDiff2inhomogeneous(int xDim,int yDim,double diffDefault){
        super(xDim,yDim,false,false);
        diffRates=new double[xDim*yDim];
        Arrays.fill(diffRates,diffDefault);
    }
    public void DiffuseInhomogeneous(boolean boundaryCond,double boundaryValue,boolean wrapX,boolean wrapY){
        Utils.Diffusion2inhomogeneous(currField, nextField,diffRates,xDim,yDim,boundaryCond,boundaryValue,wrapX,wrapY);
    }
    public void DiffuseInhomogeneous(boolean boundaryCond,double boundaryValue){
        Utils.Diffusion2inhomogeneous(currField, nextField,diffRates,xDim,yDim,boundaryCond,boundaryValue,wrapX,wrapY);
    }


    public void SetDiff(int x,int y,double val){
        diffRates[x*yDim+y]=val;
    }
    public void SetDiff(int i,double val){
        diffRates[i]=val;
    }
    public void SetDiffAll(double val){
        Arrays.fill(diffRates,val);
    }
    public double GetDiff(int x,int y){
        return diffRates[x*yDim+y];
    }
    public double GetDiff(int i){
        return diffRates[i];
    }
}
