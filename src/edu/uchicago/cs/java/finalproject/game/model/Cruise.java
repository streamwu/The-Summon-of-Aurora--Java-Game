package edu.uchicago.cs.java.finalproject.game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import edu.uchicago.cs.java.finalproject.controller.Game;

public class Cruise extends Sprite {
    private final double FIRE_POWER = 18.0;
	//private final double FIRE_POWER_VERTICAL = 20.0;
    //private final double FIRE_POWER_PARALLEL = 30.0;

	private final int MAX_EXPIRE = 80;
	
	//for drawing alternative shapes
	//you could have more than one of these sets so that your sprite morphs into various shapes
	//throughout its life
		public double[] dLengthsAlts;
		public double[] dDegreesAlts;

        public Asteroid asteroid;

	public Cruise(Falcon fal, Asteroid ast) {

		super();

        asteroid = ast;

		//defined the points on a cartesean grid
		ArrayList<Point> pntCs = new ArrayList<Point>();


		pntCs.add(new Point(0, 5));
		pntCs.add(new Point(1, 3));
		pntCs.add(new Point(1, 0));
		pntCs.add(new Point(6, 0));
		pntCs.add(new Point(6, -1));
		pntCs.add(new Point(1, -1));
		pntCs.add(new Point(1, -2));

		pntCs.add(new Point(-1, -2));
		pntCs.add(new Point(-1, -1));
		pntCs.add(new Point(-6, -1));
		pntCs.add(new Point(-6, 0));
		pntCs.add(new Point(-1, 0));
		pntCs.add(new Point(-1, 3));
		assignPolarPoints(pntCs);
		
		
		//these are alt points
		ArrayList<Point> pntAs = new ArrayList<Point>();
		pntAs.add(new Point(0, 5));
		pntAs.add(new Point(1, 3));
		pntAs.add(new Point(1, -2));
		pntAs.add(new Point(-1, -2));
		pntAs.add(new Point(-1, 3));
		assignPolorPointsAlts(pntAs);

		//a cruis missile expires after 25 frames
		setExpire(MAX_EXPIRE);
		setRadius(20);

		//everything is relative to the falcon ship that fired the bullet
		setDeltaX(fal.getDeltaX()
				+ Math.cos(Math.toRadians(fal.getOrientation())) * FIRE_POWER);
		setDeltaY(fal.getDeltaY()
				+ Math.sin(Math.toRadians(fal.getOrientation())) * FIRE_POWER);
		setCenter(fal.getCenter());

		//set the bullet orientation to the falcon (ship) orientation
		setOrientation(fal.getOrientation());
		setColor(Color.RED);

	}

	
	//assign for alt imag
	protected void assignPolorPointsAlts(ArrayList<Point> pntCs) {
		 dDegreesAlts = convertToPolarDegs(pntCs);
		 dLengthsAlts = convertToPolarLens(pntCs);

	}
	
	@Override
	public void move() {
        /*int orientation = (int)(asteroid.getOrientation() + 270 +
                                Math.toDegrees(Math.atan(FIRE_POWER_PARALLEL / FIRE_POWER_VERTICAL)));
        setOrientation(orientation);
        setDeltaX(asteroid.getDeltaX() + Math.cos(Math.toRadians(orientation)) * FIRE_POWER);
        setDeltaY(asteroid.getDeltaY() + Math.sin(Math.toRadians(orientation)) * FIRE_POWER);
        */
        if(!CommandCenter.movFoes.contains(asteroid)) {
            int n1 = CommandCenter.movFoes.size();
            int n2 = Game.R.nextInt(n1);
            asteroid = (Asteroid)CommandCenter.movFoes.get(n2);
        }

        else {
            float x1 = 1;
            float y1 = 0;

            float x2 = asteroid.getCenter().x - getCenter().x;
            float y2 = asteroid.getCenter().y - getCenter().y;

            float dotProduct = x1 * x2 +  y1 * y2;
            float crossProduct = x1 * y2 - y1 * x2;

            double finalOrientation = Math.acos(dotProduct / Math.sqrt(y2 * y2 + x2 * x2));

            if(crossProduct < 0)
                finalOrientation = 2 * Math.PI - finalOrientation;

            finalOrientation = finalOrientation * 180 / Math.PI;

            setOrientation((int)finalOrientation);

            setDeltaX(Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER);
            setDeltaY(Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER);

            super.move();

            if (getExpire() < MAX_EXPIRE -5){
                setDeltaX(getDeltaX() * 1.07);
                setDeltaY(getDeltaY() * 1.07);
            }


        }


	}
	
	@Override
	public void draw(Graphics g){
		
		if (getExpire() < MAX_EXPIRE -5)
			super.draw(g);
		else{ 
			drawAlt(g);
		}
		
	}
	
	

    public void drawAlt(Graphics g) {
    	setXcoords( new int[dDegreesAlts.length]);
    	setYcoords( new int[dDegreesAlts.length]);
        setObjectPoints( new Point[dDegrees.length]);

        for (int nC = 0; nC < dDegreesAlts.length; nC++) {
        	
        	setXcoord((int) (getCenter().x + getRadius() 
                    * dLengthsAlts[nC] 
                    * Math.sin(Math.toRadians(getOrientation()) + dDegreesAlts[nC])), nC);
        	

        	setYcoord((int) (getCenter().y - getRadius()
                            * dLengthsAlts[nC]
                            * Math.cos(Math.toRadians(getOrientation()) + dDegreesAlts[nC])), nC);
            //need this line of code to create the points which we will need for debris
        	setObjectPoint( new Point(getXcoord(nC), getYcoord(nC)), nC);
        }
        
        g.setColor(Color.RED);
        g.fillPolygon(getXcoords(), getYcoords(), dDegreesAlts.length);
    }


	//override the expire method - once an object expires, then remove it from the arrayList.
	@Override
	public void expire() {
		if (getExpire() == 0)
			CommandCenter.movFriends.remove(this);
		else
			setExpire(getExpire() - 1);
	}

}
