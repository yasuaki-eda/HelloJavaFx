package ga.common;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

/**
 * GA計算用に特徴量や類似度を保持するように拡張したMatです。
 * @author eda
 *
 */
public class GaMat implements Comparable<GaMat> {

  private Mat img;
  private Mat descriptors = new Mat(0, 0, 0);        // 特徴量
  private List<DMatch> matchesList = new ArrayList<DMatch>();    // 一致状態の上位
  private static DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
  private static FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
  private static DescriptorExtractor executor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
  private double similarity = 0;

  /**
   * コンストラクタ
   * @param mat
   */
  public GaMat(Mat mat){
    img = mat;
  }

  /**
   * 特徴量を計算します。
   */
  public void calcDiscriptor(){
    // GRAY画像に変換
    Mat gray = new Mat(img.rows(), img.cols(), img.type());
    Imgproc.cvtColor( img, gray, Imgproc.COLOR_BGR2GRAY);
    // 特徴量の抽出
    MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
    detector.detect(gray, keyPoint01);
    executor.compute(gray, keyPoint01, descriptors);

  }

  /**
   * 参照画像との類似度を計算します。
   * 特徴量計算後に実行する必要があります。
   * 類似度Listは類似度が高い順にソートします。
   * @param src
   */
  public void calcMatchesList(Mat descriptor1){
    MatOfDMatch matches = new MatOfDMatch();
    matcher.match(descriptor1, descriptors, matches);
    matchesList = matches.toList();
    matchesList.sort(new DMatComparator());   // distance順にソート
  }

  /**
   * 指定したLankまでのmachesListから類似度を計算します。
   * @param lank
   * @return
   */
  public void calcSimilarity(int lank){
    int num = Math.min(lank,  matchesList.size());
    for ( int i = 0; i < num; i++  ){
      similarity += matchesList.get(i).distance;
    }
    similarity /= num;
  }

  public double getSimilarity(){
    return similarity;
  }

  /**
   * 特徴量Matのgetterです。
   * @return
   */
  public Mat getDescriptors(){
    return descriptors;
  }

  public void setImg(Mat mat){
    this.img = mat;
  }

  public Mat getImg(){
    return this.img;
  }

  @Override
  public int compareTo(GaMat o) {
    if (o == null) return -1;
    return (int) (this.similarity - o.similarity);
  }

}
