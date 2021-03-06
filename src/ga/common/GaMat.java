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
  private double score;   // 集団の中におけるスコア(集団全体の合計が1になる)
  private static double LESS_MATCHES_PENALTY = 10000;
  private static double LESS_HIST_PENALTY = 100;

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

    // 後始末
    gray.release();

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
   * 以下の2つから類似度を計算します。
   * ①指定したLankまでのmachesList
   * ②ヒストグラムの類似度
   * 類似度の値域(0,1]、一致しているときに1になる。
   * @param lank
   * @return
   */
  public void calcSimilarity(int lank, int[] targetHist){
    int num = Math.min(lank,  matchesList.size());

    if ( num <= 0 ) {
      similarity = 1 / LESS_HIST_PENALTY * 5;
      return;
    }

    // 特徴点による類似度計算
    for ( int i = 0; i < num; i++  ){
      similarity += matchesList.get(i).distance;
    }
    // 一致したときに0にならないように1を加算
    similarity += 1;

    // 特徴量が少ないとき、不足点に大きなコストを与える
    if ( matchesList.size() < lank ) {
      similarity += (lank - matchesList.size() ) * LESS_MATCHES_PENALTY;
    }

    // ヒストグラムによる類似度
//    int[] hist = new int[targetHist.length];
//    UtilImage.calcHistgram(this.getImg(), hist);
//    double histSimilarity = UtilImage.calcHistgramIntersection(hist, targetHist);
//    similarity += (1 - histSimilarity) * LESS_HIST_PENALTY;

    similarity /= lank;

    // 逆数をとり値域を(0, 1]にする
    similarity = 1 / similarity;
  }


  /**
   * 類似度を計算します。
   * ピクセル同士比較して、同じ色の場合+1、異なる場合0とします。
   * 一致しているときにsimilarity=1、0に近いほど外れている。
   * @param targetMat : imageと同じサイズの目標画像
   */
  public void calcSimilarity(Mat targetMat){

    similarity = 1;
    for ( int y = 0; y < this.img.rows(); y++ ) {
      for ( int x = 0; x < this.img.cols(); x++ ) {
        byte[] data = new byte[3];
        byte[] data2 = new byte[3];
        targetMat.get(y,  x, data);
        img.get(y,  x, data2);
        if ( data[0] == data2[0] && data[1] == data2[1] && data[2] == data2[2] ) {
          similarity++;
        }
      }
    }

    // 値域を(0, 1]にする(similarity=0は取らない)
    similarity = similarity / (double)( img.rows() * img.cols() + 1);
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

  /**
   * similarityの降順に並べる
   */
  @Override
  public int compareTo(GaMat o) {
    if (o == null) return -1;
    if ( o.similarity < this.similarity ) return -1;
    if ( this.similarity  < o.similarity ) return 1;
    return 0;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

}
