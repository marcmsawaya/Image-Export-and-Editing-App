package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Image {

    private final List<Pixel> rows; // data structure for image (represented by a rows of linked list of pixels

    private int width;
    private int height;

    /**
     * initializes rows field by looping through pixels of inputted buffered image
     * @param img buffered image
     */
    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }

    }

    /**
     * turns internal image represented by the rows field into a buffered image type
     * @return returns buffered image of internal image
     */
    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB());
                pixel = pixel.right;
            }
        }
        return image;
    }

    /**
     * width getter
     * @return returns this.width
     */
    public int getWidth() {
        return width;
    }

    /**
     * returns horizontal energy of a current pixel given the pixel above and below it
     * @param above pixel above the current pixel in image
     * @param current pixel whos horizontal energy value will be calculated
     * @param below pixel below the current pixel in image
     * @return horizontal energy of current pixel
     */
    double getHorizontalEnergy(Pixel above, Pixel current, Pixel below){
        return (above.left.brightness() + 2 * current.left.brightness() + below.left.brightness()) -
                (above.right.brightness() + 2 * current.right.brightness() + below.right.brightness());
    }

    /**
     * returns vertical energy of a current pixel given the pixel above and below it
     * @param above pixel above the current pixel in image
     * @param current pixel whos vertical energy value will be calculated
     * @param below pixel below the current pixel in image
     * @return vertical energy of current pixel
     */
    double getVerticalEnergy(Pixel above, Pixel current, Pixel below){
        return (above.left.brightness() + 2 * above.brightness() + above.right.brightness()) -
                (below.left.brightness() + 2 * below.brightness() + below.right.brightness());
    }

    /**
     * returns total energy of the current pixel
     * @param above pixel above the current pixel in image
     * @param current pixel whos total energy will be calculated
     * @param below pixel below the current pixel in image
     * @return total energy of current pixel
     */
    double energy(Pixel above, Pixel current, Pixel below) {

        double horizontalEnergy = getHorizontalEnergy(above, current, below);
        double verticalEnergy = getVerticalEnergy(above, current, below);

        return Math.sqrt(horizontalEnergy * horizontalEnergy + verticalEnergy * verticalEnergy);
    }

    /**
     * loops through all pixels in image by going row by row
     * for each pixel, call the energy method on it and set its energy property equal to the result
     */
    public void calculateEnergy() {
        Pixel currentPixel;
        Pixel abovePixel;
        Pixel belowPixel;

        for(int y = 0; y < height; y ++) {
            // edge case testing abovePixel and belowPixel values

            if (y == 0) {
                abovePixel = null;
            } else {
                abovePixel = rows.get(y - 1);
            }

            if (y == height - 1) {
                belowPixel = null;
            } else {
                belowPixel = rows.get(y + 1);
            }

            currentPixel = rows.get(y);

            for (int x = 0; x < width; x++) {

                if (currentPixel.left == null || currentPixel.right == null || y == 0 || y == height - 1) {
                    currentPixel.energy = currentPixel.brightness();
                } else {
                    currentPixel.energy = energy(abovePixel, currentPixel, belowPixel);
                }

                if (y == 0) {
                    currentPixel = currentPixel.right;
                    belowPixel = belowPixel.right;

                } else if (y == height - 1) {
                    currentPixel = currentPixel.right;
                    abovePixel = abovePixel.right;
                } else {
                    currentPixel = currentPixel.right;
                    abovePixel = abovePixel.right;
                    belowPixel = belowPixel.right;

                }
            }

        }
    }

    /**
     * given a list of pixels and a color, highlights the given pixels on the image the supplied color
     * @param seam list of pixels that will be highlighted
     * @param color color of highlighted seam
     * @return returns original, un-highlighted seam
     */
    public List<Pixel> higlightSeam(List<Pixel> seam, Color color) {

        int iterationNum = 0;

        // for each pixel in seam, create a new pixel with the specified color and modify pointers correctly
        for (Pixel pixel : seam) {

            Pixel newPixel = new Pixel(color);

            // new pixel points to same pixels as current pixel
            newPixel.left = pixel.left;
            newPixel.right = pixel.right;

            // if not first pixel in row
            if (pixel.left != null) {
                pixel.left.right = newPixel; // removes current pixel from image by modifying its neighbor pixel pointer to new pixel
            } else {
                rows.set(iterationNum, newPixel);
            }

            if (pixel.right != null) {
                pixel.right.left = newPixel; // removes current pixel from image by modifying its neighbor pixel pointer to new pixel
            }

            iterationNum ++;
        }

        return seam; // returns original seam
    }

    /**
     * removes a list of supplied pixels from image
     * @param seam list of pixels that will be removed from image
     */
    public void removeSeam(List<Pixel> seam) {

        // loops throguh seam and deletes each pixel
        for (int row = 0; row < height; row++) {
            Pixel seamPixel = seam.get(row);

            if (seamPixel.left == null) { // if current pixel from seam is the first in the row
                rows.set(row, seamPixel.right);
            } else {
                seamPixel.left.right = seamPixel.right; // modify neighbor pixel pointers to delete current pixel from image
            }

            if (seamPixel.right != null) { // if not last pixel in row
                seamPixel.right.left = seamPixel.left;// modify neighbor pixel pointers to delete current pixel from image
            }
        }
        width--;
    }

    /**
     * adds a list of supplied pixels to image
     * @param seam list of pixels that will be added to image
     */
    public void addSeam(List<Pixel> seam) {
        int iterationNum = 0;

        // loops through each pixel in seam, adding it back with the correct pointers
        for (Pixel pixel : seam) {
            if (pixel.left == null) {
                rows.set(iterationNum, pixel);
            } else {
                pixel.left.right = pixel;
            }

            if (pixel.right != null) {
                pixel.right.left = pixel;
            }
            
            iterationNum++;
        }

        width++;
    }

    /**
     * finds seam with maximum cumulative value defined by the valueGetter lambda function
     * @param valueGetter lambda function which takes in a pixel and returns a double value
     *                    (such as energy or a color component)
     * @return seam with maximum cumulative value
     */
    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        HashMap<Pixel, Pixel> p2p = new HashMap<>(); // map given pixel to a pixel in which it got its max value from
        HashMap<Pixel, Double> p2s = new HashMap<>(); // maps pixel to its maximum cumulative value

        // intializes first row of values and adds them to p2s hashmap
        Pixel current = rows.getFirst();
        while (current != null) {
            p2s.put(current, valueGetter.apply(current));
            current = current.right;
        }

        for(int y = 1; y < height; y++){
            Pixel above = rows.get(y-1);
            current = rows.get(y);

            while(current != null){ // loops through all pixels in current row
                double c_value = valueGetter.apply(current); // value of current pixel

                // sets values of above.left, above and above.right pixels to negative infinity as default
                double al_value = Double.NEGATIVE_INFINITY;
                double a_value = Double.NEGATIVE_INFINITY;
                double ar_value = Double.NEGATIVE_INFINITY;

                if (above.left != null) {
                    al_value = p2s.get(above.left) + c_value; // sum of above.left and current pixel
                }

                if (above != null) {
                    a_value = p2s.get(above) + c_value; // sum of above and current pixel
                }

                if (above.right != null) {
                    ar_value = p2s.get(above.right) + c_value; // sum of above.right and current pixel
                }

                // compares values of above pixels to get maximum cumulative value
                if (al_value > a_value) {
                    if (al_value > ar_value) {
                        /*
                        if max value is from summing above.left and current value,
                         add pointer to above.left for current pixel to backtrack later for max seam
                         */
                        p2p.put(current, above.left);
                        p2s.put(current, al_value); // maps pixel to its max value
                    } else {
                        /*
                        if max value is from summing above.right and current value,
                         add pointer to above.left for current pixel to backtrack later for max seam
                         */
                        p2p.put(current, above.right);
                        p2s.put(current, ar_value); // maps pixel to its max value
                    }
                } else {
                    if (a_value > ar_value) {
                        /*
                        if max value is from summing above and current value,
                         add pointer to above.left for current pixel to backtrack later for max seam
                         */
                        p2p.put(current, above);
                        p2s.put(current, a_value); // maps pixel to its max value
                    } else {
                        /*
                        if max value is from summing above.right and current value,
                         add pointer to above.left for current pixel to backtrack later for max seam
                         */
                        p2p.put(current, above.right);
                        p2s.put(current, ar_value); // maps pixel to its max value
                    }
                }

                above = above.right;
                current = current.right;
            }
        }

        // gets maximum value from last row

        Pixel bottomRow = rows.get(height - 1);
        current = bottomRow;
        Pixel maxPixel = current;
        double maxValue = p2s.get(current);

        while (current != null) {
            if (p2s.get(current) > maxValue) {
                maxValue = p2s.get(current);
                maxPixel = current;
            }
            current = current.right;
        }

        List<Pixel> path = new ArrayList<>();
        current = maxPixel;

        /*
        goes from bottom to top of image through pixel to pixel hashmap
         (which contains pointers from where the current pixel got its maximum value),
         creating a maximum seam

         */
        while (current != null) {
            path.add(current);
            current = p2p.get(current);
        }

        // since seam was created from bottom up, reverse it and then return it
        Collections.reverse(path);

        return path;
    }


    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
    }

    public List<Pixel> getLowestEnergySeam() {
        calculateEnergy();
        return getSeamMaximizing(pixel -> -pixel.energy);


    }
}