package util;

import java.io.ByteArrayInputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
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
   * 減色処理です
   * CV_8UC3の値域は [-127,127]
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



}
