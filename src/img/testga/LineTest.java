package img.testga;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;
import util.UtilImage;

/**
 * ランダムな線を描画します。
 * 残課題 : 目標とする画像のヒストグラムと比較することで色を制限したい。
 * @author Eda
 *
 */
public class LineTest extends  ImageViewApplication {

  private static final int MAX_LENGTH = 30;
  private static final int MAX_GENERATION = 5000;

  @Override
  protected Mat createDstMat(Mat src) {

    Mat dst = src.clone();
    Point min = new Point(0, 0);
    Point max = new Point(dst.cols(), dst.rows());

    for ( int i = 0; i < MAX_GENERATION; i++ ) {
      Point start = UtilImage.makeRandomPoint(min, max);
      Imgproc.line(dst, start , UtilImage.makeRandomPoint(start, MAX_LENGTH),  UtilImage.makeRandomScalar(),
          (int)(Math.random() * 5 + 1) );

    }

    return dst;
  }

  public static void main(String args[]) {
    launch(args);
  }


}
