package application;
	

import javafx.application.Application;
import javafx.scene.image.*;
import javafx.stage.Stage;


public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		// Configure SceneManager with the primary stage.
		SceneManager.setPrimaryStage(primaryStage);
		
		// Set the main scene using SceneManager.
		SceneManager.setMainScene();

		// Configure and display the primary stage.
	    primaryStage.setTitle("Project1");
	    primaryStage.setMaximized(true);
	    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/pictures/icon.png")));
	    primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

		