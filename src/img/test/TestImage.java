package img.test;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestImage extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		VBox root = new VBox();
		Image img = new Image(new File("C:\\Users\\YASUAKI\\Pictures\\開発用\\2015\\lena.jpg").toURI().toString() );
//		Image img2 = new Image(new File("C:\\Users\\YASUAKI\\Pictures\\開発用\\2015\\kao01.jpg").toURI().toString() );
		ImageView imgView = new ImageView(img);
//		ImageView imgView2 = new ImageView(img2);
		root.getChildren().add(imgView);
//		root.getChildren().add(imgView2);

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
