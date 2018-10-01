package img.affine;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;

/**
 * 回転 & 拡縮を実行します。
 */
public class Rotation extends ImageViewApplication {

  @Override
  protected Mat createDstMat(Mat src) {
    Mat rotMat = new Mat(2, 3, CvType.CV_32FC1 );
    Mat dst = new Mat(src.rows(), src.cols(), src.type());
    Point center = new Point(dst.cols()/2, dst.rows()/2);
    rotMat = Imgproc.getRotationMatrix2D(center, 30, 0.8);
    Imgproc.warpAffine(src, dst, rotMat, dst.size());
    return dst;
  }


  public static void main(String args[]) {
    launch(args);
  }

}
