package edu.uchicago.cs.java.finalproject.game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import edu.uchicago.cs.java.finalproject.controller.Game;

public class ET_bullet extends Sprite {
    private final double FIRE_POWER = 10.0;
    //private final double FIRE_POWER_VERTICAL = 20.0;
    //private final double FIRE_POWER_PARALLEL = 30.0;

    private final int MAX_EXPIRE = 150;

    //for drawing alternative shapes
    //you could have more than one of these sets so that your sprite morphs into various shapes
    //throughout its life
    public double[] dLengthsAlts;
    public double[] dDegreesAlts;


    public ET_bullet(UFO ufo) {

        super();

        //defined the points on a cartesean grid
        ArrayList<Point> pntCs = new ArrayList<Point>();


        pntCs.add(new Point(0, 3));

        pntCs.add(new Point(1, 2));
        pntCs.add(new Point(1, 0));
        pntCs.add(new Point(4, 0));
        pntCs.add(new Point(1, -1));

        pntCs.add(new Point(0, -2));

        pntCs.add(new Point(-1, -1));
        pntCs.add(new Point(-4, -1));
        pntCs.add(new Point(-1, 0));
        pntCs.add(new Point(-1, 2));

        assignPolarPoints(pntCs);

        /*
        //these are alt points
        ArrayList<Point> pntAs = new ArrayList<Point>();
        pntAs.add(new Point(0, 5));
        pntAs.add(new Point(1, 3));
        pntAs.add(new Point(1, -2));
        pntAs.add(new Point(-1, -2));
        pntAs.add(new Point(-1, 3));
        assignPolorPointsAlts(pntAs);
        */


        setExpire(MAX_EXPIRE);
        setRadius(20);

        //everything is relative to the falcon ship that fired the bullet
        setDeltaX(ufo.getDeltaX()
                + Math.cos(Math.toRadians(ufo.getOrientation())) * FIRE_POWER);
        setDeltaY(ufo.getDeltaY()
                + Math.sin(Math.toRadians(ufo.getOrientation())) * FIRE_POWER);
        setCenter(ufo.getCenter());

        //set the bullet orientation to the falcon (ship) orientation
        setOrientation(ufo.getOrientation());
        setColor(Color.BLACK);

    }


    @Override
    public void draw(Graphics g){
        super.draw(g);
        g.setColor(new Color(200, 255, 0));
        g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
    }




    //override the expire method - once an object expires, then remove it from the arrayList.
    @Override
    public void expire() {
        if (getExpire() == 0)
            CommandCenter.movFoes.remove(this);
        else
            setExpire(getExpire() - 1);
    }

}
