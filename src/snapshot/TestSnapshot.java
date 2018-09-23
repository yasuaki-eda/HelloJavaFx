package snapshot;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestSnapshot extends Application {
	private Label label;
	private TextField field;
	private Button button;
	private VBox imgBox;

	@Override
	public void start(Stage primaryStage) throws Exception {

		BorderPane pane = new BorderPane();
		label = new Label("This is JavaFX!");
		field = new TextField();
		button = new Button("Click");
		button.setOnAction((ActionEvent)->{
			String msg = "you typed: " + field.getText();
			label.setText(msg);
			WritableImage img = imgBox.snapshot(new SnapshotParameters(), null);
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File("test.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		imgBox = new VBox();
		Image img = new Image(new File("C:\\Users\\YASUAKI\\Pictures\\開発用\\2015\\lena.jpg").toURI().toString() );
		ImageView imgView = new ImageView(img);
		imgBox.getChildren().add(imgView);

		pane.setTop(label);
		pane.setCenter(field);
		pane.setBottom(button);
		field.setPromptText("入力してください");
//		Scene scene = new Scene(pane, 320, 150);
		imgBox.getChildren().add(pane);
		Scene scene2 = new Scene(imgBox);
		primaryStage.setScene(scene2);
		primaryStage.show();


	}

	public static void main(String[] args) {
		launch(args);
	}


}
