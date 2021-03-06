package ga.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
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
  private double[] targetHistNorm;
  private int[] targetHist;
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
  private static final int GENERATION_MAX = 100000;   // 計算する世代数
  private static final int generationSize = 200;
  private static final int imgWidth = 128;
  private static final int imgHeight = 128;
  private List<GaMat> matList = new ArrayList<GaMat>();
  private List<GaMat> nextGenList = new ArrayList<GaMat>();
  private static final int selectionSimilarityLank = 50;
  private static final int nonCombinationNum = 5; //交叉せずそのまま次世代に渡す数
  private static final int MUTATION_MAX_LINE_LENGTH = 10;
  private static final int MUTATION_MAX_LINE_THICK = 2;
  private static final int MUTATION_LINE_NUM = 2;    // 突然変異1回につき追記する線の数
  private static final double MUTATION_RATE = 0.10;  // 突然変異確率
  private static final int COMBINATION_ROI_NUM = 20;    // 交叉処理1回につき、交叉するROIの数
  private static int HIST_SIZE = 64;



  @Override
  public void start(Stage primaryStage) throws Exception {

    // 減色 & 縮小する
    Mat readMat = UtilImage.decreaseColorMat(Imgcodecs.imread(targetImagePath));

    System.out.println("" + readMat.rows() + " rate:" + (double)imgWidth/readMat.rows()  );
    Imgproc.resize(readMat, readMat, new Size(imgWidth, imgHeight));

    targetMat  = new GaMat( readMat );
//    targetMat  = new GaMat( Imgcodecs.imread(targetImagePath));


    // ヒストグラムを取得
    targetHistNorm = new double[HIST_SIZE];
    targetHist = new int[HIST_SIZE];
    UtilImage.calcHistgram(targetMat.getImg(), targetHist);
    UtilImage.calcHistgramNorm(targetMat.getImg(), targetHistNorm);

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

      for ( int i = 0; i < GENERATION_MAX; i++ ){

        System.out.println("loop:" + i );

        // メイン処理
        // 選択
        System.out.println(" selection, ");
        selection();

        // 次世代の最適画像を描画
        System.out.print(" score:" + nextGenList.get(0).getScore() + " similarity:" + nextGenList.get(0).getSimilarity());
        viewMat.setImg(nextGenList.get(0).getImg().clone());

        // 交叉
        System.out.print(" recombination, ");
        recombination();

        // 突然変異
        System.out.println(" mutaiton.");
        mutation();


        // 描画画像の更新
        Platform.runLater(() -> imView.setImage(UtilImage.createFxImage(viewMat.getImg(), fxImageWidth, fxImageHeight)));

        if ( i % 1000 == 0 ) {
          Thread.sleep(300);
        } else {
          Thread.sleep(10);
        }
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
//      list.add(new GaMat(  new Mat(imgWidth, imgHeight, CvType.CV_8UC3, new Scalar(0, 0, 0))));
//        list.add(new GaMat(
//            UtilImage.createRandomImageFromHist(targetMat.getImg().cols() , targetMat.getImg().rows(), targetHistNorm)));

//      list.add(new GaMat(
//            UtilImage.createRandomImageFromHist(
//                targetMat.getImg().cols(), targetMat.getImg().rows(), targetHistNorm, 30000, 20, 3)
//          ));
      list.add(new GaMat(
          UtilImage.createRandomImageFromHist(
              imgWidth, imgHeight, targetHistNorm, 10000, 10, 3, 0.5)
        ));
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
      // 類似度計算方法の変更
//      gaMat.calcDiscriptor();
//      gaMat.calcMatchesList(targetMat.getDescriptors());
//      gaMat.calcSimilarity(selectionSimilarityLank, targetHist);

      gaMat.calcSimilarity(targetMat.getImg());

//      System.out.println(" similarity:" + gaMat.getSimilarity());
    }

    // スコアを計算
    calcScore2(matList);
    Collections.sort(matList);


    // 上位が選ばれやすいように次の世代に渡す
    nextGenList.clear();

    while ( nextGenList.size() < generationSize  ) {
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
      calcScore2(matList);
    }

    System.out.println("selection." + " no4 matList size():" + matList.size());
//    for ( GaMat ga : matList ) {
//      System.out.println(" oldGeneration score:"  + ga.getScore());
//    }


    // ソート
    Collections.sort(nextGenList);

    System.out.println("selection." + " no5. nextGenList size():" + nextGenList.size());

    // debug用再計算
    calcScore2(nextGenList);
//    for ( GaMat ga : nextGenList ) {
//      System.out.println(" next score:"  + ga.getScore() + " similarity:" + ga.getSimilarity());
//    }



  }

  /**
   *  GaMatリストのスコアを計算 & セットします。
   */
  private void calcScore(List<GaMat> list) {

    if ( list.size() == 1 ) {
      list.get(0).setScore(0.000001);
      return;
    }


    double tmpSum = 0;
    for ( GaMat gaMat : list) {
      tmpSum += gaMat.getSimilarity();
    }

    if ( tmpSum != 0 ) {
      for ( GaMat gaMat : list ) {
        gaMat.setScore( gaMat.getSimilarity()/tmpSum );

      }
    } else {
      for ( GaMat gaMat : list ) {
        gaMat.setScore( 1 / (double)list.size()  );
      }
    }


  }


  /**
   *  GaMatリストのスコアを計算 & セットします。
   */
  private void calcScore2(List<GaMat> list) {

    if ( list.size() == 1 ) {
      list.get(0).setScore(0.000001);
      return;
    }

    // similarityが高い順にソート
    Collections.sort(matList);

    double n = matList.size();
    double a = 3 * ( n*n*n + 9*n*n + 9*n ) ;

    for ( int i = 0; i < n; i++ ) {
      double score = a / ( i - n - 1 ) / ( i - n - 1 );
      matList.get(i).setScore(score);
    }
  }



  /**
   * 交叉処理
   */
  private void recombination() {

    // スコアの再計算
    calcScore2(nextGenList);


    // リストのクリア、メモリの解放
    for ( GaMat gaMat : matList ) {
      gaMat.getImg().release();
    }
    matList.clear();

    // 上位は交叉せず次世代に進む
    for ( int i = 0; i < nonCombinationNum ; i++ ) {
      matList.add(nextGenList.get(i));
    }

    // 2つを選択し交叉
    for (int i = 0; i <= generationSize  - nonCombinationNum; i++ ) {

      // 2つを選択
      int[] numbers = getPairNum(nextGenList);

      // 交叉処理
      Mat dst1 = new Mat();
      Mat dst2 = new Mat();

//      UtilImage.createRecombinationMat(nextGenList.get(numbers[0]).getImg(), nextGenList.get(numbers[1]).getImg(),
//          dst1, dst2);
      UtilImage.createRecombinationMat(nextGenList.get(numbers[0]).getImg(), nextGenList.get(numbers[1]).getImg(),
          dst1, dst2, COMBINATION_ROI_NUM);

      matList.add(new GaMat(dst1));
      matList.add(new GaMat(dst2));
    }

    // nextGenListの解放
    a : for ( GaMat ga1 : nextGenList ) {
      for ( GaMat ga2 : matList ) {
        if ( ga1.equals(ga2) ) {
          continue a;
        }
      }
      ga1.getImg().release();
    }
    nextGenList.clear();



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
    double[] similarity = new double[size];   // 類似度
    double[] score = new double[size];         // 類似度を正規化したもの

    double sum = 0;
    for ( int i = 0; i < size; i++ ) {
      similarity[i] = list.get(i).getSimilarity();
      sum += similarity[i];
    }
    for ( int i = 0; i < size; i++ ) {
      score[i] =  similarity[i] / sum;
    }

    // 1回目の抽選
    double rate = Math.random();
    double rateSum = 0;
    for ( int i = 0; i < size; i++ ) {
      rateSum += score[i];
      if ( rate < rateSum) {
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
        score[i] = 0.00000001;
      } else {
        score[i] =  similarity[i] / sum;
      }
    }

    // 2回目の抽選
    rate = Math.random();
    rateSum = 0;
    for ( int i = 0; i < size; i++ ) {
      rateSum += score[i];
      if ( rate < rateSum) {
        ret[1] = i;
      }
    }

    return ret;
  }


  /**
   * 突然変異します。
   */
  private void mutation() {

    for (int i =0; i<matList.size(); i++) {
      if ( MUTATION_RATE <  Math.random() ) continue;
      Mat src = matList.get(i).getImg();

      for ( int j = 0; j < MUTATION_LINE_NUM; j++ ) {
        UtilImage.createRandomImageFromHist(src, targetHistNorm, MUTATION_MAX_LINE_LENGTH, MUTATION_MAX_LINE_THICK, 0.5);
      }

    }

  }



}
