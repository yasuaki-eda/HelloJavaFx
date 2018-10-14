package img.testga;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import img.common.ImageViewApplication;
import util.UtilImage;

/**
 * src画像のヒストグラムから生成したランダムな色による
 * ランダムな画像を生成します。
 * @author Eda
 *
 */
public class RandomImageCreate extends ImageViewApplication {

  @Override
  protected Mat createDstMat(Mat src) {

    double[] hist = new double[64];
    UtilImage.calcHistgramNorm(src, hist);
    return createRandomImageFromHist(src.cols()/4, src.rows()/4, hist);
  }

  public Mat createRandomImageFromHist(int width, int height, double[] hist) {
    Mat dst = new Mat(width, height, CvType.CV_8UC3);
    Scalar color = null;

    for ( int y = 0; y < dst.height(); y++ ) {
      for ( int x = 0; x < dst.width(); x++ ) {

        color = UtilImage.createRandomColorWithHistRate(hist);
        double colVal[]  = color.val;

        if ( 255 < colVal[0] ) colVal[0] -= 256;
        if ( 255 < colVal[1] ) colVal[1] -= 256;
        if ( 255 < colVal[2] ) colVal[2] -= 256;

        byte[] data = new byte[3];
        data[0] =  (byte) colVal[0];
        data[1] = (byte)colVal[1];
        data[2] = (byte)colVal[2];
        dst.put(y, x, data);

      }
    }
    return dst;
  }


  public static void main(String args[]) {
    launch(args);
  }

}
