package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.imageio.ImageIO;

/*APPLICATION SERVICE LAYER*/
public class ImageEditor {

    /**
     * Command interface with two abstract methods, execute and undo, which all actions of image editor implement
     */
    public interface Command {
        void execute() throws IOException;
        void undo() throws IOException;
    }

    private Image image;
    private List<Pixel> highlightedSeam = null;
    private final Deque<Command> commandHistory = new ArrayDeque<>(); // undo stack of commands

    /**
     * loads an image from a file path
     * @param filePath filepath of image
     * @throws IOException throws if file cannot be read or accessed
     */
    public void load(String filePath) throws IOException {
        File originalFile = new File(filePath);
        BufferedImage img = ImageIO.read(originalFile);
        image = new Image(img);
        save("target/currentImg.png");
    }

    /**
     * save an image to a filepath
     * @param filePath filepath where image should be saved
     * @throws IOException throws if image cannot be saved at the given filepath
     */
    public void save(String filePath) throws IOException {
        BufferedImage img = image.toBufferedImage();
        ImageIO.write(img, "png", new File(filePath));
    }

    /**
     * checks if the width of te image is greater than 1 meaning that it can be modified
     * @return boolean (true if width > 1, false otherwise)
     */
    public boolean checkImageWidth(){
        return image.getWidth() > 1;
    }

    /**
     * highlights the greenest seam of the image
     * @throws IOException throws if image cannot be modified or accessed
     */
    public void highlightGreenest() throws IOException {
        if(!checkImageWidth()){
            System.out.println("[ERROR] Image Width not long enough for more operations");
            return;
        }
        Command command = new HGCommand(this); // new instance of HGCommand is created
        command.execute(); // HGCommand execute which highlights greenest seam
        commandHistory.push(command); // action is pushed onto undo stack
        save("target/currentImg.png"); // saved for live image updates
        System.out.println("[INFO] Highlighted Greenest Seam"); // terminal info message
    }

    /**
     * highlights seam with lowest energy
     * @throws IOException throws if image cannot be modified or accessed
     */
    public void highlightLowestEnergySeam() throws IOException {
        if(!checkImageWidth()){
            System.out.println("[ERROR] Image Width not long enough for more operations");
            return;
        }
        Command command = new HLECommand(this); // new instance of HLECommand
        command.execute(); // highlights lowest energy seam
        commandHistory.push(command); // adds command to undo stack
        save("target/currentImg.png"); // saves image for live updates
        System.out.println("[INFO] Highlighted Lowest Energy Seam"); // terminal info message
    }

    /**
     * removes current highlighted seam
     * @throws IOException throws if image cannot be modified or accessed
     */
    public void removeHighlighted() throws IOException {
        // checks if image is too small to be modified
        if(!checkImageWidth()){
            System.out.println("[ERROR] Image Width not long enough for more operations");
            return;
        }

        // checks if there exists a highlighted seam to remove
        if (highlightedSeam == null) {
            return;
        }

        Command command = new RHCommand(this); // new instance of RHCommand
        command.execute(); // RHCommand execute which removes current highlighted column
        commandHistory.pop(); // removes highlighted command which was on undo stack previously (as the highlight cannot be undone as the highlighted seam will be removed)
        commandHistory.push(command); // adds remove highlighted action to undo stack
        save("target/currentImg.png"); // save image for live updates
        System.out.println("[INFO] Removed highlighted seam "); // terminal info message
    }

    /**
     * undoes the last command through the command undo stack "commandHistory"
     * @throws IOException throws if image cannot be modified or accessed
     */
    public void undo() throws IOException {
        if (!commandHistory.isEmpty()) { // if stack is not empty
            Command command = commandHistory.pop(); // removes latest command rom undo stack
            command.undo(); // calls undo implementation of command
            save("target/currentImg.png"); // saves image for live updates
            System.out.println("[INFO] You have " + commandHistory.size() + " undo operations left."); // terminal info message
        } else {
            System.out.println("[INFO] You have no more operations to undo"); // undo stack is empty
        }
    }

