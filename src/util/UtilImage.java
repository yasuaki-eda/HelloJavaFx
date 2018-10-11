package util;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.Image;

/**
 * 画像処理Utilクラス
 * @author Eda
 *
 */
public class UtilImage {

  private UtilImage() {
  }

  /**
   * Mat ⇒ JavaFXのImageを生成
   * @return
   */
  public static Image createFxImage(Mat srcMat, int fxImageWidth, int fxImageHeight){
    MatOfByte byteMat = new MatOfByte();
    Imgcodecs.imencode(".bmp", srcMat, byteMat);
    Image img = new Image(new ByteArrayInputStream( byteMat.toArray() ), fxImageWidth, fxImageHeight, false, false);
    return img;
  }


  /**
   * 減色画像を作成します。
   * CV_8UC3の値域は [-127,127]
   * 参考 : http://aidiary.hatenablog.com/entry/20091003/1254574041
   * @param src
   * @return
   */
  public static Mat decreaseColorMat(Mat src) {

    Mat dst = new Mat(src.width(), src.height(), CvType.CV_8UC3);
    for ( int y = 0; y < dst.height(); y++ ) {
      for ( int x = 0; x < dst.width(); x++ ) {
        byte[] data = new byte[3];
        src.get(y,  x, data);
        data[0] = (byte)decreaseColor(data[0]);
        data[1] = (byte)decreaseColor(data[1]);
        data[2] = (byte)decreaseColor(data[2]);
        dst.put(y, x, data);

      }
    }
    return dst;
  }

  /**
   * 画像を回転拡縮します。回転の中心は画像の中心です。
   * @param src   : 入力画像
   * @param angle : 回転角(degree)
   * @param size  : 拡大率
   * @return
   */
  public static Mat rotateMatFromCenter(Mat src, double angle, double size) {
    Mat rotMat = new Mat(2, 3, CvType.CV_32FC1 );
    Mat dst = new Mat(src.rows(), src.cols(), src.type());
    Point center = new Point(dst.cols()/2, dst.rows()/2);
    if ( size < 0 ) size = 0;
    rotMat = Imgproc.getRotationMatrix2D(center, angle, size);
    Imgproc.warpAffine(src, dst, rotMat, dst.size());
    return dst;
  }

  /**
   * ランダムな点を出力します。
   * @param min
   * @param max
   * @return
   */
  public static Point makeRandomPoint(Point min, Point max) {
    int x = (int) (Math.random() * (max.x - min.x) + min.x);
    int y = (int) (Math.random() * (max.y - min.y) + min.y);
    return new Point(x, y);
  }

  /**
   * 中心点から指定した長さを越えないランダムな点を返します。
   */
  public static Point makeRandomPoint(Point center, double maxLength) {
    double angle = Math.random() * 2 * Math.PI;
    double length = Math.random() * maxLength;
    int x = (int)( center.x + length * Math.cos(angle) );
    int y = (int)( center.y + length * Math.sin(angle) );
    return new Point(x, y);
  }

  /**
   * ランダムな色を生成します。
   * Scalarの地域[0, 255)
   * @return
   */
  public static Scalar makeRandomScalar() {
    int blue = (int)(Math.random() * 256);
    int green = (int)(Math.random() * 256) ;
    int red = (int)(Math.random() * 256);
    return new Scalar(blue, green, red);
  }



  /**
   * 減色処理です
   * CV_8UC3の値域は [-127,127]だがScalorとの関係は、0⇒0、128⇒-127、255⇒-1になる。
   * @param val
   * @return
   */
  public static int decreaseColor(int val) {
    if ( val < -64 ) {
       return -96;
    } else if ( val < 0 ) {
       return -32;
    } else if ( val < 64 ) {
       return 32;
    } else {
      return 96;
    }
  }

  /**
   * ヒストグラムのbin番号を計算します。
   * 1色あたり4階調 ^ 3色 = 64のbin番号に振り分けます。
   * CV_8UC3の値域は [-127,127]だがScalorとの関係は、0⇒0、128⇒-127、255⇒-1になる。
   * @param blue : 値域 [-127, 127]
   * @param green : 値域 [-127, 127]
   * @param red : 値域 [-127, 127]
   * @return
   */
  public static int rgb2bin(int blue, int green, int red) {
    if (blue < 0) blue += 255;
    if (green < 0) green += 255;
    if (red < 0) red += 255;

    int blueNo = blue / COLOR_GRADATION_LEVEL;
    int greenNo = green / COLOR_GRADATION_LEVEL;
    int redNo = red  / COLOR_GRADATION_LEVEL;

    return blueNo + greenNo * 4 + redNo * 16;
  }

