package edu.uchicago.cs.java.finalproject.game.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uchicago.cs.java.finalproject.controller.Game;

import javax.imageio.ImageIO;


public class Falcon extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================
	
	private final double THRUST = .70;
    private double thrust;

	final int DEGREE_STEP = 7;


	private boolean bShield = false;
    private boolean bHyperspace = false;
	private boolean bFlame = false;
	private boolean bProtected; //for fade in and out
	
	private boolean bThrusting = false;
	private boolean bTurningRight = false;
	private boolean bTurningLeft = false;
	
	private int nShield;

	private final double[] FLAME = { 23 * Math.PI / 24 + Math.PI / 2,
			Math.PI + Math.PI / 2, 25 * Math.PI / 24 + Math.PI / 2 };

	private int[] nXFlames = new int[FLAME.length];
	private int[] nYFlames = new int[FLAME.length];

	private Point[] pntFlames = new Point[FLAME.length];

    private Point transportedPosition;  //used for hyperspace
    BufferedImage image;
	
	// ==============================================================
	// CONSTRUCTOR 
	// ==============================================================
	
	public Falcon() {
		super();
        transportedPosition = new Point(-1, -1);
        thrust = THRUST;

		ArrayList<Point> pntCs = new ArrayList<Point>();

        try{
            image = ImageIO.read(new File("green.jpg"));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
		// top of ship
		pntCs.add(new Point(0, 18)); 
		
		//right points
		pntCs.add(new Point(3, 3)); 
		pntCs.add(new Point(12, 0)); 
		pntCs.add(new Point(13, -2)); 
		pntCs.add(new Point(13, -4)); 
		pntCs.add(new Point(11, -2)); 
		pntCs.add(new Point(4, -3)); 
		pntCs.add(new Point(2, -10)); 
		pntCs.add(new Point(4, -12)); 
		pntCs.add(new Point(2, -13)); 

		//left points
		pntCs.add(new Point(-2, -13)); 
		pntCs.add(new Point(-4, -12));
		pntCs.add(new Point(-2, -10)); 
		pntCs.add(new Point(-4, -3)); 
		pntCs.add(new Point(-11, -2));
		pntCs.add(new Point(-13, -4));
		pntCs.add(new Point(-13, -2)); 
		pntCs.add(new Point(-12, 0)); 
		pntCs.add(new Point(-3, 3)); 
		

		assignPolarPoints(pntCs);

		setColor(Color.white);
		
		//put falcon in the middle.
		setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2 - 60));
		
		//with random orientation
		setOrientation(Game.R.nextInt(360));
		
		//this is the size of the falcon
		setRadius(35);

		//these are falcon specific
		setProtected(true);
		setFadeValue(0);
	}
	
	
	// ==============================================================
	// METHODS 
	// ==============================================================

	public void move() {
		super.move();
		if (bThrusting) {
			bFlame = true;
			double dAdjustX = Math.cos(Math.toRadians(getOrientation()))
					* thrust;
			double dAdjustY = Math.sin(Math.toRadians(getOrientation()))
					* thrust;
			setDeltaX(getDeltaX() + dAdjustX);
			setDeltaY(getDeltaY() + dAdjustY);
		}
		if (bTurningLeft) {

			if (getOrientation() <= 0 && bTurningLeft) {
				setOrientation(360);
			}
			setOrientation(getOrientation() - DEGREE_STEP);
		} 
		if (bTurningRight) {
			if (getOrientation() >= 360 && bTurningRight) {
				setOrientation(0);
			}
			setOrientation(getOrientation() + DEGREE_STEP);
		}

	} //end move

	public void rotateLeft() {
		bTurningLeft = true;
	}

	public void rotateRight() {
		bTurningRight = true;
	}

	public void stopRotating() {
		bTurningRight = false;
		bTurningLeft = false;
	}

	public void thrustOn() {
		bThrusting = true;
	}

	public void thrustOff() {
		bThrusting = false;
		bFlame = false;
        setDeltaX(0);
        setDeltaY(0);
	}

	private int adjustColor(int nCol, int nAdj) {
		if (nCol - nAdj <= 0) {
			return 0;
		} else {
			return nCol - nAdj;
		}
	}

	public void draw(Graphics g) {
        super.draw(g);
		//does the fading at the beginning or after hyperspace
		Color colShip;
		if (getFadeValue() == 255) {
			colShip = Color.BLACK;
		} else {
			colShip = new Color(adjustColor(getFadeValue(), 200), getFadeValue(), adjustColor(
					getFadeValue(), 175));
		}

//		//shield on
		 if (bShield && nShield > 0) {


			setNShield(getNShield() - 1);

			g.setColor(Color.YELLOW);
			g.drawOval(getCenter().x - getRadius(),
					getCenter().y - getRadius(), getRadius() * 2,
					getRadius() * 2);


		}
		if(bShield && nShield == 0){
            setBShield(false);
            setProtected(false, getFadeValue());
        }
		//end of shield

        //hyperspace
        if(bHyperspace) {
            setTransportedPosition();
            setCenter(transportedPosition);
            bHyperspace = false;
        }


		//thrusting
		if (bFlame) {
			g.setColor(colShip);
			//the flame
			for (int nC = 0; nC < FLAME.length; nC++) {
				if (nC % 2 != 0) //odd
				{
					pntFlames[nC] = new Point((int) (getCenter().x + 2
							* getRadius()
							* Math.sin(Math.toRadians(getOrientation())
									+ FLAME[nC])), (int) (getCenter().y - 2
							* getRadius()
							* Math.cos(Math.toRadians(getOrientation())
									+ FLAME[nC])));

				} else //even
				{
					pntFlames[nC] = new Point((int) (getCenter().x + getRadius()
							* 1.1
							* Math.sin(Math.toRadians(getOrientation())
									+ FLAME[nC])),
							(int) (getCenter().y - getRadius()
									* 1.1
									* Math.cos(Math.toRadians(getOrientation())
											+ FLAME[nC])));

				} //end even/odd else

			} //end for loop

			for (int nC = 0; nC < FLAME.length; nC++) {
				nXFlames[nC] = pntFlames[nC].x;
				nYFlames[nC] = pntFlames[nC].y;

			} //end assign flame points

			g.setColor( Color.yellow);
			g.fillPolygon(nXFlames, nYFlames, FLAME.length);

		} //end if flame

        //g.setColor(Color.ORANGE);
        //g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);




        drawFalconWithColor(g, colShip);
        fillFalconWithTag(g, colShip);


	} //end draw()


    public void drawFalconWithColor(Graphics g, Color col) {

        g.setColor(col);

        g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);

        g.setColor(Color.GREEN);
        g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);

    }

	public void fillFalconWithTag(Graphics g, Color col) {

		//g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);

        g.setColor(col);

        double r = getRadius()/3;
        g.drawImage(image, (int)(getCenter().x - r/2), (int)(getCenter().y - r/2), (int)r, (int)r, null);

	}

    public void setThrust(double v){
        thrust = v;
    }

    public double getThrust(){
        return thrust;
    }

	public void fadeInOut() {
		if (getProtected()) {
			setFadeValue(getFadeValue() + 3);
		}
        if(getFadeValue() > 255)
            setFadeValue(255);

		if (getFadeValue() == 255) {
			setProtected(false);
		}
	}
	
	public void setProtected(boolean bParam) {
		if (bParam) {
			setFadeValue(0);
		}
		bProtected = bParam;
	}

	public void setProtected(boolean bParam, int n) {
		if (bParam && n % 3 == 0) {
			setFadeValue(n);
		} else if (bParam) {
			setFadeValue(0);
		}
		bProtected = bParam;
	}	

	public boolean getProtected() {
        return bProtected || bShield;
    }

    //Shield block
	public void setNShield(int n) {nShield = n;}
	public int getNShield() {return nShield;}

    public void setBShield(boolean bParam){
        bShield = bParam;
        setProtected(bParam, getFadeValue());
    }

    //HyperSpace block
    public void setBHyperspace(boolean bParam) {bHyperspace = bParam;}

    public void setTransportedPosition(){
        do{
            //random delta-x
            int nDX = Game.R.nextInt(500);
            if(nDX %2 ==0)
                nDX = -nDX;

            //random delta-y
            int nDY = Game.R.nextInt(500);
            if(nDY %2 ==0)
                nDY = -nDY;

            transportedPosition = new Point(getCenter().x + nDX, getCenter().x + nDY);

        } while (Game.collisions());

    }
    //HyperSpace block ends
	
} //end class
