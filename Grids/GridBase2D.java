package Grids;

import Tools.Utils;

/**
 * Created by bravorr on 5/17/17.
 */
abstract class GridBase2D extends GridBase{
    public final int xDim;
    public final int yDim;
    public final int length;
    public boolean wrapX;
    public boolean wrapY;
    GridBase2D(int xDim,int yDim,boolean wrapX,boolean wrapY){
        this.xDim=xDim;
        this.yDim=yDim;
        this.wrapX=wrapX;
        this.wrapY=wrapY;
        this.length=xDim*yDim;
    }
    public int I(int x, int y){
        //gets grid index from location
        return x*yDim+y;
    }

    /**
     * gets the index of the square at the specified coordinates with wrap around
     */
    public int SQwrapI(int x, int y){
        //wraps Coords to proper index
        if(In(x,y)) { return I(x,y);}
        return I(Utils.ModWrap(x,xDim),Utils.ModWrap(y,yDim));
    }

    /**
     * gets the xDim component of the square at the specified index
     */
    public int ItoX(int i){
        return i/yDim;
    }

    /**
     * gets the yDim component of the square at the specified index
     */
    public int ItoY(int i){
        return i%yDim;
    }

    /**
     * gets the index of the square that contains the specified coordinates
     */
    public int I(double x, double y){
        //gets grid index from location
        return (int)Math.floor(x)*yDim+(int)Math.floor(y);
    }

    /**
     * returns whether the specified coordinates are inside the grid bounds
     */
    public boolean In(int x, int y){
        return x >= 0 && x < xDim && y >= 0 && y < yDim;
    }

    /**
     * returns whether the specified coordinates are inside the grid bounds
     */
    public boolean In(double x, double y){
        int xInt=(int)Math.floor(x);
        int yInt=(int)Math.floor(y);
        return In(xInt,yInt);
    }

//    /**
//     * gets the indices of the squares that lie within a given radius of a position
//     * argument array must be large enough to fit all indices in the maximum case, something like (rad*2)^2
//     */
//    public int SQsInRad(final int[] ret,final boolean wrapX,final boolean wrapY,final double centerX,final double centerY,final double rad){
//        int retCt=0;
//        for (int x = (int)Math.floor(centerX-rad); x <(int)Math.ceil(centerX+rad) ; x++) {
//            for (int y = (int)Math.floor(centerY-rad); y <(int)Math.ceil(centerY+rad) ; y++) {
//                int retX=x; int retY=y;
//                boolean inX=Utils.InDim(xDim,retX);
//                boolean inY=Utils.InDim(yDim,retY);
//                if((!wrapX&&!inX)||(!wrapY&&!inY)){
//                    continue;
//                }
//                if(wrapX&&!inX){
//                    retX=Utils.ModWrap(retX,xDim);
//                }
//                if(wrapY&&!inY){
//                    retY=Utils.ModWrap(retY,yDim);
//                }
//                ret[retCt]=I(retX,retY);
//                retCt++;
//            }
//        }
//        return retCt;
//    }
    /**
     * writes into ret the coordinates of SQs with the centerX and centerY coordinates added
     * returns the number of squares that were written into ret. any coordinates that fall out of bounds are not written
     * @param SQs list of coordinates of the form [xDim,yDim,xDim,yDim,...]
     * @param ret list into which the displaced coordinates will be written
     * @param centerX xDim displacement of coordinates
     * @param centerY yDim displacement of coordinates
     * @param wrapX whether to wrap the coordinates that fall out of bounds in the X direction
     * @param wrapY whether to wrap the coordinates that fall out of bounds in the Y direction
     * @return the number of coordinates written into the ret array
     */
    public int SQsToLocalIs(int[] SQs, int[] ret, int centerX, int centerY, boolean wrapX, boolean wrapY){
        //moves coordinates to be around origin
        //if any of the coordinates are outside the bounds, they will not be added
        int ptCt=0;
        for(int i=0;i<SQs.length/2;i++) {
            int x = SQs[i * 2] + centerX;
            int y = SQs[i * 2 + 1] + centerY;
            if (!Utils.InDim(xDim, x)) {
                if (wrapX) {
                    x = Utils.ModWrap(x, xDim);
                } else {
                    continue;
                }
            }
            if (!Utils.InDim(yDim, y)) {
                if (wrapY) {
                    y = Utils.ModWrap(y, yDim);
                } else {
                    continue;
                }
            }
            ret[ptCt]= I(x,y);
            ptCt++;
        }
        return ptCt;
    }
    public int SQsToLocalIs(int[] SQs, int[] ret, int centerX, int centerY){
        //moves coordinates to be around origin
        //if any of the coordinates are outside the bounds, they will not be added
        int ptCt=0;
        for(int i=0;i<SQs.length/2;i++) {
            int x = SQs[i * 2] + centerX;
            int y = SQs[i * 2 + 1] + centerY;
            if (!Utils.InDim(xDim, x)) {
                if (wrapX) {
                    x = Utils.ModWrap(x, xDim);
                } else {
                    continue;
                }
            }
            if (!Utils.InDim(yDim, y)) {
                if (wrapY) {
                    y = Utils.ModWrap(y, yDim);
                } else {
                    continue;
                }
            }
            ret[ptCt]= I(x,y);
            ptCt++;
        }
        return ptCt;
    }
    public double DistSq(double x1, double y1, double x2, double y2, boolean wrapX, boolean wrapY){
        return Utils.DistSq2D(x1,y1,x2,y2, xDim, yDim, wrapX,wrapY);
    }
    public double DistSq(double x1, double y1, double x2, double y2){
        return Utils.DistSq2D(x1,y1,x2,y2, xDim, yDim, wrapX,wrapY);
    }
}
