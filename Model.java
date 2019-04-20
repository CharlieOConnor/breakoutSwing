import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import java.util.concurrent.TimeUnit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color; 

import java.io.*;
import sun.audio.*;

/**
 * Model of the game of breakout
 * @author Mike Smith University of Brighton
 */

public class Model extends Observable
{
    
  // Border
  private static final int B              = 6;  // Border offset
  private static final int M              = 40; // Menu offset
  
  // Size of things
  private static final float BALL_SIZE    = 30; // Ball side
  private static final float BRICK_WIDTH  = 50; // Brick size
  private static final float BRICK_HEIGHT = 30;

  private static final int BAT_MOVE       = 10; // Distance to move bat
   
  // Scores
  private static final int HIT_BRICK      = 50;  // Score
  private static final int HIT_BOTTOM     = -200;// Score

  private GameObj ball;                // The ball
  public static List<GameObj> bricks;  // The bricks
  private GameObj bat;                // The bat
  
  public boolean runGame = true;       // Game running
  private boolean fast = false;         // Sleep in run loop

  private int score = 0;
  private int brickCount= -1;
  private int lives = 3;
  private int brickLives = 2;
  
  public boolean gameOver;
  public boolean startGame;
  private final float W;         // Width of area
  private final float H;         // Height of area

  public Model( int width, int height )
  {
    this.W = width; this.H = height;
  }
  
  private Audio audio;
  
  /**
   * Create in the model the objects that form the game
   */

  public void createGameObjects()
  {
    synchronized( Model.class )
    {
      ball   = new GameObj(W/2, H/2, BALL_SIZE, BALL_SIZE, Colour.WHITE );
      bat    = new GameObj(W/2, H - BRICK_HEIGHT*1.5f, BRICK_WIDTH*3, 
                              BRICK_HEIGHT/4, Colour.GRAY);                             
      bricks = new ArrayList<>();
      // *[1]******************************************************[1]*
      // * Fill in code to place the bricks on the board              *
      // **************************************************************
      int x = 90;
      int y = 60;
      for (int i = 0; i <= 5; i++) {
          x  = 90;
          y += 40;     
       for (int j = 0; j <= 6; j++) {
           bricks.add(new GameObj(x, y, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
           x += 60;
           brickCount += 1;
        }
    }

    }
  }
  public void drawHearts(Graphics2D g) {
      int a = 15;
      int b = 785;
      g.setColor(Color.RED);
      g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 30));
    for (int c = 0; c < lives; c++) {
        g.drawString("\u2665", a, b);
        a += 25;
    }
    }
  private ActivePart active  = null;

  /**
   * Start the continuous updates to the game
   */
  public void startGame()
  {
    synchronized ( Model.class )
    {
      stopGame();
      active = new ActivePart();
      Thread t = new Thread( active::runAsSeparateThread );
      t.setDaemon(true);   // So may die when program exits
      t.start();
    }
  }

  /**
   * Stop the continuous updates to the game
   * Will freeze the game, and let the thread die.
   */
  public void stopGame()
  {  
    synchronized ( Model.class )
    {
      if ( active != null ) { active.stop(); active = null; }
    }
  }

  public GameObj getBat()             { return bat; }

  public GameObj getBall()            { return ball; }
  

  public List<GameObj> getBricks()    { return bricks; }
  
  
  
  
  
  /**
   * Add to score n units
   * @param n units to add to score
   */
  protected void addToScore(int n)    { score += n; }
  
  public int getScore()               { return score; }
  
  public int getBrickCount()      { return brickCount; }
  
  public int getLives()               { return lives; }
  
  public int getBrickLives()             { return brickLives; }
  


  /**
   * Set speed of ball to be fast (true/ false)
   * @param fast Set to true if require fast moving ball
   */
  public void setFast(boolean fast)   
  { 
    this.fast = fast; 
  }

