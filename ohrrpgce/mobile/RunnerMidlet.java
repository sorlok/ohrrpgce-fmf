/*
 * RunnerMidlet.java
 * Created on November 29, 2006, 3:32 PM
 */

package ohrrpgce.mobile;

import java.util.Stack;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import ohrrpgce.adapter.midlet.AdapterGenerator;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.midlet.InputAdapter;
import ohrrpgce.data.loader.RPGLoadSizeListener;
import ohrrpgce.game.Engine;
import ohrrpgce.game.EngineSwitcher;
import ohrrpgce.game.GameEngine;
import ohrrpgce.game.MenuEngine;
import ohrrpgce.menu.Action;
import ohrrpgce.runtime.EngineManager;
import ohrrpgce.runtime.MetaDisplay;
import ohrrpgce.runtime.OHRRPG;

/**
 * Midlet shell for OHRRPGCE games. Essentially, this contains all the "under the hood" things specific to
 *   J2ME (like error catching, DOS timer simulation, etc.) while GameEngine.java contains higher-level game logic.
 * @author  sethhetu
 * @version 1.0
 */
public class RunnerMidlet extends MIDlet {
    private static RunnerMidlet instance;
    public static Action backgroundAction;
    private RPGCanvas canvas;
    
    public RunnerMidlet() {
        this.instance = this;
    }
    
    public void startApp() {
        canvas = new RPGCanvas();
        Display.getDisplay(this).setCurrent(canvas);
    }
    public void pauseApp() {}

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        if (!unconditional) {
        	if (!canvas.engManager.canExit())
                throw new MIDletStateChangeException();
        } else
            System.out.println("Unconditional Exit");
    }
    
    public static void quit() {
        try {
            instance.destroyApp(false);
            instance.notifyDestroyed();
            instance = null;
        } catch (Exception mex) {
            System.out.println("EX: " + mex.getClass().getName());
        }
    }
}


class RPGCanvas extends GameCanvas implements CommandListener {
	//Drawing-related objects
    private Graphics g;
    
    public EngineManager engManager;
    
    public RPGCanvas() {
        //Suppress key events
        super(true);
        
        //Add the exit command
        setCommandListener(this);
        addCommand(new Command("Exit", Command.EXIT, 1));
        
        //Start the engine manager
        GraphicsAdapter.init(getGraphics(), new Action() {
            public boolean perform(Object caller) {
                flushGraphics();
                return true;
            }
        });
        engManager = new EngineManager(new AdapterGenerator(new int[]{getWidth(), getHeight()}), new InputAdapter(this)); 
        engManager.startYourEngines();
    }
    
   /* public Engine getCurrEngine() {
        return engines[currEngine];
    }*/

 
    
    public void commandAction(Command command, Displayable displayable) {
       if (command.getCommandType() == Command.EXIT) {
           RunnerMidlet.quit();
       }
    }
    
    //Code for displaying simple errors.
  /*  private void drawGeneralError(String msg, String caption) {
        //So we don't fail out here...
        float mem = Runtime.getRuntime().totalMemory()/1024F;
        for (int i=0; i<engines.length; i++)
            engines[i] = null;
        System.gc();
        
        //Do we have something to say?
        if (MetaDisplay.DEBUG_CONSOLE)
            msg += (" {" + MetaDisplay.DEBUG_MSG + "}");
        
        int acc = 10;
        Graphics g = getGraphics();
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        //Figure
        int margin = 5;
      //  int cpl = (getWidth()-margin*4)/MetaDisplay.errorMsgFnt.charWidth('W');
        String[] errorStrings = new String[(int)(Math.ceil(MetaDisplay.errorMsgFnt.stringWidth(msg)/(getWidth()-margin*2)))+1];
        StringBuffer sb = new StringBuffer();
        int index=0;
        for (int i=0; i<msg.length(); i++) {
            sb.append(msg.charAt(i));
            if (MetaDisplay.errorMsgFnt.stringWidth(sb.toString())>=(getWidth()-margin*2) || i==msg.length()-1) {
                errorStrings[index++] = sb.toString();
                sb = new StringBuffer();
            }
        }

        g.setColor(0xFF0000);
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE));
        g.drawString(caption, getWidth()/2, acc, Graphics.HCENTER|Graphics.TOP);
        acc += g.getFont().getHeight();

        g.setColor(0);
        g.setFont(MetaDisplay.errorMsgFnt);
        for (int i=0; i<errorStrings.length; i++) {
            g.drawString(errorStrings[i], 10, acc, Graphics.LEFT|Graphics.TOP);
            acc += g.getFont().getHeight();
        }
        acc += g.getFont().getHeight();

        g.drawString("Total Memory:       " + floatPrint(mem) + "K", 10, acc, Graphics.LEFT|Graphics.TOP);
        flushGraphics();
    }*/
    
    //Convenience method
  /*  public static String floatPrint(float fl) {
        int prim = (int)fl;
        float rem = fl - prim;
        return prim + "." + (int)(rem*10);
    }*/
    
 
}