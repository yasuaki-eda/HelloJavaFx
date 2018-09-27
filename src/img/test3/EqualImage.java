package img.test3;

import org.opencv.core.Mat;

import img.common.ImageViewApplication;

public class EqualImage extends ImageViewApplication {

  @Override
  protected Mat createDstMat(Mat src) {
    Mat dst = src.clone();
    return dst;
  }


  public static void main(String args[]) {
    launch(args);
  }

}
