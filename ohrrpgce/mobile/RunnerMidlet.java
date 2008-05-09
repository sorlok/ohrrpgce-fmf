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
import ohrrpgce.game.LiteException;
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
    
    public static void quit(boolean unconditional) {
        try {
            instance.destroyApp(unconditional);
            instance.notifyDestroyed();
            instance = null;
        } catch (MIDletStateChangeException mex) {
            return;
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
           try {
              RunnerMidlet.quit(false);
           } catch (Exception ex) {
              engManager.notifyOfError(ex); 
           }
            
       }
    }
    

 
}