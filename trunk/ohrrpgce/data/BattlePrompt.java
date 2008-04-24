/*
 * BattlePrompt.java
 * Created on July 31, 2007, 12:27 AM
 */

package ohrrpgce.data;

import java.util.Vector;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.loader.BattleFormationParser;
import ohrrpgce.data.loader.PictureParser;
import ohrrpgce.game.SimpleCanvas;
import ohrrpgce.game.SimpleTextBox;
import ohrrpgce.menu.ImageSlice;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;

/**
 * A simple class that informs the user of random encounters. 
 *   This class will sit in place until the BATTLE module is 
 *   created. 
 * NOTE: This code shares similar functionality with Message. But since it's temporary 
 *    anyways, let's refactor later, shall we?
 * @author Seth N. Hetu
 */
public class BattlePrompt {
    //Source o' data
    private RPG parent;
    
    //Display components
    private static SimpleCanvas background;
    private static SimpleCanvas highlight;
    private static SimpleTextBox enemyNames;
    private static SimpleTextBox outcomeChoice;
    private SimpleCanvas enemiesBox;
    private Vector enemiesImages;
    private int cursorPos;
    
    //Size...
    private static int dispWidth = -1;
    private static int dispHeight = -1;
    private static final int MARGIN = (Message.FONT_SIZE+Message.FONT_MARGIN+1);
    private static int numBoxChars;
    private static int enBoxWidth;
    private static int enBoxHeight;
    
    public BattlePrompt(RPG parent) {
        this.parent = parent;
    }
    
    public void setEncounterData(BattleFormation form) {
        if (dispWidth==-1 || dispHeight==-1)
            throw new RuntimeException("BattlePrompt requires initPrompts() to be called at least once.");
       
        setBox(form);
    }
    
    public void processInput(int key) {
        if (key==NPC.DIR_DOWN)
            cursorPos++;
        else if (key==NPC.DIR_UP)
            cursorPos--;
        if (cursorPos<0)
            cursorPos = 2;
        if (cursorPos>2)
            cursorPos = 0;
        
        highlight.setPosition(outcomeChoice.getPosX()-outcomeChoice.getWidth(),
                outcomeChoice.getPosY() + (MARGIN)*cursorPos
                );
    }
    
    public static void initPrompts(int width, int height) {
        //Set basic stats
        dispWidth = width;
        dispHeight = height;
    }
    
    
    private static ImageSlice makeImage(int[] spriteData, int paletteID, RPG game, int[] rectangle) {    	
    	MenuFormatArgs mf = new MenuFormatArgs();
    	mf.borderColors = new int[]{};
    	mf.fillType = MenuSlice.FILL_NONE;
    	mf.xHint = rectangle[0];
    	mf.yHint = rectangle[1];
    	mf.widthHint = rectangle[2];
    	mf.heightHint = rectangle[3];
    	ImageSlice res =  new ImageSlice(mf, spriteData, paletteID, game, rectangle[2]);
    	res.doLayout();
    	return res;
    }
    
    
    private void setBox(BattleFormation form) {
        //Prepare the "choice" box
        int[] clrs = parent.getTextBoxColors(0);
        if (outcomeChoice==null) {
            outcomeChoice = new SimpleTextBox("Win\nLose\nRun", parent.font, 0xDD000000|clrs[1], 0xDD000000|clrs[0], true, MenuSlice.FILL_SOLID);
            outcomeChoice.setLayoutRule(GraphicsAdapter.RIGHT|GraphicsAdapter.TOP);
            
            highlight = new SimpleCanvas(outcomeChoice.getWidth(), Message.FONT_MARGIN+Message.FONT_SIZE+2, 0x66FF0000, new int[]{0xFF0000}, MenuSlice.FILL_TRANSLUCENT);
            processInput(NPC.DIR_RIGHT); //Just set its position...
        }
        
        //Prepare the enemy Names box
        setEnemySpecificData(form);
        
        //Prepare the background box
        background = new SimpleCanvas(dispWidth, dispHeight/2+enemyNames.getHeight(), 0xDD000000|clrs[0], new int[]{clrs[1], 0}, MenuSlice.FILL_TRANSLUCENT);
    }
    
