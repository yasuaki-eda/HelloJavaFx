package actionevent;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {

	private Label label;
	private TextField field;
	private Button button;


	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		label = new Label("This is JavaFX!");
		field = new TextField();
		button = new Button("Click");
		button.setOnAction((ActionEvent)->{
			String msg = "you typed: " + field.getText();
			label.setText(msg);
		});

		BorderPane pane = new BorderPane();
		pane.setTop(label);
		pane.setCenter(field);
		pane.setBottom(button);
		field.setPromptText("入力してください");
		Scene scene = new Scene(pane, 320, 150);
		primaryStage.setScene(scene);
		primaryStage.show();;
	}

}
