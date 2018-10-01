package img.test3;

import java.util.Collections;
import java.util.List;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;

public class DetectFeature extends ImageViewApplication {

  @Override
  protected Mat createDstMat(Mat src) {
    // GRAY画像に変換
    Mat gray = new Mat(src.rows(), src.cols(), src.type());
    Imgproc.cvtColor( src, gray, Imgproc.COLOR_BGR2GRAY);
    Mat dst = src.clone();

    // 特徴量の抽出
    FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
    DescriptorExtractor executor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
    MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
    detector.detect(gray, keyPoint01);

    // 特徴点の描画
    List<KeyPoint> keyList = keyPoint01.toList();
    Collections.sort( keyList,((o1, o2) -> (int)( o2.response - o1.response)));  // responseの降順にソート
    for ( KeyPoint key : keyList ) {
      Imgproc.circle(dst, key.pt, 3, new Scalar(128, 100, 0, 50));
    }

    Mat descriptors = new Mat(0, 0, 0);  // nullでなければ何でもよい(自動的にrows, cols, typeがセットされる)
    executor.compute(gray, keyPoint01, descriptors);

    System.out.println("decsriptors rows:" + descriptors.rows() + " cols:" + descriptors.cols() +
        " type:" + descriptors.type() +
        " keyPoints size:" + keyList.size());

   return dst;
  }

  public static void main(String args[]) {
    launch(args);
  }

}
