package uk.ac.nulondon;

import java.awt.Color;

public class Pixel {

    // pixel pointers
    Pixel left;
    Pixel right;

    double energy;
    Color color;

    public Pixel(int rgb) { // constructor that sets color field from inputted RGB
        this.color = new Color(rgb);
    }

    public Pixel(Color color) { // constructor that sets color field to inputted color
        this.color = color;
    }

    public double brightness() { // gets brightness of pixel by getting the average of its RGB components
        return (color.getBlue() + color.getRed() + color.getGreen()) / 3.0;
    }

    public double getGreen() { // getter for green component of color in pixel
        return color.getGreen();
    }

    public Color getColor(){return this.color;} // getter for Color field

}
