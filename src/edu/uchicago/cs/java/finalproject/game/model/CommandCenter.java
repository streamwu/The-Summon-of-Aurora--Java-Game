package edu.uchicago.cs.java.finalproject.game.model;

import java.util.concurrent.CopyOnWriteArrayList;

import edu.uchicago.cs.java.finalproject.sounds.Sound;

// I only want one Command Center and therefore this is a perfect candidate for static
// Able to get access to methods and my movMovables ArrayList from the static context.
public class CommandCenter {

	private static int nNumFalcon;
    private static int nNumShield;   //number of shields you can use in certain level
    private static int nNumHyperspace;    //number of shields you can use in certain level
    private static int nNumCruise;     //number of cruises you have
    private static int nNumUFO;
	private static int nLevel;
	private static long lScore;
	private static Falcon falShip;
	private static boolean bPlaying;
	private static boolean bPaused;
	
	// These ArrayLists are thread-safe
	public static CopyOnWriteArrayList<Movable> movDebris = new CopyOnWriteArrayList<Movable>();
	public static CopyOnWriteArrayList<Movable> movFriends = new CopyOnWriteArrayList<Movable>();
	public static CopyOnWriteArrayList<Movable> movFoes = new CopyOnWriteArrayList<Movable>();
	public static CopyOnWriteArrayList<Movable> movFloaters = new CopyOnWriteArrayList<Movable>();
    public static CopyOnWriteArrayList<Movable> movCruiseFloaters = new CopyOnWriteArrayList<Movable>();
	

	// Constructor made private - static Utility class only
	private CommandCenter() {}
	
	public static void initGame(){
		setLevel(1);
		setScore(0);
		setNumFalcons(5);
        setNumShield(3);
        setNumHyperspace(3);
        setNumCruise(0);
        setNumUFO(0);
		spawnFalcon(true);
	}
	
	// The parameter is true if this is for the beginning of the game, otherwise false
	// When you spawn a new falcon, you need to decrement its number
	public static void spawnFalcon(boolean bFirst) {

		if (getNumFalcons() != 0) {
			falShip = new Falcon();
			movFriends.add(falShip);
			if (!bFirst)
			    setNumFalcons(getNumFalcons() - 1);
		}
		
		Sound.playSound("shipspawn.wav");

	}
	
	public static void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
	}

	public static boolean isPlaying() {
		return bPlaying;
	}

	public static void setPlaying(boolean bPlaying) {
		CommandCenter.bPlaying = bPlaying;
	}

	public static boolean isPaused() {
		return bPaused;
	}

	public static void setPaused(boolean bPaused) {
		CommandCenter.bPaused = bPaused;
	}
	
	public static boolean isGameOver() {		//if the number of falcons is zero, then game over
		if (getNumFalcons() == 0) {
			return true;
		}
		return false;
	}

	public static int getLevel() {
		return nLevel;
	}

	public  static long getScore() {
		return lScore;
	}

	public static void setScore(long lParam) {
		lScore = lParam;
	}

	public static void setLevel(int n) {
		nLevel = n;
	}

	public static int getNumFalcons() {
		return nNumFalcon;
	}

	public static void setNumFalcons(int nParam) {
		nNumFalcon = nParam;
	}

    //Shield processing block
    public static int getNumShield() {
        return nNumShield;
    }

    public static void setNumShield(int nParam) {
        nNumShield = nParam;
    }

    public static void setShield(){
        if(getNumShield() > 0){
            falShip.setBShield(true);
            falShip.setNShield(80);
        }
        else if(getNumShield() == 0)
            falShip.setBShield(false);
    }
    //Shield processing block ends


    //Hyperspace processing block
    public static int getNumHyperspace() {
        return nNumHyperspace;
    }

    public static void setNumHyperspace(int nParam) {
        nNumHyperspace = nParam;
    }

    public static void setHyperspace(){
        if(getNumHyperspace() > 0) {
            falShip.setBHyperspace(true);
        }
        else if(getNumHyperspace() == 0)
            falShip.setBHyperspace(false);
    }
    //Hyperspace processing block ends


    //Cruise block
    public static void setNumCruise(int n){ nNumCruise = n;}
    public static int getNumCruise(){return nNumCruise;}

    public static Falcon getFalcon(){
		return falShip;
	}

    public static void setNumUFO(int n) {nNumUFO = n;}
    public static int getNumUFO() {return nNumUFO;}


	public static void setFalcon(Falcon falParam){
		falShip = falParam;
	}

	public static CopyOnWriteArrayList<Movable> getMovDebris() {
		return movDebris;
	}



	public static CopyOnWriteArrayList<Movable> getMovFriends() {
		return movFriends;
	}



	public static CopyOnWriteArrayList<Movable> getMovFoes() {
		return movFoes;
	}


	public static CopyOnWriteArrayList<Movable> getMovFloaters() {
		return movFloaters;
	}


	
	
}
