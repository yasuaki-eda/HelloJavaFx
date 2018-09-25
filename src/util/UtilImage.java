package util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * 画像処理Utilクラス
 * @author Eda
 *
 */
public class UtilImage {

  private UtilImage() {
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