  /* 色階調 */
  public static int COLOR_GRADATION_LEVEL = 64;

  /**
   * 入力画像から 4*4*4 = 64階調のヒストグラムを作成します。
   * @param src  : 入力画像
   * @param hist : 出力結果int[64]
   */
  public static void calcHistgram(Mat src, int[] hist) {
    if (hist.length < COLOR_GRADATION_LEVEL ) {
      return;
    }

    for ( int i = 0; i < COLOR_GRADATION_LEVEL; i++ ) {
      hist[i] = 0;
    }

    byte[] data = new byte[3];
    for ( int y = 0; y < src.rows(); y++) {
      for ( int x = 0; x < src.cols(); x++ ) {
        src.get(y,  x, data);
        hist[rgb2bin(data[0], data[1], data[2])]++;
      }
    }
  }


  /**
   * 入力画像から 4*4*4 = 64階調のヒストグラムを作成します。
   * ヒストグラムは正規化します。
   * @param src  : 入力画像
   * @param hist : 出力結果double[64]
   */
  public static void calcHistgramNorm(Mat src, double[] hist) {
    if (hist.length < COLOR_GRADATION_LEVEL ) {
      return;
    }

    for ( int i = 0; i < COLOR_GRADATION_LEVEL; i++ ) {
      hist[i] = 0;
    }

    byte[] data = new byte[3];
    int count = 0;
    for ( int y = 0; y < src.rows(); y++) {
      for ( int x = 0; x < src.cols(); x++ ) {
        src.get(y,  x, data);
        hist[rgb2bin(data[0], data[1], data[2])] += 1;
        count++;
      }
    }

    for ( int i=0 ; i < COLOR_GRADATION_LEVEL; i++ ) {
      hist[i] = hist[i] / count;
    }
  }


  /**
   * 2つのヒストグラムの類似度をHistgramIntersectionにより計算します。
   * 正規化していて値域は[0, 1.0]です。1.0の時、2つのヒストグラムは一致します。
   * @param hist1
   * @param hist2
   * @return
   */
  public static double calcHistgramIntersection(int[] hist1, int[] hist2) {
    if ( hist1.length < COLOR_GRADATION_LEVEL || hist2.length < COLOR_GRADATION_LEVEL ) return -1;

    int ret = 0;
    double sum = 0;
    for ( int i = 0; i < COLOR_GRADATION_LEVEL; i++ ) {
      ret += Math.min( hist1[i], hist2[i] );
      sum += hist1[i];
    }
   return ret / sum ;
  }


  /**
   * 交叉画像を生成します。
   * @param src1 : 入力1
   * @param src2 : 入力2
   * @param dst1 : 交叉結果画像1
   * @param dst2 : 交叉結果画像2
   */
  public static void createRecombinationMat(Mat src1, Mat src2, Mat dst1, Mat dst2) {
    // ランダムな矩形を生成
    Point start = UtilImage.makeRandomPoint(new Point(0, 0), new Point(src2.rows(), src2.cols()));
    int width = (int) (Math.random() * ( src2.rows() - start.x ) );
    int height = (int) (Math.random() * ( src2.cols() - start.y ) );

    // maskを作成
    Mat mask = new Mat(src1.rows(), src1.cols(), CvType.CV_8UC1, new Scalar(0));
    Imgproc.rectangle(mask, start, new Point(start.x + width, start.y + height), new Scalar(255), -1);
    Mat maskNot = new Mat(src1.rows(), src1.cols(), CvType.CV_8UC1);
    Core.bitwise_not(mask, maskNot);

    Mat dst11 = new Mat();
    src1.copyTo(dst1, mask);
    src1.copyTo(dst11, maskNot);

    Mat dst21 = new Mat();
    src2.copyTo(dst2, mask);
    src2.copyTo(dst21, maskNot);

    Core.add(dst1, dst21, dst1);
    Core.add(dst2, dst11, dst2);

  }

  /**
   * histgramの比率に応じたランダムな色を作成します。
   * @param hist double[64] BGRは4階調ずつ
   * @return
   */
  public static Scalar createRandomColorWithHistRate(double[] hist ) {

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

}