    /**
     * Highlight greenest command class (implements Command interface)
     * Contains logic on how to execute (highlight greenest seam)
     *  and undoing an operation (undo)
     */
    public class HGCommand implements Command{
        private final ImageEditor editor; // internal version of editor
        private List<Pixel> previousHighlighted;
        private List<Color> originalColors;

        public HGCommand(ImageEditor editor){ // constructor assinging value to editor
            this.editor = editor;
        }

        /**
         * highlights the greenst seam in image
         * @throws IOException throws if image cannot be modified or accessed
         */
        @Override
        public void execute() throws IOException {
            previousHighlighted = editor.highlightedSeam;
            List<Pixel> greenestSeam = editor.image.getGreenestSeam(); // gets greenest seam from image
            originalColors = new ArrayList<>(greenestSeam.size()); // list of original seam colors before highlight

            for (Pixel pixel : greenestSeam){
                originalColors.add(pixel.getColor()); // gets original color from seam to be highlighted
            }

            // highlights greenest seam in green and returns old color values to highlighted seam
            editor.highlightedSeam = editor.image.higlightSeam(greenestSeam, Color.GREEN);
        }

        /**
         * Undoes highlighting of greenest seam by adding back original seam before the highlight
         * @throws IOException throws if image cannot be modified or accessed
         */
        @Override
        public void undo() throws IOException {
            if(editor.highlightedSeam != null && originalColors != null){
                List<Pixel> currentHighlighted = editor.highlightedSeam;
                
                editor.image.addSeam(currentHighlighted); // adds back old seam pre-highlight
            }

            editor.highlightedSeam = previousHighlighted;
        }
    }

    /**
     * Remove highlighted command class (implements Command interface)
     * Contains logic for execute (removing highlighted seam) and undoing execute
     */
    public class RHCommand implements Command {
        private final ImageEditor editor;
        private List<Pixel> removedSeam;

        public RHCommand(ImageEditor editor) {
            this.editor = editor;
        }

        /**
         * removes current highlighted seam
         */
        @Override
        public void execute(){
            if(editor.highlightedSeam != null){
                removedSeam = new ArrayList<>(editor.highlightedSeam); // sets removedSeam equal to current highlighted seam
                editor.image.removeSeam(editor.highlightedSeam); // removes highlighted seam
                editor.highlightedSeam = null;
            }
        }

        /**
         * undoes removal of highlighted seam by adding back removedSeam
         */
        @Override
        public void undo(){
            if (removedSeam != null) {
                editor.image.addSeam(removedSeam);
            }
        }
    }

    /**
     * Highlight lowest energy command class (implements Command interface)
     * Contains logic on execute (highlighting lowest overall energy seam) and undoing highlight
     */
    public class HLECommand implements Command{
        private final ImageEditor editor;
        private List<Pixel> previousHighlightedSeam;
        private List<Color> originalColors;

        public HLECommand(ImageEditor editor) {
            this.editor = editor;
        }

        /**
         * highlights lowest energy seam
         * @throws IOException throws if image cannot be modified or accessed
         */
        @Override
        public void execute() throws IOException {
            previousHighlightedSeam = editor.highlightedSeam;

            List<Pixel> lowestEnergySeam = editor.image.getLowestEnergySeam(); // gets lowest energy seam from image
            originalColors = new ArrayList<>(lowestEnergySeam.size()); // list of original color of un-highlighted pixels

            for (Pixel pixel : lowestEnergySeam) {
                originalColors.add(pixel.getColor());
            }

            editor.highlightedSeam = editor.image.higlightSeam(lowestEnergySeam, Color.RED); // highlights lowest energy seam red
        }

        /**
         * undoes highlighting of lowest energy seam
         * @throws IOException throws if image cannot be modified or accessed
         */
        @Override
        public void undo() throws IOException{
            if (editor.highlightedSeam != null && originalColors != null) {
                List<Pixel> currentHighlighted = editor.highlightedSeam;
                editor.image.addSeam(currentHighlighted); // adds back pixels of the currently highlighted seam
            }

            editor.highlightedSeam = previousHighlightedSeam;
        }
    }

}
