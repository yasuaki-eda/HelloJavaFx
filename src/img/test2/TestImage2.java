package img.test2;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 画像の利用2
 * ・画像にピクセル単位でアクセス
 * ・OpenCV3の利用
 * @author Eda
 *
 */
public class TestImage2 extends Application {

	private static final String srcPath = "./image/lena.jpg";
	private Image src;
	private static final String dstRootDir = ".\\";

	public void start(Stage primaryStage) throws Exception{
		VBox vbox = new VBox();
//		ImageView view = new ImageView(createImage());
		ImageView view = new ImageView(createOpenCVImage());
		vbox.getChildren().add(view);

//		UtilFile.createTimestampDir(dstRootDir, "");

		Scene scene = new Scene(vbox);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private Image createOpenCVImage() {
		Mat srcMat = Imgcodecs.imread(srcPath);
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", srcMat, byteMat);
		Image img = new Image(new ByteArrayInputStream( byteMat.toArray() ), 256, 256, false, false);
		return img;
	}


	/**
	 * 描画する画像を生成します。
	 * @return
	 */
	private Image createImage() {
		Image img = new Image(new File(srcPath).toURI().toString(), 256, 256, false, false );
		WritableImage viewImage = createReverseRGBImage(img);
		return viewImage;
	}


	/**
	 * RGBを入れ替えた画像を生成します。
	 * @param img
	 * @return
	 */
	private WritableImage createReverseRGBImage(Image img) {
		WritableImage wImg = new WritableImage((int)img.getWidth(), (int)img.getHeight());

		// pixcel単位の操作
		PixelReader reader = img.getPixelReader();
		PixelWriter writer = wImg.getPixelWriter();

		for ( int x = 0; x < wImg.getWidth(); x++) {
			for ( int y = 0; y < wImg.getHeight(); y++ ) {
				int tmp = reader.getArgb(x, y);
				int val = (tmp >> 8) | ( tmp << 16 );
				writer.setArgb(x, y, val);
			}
		}
		return wImg;
	}


	public static void main(String args[]) {
		launch(args);
	}

	static{
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

}
