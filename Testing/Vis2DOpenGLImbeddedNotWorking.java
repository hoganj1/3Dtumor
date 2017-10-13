package Testing;
import Misc.GuiComp;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.awt.*;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by rafael on 5/28/17.
 */
public class Vis2DOpenGLImbeddedNotWorking implements GuiComp {
    AWTGLCanvas canvas;
    public final int compX;
    public final int compY;
    public final int xPix;
    public final int yPix;
    public final int xDim;
    public final int yDim;
    public Vis2DOpenGLImbeddedNotWorking(int xPix, int yPix, int xDim, int yDim, int compX, int compY) {
        try{
            this.canvas=new AWTGLCanvas();
            this.canvas.setSize(xPix,yPix);
            this.canvas.setVisible(true);
        }
        catch (LWJGLException e) {
            System.err.println("unable to create AWTGLCanvas display");
        }
        this.compX=compX;
        this.compY=compY;
        this.xPix=xPix;
        this.yPix=yPix;
        this.xDim=xDim;
        this.yDim=yDim;
        try {
            Display.setDisplayMode(new DisplayMode(xPix, yPix));
            Display.setParent(canvas);
        }
        catch (LWJGLException e) {
                e.printStackTrace();
                System.err.println("unable to create Vis2D display");
            }
    }
    public Vis2DOpenGLImbeddedNotWorking(int xPix, int yPix, int xDim, int yDim) {
        try{
            this.canvas=new AWTGLCanvas();
            this.canvas.setSize(xPix,yPix);
            this.canvas.setVisible(true);
        }
        catch (LWJGLException e) {
            System.err.println("unable to create AWTGLCanvas display");
        }
        this.compX=1;
        this.compY=1;
        this.xPix=xPix;
        this.yPix=yPix;
        this.xDim=xDim;
        this.yDim=yDim;
        try {
            Display.setDisplayMode(new DisplayMode(xPix, yPix));
            Display.setParent(canvas);
        }
        catch (LWJGLException e) {
            e.printStackTrace();
            System.err.println("unable to create Vis2D display");
        }
    }
    public void Setup(){
        try {
            Display.create();
        }
        catch (LWJGLException e) {
            e.printStackTrace();
            System.err.println("unable to setup Vis2D display");
        }
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0,xDim,0,yDim,-1,1);
        glMatrixMode(GL_MODELVIEW);
        glDisable(GL_DEPTH_TEST);
    }
    public void Clear(float r,float g,float b){
        glClearColor(r,g,b,1);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    }
    public void Show(){
        Display.update();
    }
    public boolean CheckClosed(){
        return Display.isCloseRequested();
    }
    public void Dispose(){
        Display.destroy();
    }
    public void FanShape(float centerX,float centerY,float scale,float[]points,float r,float g,float b) {
        glColor3f(r,g,b);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(centerX,centerY);
        for (int i = 0; i < points.length/2 ; i++) {
            glVertex2f(points[i*2]*scale+centerX,points[i*2+1]*scale+centerY);
        }
        glEnd();
    }

    @Override
    public int compX() {
        return compX;
    }

    @Override
    public int compY() {
        return compY;
    }

    @Override
    public boolean IsActive() {
        return false;
    }

    @Override
    public void SetActive(boolean isActive) {
        
    }

    @Override
    public void GetComps(ArrayList<Component> putHere, ArrayList<Integer> coordsHere, ArrayList<Integer> compSizesHere) {
        putHere.add(canvas);
        coordsHere.add(0);
        coordsHere.add(0);
        compSizesHere.add(compX);
        compSizesHere.add(compY);

    }
}
