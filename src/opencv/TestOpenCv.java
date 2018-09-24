package opencv;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestOpenCv extends Application {

	private static final String srcPath = "./image/lena.jpg";
	private static final String dstRootDir = ".\\";


	@Override
	public void start(Stage primaryStage) throws Exception{
		VBox vbox = new VBox();
		ImageView imView = new ImageView(createOpenCVImage());
		vbox.getChildren().add(imView);
		Scene scene = new Scene(vbox);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

  private Image createOpenCVImage() {
    Mat srcMat = Imgcodecs.imread(srcPath);
    MatOfByte byteMat = new MatOfByte();
    Imgcodecs.imencode(".bmp", decreaseColorMat(srcMat), byteMat);
    Image img = new Image(new ByteArrayInputStream( byteMat.toArray() ), 256, 256, false, false);
    return img;
  }

	/**
	 * 減色画像を作成します。
	 * CV_8UC3の値域は [-127,127]
	 * 参考 : http://aidiary.hatenablog.com/entry/20091003/1254574041
	 * @param src
	 * @return
	 */
	public Mat decreaseColorMat(Mat src) {

		Mat dst = new Mat(src.width(), src.height(), CvType.CV_8UC3);
		for ( int y = 0; y < dst.height(); y++ ) {
		  for ( int x = 0; x < dst.width(); x++ ) {
		    byte[] data = new byte[3];
		    src.get(y,  x, data);
		    data[0] = (byte)decreaseColor(data[0]);
        data[1] = (byte)decreaseColor(data[1]);
        data[2] = (byte)decreaseColor(data[2]);
		    dst.put(y, x, data);

		  }
		}
		return dst;
	}

	/**
	 * 減色処理です
   * CV_8UC3の値域は [-127,127]
	 * @param val
	 * @return
	 */
	public int decreaseColor(int val) {
	  if ( val < -64 ) {
	     return -96;
	  } else if ( val < 0 ) {
	     return -32;
	  } else if ( val < 64 ) {
	     return 32;
	  } else {
	    return 96;
	  }
	}


	public static void main(String args[]) {
		launch(args);
	}
  static{
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }


}
