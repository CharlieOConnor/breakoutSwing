    import javax.swing.ImageIcon;
    import javax.swing.JFrame;
    import java.awt.Color;
    import java.awt.Dimension;
    import java.awt.Font;
    import java.awt.FontMetrics;
    import java.awt.Graphics;
    import java.awt.Graphics2D;
    import java.awt.Image;
    import java.awt.event.KeyEvent;
    import java.awt.event.KeyListener;
    import java.awt.geom.Rectangle2D;
    import java.awt.geom.Ellipse2D;
    import java.awt.image.BufferedImage;
    import java.util.List;
    import java.util.Observable;
    import java.util.Observer;
    
    import javax.imageio.ImageIO;
    
    
    /**
     * Displays a graphical view of the game of breakout.
     *  Uses Graphics2D would need to be re-implemented for Android.
     * @author Mike Smith University of Brighton
     */
    public class View extends JFrame implements Observer
    { 
      private Controller controller;
      private GameObj   bat;            // The bat
      private GameObj   ball;           // The ball
      private List<GameObj> bricks;     // The bricks
      private int       score;     // The score
      private int       frames;     // Frames output
      private int       brickCount; // The Number of remaining bricks
      private boolean   runGame;
      private int       lives;      // The number of lives I have
      private boolean gameOver;
      public Audio sound = new Audio(); // Allows methods from the Audio class to be called
      public boolean startGame;
    
      public final int width;  // Size of screen Width
      public final int height;  // Size of screen Height
    
      
      
      /**
       * Construct the view of the game
       * @param width Width of the view pixels
       * @param height Height of the view pixels
       */
      public View(int width, int height)
      {
        this.width = width; this.height = height;
        
        setSize(width, height);                 // Size of window
        addKeyListener( new Transaction() );    // Called when key press
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Timer.startTimer();
        //sound.youLose(false);
       }
    
      /**
       *  Code called to draw the current state of the game
       *   Uses draw:       Draw a shape
       *        fill:       Fill the shape
       *        setPaint:   Colour used
       *        drawString: Write string on display
       *  @param g Graphics context to use
       */
      public void drawActualPicture( Graphics2D g )
      {
        final int  RESET_AFTER = 200; // Movements
        frames++;
        synchronized( Model.class )   // Make thread safe
        {
          if(startGame ) {
          g.setPaint( Color.BLACK ); // Black background
          g.fill( new Rectangle2D.Float( 0, 0, width, height) ); //Shapoe and size of window
      
          
          
          
          displayBall( g, ball );   // Display the Ball
          displayGameObj( g, bat  );   // Display the Bat
        
      
         
          // *[4]****************************************************[4]*
          // * Display the bricks that make up the game                 *
          // * Fill in code to display bricks                           *
          // * Remember only a visible brick is to be displayed         *
          // ************************************************************
         
          /** Display bricks that are visible from the Model class */          
           for (GameObj brick : Model.bricks) { 
               if (brick.isVisible())
               displayGameObj( g, brick); 
 
           }
           
          /** Create the hearts for the game */
             int a = 15;
             int b = 785;
             g.setColor(Color.RED);
             g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 30));
             for (int c = 0; c < lives; c++) {
               g.drawString("\u2665", a, b);
                a += 25;
             }
                    
          /** Set the score, fps rate and brick count at the bottom of the screen */
          g.setPaint( Color.WHITE );    
          Font font = new Font("SansSerif",Font.BOLD,24); 
          g.setFont( font );
          FontMetrics fm = getFontMetrics( font );
          String fmt = "             Score = %6d    fps=%5.1f    bricks=%3d";
          String text = String.format(fmt, score, 
                                      frames/(Timer.timeTaken()/1000.0), brickCount
                         );
          if ( frames > RESET_AFTER ) 
            { frames = 0; Timer.startTimer(); }
          g.drawString( text, width /2-fm.stringWidth(text)/2, 785  );
        } else {
            g.drawString( "hello tgis is home screen!", width/2, 350  );
        }
        }
      }
      
      /** Get the size and shape of the bricks from the GameObj class */
      private void displayGameObj( Graphics2D g, GameObj go )
      {
        g.setColor( go.getColour().forSwing() );
        g.fill( new Rectangle2D.Float( go.getX(),     go.getY(), 
                                       go.getWidth(), go.getHeight() ) );
      }
      
      /** Get the size and shape of the ball from the GamObj class */
      private void displayBall( Graphics2D g, GameObj go )
      {
        g.setColor( go.getColour().forSwing() );
        g.fill( new Ellipse2D.Float( go.getX(),     go.getY(), 
                                       go.getWidth(), go.getHeight() ) );
      } 
      
      /**
       * Called indirectly from the model when its state has changed
       * @param aModel Model to be displayed
       * @param arg    Any arguments (Not used)
       */
      @Override
      public void update( Observable aModel, Object arg )
      {
        Model model = (Model) aModel;
        // Get from the model the ball, bat, bricks & score
        ball    = model.getBall();              // Ball
        bricks  = model.getBricks();            // Bricks
        bat     = model.getBat();               // Bat
        score   = model.getScore();             // Score
        lives   = model.getLives();             // Remaining Lives
        brickCount = model.getBrickCount();     // Remaining Brick
        Debug.trace("Update"); 
        runGame = model.runGame;
        startGame = model.startGame;
        gameOver = model.gameOver;
       repaint();                              // Re draw game
      }
    
      /**
       * Called by repaint to redraw the Model
       * @param g    Graphics context
       */
      @Override
      public void update( Graphics g )          // Called by repaint
      {
        drawPicture( (Graphics2D) g );          // Draw Picture
      }
    
      /**
       * Called when window is first shown or damaged
       * @param g    Graphics context
       */
      @Override
      public void paint( Graphics g )    
                        
      /** Conditions to draw seperate 
       * result screens after the game */        // When 'Window' is first
      {                                          //  shown or damaged
        drawPicture( (Graphics2D) g );          // Draw Picture
        if (brickCount == 0 && lives < 3) {
            drawWin( (Graphics2D) g);           
        }
         else if (brickCount == 0 && lives == 3 && score != 0) {
            drawPerfectWin( (Graphics2D) g);
        }
        else if (lives == 0) {
            drawLose( (Graphics2D) g);
        }
      }
    
      private BufferedImage theAI;              // Alternate Image
      private Graphics2D    theAG;              // Alternate Graphics
    
      /**
       * Double buffer graphics output to avoid flicker
       * @param g The graphics context
       */
      private void drawPicture( Graphics2D g )   // Double buffer
      {                                          //  to avoid flicker
        if ( bricks == null ) return;            // Race condition
        if (  theAG == null )
        {
          Dimension d = getSize();              // Size of curr. image
          theAI = (BufferedImage) createImage( d.width, d.height );
          theAG = theAI.createGraphics();
        }
        drawActualPicture( theAG );             // Draw Actual Picture
        g.drawImage( theAI, 0, 0, this );       //  Display on screen
      }
      
      /** If the player wins, draw the normal win screen and 
       * play the youWin sound effect from the Audio class */
      public void drawWin (Graphics2D g) 
      {      
          
          sound.youWin();
          Font font = new Font("SansSerif",Font.BOLD,48); 
          g.setFont( font );
          g.setPaint( Color.blue );
          g.drawString("You Win!", 200, 400);
          
          try {
              Thread.sleep(1000);
            }
          catch (Exception e) {
              System.out.print("1 second pause did not occur");
            }
          g.drawString("Press Space", 175, 500); 
      }
      
      /** If the player gets a perfect win (3 lives) then draw the perfect 
       * win screen and play the perfectWin sound effect from the Audio class */
      public void drawPerfectWin (Graphics2D g) 
      {
            Font font = new Font("SansSerif",Font.BOLD,48); 
            g.setFont( font );
            g.setPaint( Color.blue );
            g.drawString("Perfect Score!!", 100, 400);

            sound.perfectWin();
            try {
              Thread.sleep(500);
            }
          catch (Exception e) {
              System.out.print("Half second pause did not occur");
            }
          g.drawString("Press Space", 175, 500);               
      }
      
      /** If the player loses, draw the losing screen and play 
       * the youLose sound effect from the Audio class */
      public void drawLose (Graphics2D g) 
      {       

          sound.youLose(true);
          Font font = new Font("SansSerif",Font.BOLD,48); 
          g.setFont( font );
          g.setPaint( Color.red );
          g.drawString("You Lose...", 200, 400);
          try {
              Thread.sleep(500);
            }
          catch (Exception e) {
              System.out.print("Half second pause did not occur");
            }
          g.drawString("Press Space", 175, 500); 
      }
    
      /**
       * Need to be told where the controller is
       * @param aPongController The controller used
       */
      public void setController(Controller aPongController)
      {
        controller = aPongController;
      }
    
      /**
       * Methods Called on a key press 
       *  calls the controller to process
       */
      private class Transaction implements KeyListener  // When character typed
      {
        @Override
        public void keyPressed(KeyEvent e)      // Obey this method
        {
          // Make -ve so not confused with normal characters
          controller.userKeyInteraction( -e.getKeyCode() );
        }
    
        @Override
        public void keyReleased(KeyEvent e)
        {
          // Called on key release including specials
        }
    
        @Override
        public void keyTyped(KeyEvent e)
        {
          // Send internal code for key
          controller.userKeyInteraction( e.getKeyChar() );
        }
        
      }
     }
    