  /**
   * Move the bat. (-1) is left or (+1) is right
   * @param direction - The direction to move
   */
  public void moveBat( int direction )
  {
    // *[2]******************************************************[2]*
    // * Fill in code to prevent the bat being moved off the screen *
    // **************************************************************
    float x = bat.getX();
     if ((x >= W - B - BRICK_WIDTH*3) && (direction == 2))  direction = 0;
     if ((x <= 0 + B) && (direction == -2)               )  direction = 0;
    float dist = direction * BAT_MOVE;    // Actual distance to move
    Debug.trace( "Model: Move bat = %6.2f", dist );
    bat.moveX(dist);
  }  
  public void resetGame() {
   stopGame();
   score = 0;
   lives = 3;
   brickLives = 2;
   brickCount = 0;
   createGameObjects();
   try { 
       Thread.sleep(1000);
    }
    catch (Exception e) {
        System.out.print("1 second pause did not occur");
    }
   startGame();
    }
  /**
   * This method is run in a separate thread
   * Consequence: Potential concurrent access to shared variables in the class
   */
  class ActivePart
  {
    private boolean runGame = true;

    public void stop()
    {
      runGame = false;
    }

    public void runAsSeparateThread()
    {
      final float S = 5; // Units to move (Speed)
      try
      {
        synchronized ( Model.class ) // Make thread safe
        {
          GameObj       ball   = getBall();     // Ball in game
          GameObj       bat    = getBat();      // Bat
          List<GameObj> bricks = getBricks();   // Bricks
        }
  
        while (runGame)
        {
          synchronized ( Model.class ) // Make thread safe
          {
            float x = ball.getX();  // Current x,y position
            float y = ball.getY();
            // Deal with possible edge of board hit
            if (x >= W - B - BALL_SIZE)  ball.changeDirectionX();
            if (x <= 0 + B            )  ball.changeDirectionX();
            if (y >= H - B - BALL_SIZE)  // Bottom
            { 
            
             TimeUnit.SECONDS.sleep(1);
             ball   = new GameObj(W/2, H/2, BALL_SIZE, BALL_SIZE, Colour.WHITE ); addToScore( HIT_BOTTOM ); 
            
               lives -= 1;
               if (lives == 0) {  
                   runGame = false;
                   gameOver = true;
                }
            
              
            }
            if (y <= 0 + M            )  ball.changeDirectionY();

            // As only a hit on the bat/ball is detected it is 
            //  assumed to be on the top or bottom of the object.
            // A hit on the left or right of the object
            //  has an interesting affect
    
            boolean hit = false;
            // *[3]******************************************************[3]*
            // * Fill in code to check if a visible brick has been hit      *
            // *      The ball has no effect on an invisible brick          *
            // **************************************************************
            
            for (GameObj brick : bricks) {
                if (brick.hitBy(ball) && brick.isVisible() == true) {  
                    if(brick.getColour() == Colour.GRAY) {
                     hit = true;
                     brick.colour = Colour.RED;
                     Audio sound = new Audio();
                     sound.brickSound();
                    }
                   else  { 
                    hit = true; 
                    brick.setVisibility(false);
                    addToScore( HIT_BRICK );
                    brickCount -= 1;
                    Audio sound = new Audio();
                    sound.brickSound();
                  }
                }
            }
            
            if (hit)
              ball.changeDirectionY();
    
            if ( ball.hitBy(bat) ) {
            Audio sound = new Audio();
            sound.batSound();
            ball.changeDirectionY();
        }
           
            
              
          }
            
          modelChanged();      // Model changed refresh screen
          Thread.sleep( fast ? 2 : 20 );
          ball.moveX(S);  ball.moveY(S);
          
          
          if (brickCount == 0) {
              ball.setVisibility(false);
              runGame = false; 
              //gameOver = true; 
            }
        }
      } catch (Exception e) 
      { 
        Debug.error("Model.runAsSeparateThread - Error\n%s", 
                    e.getMessage() );
      }
    }
  }
  
  /**
   * Model has changed so notify observers so that they
   *  can redraw the current state of the game
   */
  public void modelChanged()
  {
    setChanged(); notifyObservers();
  }

}


