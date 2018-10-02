package img.affine;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

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
 * 画像を連続回転します。
 * @author Eda
 *
 */
public class Animation extends Application {

  private String imagePath = "./image/lena.jpg";
  private Mat src;
  private Mat dst;
  private Image fxImage;
  private ImageView imView;
  private VBox vbox;
  private Scene scene;
  private int fxImageWidth = 256;
  private int fxImageHeight = 256;
  private ExecutorService es;
  private ImageTask<Boolean> task;
  private int loopNum = 300;

  @Override
  public void start(Stage primaryStage) throws Exception {
    readSrc();

    // スレッドの設定
    es = Executors.newSingleThreadExecutor();
    task = new ImageTask<Boolean>();

    vbox = new VBox();
    imView = new ImageView(createFxImage(src));
    vbox.getChildren().add(imView);
    scene = new Scene(vbox);

    es.execute(task);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void readSrc(){
    src = Imgcodecs.imread(imagePath);
  }


  public class ImageTask<T> extends Task<Boolean>{
    @Override
    protected Boolean call() throws Exception {

      for ( int i = 0; i < loopNum; i++ ){
        Mat dst = UtilImage.rotateMatFromCenter(src, i * 2, 1);
        Platform.runLater(() -> imView.setImage(createFxImage(dst)));
        Thread.sleep(100);
      }

      return true;
    }
  }


  /**
   * Mat ⇒ JavaFXのImageを生成
   * @return
   */
  protected Image createFxImage(Mat srcMat){
    MatOfByte byteMat = new MatOfByte();
    Imgcodecs.imencode(".bmp", srcMat, byteMat);
    Image img = new Image(new ByteArrayInputStream( byteMat.toArray() ), fxImageWidth, fxImageHeight, false, false);
    return img;
  }

  public static void main(String args[]){
    launch(args);
  }

  static{
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

}
