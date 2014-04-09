package edu.uchicago.cs.java.finalproject.game.model;


import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import edu.uchicago.cs.java.finalproject.controller.Game;
import edu.uchicago.cs.java.finalproject.game.view.OffScreenImage;

import javax.imageio.ImageIO;
import javax.swing.*;

public class UFO extends Sprite {

    BufferedImage image;

    //radius of a large asteroid
    private final int RAD = 100;
    private final int ET_FIRE = 500;
    private boolean killed = false;
    private int nSpin;
    private final int SPIN = 10;
    private int level;

    public UFO(int level){

        //call Sprite constructor
        super();

        nSpin = SPIN;
        this.level = level;

        try{
            image = ImageIO.read(new File("ufo.png"));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        int nX = Game.R.nextInt(15);
        int nY = Game.R.nextInt(15);

        //set random DeltaX
        if (nX % 2 == 0)
            setDeltaX(nX);
        else
            setDeltaX(-nX);

        //set random DeltaY
        if (nY % 2 == 0)
            setDeltaX(nY);
        else
            setDeltaX(-nY);

        setCenter(new Point(Game.R.nextInt(Game.DIM.width),
                Game.R.nextInt(Game.DIM.height)));

        //with random orientation
        setOrientation(Game.R.nextInt(360));

        //this is the size of the falcon
        setRadius(RAD);

    }


    public void draw(Graphics g){
        double r = getRadius() / 2;
        g.drawImage(image, getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius(), (int)r, null);

        //generate ET_bullet, with 1/10 possibility

        if(level == 3 && Game.R.nextInt(80) == 1) {
            CommandCenter.movFoes.add(new ET_bullet(this));
        }

        //when it's killed, self-rotate a cycle and then disappear
        if (killed){
            setDeltaX(0);
            setDeltaY(0);
            if(nSpin > 0) {
                Graphics2D g2 = (Graphics2D) g;
                AffineTransform origForm = g2.getTransform(); // reserve context

                AffineTransform tx = new AffineTransform();
                double radians = 30 * Math.PI / 180; // rotation angle
                tx.rotate(radians, getCenter().x, getCenter().y); // rotation centre
                g2.setTransform(tx);

                double r1 = getRadius() / 2;
                int r2 = (int)r1;
                g2.drawImage(image, getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius(), r2, null);

                g2.setTransform(origForm); // recover context

                nSpin--;
            }
            if(nSpin == 0)
                CommandCenter.movFoes.remove(this);
        }

    }



    //overridden
    public void move(){
        super.move();

        //an asteroid spins, so you need to adjust the orientation at each move()

    }

    public void setKilled(boolean kill){
        killed = kill;
    }



}
