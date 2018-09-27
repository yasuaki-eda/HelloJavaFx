package img.common;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 画像処理テスト用Application基底クラス
 * 画像読込み ⇒ ○○の処理 ⇒ 画像の表示 を行います。
 * @author eda
 *
 */
abstract public class ImageViewApplication extends Application {

	/* 画像のパス 日本語は使用不可 */
	protected String imagePath = "";
  /* 入力画像 */
	protected Mat src;
  /* JavaFX  */
	protected ImageView imView;
	protected VBox vbox;
	protected Scene scene;
	protected Image fxImage;
	private int fxImageWidth;
  private int fxImageHeight;

	/**
	 * JavaFX.Applicationの関数
	 */
	public void start(Stage primaryStage) throws Exception{
	  initImgViewApp();
		vbox = new VBox();
		fxImage = createFxImage();
		imView = new ImageView(fxImage);
		vbox.getChildren().add(imView);
		scene = new Scene(vbox);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * 入力画像⇒何らかの変換で画像を生成⇒ JavaFXのImageを生成
	 * @return
	 */
	protected Image createFxImage(){
		Mat srcMat = Imgcodecs.imread(imagePath);
		Mat dstMat = createDstMat(srcMat);
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", dstMat, byteMat);
		Image img = new Image(new ByteArrayInputStream( byteMat.toArray() ), fxImageWidth, fxImageHeight, false, false);
		return img;
	}

	/**
	 * 初期化関数です
	 */
	protected void initImgViewApp(){
    initImagePath();
    initFxImageWidth();
    initFxImageHeight();
	}

	/**
	 * 実装するメインロジック
	 * @param src
	 * @return
	 */
  abstract protected Mat createDstMat(Mat src);

  /**
   * パスを変えたい場合Overrideする
   */
	protected void initImagePath(){
		imagePath = "./image/lena.jpg";
	}

  /**
   * 表示サイズを変えたい場合Overrideする
   */
	protected void initFxImageWidth(){
	  fxImageWidth = 256;
	}

  /**
   * 表示サイズを変えたい場合Overrideする
   */
  protected void initFxImageHeight(){
    fxImageHeight = 256;
  }

  static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

  public int getFxImageWidth() {
    return fxImageWidth;
  }

  public void setFxImageWidth(int fxImageWidth) {
    this.fxImageWidth = fxImageWidth;
  }

  public int getFxImageHeight() {
    return fxImageHeight;
  }

  public void setFxImageHeight(int fxImageHeight) {
    this.fxImageHeight = fxImageHeight;
  }



}
