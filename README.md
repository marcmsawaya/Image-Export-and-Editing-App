# Image Editor 2 (AE3)

## Description

CLI image editor which allows users to delete seams of a given property from a provided image 

Provides features such as the highlighting of seams to visualize changes before they are confirmed  


## Installing and Execution 

* Download and open ZIP File
* Open up terminal and navigate to directory containg Main.java file

```
/src/main/java/uk/ac/nulondon
```

### Executing program

* Compile and run main file 
```
java Main.java
```


NB : I must point out before the explanation of the program that the first run must be a test run where the user presses either 'g' or 'e' and confirms that action. This will generate the target folder and images inside it needed for the program. Now let's dive into How To Use it :
## How to use 

* The program will give a prompt to provide a file path to the image 
  * Example file path from image provided in resources folder : 
  * ```
    src/main/resources/beach.png
    ```
* The program will then provide the following options: 
  * Remove greenest seam (g): 
  * Remove lowest energy seam (e): 
  * Undo (u): To undo a deletion, simply type u 
  * Quit (q): The quit the program and save changes, simply type q 

### Highlighting 
* This program provides a highlighting feature which visualizes changes to the internal image before edits are saved
* Edits and highlights to the internal image are saved in the file path below: 
```
Image_Editor/Target/currentImg.png 
```
* Greenest seam will be highlighted in green, lowest energy seam will be highlighted in red
* 
* After confirming a deletion of a column, this deletion will be shown in the same image file after refreshing 
* Any change (deletion, undo or highlight) can be visualized in this currentImg.png file before saving
* Final image is saved under Target/finalImg.png
