package img.test3;

import java.util.Collections;
import java.util.List;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.AgastFeatureDetector;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;

public class KeyPointTest extends ImageViewApplication {

  @Override
  protected Mat createDstMat(Mat src) {
    // GRAY画像に変換
    Mat gray = new Mat(src.rows(), src.cols(), src.type());
    Imgproc.cvtColor( src, gray, Imgproc.COLOR_BGR2GRAY);
    Mat dst = src.clone();

    // 特徴量の抽出
    AgastFeatureDetector detector = AgastFeatureDetector.create();
    MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
    detector.detect(gray, keyPoint01);

    // 特徴点の描画
    List<KeyPoint> keyList = keyPoint01.toList();
    Collections.sort( keyList,((o1, o2) -> (int)( o2.response - o1.response)));  // responseの降順にソート
    for ( KeyPoint key : keyList ) {
      if ( key.response > 50 ) {
        Imgproc.circle(dst, key.pt, 3, new Scalar(128, 100, 0, 50));
      }
      System.out.println("key" + key.response);
    }

   return dst;
  }

  public static void main(String args[]) {
    launch(args);
  }


}
