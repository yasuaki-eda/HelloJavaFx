package ga.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ga.common.GaMat;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.UtilImage;

/**
 * 遺伝的アルゴリズムのテスト
 * @author eda
 *
 */
public class TestGa extends Application {

  public static void main(String args[]){
    launch(args);
  }

  static{
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  private GaMat targetMat;  // 目標とする画像
  private final String targetImagePath = "./image/lena.jpg";
  private GaMat viewMat;
  /* 計算メインスレッド */
  private ExecutorService calcMainService;
  private Task<Boolean> task;
  /* JavaFX  */
  protected ImageView imView;
  protected VBox vbox;
  protected Scene scene;
  protected Image fxImage;
  private static final int fxImageWidth = 128;
  private static final int fxImageHeight = 128;
  /* GAパラメタ */
  private static final int loopNum = 300;
  private static final int generationSize = 20;
  private static final int imgWidth = 128;
  private static final int imgHeight = 128;
  private static final double sigma = 0.0001;  // 突然変異確率
  private List<GaMat> matList = new ArrayList<GaMat>();
  private List<GaMat> nextGenList = new ArrayList<GaMat>();
  private static final int selectionSimilarityLank = 50;
  private static final int nonCombinationNum = 2; //交叉せずそのまま次世代に渡す数


  @Override
  public void start(Stage primaryStage) throws Exception {
    targetMat  = new GaMat( Imgcodecs.imread(targetImagePath) );
    calcMainService = Executors.newSingleThreadExecutor();
    task = new GaCalcTask<Boolean>();

    vbox = new VBox();
    imView = new ImageView(UtilImage.createFxImage(targetMat.getImg(), fxImageWidth, fxImageHeight));
    vbox.getChildren().add(imView);
    scene = new Scene(vbox);

    calcMainService.execute(task);
    primaryStage.setScene(scene);
    primaryStage.show();

  }


  /**
   *
     全体の流れ
     目標画像の読み込み
     初期画像の生成 (黒一色)
     for (世代数まで){
       選択
       交叉
       突然変異
       結果の出力(ログ、CSV、画像など)
      }
   * @author eda
   *
   * @param <T>
   */
  public class GaCalcTask<T> extends Task<Boolean>{
    @Override
    protected Boolean call() throws Exception {

      // 初期化処理
      initGa();

      for ( int i = 0; i < loopNum; i++ ){

        // メイン処理
        // 選択
        selection();

        // 次世代の最適画像を描画
        viewMat.setImg(nextGenList.get(0).getImg().clone());

        // 交叉
        recombination();

        // 突然変異


        // 描画画像の更新
        Platform.runLater(() -> imView.setImage(UtilImage.createFxImage(viewMat.getImg(), fxImageWidth, fxImageHeight)));
        Thread.sleep(10);
      }

      return true;
    }
  }

  /**
   * GA実行前初期化関数です
   */
  private void initGa(){

    // 黒画像を生成
    addInitImage(generationSize, matList);
    viewMat = new GaMat(targetMat.getImg());

    // 目標画像の特徴量を計算
    targetMat.calcDiscriptor();

  }

  /**
   * 初期画像をlistにaddします。
   * @param num
   * @param list
   */
  private void addInitImage(int num, List<GaMat> list) {
    for ( int i = 0; i< num; i++ ){
      list.add(new GaMat(  new Mat(imgWidth, imgHeight, CvType.CV_8UC3, new Scalar(0, 0, 0))));
    }
  }

  /**
   * 選択処理
   */
  private void selection(){
    double sum = 0;

    int oldSize = matList.size();
    if ( oldSize < generationSize ) {
      // 不足分は黒画像を生成
      addInitImage(generationSize - oldSize, matList);
    }

    // 類似度を計算
    for ( GaMat gaMat : matList ){
      gaMat.calcDiscriptor();
      gaMat.calcMatchesList(targetMat.getDescriptors());
      gaMat.calcSimilarity(selectionSimilarityLank);
      sum += gaMat.getSimilarity();
    }
    // スコアを計算
    for ( GaMat gaMat : matList ) {
      gaMat.setScore( (sum - gaMat.getSimilarity())/sum );
    }
    Collections.sort(matList);

    // 上位が選ばれやすいように次の世代に渡す
    nextGenList.clear();
    for ( int i = 0; i < generationSize; i++ ){
      double val = Math.random();
      double tmpVal = 0;
      for ( GaMat gaMat : matList ){
        tmpVal += gaMat.getScore();
        if ( val < tmpVal  ){
          nextGenList.add(gaMat);
          matList.remove(gaMat);
          break;
        }
      }

      // スコアの再計算
      calcScore(matList);
    }

    // ソート
    Collections.sort(nextGenList);

  }

  /**
   *  GaMatリストのスコアを計算 & セットします。
   */
  private void calcScore(List<GaMat> list) {
    double tmpSum = 0;
    for ( GaMat gaMat : list) {
      tmpSum += gaMat.getSimilarity();
    }
    for ( GaMat gaMat : list) {
      gaMat.setScore( (tmpSum - gaMat.getSimilarity())/tmpSum );
    }
  }


  /**
   * 交叉処理
   */
  private void recombination() {

    // スコアの再計算
    calcScore(nextGenList);

    // 上位は交叉せず次世代に進む
    for ( int i = 0; i < nonCombinationNum ; i++ ) {
      matList.add(nextGenList.get(i));
    }

    // 2つを選択し交叉
    for (int i = 0; i < generationSize  - nonCombinationNum; i++ ) {

      // 2つを選択
      int[] numbers = getPairNum(nextGenList);

      // 交叉処理
      createRecombinationMat(nextGenList.get(numbers[0]), nextGenList.get(numbers[1]), matList);

    }

  }

  /**
   * 交叉処理を行い、結果をdstList配列にaddします。
   * @param src1 : 画像1
   * @param src2 : 画像2
   * @param dstList : 出力格納用list
   */
  private void createRecombinationMat(GaMat src1, GaMat src2, List<GaMat> dstList) {

    Point start = UtilImage.makeRandomPoint(new Point(0, 0), new Point(src1.getImg().width(), src1.getImg().height()));
    int width = (int) (Math.random() * ( src1.getImg().width() - start.x ) );
    int height = (int) (Math.random() * ( src1.getImg().height() - start.y ) );
    Rect rect = new Rect((int)start.x, (int)start.y, width, height);
    Mat roiMat1 = new Mat(src1.getImg(), rect);
    Mat roiMat2 = new Mat(src2.getImg(), rect);

    // TODO : 動作検証

    // ROIと同じ
    Mat mask = new Mat(src1.getImg().width(), src1.getImg().height(), CvType.CV_8UC1, new Scalar(0));
    Imgproc.rectangle(mask, start, new Point(start.x + width, start.y + height), new Scalar(255), -1);

    // 生成1
    Mat dst1 = src1.getImg().clone();
    roiMat2.copyTo(dst1, mask);

    // 生成2
    Mat dst2 = src2.getImg().clone();
    roiMat1.copyTo(dst2, mask);

    // 結果の格納
    dstList.add( new GaMat(dst1) );
    dstList.add( new GaMat(dst2) );
  }


  /**
   * GaMatリストのscoreを参考に2つの番号を返します。
   * scoreが高い程選ばれやすい。重複はしない。
   * @param list
   * @return
   */
  private int[] getPairNum(List<GaMat> list) {
    int[] ret = new int[2];
    int size = list.size();
    double[] similarity = new double[size];
    double[] score = new double[size];

    double sum = 0;
    for ( int i = 0; i < size; i++ ) {
      similarity[i] = list.get(i).getSimilarity();
      sum += similarity[i];
    }
    for ( int i = 0; i < size; i++ ) {
      score[i] = ( sum - similarity[i] ) / sum;
    }

    // 1回目の抽選
    double rate = Math.random();
    double rateSum = 0;
    for ( int i = 0; i < size; i++ ) {
      rateSum += similarity[i];
      if ( rateSum < score[i]) {
        ret[0] = i;
      }
    }

    // scoreの再計算 (抽選で選ばれた番号を除外 )
    for ( int i = 0; i < size; i++ ) {
      if ( i == ret[0] ) continue;
      sum += similarity[i];
    }
    for ( int i = 0; i < size; i++ ) {
      if ( i == ret[0] ) {
        score[i] = 0;
      } else {
        score[i] = ( sum - similarity[i] ) / sum;
      }
    }

    // 2回目の抽選
    rate = Math.random();
    rateSum = 0;
    for ( int i = 0; i < size; i++ ) {
      rateSum += similarity[i];
      if ( rateSum < score[i]) {
        ret[1] = i;
      }
    }

    return ret;
  }





}
