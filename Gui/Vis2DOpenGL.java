package Gui;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by rafael on 5/28/17.
 */
public class Vis2DOpenGL{
    final boolean active;
    final public int xPix;
    final public int yPix;
    final public int xDim;
    final public int yDim;
    public Vis2DOpenGL(int xPix, int yPix, int xDim, int yDim, String title, boolean active) {
        this.active=active;
        this.xPix=xPix;
        this.yPix=yPix;
        this.xDim=xDim;
        this.yDim=yDim;
        if(active) {
            try {
                Display.setDisplayMode(new DisplayMode(xPix, yPix));
                Display.setTitle(title);
                Display.create();
            } catch (LWJGLException e) {
                e.printStackTrace();
                System.err.println("unable to create Vis3D display");
            }
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, xDim, 0, yDim, -1, 1);
            glMatrixMode(GL_MODELVIEW);
        }
    }
    public Vis2DOpenGL(int xPix, int yPix, int xDim, int yDim, String title) {
        this.active=true;
        this.xPix=xPix;
        this.yPix=yPix;
        this.xDim=xDim;
        this.yDim=yDim;
        if(active) {
            try {
                Display.setDisplayMode(new DisplayMode(xPix, yPix));
                Display.setTitle(title);
                Display.create();
            } catch (LWJGLException e) {
                e.printStackTrace();
                System.err.println("unable to create Vis3D display");
            }
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, xDim, 0, yDim, -1, 1);
            glMatrixMode(GL_MODELVIEW);
        }
    }
    public void Clear(float r,float g,float b){
        if (active) {
            glClearColor(r, g, b, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }
    }
    public void Show(){
        if(active) {
            Display.update();
        }
    }
    public boolean CheckClosed(){
        if(active) {
            return Display.isCloseRequested();
        }
        else{
            return true;
        }
    }
    public void Dispose(){
        if(active) {
            Display.destroy();
        }
    }
    public boolean IsActive(){
        return active;
    }
    public void FanShape(float centerX,float centerY,float scale,float[]points,float r,float g,float b) {
        if (active) {
            glColor3f(r, g, b);
            glBegin(GL_TRIANGLE_FAN);
            glVertex2f(centerX, centerY);
            for (int i = 0; i < points.length / 2; i++) {
                glVertex2f(points[i * 2] * scale + centerX, points[i * 2 + 1] * scale + centerY);
            }
            glEnd();
        }
    }
    public void FanShape(float centerX,float centerY,float scale,float[]points,float r,float g,float b,float a) {
        if (active) {
            glColor4f(r, g, b,a);
            glBegin(GL_TRIANGLE_FAN);
            glVertex2f(centerX, centerY);
            for (int i = 0; i < points.length / 2; i++) {
                glVertex2f(points[i * 2] * scale + centerX, points[i * 2 + 1] * scale + centerY);
            }
            glEnd();
        }
    }
}
