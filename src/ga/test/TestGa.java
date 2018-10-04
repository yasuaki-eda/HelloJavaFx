package ga.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

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

        selection();

        // メイン処理
        viewMat.setImg(UtilImage.rotateMatFromCenter(targetMat.getImg(), i * 2, 1) );

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
    for ( int i = 0; i< generationSize; i++ ){
      matList.add(new GaMat(  new Mat(imgWidth, imgHeight, CvType.CV_8UC3, new Scalar(0, 0, 0))));
    }
    viewMat = new GaMat(targetMat.getImg());

    // 目標画像の特徴量を計算
    targetMat.calcDiscriptor();

  }

  /**
   * 選択処理
   */
  private void selection(){
    double sum = 0;
    for ( GaMat gaMat : matList ){
      gaMat.calcDiscriptor();
      gaMat.calcMatchesList(targetMat.getDescriptors());
      gaMat.calcSimilarity(selectionSimilarityLank);
      sum += gaMat.getSimilarity();
    }
    Collections.sort(matList);

    // 上位を次の世代に渡す
    int oldSize = matList.size();

    nextGenList.clear();
    for ( int i = 0; i < generationSize; i++ ){

      double val = Math.random() * sum;
      double tmpVal = 0;
      for ( GaMat gaMat : matList ){
        tmpVal += gaMat.getSimilarity();
        if ( sum - tmpVal < val ){
          nextGenList.add(gaMat);
        }

      }


    }

  }



}
