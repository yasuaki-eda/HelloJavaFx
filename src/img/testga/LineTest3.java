package img.testga;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;
import util.UtilImage;

/**
 * 線分を連続して繋げて生成することで、複雑な線を作成する。
 * @author Eda
 *
 */
public class LineTest3 extends ImageViewApplication {
  private static int HIST_SIZE = 64;
  private static int MAX_LENGTH = 20;
  private static int MAX_GENERATION = 100;
  private static int MAX_LINE_TICKNESS = 3;

  @Override
  protected Mat createDstMat(Mat src) {

    double[] hist = new double[HIST_SIZE];
    UtilImage.calcHistgramNorm(src, hist);

    // 黒色画像を生成
    Mat dst = new Mat(src.rows(), src.cols(), CvType.CV_8UC3, new Scalar(0));
    Point min = new Point(0, 0);
    Point max = new Point(dst.cols(), dst.rows());

    for ( int i = 0; i < MAX_GENERATION; i++ ) {
      Point start = UtilImage.makeRandomPoint(min, max);
      Point end = UtilImage.makeRandomPoint(start, MAX_LENGTH);
      Scalar color = UtilImage.createRandomColorWithHistRate(hist);

      Imgproc.line(dst, start , end, color,
          (int)(Math.random() * MAX_LINE_TICKNESS + 1) );

      Point oldStart = start;
      Point nextStart = end;
      Point nextEnd;

      // 1/2の確率で次の線分を引く
      while ( Math.random() < 0.5 ) {
        nextEnd = makeRandomPoint(oldStart, nextStart, MAX_LENGTH);
        Imgproc.line(dst, nextStart, nextEnd, color,
            (int)(Math.random() * MAX_LINE_TICKNESS + 1) );
        oldStart = nextStart;
        nextStart = nextEnd;
      }

    }
    return UtilImage.decreaseColorMat(dst);
  }

  /**
   * 線分の終点を起点としたランダムな点を出力します。
   * ただし、線分の向いている方向(-π/2<=x<=π/2)に限定します。
   * @param start
   * @param end
   * @param maxLength
   * @return
   */
  public Point makeRandomPoint(Point start, Point end, double maxLength) {
    double angle = Math.random() * Math.PI - Math.PI /2;  // -π/2 <= angle <= π/2
    double length = Math.random() * maxLength;

    // start-end線分のなす角
    double x1 = end.x - start.x;
    double y1 = end.y - start.y;

    if ( x1 == 0 ) {
      // 何もしない
    } else if ( y1 == 0 ) {
      angle += Math.PI / 2;
    } else {
      angle += Math.atan2(y1, x1);
    }

    int x = (int)( end.x + length * Math.cos(angle) );
    int y = (int)( end.y + length * Math.sin(angle) );
    return new Point(x, y);

  }


  public static void main(String args[]) {
    launch(args);
  }

}
