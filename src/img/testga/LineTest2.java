package img.testga;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;
import util.UtilImage;

/**
 * ランダムな線を引きます。
 * 色は、srcのヒストグラムの形状に合わせます。
 * @author Eda
 *
 */
public class LineTest2 extends ImageViewApplication {

  private static int HIST_SIZE = 64;
  private static int MAX_GENERATION = 1000;
  private static final int MAX_LENGTH = 30;

  @Override
  protected Mat createDstMat(Mat src) {

    double[] hist = new double[HIST_SIZE];
    UtilImage.calcHistgramNorm(src, hist);

    for ( int i = 0; i<HIST_SIZE; i++ ) {
      System.out.println("i:" + i + " val:" + hist[i]);
    }

    Mat dst = src.clone();
    Point min = new Point(0, 0);
    Point max = new Point(dst.cols(), dst.rows());

    for ( int i = 0; i < MAX_GENERATION; i++ ) {
      Point start = UtilImage.makeRandomPoint(min, max);
      Imgproc.line(dst, start , UtilImage.makeRandomPoint(start, MAX_LENGTH),
          createRandomColorWithHistRate(hist),
          (int)(Math.random() * 5 + 1) );
    }


    return UtilImage.decreaseColorMat(dst);
  }

  /**
   * histgramの比率に応じたランダムな色を作成します。
   * @param hist double[64] BGRは4階調ずつ
   * @return
   */
  private  static Scalar createRandomColorWithHistRate(double[] hist ) {

    double rand = Math.random();
    int binNo = 0;
    double sum = 0;
    for ( int i = 0; i < hist.length; i++ ) {
      sum += hist[i];
      if ( rand < sum ) {
        binNo = i;
        break;
      }
    }

    int blueNo = (binNo % 4) ;
    int greenNo = ( (( binNo - blueNo ) / 4 )  % 4 ) ;
    int redNo =(  (( binNo - blueNo - greenNo * 4 ) / 16 ) % 4 ) ;

    blueNo = blueNo * 64 + 32 ;
    greenNo = greenNo * 64 + 32 ;
    redNo = redNo * 64 + 32;

    return new Scalar(blueNo, greenNo, redNo);
  }

  @Override
  protected void initImagePath(){
    imagePath = "./image/lena.jpg";
  }




  public static void main(String args[]) {
    launch(args);
  }

}