    private void setEnemySpecificData(BattleFormation form) {
        enemyNames = new SimpleTextBox("1\n2\n3\n4", parent.font, 0, 0, true, MenuSlice.FILL_NONE);
        numBoxChars = (dispWidth-4)/(Message.FONT_SIZE+Message.FONT_MARGIN+1);
        enBoxWidth = dispWidth-4 - 2*MARGIN;
        enBoxHeight = dispHeight/2-MARGIN-2;
        int blockSize = (PictureParser.PT_LARGE_ENEMY_SIZES[0]-PictureParser.PT_MED_ENEMY_SIZES[0])/2 + PictureParser.PT_MED_ENEMY_SIZES[0];
        int numCols = enBoxWidth/blockSize;
        int numRows = enBoxHeight/blockSize;
        int widthChunk = BattleFormationParser.ENEMY_X_RANGE/numCols;
        int heightChunk = BattleFormationParser.ENEMY_Y_RANGE/numRows;
        int[][] boxOffset = new int[numRows][];
        for (int i=0; i<boxOffset.length; i++) {
            boxOffset[i] = new int[numCols];
            for (int x=0; x<numCols; x++) {
                boxOffset[i][x] = 1;
            }
        }
            
        //Reset enemy names
        StringBuffer sb = new StringBuffer("");
        Vector v = new Vector();        
        enemiesBox = new SimpleCanvas(0x44DDDDDD, new int[]{}, MenuSlice.FILL_TRANSLUCENT);
        enemiesBox.setSize(enBoxWidth, enBoxHeight);
        enemiesBox.setPosition(MARGIN+2, MARGIN+2);
        enemiesImages = new Vector();
        OUTER:
        for (int id=0; id<form.enemies.length; id++) {
            int enID = form.enemies[id];
         
            try {
                int xBlock = form.enemyXPos[id]/widthChunk;
                int yBlock = form.enemyYPos[id]/heightChunk;
                int off = boxOffset[yBlock][xBlock];
                boxOffset[yBlock][xBlock] = off + MARGIN;

                Enemy en = parent.getEnemy(enID);
                int xPos = xBlock*blockSize+off;
                int yPos = yBlock*blockSize+off;
                try {
                    enemiesImages.addElement(
                			BattlePrompt.makeImage(en.getBattlePic().spData[0], en.spritePaletteID, parent, new int[]{xPos, yPos, en.getSizeInPix(), en.getSizeInPix()})
                			);
                } catch (Exception ex) {
                    throw new RuntimeException("Can't overlay image: " + xPos + "," + yPos);
                }
            } catch (Exception ex) {
                //'cause I don't trust myself...
                System.out.println("ERROR loading battle box!");
            }

            for (int i=0; i<v.size(); i++) {
                int[] rec = (int[])v.elementAt(i);
                if (rec[0]==enID) {
                    rec[1]++;
                    continue OUTER;
                }
            }
            v.addElement(new int[]{enID, 1});
        }
        
        //Set text
        for (int i=0; i<v.size(); i++) {
            int[] rec = (int[])v.elementAt(i);
            Enemy en = parent.getEnemy(rec[0]);
            if (i==3 && v.size()>4) {
                sb.append("And more...");
                break;
            } else {
                sb.append(en.name);
                int numSpaces = numBoxChars-en.name.length()-3-2;
                for (int c=0; c<numSpaces; c++)
                    sb.append(" ");
                sb.append("x");
                if (rec[1]<10)
                    sb.append(" ");
                sb.append(rec[1]);
                sb.append("\n");
            }
        }
        
        enemyNames = new SimpleTextBox(sb.toString(), parent.font, 0, 0, true, MenuSlice.FILL_NONE);
        enemyNames.setLayoutRule(GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
        enemyNames.setPosition(2+MARGIN, dispHeight/2);
        outcomeChoice.setPosition(dispWidth-MARGIN-2, dispHeight/2+enemyNames.getHeight()+MARGIN);
    }
    
    
    public void paint(int screenWidth, int screenHeight) {
        background.paint();
        
        enemyNames.paint();
        enemiesBox.paint();
        
        for (int i=0; i<enemiesImages.size(); i++)
        	((ImageSlice)enemiesImages.elementAt(i)).paintMenuSlice(-1);
        
        outcomeChoice.paint();
        highlight.paint();
    }
    
}





