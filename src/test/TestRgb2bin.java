package test;

import util.UtilImage;

public class TestRgb2bin {

  public static void main (String args[]) {

    int red = -127;
    int blue = -127;
    int green = -127;
    int bin = 0;

    for (int i = 0; i< 10; i++ ) {
      green = -127;
      for ( int j =0; j< 10; j++ ) {
        red = -127;
        for ( int k = 0; k < 10; k++ ) {
          bin = UtilImage.rgb2bin(blue, green, red);
          red = red + 25;
        }
        green = green +  25;
      }
      blue = blue + 25;
    }


  }




}
