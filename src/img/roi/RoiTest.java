package img.roi;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.UtilImage;

public class RoiTest extends Application {


  private Mat src1;
  private Mat src2;
  private Mat dst1;
  private Mat dst2;

  @Override
  public void start(Stage primaryStage) throws Exception{

    initMat();
    createDstMat();

    HBox hbox = new HBox();
    ImageView imView1 = new ImageView( UtilImage.createFxImage(dst1, dst1.rows(), dst1.cols())  );
    hbox.getChildren().add(imView1);
    ImageView imView2 = new ImageView( UtilImage.createFxImage(dst2, dst2.rows(), dst2.cols())  );
    hbox.getChildren().add(imView2);
    Scene scene = new Scene(hbox);
    primaryStage.setScene(scene);
    primaryStage.show();
  }


  private void createDstMat() {

//    // ランダムな矩形を生成
//    Point start = UtilImage.makeRandomPoint(new Point(0, 0), new Point(src2.rows(), src2.cols()));
//    int width = (int) (Math.random() * ( src2.rows() - start.x ) );
//    int height = (int) (Math.random() * ( src2.cols() - start.y ) );
//
//    // maskを作成
//    Mat mask = new Mat(src1.rows(), src1.cols(), CvType.CV_8UC1, new Scalar(0));
//    Imgproc.rectangle(mask, start, new Point(start.x + width, start.y + height), new Scalar(255), -1);
//    Mat maskNot = new Mat(src1.rows(), src1.cols(), CvType.CV_8UC1);
//    Core.bitwise_not(mask, maskNot);
//
//    dst1 = new Mat();
//    Mat dst11 = new Mat();
//    src1.copyTo(dst1, mask);
//    src1.copyTo(dst11, maskNot);
//
//    dst2 = new Mat();
//    Mat dst21 = new Mat();
//    src2.copyTo(dst2, mask);
//    src2.copyTo(dst21, maskNot);
//
//    Core.add(dst1, dst21, dst1);
//    Core.add(dst2, dst11, dst2);

    dst1 = new Mat();
    dst2 = new Mat();
    UtilImage.createRecombinationMat(src1, src2, dst1, dst2);

  }




  /**
   * src画像を生成します。
   */
  private void initMat() {
    src1 = Imgcodecs.imread("./image/lena2.jpg");
    src2 = Imgcodecs.imread("./image/lena3.jpg");
  }


  public static void main(String args[]) {
    launch(args);
  }
  static{
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

}
