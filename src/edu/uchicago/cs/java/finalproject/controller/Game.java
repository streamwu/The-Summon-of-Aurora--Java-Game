package edu.uchicago.cs.java.finalproject.controller;

import sun.audio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;

import edu.uchicago.cs.java.finalproject.game.model.*;
import edu.uchicago.cs.java.finalproject.game.view.*;
import edu.uchicago.cs.java.finalproject.sounds.Sound;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
	// FIELDS
	// ===============================================

    private String strDisplay = "";

	public static final Dimension DIM = new Dimension(1100, 700); //the dimension of the game.
	private GamePanel gmpPanel;
	public static Random R = new Random();
	public final static int ANI_DELAY = 45; // milliseconds between screen
											// updates (animation)
	private Thread thrAnim;
	private int nLevel = 3;
	private int nTick = 0;
	private ArrayList<Tuple> tupMarkForRemovals;
	private ArrayList<Tuple> tupMarkForAdds;
	private boolean bMuted = true;
    private OffScreenImage offScreenImage;

    private boolean showInstr = false;
    private boolean bShowLevel = false;
    private boolean successfullyEnds = false;
    private int nShowLevel;
    private int nShowFail;
    private int nShowCongratulations;
    private final int SHOW_MESSAGE_PAUSE1 = 50;
    private final int SHOW_MESSAGE_PAUSE2 = 150;

	private final int PAUSE = 80, // p key
			QUIT = 81, // q key
			LEFT = 37, // rotate left; left arrow
			RIGHT = 39, // rotate right; right arrow
			UP = 38, // thrust; up arrow
			START = 83, // s key
			FIRE = 32, // space key
			MUTE = 77, // m-key mute
            INSTRUCTION = 73,   //i-key

	// for possible future use
	 HYPER = 68, 					// d key
	 SHIELD = 65, 				// a key arrow
	// NUM_ENTER = 10, 				// hyp
	 SPECIAL = 70; 					// fire special weapon;  F key

	private Clip clpThrust;
	private Clip clpMusicBackground;

	private static final int SPAWN_NEW_SHIP_FLOATER = 500;
    private static final int SPAWN_CRUISE = 450;


    BufferedImage image;
    BufferedImage instr_image;

	// ===============================================
	// ==CONSTRUCTOR
	// ===============================================

	public Game() {

        offScreenImage = new OffScreenImage();
		gmpPanel = new GamePanel(DIM,offScreenImage);
		gmpPanel.addKeyListener(this);

		clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
		clpMusicBackground = Sound.clipForLoopFactory("music-background.wav");

        try{
            image = ImageIO.read(new File("aurora.jpg"));
            instr_image = ImageIO.read(new File("instr.png"));
        }

        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        nShowLevel = SHOW_MESSAGE_PAUSE1;
        nShowFail = SHOW_MESSAGE_PAUSE2;
        nShowCongratulations = SHOW_MESSAGE_PAUSE2;
	}

	// ===============================================
	// ==METHODS
	// ===============================================

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() { // uses the Event dispatch thread from Java 5 (refactored)
					public void run() {
						try {
							Game game = new Game(); // construct itself
							game.fireUpAnimThread();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void fireUpAnimThread() { // called initially
		if (thrAnim == null) {
			thrAnim = new Thread(this); // pass the thread a runnable object (this)
			thrAnim.start();
		}
	}

	// implements runnable - must have run method
	public void run() {

		// lower this thread's priority; let the "main" aka 'Event Dispatch'
		// thread do what it needs to do first
		thrAnim.setPriority(Thread.MIN_PRIORITY);

		// and get the current time
		long lStartTime = System.currentTimeMillis();

		// this thread animates the scene
		while (Thread.currentThread() == thrAnim) {

			tick();
			spawnNewShipFloater();
            spawnNewCruiseFloater();


            drawOffScreen();

			//gmpPanel.update(gmpPanel.getGraphics()); // update takes the graphics context we must
														// surround the sleep() in a try/catch block
														// this simply controls delay time between 
														// the frames of the animation

			//this might be a good place to check for collisions
			checkCollisions();
			//this might be a god place to check if the level is clear (no more foes)
			//if the level is clear then spawn some big asteroids -- the number of asteroids 
			//should increase with the level.
            if(CommandCenter.isPlaying()){
                checkNewLevel();
            }




			try {
				// The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update) 
				// between frames takes longer than ANI_DELAY, then the difference between lStartTime - 
				// System.currentTimeMillis() will be negative, then zero will be the sleep time
				lStartTime += ANI_DELAY;
				Thread.sleep(Math.max(0,
						lStartTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				// just skip this frame -- no big deal
				continue;
			}
		} // end while
	} // end run

    // used for hyperspace
    public static boolean collisions(){
        boolean thereIsCollision = false;
        Point pntFriendCenter, pntFoeCenter;
        int nFriendRadius, nFoeRadius;

        for (Movable movFriend : CommandCenter.movFriends) {
            for (Movable movFoe : CommandCenter.movFoes) {
                if ((movFriend instanceof Falcon) ){
                    pntFriendCenter = movFriend.getCenter();
                    pntFoeCenter = movFoe.getCenter();
                    nFriendRadius = movFriend.getRadius();
                    nFoeRadius = movFoe.getRadius();
                    //detect collision
                    if (pntFriendCenter.distance(pntFoeCenter) < (nFriendRadius + nFoeRadius))
                        thereIsCollision = true;
                }
            }
        }
        return thereIsCollision;
    }


	private void checkCollisions() {

		
		//@formatter:off
		//for each friend in movFriends
			//for each foe in movFoes
				//if the distance between the two centers is less than the sum of their radii
					//mark it for removal
		
		//for each mark-for-removal
			//remove it
		//for each mark-for-add
			//add it
		//@formatter:on
		
		//we use this ArrayList to keep pairs of movMovables/movTarget for either
		//removal or insertion into our arrayLists later on
		tupMarkForRemovals = new ArrayList<Tuple>();
		tupMarkForAdds = new ArrayList<Tuple>();

		Point pntFriendCenter, pntFoeCenter;
		int nFriendRadius, nFoeRadius;

		for (Movable movFriend : CommandCenter.movFriends) {
			for (Movable movFoe : CommandCenter.movFoes) {

				pntFriendCenter = movFriend.getCenter();
				pntFoeCenter = movFoe.getCenter();
				nFriendRadius = movFriend.getRadius();
				nFoeRadius = movFoe.getRadius();

				//detect collision
                if((movFoe instanceof Asteroid || movFoe instanceof UFO) &&
                   (pntFriendCenter.distance(pntFoeCenter) < (nFriendRadius + nFoeRadius))) {
                    //falcon
                    if ((movFriend instanceof Falcon) ){
                        if (!CommandCenter.getFalcon().getProtected()){
                            tupMarkForRemovals.add(new Tuple(CommandCenter.movFriends, movFriend));
                            CommandCenter.spawnFalcon(false);
                            createDebris((Sprite)movFriend, tupMarkForAdds);

                            if(movFoe instanceof Asteroid) {
                                createDebris((Sprite)movFoe, tupMarkForAdds);
                                killFoe(movFoe);
                            }
                            else if((movFoe instanceof UFO)) {
                                killUFO(movFoe);
                            }

                        }
                    }
                    //not the falcon
                    else {
                        tupMarkForRemovals.add(new Tuple(CommandCenter.movFriends, movFriend));
                        if(movFoe instanceof Asteroid) {
                            createDebris((Sprite)movFoe, tupMarkForAdds);
                            killFoe(movFoe);
                        }
                        else if((movFoe instanceof UFO))
                            killUFO(movFoe);

                        CommandCenter.setScore(CommandCenter.getScore() + 1);
                    }//end else

                    //explode/remove foe
                }


                //movFoe is bullet coming from UFO
                else if(movFriend instanceof Falcon && movFoe instanceof ET_bullet &&
                        pntFriendCenter.distance(pntFoeCenter) < (nFriendRadius + nFoeRadius)){
                    tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));

                    tupMarkForRemovals.add(new Tuple(CommandCenter.movFriends, movFriend));
                    CommandCenter.spawnFalcon(false);
                    createDebris((Sprite)movFriend, tupMarkForAdds);
                }


			}//end inner for
		}//end outer for



        //check for collisions between falcon and cruise(if collide, cruise is picked up)
        if (CommandCenter.getFalcon() != null){
            Point pntFalCenter = CommandCenter.getFalcon().getCenter();
            int nFalRadius = CommandCenter.getFalcon().getRadius();
            Point pntCruiseFloaterCenter;
            int nCruiseFloaterRadius;

            for (Movable movCruiseFloater : CommandCenter.movCruiseFloaters) {
                pntCruiseFloaterCenter = movCruiseFloater.getCenter();
                nCruiseFloaterRadius = movCruiseFloater.getRadius();

                //detect collision
                if (pntFalCenter.distance(pntCruiseFloaterCenter) < (nFalRadius + nCruiseFloaterRadius)) {

                    CommandCenter.setNumCruise(CommandCenter.getNumCruise() + 1);
                    //CommandCenter.movFloaters.add(new NewShipFloater());
                    tupMarkForRemovals.add(new Tuple(CommandCenter.movCruiseFloaters, movCruiseFloater));
                    Sound.playSound("pacman_eatghost.wav");

                }//end if
            }//end inner for
        }


		//check for collisions between falcon and floaters
		if (CommandCenter.getFalcon() != null){
			Point pntFalCenter = CommandCenter.getFalcon().getCenter();
			int nFalRadius = CommandCenter.getFalcon().getRadius();
			Point pntFloaterCenter;
			int nFloaterRadius;
			
			for (Movable movFloater : CommandCenter.movFloaters) {
				pntFloaterCenter = movFloater.getCenter();
				nFloaterRadius = movFloater.getRadius();
	
				//detect collision
				if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadius + nFloaterRadius)) {

                    CommandCenter.setNumFalcons(CommandCenter.getNumFalcons() + 1);
					tupMarkForRemovals.add(new Tuple(CommandCenter.movFloaters, movFloater));
					Sound.playSound("pacman_eatghost.wav");
	
				}//end if 
			}//end inner for
		}//end if not null
		
		//remove these objects from their appropriate ArrayLists
		//this happens after the above iterations are done
		for (Tuple tup : tupMarkForRemovals) 
			tup.removeMovable();
		
		//add these objects to their appropriate ArrayLists
		//this happens after the above iterations are done
		for (Tuple tup : tupMarkForAdds) 
			tup.addMovable();

		//call garbage collection
		System.gc();
		
	}//end meth


    private void createDebris(Sprite spr, ArrayList tupTups){

        Point[] pntCs = spr.getObjectPoints();
        for (int nC = 0; nC < pntCs.length -1 ; nC++) {
            tupTups.add(new Tuple(CommandCenter.movDebris, new Debris(spr, pntCs[nC], pntCs[nC+1])));
        }
        tupTups.add(new Tuple(CommandCenter.movDebris, new Debris(spr, pntCs[0], pntCs[pntCs.length-1])));

    }

	private void killFoe(Movable movFoe) {
		
		if (movFoe instanceof Asteroid){

			//we know this is an Asteroid, so we can cast without threat of ClassCastException
			Asteroid astExploded = (Asteroid)movFoe;
			//big asteroid 
			if(astExploded.getSize() == 0){
				//spawn two medium Asteroids
				tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
				tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
				
			} 
			//medium size asteroid exploded
			else if(astExploded.getSize() == 1){
				//spawn three small Asteroids
				tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
				tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
				tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
			}
			//remove the original Foe	
			tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
		
			
		} 
		//not an asteroid
		else {
			//remove the original Foe
			tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
		}

		
	}


    private void killUFO(Movable ufo){
        if(ufo instanceof UFO){
            ((UFO) ufo).setKilled(true);
        }

    }

	//some methods for timing events in the game,
	//such as the appearance of UFOs, floaters (power-ups), etc. 
	public void tick() {
		if (nTick == Integer.MAX_VALUE)
			nTick = 0;
		else
			nTick++;
	}

	public int getTick() {
		return nTick;
	}

	private void spawnNewShipFloater() {
		//make the appearance of power-up dependent upon ticks and levels
		//the higher the level the more frequent the appearance
		if (nTick % (SPAWN_NEW_SHIP_FLOATER - nLevel * 2) == 0) {
			CommandCenter.movFloaters.add(new NewShipFloater());
		}
	}

    private void spawnNewCruiseFloater() {
        //Similar to newShipFloater,make the appearance of cruise also dependent upon ticks and levels
        //the higher the level the more frequent the appearance
        //Once picked up, it can be used later anytime when c + space keys pressed
        if (nTick % (SPAWN_CRUISE - nLevel * 2) == 0) {
            CommandCenter.movCruiseFloaters.add(new NewCruiseFloater());
        }
    }


	// Called when user presses 's'
	private void startGame() {
		CommandCenter.clearAll();
		CommandCenter.initGame();

		CommandCenter.setLevel(0);


		CommandCenter.setPlaying(true);
		CommandCenter.setPaused(false);
		//if (!bMuted)
		   // clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);


	}

	//this method spawns new asteroids
	private void spawnAsteroids(int nNum) {
		for (int nC = 0; nC < nNum; nC++) {
			//Asteroids with size of zero are big
			CommandCenter.movFoes.add(new Asteroid(0));
		}
	}

    private void spawnUFOs(int n, int level) {

        for (int nC = 0; nC < n; nC++) {

            CommandCenter.movFoes.add(new UFO(level));
        }

    }
	
	
	private boolean isLevelClear(){
		//if there are no more Asteroids on the screen
		
		boolean bAsteroidFree = true;
		for (Movable movFoe : CommandCenter.movFoes) {
			if (movFoe instanceof Asteroid){
				bAsteroidFree = false;
				break;
			}
		}
		
		return bAsteroidFree;

		
	}
	
	private void checkNewLevel(){
		if(isLevelClear()) {

			if (CommandCenter.getFalcon() != null)
				CommandCenter.getFalcon().setProtected(true);
			
			spawnAsteroids(CommandCenter.getLevel() + 2);



			CommandCenter.setLevel(CommandCenter.getLevel() + 1);
            bShowLevel = true;

            //level 2     spawn 4 UFOs. Besides, the falcon's velocity improves
            if(CommandCenter.getLevel() == 2){
                spawnUFOs(5, 2);
                CommandCenter.getFalcon().setThrust(CommandCenter.getFalcon().getThrust() + 0.10);
            }

            //level3     spawn 6 UFOs, but there are ETs inside them  firing. Besides, the falcon's velocity improves
            if(CommandCenter.getLevel() == 3){
                spawnUFOs(7, 3);
                CommandCenter.getFalcon().setThrust(CommandCenter.getFalcon().getThrust() + 0.50);
            }

            //win!
            if(CommandCenter.getLevel() >= nLevel + 1){
                successfullyEnds = true;
            }

		}
	}


    public void drawOffScreen() {
        if (offScreenImage.getGrpOff() == null) {
            offScreenImage.reset();
        }

        Graphics grpOff = offScreenImage.getGrpOff();

            grpOff.drawImage(image,0,0, Game.DIM.width,Game.DIM.height,null);

        if(showInstr){
            showInstruction(grpOff);
        }


        if(CommandCenter.getLevel() <= nLevel && CommandCenter.isPlaying() && bShowLevel && nShowLevel > 0){
            showLevel(grpOff);
            nShowLevel--;
        }
        else if(nShowLevel == 0){
            bShowLevel = false;
            nShowLevel = SHOW_MESSAGE_PAUSE1;
        }



        else if(!showInstr){

            drawScore(grpOff);
            drawLevel(grpOff);



            if (!CommandCenter.isPlaying()) {
                displayTextOnScreen();
            } else if (CommandCenter.isPaused()) {
                strDisplay = "Game Paused";
                grpOff.drawString(strDisplay,
                        (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4);
            }

            //playing and not paused!
            else {

                //draw them in decreasing level of importance
                //friends will be on top layer and debris on the bottom
                iterateMovables(grpOff,
                        CommandCenter.movDebris,
                        CommandCenter.movFloaters,
                        CommandCenter.movCruiseFloaters,
                        CommandCenter.movFoes,
                        CommandCenter.movFriends
                );


                drawNumberShipsLeft(grpOff);
                drawNumberShields(grpOff);
                drawNumberHypers(grpOff);
                drawNumberCruises(grpOff);



                if (CommandCenter.isGameOver()) {
                    if( nShowFail > 0){

                        showFail(grpOff);
                        nShowFail--;
                    }
                    else if(nShowFail == 0)
                        CommandCenter.setPlaying(false);

                }

                if(successfullyEnds) {
                    if( nShowCongratulations > 0){
                        showCongratulations(grpOff);
                        nShowCongratulations--;
                    }
                    else if(nShowCongratulations == 0)
                        CommandCenter.setPlaying(false);
                }


            }

        }



        //when we call repaint, repaint calls update(g)
        gmpPanel.repaint();
    }





    private void showFail(Graphics g){
        g.setColor(Color.BLUE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 64));
        g.drawString("Game over..", offScreenImage.getFontWidth() + 350, offScreenImage.getFontHeight() + 350);
    }






    //for each movable array, process it.
    private void iterateMovables(Graphics g, CopyOnWriteArrayList<Movable>...movMovz){

        for (CopyOnWriteArrayList<Movable> movMovs : movMovz) {
            for (Movable mov : movMovs) {

                mov.move();
                mov.draw(g);
                mov.fadeInOut();
                mov.expire();
            }
        }

    }


    //offscreen
    // Draw the number of falcons left on the bottom-right of the screen.
    private void drawNumberShipsLeft(Graphics g) {
        Falcon fal = CommandCenter.getFalcon();
        double[] dLens = fal.getLengths();
        int nLen = fal.getDegrees().length;
        Point[] pntMs = new Point[nLen];
        int[] nXs = new int[nLen];
        int[] nYs = new int[nLen];

        //convert to cartesean points
        for (int nC = 0; nC < nLen; nC++) {
            pntMs[nC] = new Point((int) (10 * dLens[nC] * Math.sin(Math
                    .toRadians(90) + fal.getDegrees()[nC])),
                    (int) (10 * dLens[nC] * Math.cos(Math.toRadians(90)
                            + fal.getDegrees()[nC])));
        }

        //set the color to white
        g.setColor(Color.white);
        //for each falcon left (not including the one that is playing)
        for (int nD = 1; nD < CommandCenter.getNumFalcons(); nD++) {
            //create x and y values for the objects to the bottom right using cartesean points again
            for (int nC = 0; nC < fal.getDegrees().length; nC++) {
                nXs[nC] = pntMs[nC].x + Game.DIM.width - (20 * nD);
                nYs[nC] = pntMs[nC].y + Game.DIM.height - 40;
            }
            g.drawPolygon(nXs, nYs, nLen);
        }
    }

    private void drawNumberShields(Graphics g){
        int num = CommandCenter.getNumShield();
        if(num < 0)
            num = 0;
        g.drawString("Shields:          " + num, Game.DIM.width - 120, 30);
    }
    private void drawNumberHypers(Graphics g){
        int num = CommandCenter.getNumHyperspace();
        if(num < 0)
            num = 0;
        g.drawString("Hyperspaces:  " + num, Game.DIM.width - 120, 50);
    }

    private void drawNumberCruises(Graphics g){
        int num = CommandCenter.getNumCruise();
        if(num < 0)
            num = 0;
        g.drawString("Cruises:          " + num, Game.DIM.width - 120, 70);
    }



    //move to Game and pass in the offScreenImage
    // This method draws some text to the middle of the screen before/after a game
    private void displayTextOnScreen() {

        Graphics grpOff = offScreenImage.getGrpOff();
        grpOff.setColor(Color.YELLOW);
        grpOff.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        strDisplay = "**  THE SUMMON OF AURORA  **";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2 - 30, Game.DIM.height / 4);


        grpOff.setColor(Color.white);
        grpOff.setFont(new Font("SansSerif", Font.BOLD, 12));
        strDisplay = "* Press 'I' for more instructions * ";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 40);


        strDisplay = "use the arrow keys to turn and thrust";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 80);

        strDisplay = "use the space bar to fire";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 120);

        strDisplay = "'S' to Start";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 160);

        strDisplay = "'P' to Pause";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 200);

        strDisplay = "'Q' to Quit";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 240);
        strDisplay = "left pinkie on 'A' for Shield";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 280);

        strDisplay = "left middle finger on 'D' for Hyperspace";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 320);

        strDisplay = "left index finger on 'F' for Guided Missile";
        grpOff.drawString(strDisplay,
                (Game.DIM.width - offScreenImage.getFmt().stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                + offScreenImage.getFontHeight() + 360);
    }




    private void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(offScreenImage.getFnt());
        if (CommandCenter.getScore() != 0) {
            g.drawString("SCORE :  " + CommandCenter.getScore(), offScreenImage.getFontWidth(), offScreenImage.getFontHeight() + 20);
        } else {
            g.drawString("NO SCORE", offScreenImage.getFontWidth(), offScreenImage.getFontHeight() + 20);
        }
    }

    private void showLevel(Graphics g) {
        g.setColor(Color.BLUE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 64));
        g.drawString("Level " + CommandCenter.getLevel(), offScreenImage.getFontWidth() + 400, offScreenImage.getFontHeight() + 350);


    }

    private void showCongratulations(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
        g.drawString("Congratulations! ", offScreenImage.getFontWidth() + 400, offScreenImage.getFontHeight() + 220);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        g.setColor(Color.BLACK);
        g.drawString("Then, The earth comes back to the peace... ", offScreenImage.getFontWidth() + 250, offScreenImage.getFontHeight() + 400);

    }

    private void drawLevel(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(offScreenImage.getFnt());
        if (CommandCenter.isPlaying()) {
            g.drawString("LEVEL :  " + CommandCenter.getLevel(), offScreenImage.getFontWidth(), offScreenImage.getFontHeight() + 40);
        }
    }

    private void showInstruction(Graphics g){

        g.drawImage(instr_image, 0, 0, Game.DIM.width,Game.DIM.height, null);
    }


	// Varargs for stopping looping-music-clips
	private static void stopLoopingSounds(Clip... clpClips) {
		for (Clip clp : clpClips) {
			clp.stop();
		}
	}

	// ===============================================
	// KEYLISTENER METHODS
	// ===============================================

	@Override
	public void keyPressed(KeyEvent e) {
		Falcon fal = CommandCenter.getFalcon();
		int nKey = e.getKeyCode();
		// System.out.println(nKey);

		if (nKey == START && !CommandCenter.isPlaying()) {
            startGame();
            showInstr = false;
        }


        if (nKey == INSTRUCTION && !CommandCenter.isPlaying())
            showInstr = true;

		if (fal != null) {

			switch (nKey) {
			case PAUSE:
				CommandCenter.setPaused(!CommandCenter.isPaused());
				if (CommandCenter.isPaused())
					stopLoopingSounds(clpMusicBackground, clpThrust);
				else
					clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
				break;
			case QUIT:
				System.exit(0);
				break;
			case UP:
				fal.thrustOn();
				if (!CommandCenter.isPaused())
					clpThrust.loop(Clip.LOOP_CONTINUOUSLY);
				break;
			case LEFT:
				fal.rotateLeft();
				break;
			case RIGHT:
				fal.rotateRight();
				break;



			// possible future use
			// case KILL:
			// case SHIELD:
			// case NUM_ENTER:

			default:
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Falcon fal = CommandCenter.getFalcon();
		int nKey = e.getKeyCode();
		 System.out.println(nKey);

		if (fal != null) {
			switch (nKey) {
			case FIRE:
				CommandCenter.movFriends.add(new Bullet(fal));
				Sound.playSound("laser.wav");
				break;
				
			//special is a special weapon, current it just fires the cruise missile. 
			case SPECIAL:
                if(CommandCenter.getNumCruise() > 0){

                    //random select an asteroid as object
                    int n1 = CommandCenter.movFoes.size();
                    int n2 = R.nextInt(n1);
                    CommandCenter.movFriends.add(new Cruise(fal, (Asteroid)CommandCenter.movFoes.get(n2)));
                    CommandCenter.setNumCruise(CommandCenter.getNumCruise() - 1);
                }
				//Sound.playSound("laser.wav");
				break;
				
			case LEFT:
				fal.stopRotating();
				break;
			case RIGHT:
				fal.stopRotating();
				break;
			case UP:
				fal.thrustOff();
				clpThrust.stop();
				break;
            case SHIELD:
                CommandCenter.setShield();
                CommandCenter.setNumShield(CommandCenter.getNumShield() - 1);
                break;
            case HYPER:
                CommandCenter.setHyperspace();
                CommandCenter.setNumHyperspace(CommandCenter.getNumHyperspace() - 1);
                break;



			case MUTE:
				if (!bMuted){
					stopLoopingSounds(clpMusicBackground);
					bMuted = !bMuted;
				} 
				else {
					clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
					bMuted = !bMuted;
				}
				break;
				
				
			default:
				break;
			}
		}
	}

	@Override
	// Just need it b/c of KeyListener implementation
	public void keyTyped(KeyEvent e) {
	}
	

	
}

// ===============================================
// ==A tuple takes a reference to an ArrayList and a reference to a Movable
//This class is used in the collision detection method, to avoid mutating the array list while we are iterating
// it has two public methods that either remove or add the movable from the appropriate ArrayList 
// ===============================================

class Tuple{
	//this can be any one of several CopyOnWriteArrayList<Movable>
	private CopyOnWriteArrayList<Movable> movMovs;
	//this is the target movable object to remove
	private Movable movTarget;
	
	public Tuple(CopyOnWriteArrayList<Movable> movMovs, Movable movTarget) {
		this.movMovs = movMovs;
		this.movTarget = movTarget;
	}
	
	public void removeMovable(){
		movMovs.remove(movTarget);
	}
	
	public void addMovable(){
		movMovs.add(movTarget);
	}

}
