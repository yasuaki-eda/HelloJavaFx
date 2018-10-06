package opencv;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import util.UtilImage;

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
    Mat dstMat = UtilImage.decreaseColorMat(srcMat);

    Point start = UtilImage.makeRandomPoint(new Point(0, 0), new Point(dstMat.width(), dstMat.height()));
    Point end = UtilImage.makeRandomPoint(new Point(0, 0), new Point(dstMat.width(), dstMat.height()));
    Color color = makeRandomColor();
    paintLine(dstMat, start, end, color);


    for ( int i = 0; i < 1000 ; i++ ) {
      start = UtilImage.makeRandomPoint(new Point(0, 0), new Point(dstMat.width(), dstMat.height()));
      end = UtilImage.makeRandomPoint(new Point(0, 0), new Point(dstMat.width(), dstMat.height()));
      color = makeRandomColor();
      paintLine(dstMat, start, end, color);
    }


    MatOfByte byteMat = new MatOfByte();
    Imgcodecs.imencode(".bmp", dstMat, byteMat);
    Image img = new Image(new ByteArrayInputStream( byteMat.toArray() ), 512, 512, false, false);
    return img;
  }

  /**
   * ランダムな色を生成します。
   * @return
   */
  public Color makeRandomColor() {
    return Color.color(Math.random(), Math.random(), Math.random());
  }



  /**
   * 画像に開始点-終了点までの線分を書き込みます。
   * @param start
   * @param end
   * @param color
   */
  public void paintLine(Mat src, Point start, Point end, Color color) {

    if ( end.x == start.x ) return;
    double a = (end.y - start.y ) / (end.x - start.x);
    double b = - a * start.x + start.y;
    int blue = (byte) (color.getBlue() * 256 - 127);
    int red = (byte) (color.getRed() * 256 - 127);
    int green = (byte) (color.getGreen() * 256 - 127);
    double minX = Math.min(start.x, end.x);
    double minY = Math.min(start.y, end.y);
    double maxX = Math.max(start.x, end.x);
    double maxY = Math.max(start.y, end.y);

    byte[] data = new byte[3];
    for (int y = 0; y < src.height(); y++ ) {
      for ( int x = 0; x < src.width(); x++ ) {

        if ( x < minX || maxX < x || y < minY || maxY < y) {
          continue;
        }
        if ( isOnLine(x, y, start, end, a, b) ) {
          src.get(y,  x, data);
          data[0] = (byte) (data[0] + blue);
          data[0] = (byte) ((data[0] > 127) ? (data[0] - 256) : data[0]);
          data[1] = (byte) (data[1] + green);
          data[1] = (byte) ((data[1] > 127) ? (data[1] - 256) : data[1]);
          data[2] = (byte) (data[2] + red);
          data[2] = (byte) ((data[2] > 127) ? (data[2] - 256) : data[2]);
          src.put(y, x, data);
        }
      }
    }

    return;
  }

  /**
   * 座標(x, y)がStart-End線分上にあるかどうか判定します。
   * 計算の簡略化のため、Start-Endの直線の傾きaと切片bは事前に計算しておきます。
   * @param x
   * @param y
   * @param start : 起点
   * @param end : 終点
   * @param a : 傾き
   * @param b : 切片
   * @return
   */
  private boolean isOnLine(int x, int y, Point start, Point end, double a, double b) {
    if ( ( a * x + b - 2 <= y ) && ( y <= a * x + b + 2 ) ) {
      return true;
    }
    return false;
  }


	public static void main(String args[]) {
		launch(args);
	}
  static{
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }


}
