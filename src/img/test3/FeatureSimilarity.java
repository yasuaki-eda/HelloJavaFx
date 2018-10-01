package img.test3;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import img.common.ImageViewApplication;
import util.UtilImage;

/**
 * 2つの画像の類似度を計算します
 * 特徴点同士をマッチングさせ、距離の平均値を取ります。
 * @author Eda
 *
 */
public class FeatureSimilarity extends ImageViewApplication {


  @Override
  protected Mat createDstMat(Mat src) {
    Mat dst = src.clone();
    Mat descriptor1 = calcDescriptors(src);

    // 2枚目の画像
    Mat src2 = Imgcodecs.imread("./image/lena2.jpg");
    Mat src3 = UtilImage.rotateMatFromCenter(UtilImage.decreaseColorMat(src), 30, 1)  ;
    Mat descriptor2 = calcDescriptors(src3);

    MatOfDMatch matches = new MatOfDMatch();
    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
    matcher.match(descriptor1, descriptor2, matches);
    List<Double> distanceList = new ArrayList<>();
    double sum = 0;
    for (DMatch dMatch : matches.toList()) {
        distanceList.add(Double.valueOf(dMatch.distance));
        sum += Double.valueOf(dMatch.distance);
    }
    System.out.println("Distance:" +  sum);

   return dst;
  }

  /**
   * 特徴量Matを作成します。
   * @param src
   * @return
   */
  public Mat calcDescriptors(Mat src) {
    Mat descriptors = new Mat(0, 0, 0);  // nullでなければ何でもよい(自動的にrows, cols, typeがセットされる)

    // GRAY画像に変換
    Mat gray = new Mat(src.rows(), src.cols(), src.type());
    Imgproc.cvtColor( src, gray, Imgproc.COLOR_BGR2GRAY);

    // 特徴量の抽出
    FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
    DescriptorExtractor executor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
    MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
    detector.detect(gray, keyPoint01);
    executor.compute(gray, keyPoint01, descriptors);

    return descriptors;
  }



  public static void main(String args[]) {
    launch(args);
  }


}
