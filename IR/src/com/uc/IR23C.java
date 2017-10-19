package com.uc;// IR23C.java CS6054 Cheng 2015
// view 24x24 grayleval images (input as hex strings)
// Usage: java IR23C faces800.txt

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.JFrame;

public class IR23C extends JFrame{
    static final int width = 24;
    static final int height = 24; 
    static final int cols = 40;
    static final int rows = 20;
    static final int border = 4;
    static final int frameSize = width * cols * height * rows;
    static final int numberOfPics = 800;
    int[] pix = new int[frameSize];
   
  public IR23C(){ 
       setSize(width * cols + 10, height * rows + 50);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setLocationRelativeTo(null);
       setTitle("IR23C");
       setVisible(true); 
  }


  public void showPics(String filename){
     Scanner in = null;
       try{
         in = new Scanner(new File(filename));
       } catch (FileNotFoundException e){
         System.err.println(filename + " not found");
         System.exit(1);
       }
      Graphics g = getGraphics();
    for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++){
      String line = in.nextLine();
      for (int i = 0; i < height; i++) for (int j = 0; j < width; j++){
        int pos = j * height + i;
        int level = Integer.parseInt(line.substring(pos, pos + 1), 16);
        level <<= 4;
        int rgb = 0xff000000 | (level << 16) | (level << 8) | level;
           pix[(r * height + i) * (width * cols) + c * width + j] = rgb; 
      }
    }
      Image im = createImage(new MemoryImageSource(width * cols, 
         height * rows, pix, 0, width * cols));
      g.drawImage(im, 0, 30, null);
   }

   public static void main(String[] args){
      IR23C ir23 = new IR23C(); 
      ir23.showPics("output2.txt");
   }
}