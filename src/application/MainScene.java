package application;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.geometry.*;         
import javafx.scene.control.*;
import javafx.scene.text.*;

import java.io.*;
import java.util.*;

// Main scene class that represents the initial user interface.
public class MainScene extends Scene {

    // Constructor for the MainScene class.
    public MainScene(Stage primaryStage){
        super(new StackPane(), Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
        StackPane layout = (StackPane) getRoot();
        layout.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        layout.getStyleClass().add("root");
        
        // Set up the main layout using a VBox.
        VBox vBox = new VBox(250);
        vBox.setPadding(new Insets(60));
        vBox.setAlignment(Pos.TOP_CENTER);

        // Set up a welcome label.
        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.setFont(Font.font("Century Gothic", FontWeight.BOLD, 40));
        welcomeLabel.setStyle("-fx-text-fill: #000000;");

        // Array of button labels.
        String[] strings = {
            "Compress",
            "Uncompress",
        };
        
        
        // ArrayList to store buttons.
        ArrayList<Button> buttons = new ArrayList<Button>();   
        
        // Method to set up buttons with labels and actions.
        setupButtons(strings, buttons, primaryStage);
        
        // Set up a VBox to arrange buttons vertically.
        HBox ArrangementButtons = new HBox(100);
        ArrangementButtons.setAlignment(Pos.TOP_CENTER);
        ArrangementButtons.getChildren().addAll(buttons);
        
        // Add components to the main layout.
        vBox.getChildren().addAll(welcomeLabel, ArrangementButtons);
        layout.getChildren().addAll(vBox);
    }
    
 // This method sets up buttons based on an array of strings and adds them to a stage in a JavaFX application.
    public static void setupButtons(String[] strings, ArrayList<Button> buttons, Stage primaryStage) {
        // Iterate over the array of strings to create buttons
        for (int i = 0; i < strings.length; i++) {
            Button button = new Button(strings[i]); // Create a new button with text from the strings array
            buttons.add(button); // Add the button to the buttons ArrayList
            button.getStyleClass().add("custom-button"); // Add a custom CSS class for styling
            button.setPrefHeight(50); // Set preferred height for the button
            button.setPrefWidth(250); // Set preferred width for the button
        }

        // Set an action for the first button to open a file chooser for file selection
        buttons.get(0).setOnAction(e -> {
            FileChooser fileChooser = new FileChooser(); // Create a new file chooser
            File selectedFile = fileChooser.showOpenDialog(primaryStage); // Show the file chooser and get the selected file

            // Check if the selected file is valid and ends with .huf, show an error if it does
            if (selectedFile != null && selectedFile.getName().endsWith(".huf")) {
                SceneManager.showAlert("Error", ""); 
            } else if (selectedFile != null) { // If the selected file is valid and not a .huf file, change the scene
                SceneManager.setScene(new CompressScene(selectedFile));
            }       
        });
        
        // Set an action for the second button to open a file chooser with specific settings
        buttons.get(1).setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir"))); // Set the initial directory to the user's current directory
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Huffman file", "*.huf")); // Set the file extension filter
            File selectedFile = fileChooser.showOpenDialog(primaryStage); // Show the file chooser and get the selected file

            // Check if the selected file is not a .huf file and show an error if it's not
            if (selectedFile != null && !selectedFile.getName().endsWith(".huf")) {
                SceneManager.showAlert("Error", ""); 
            } else if (selectedFile != null) { // If the selected file is a .huf file, change the scene
                SceneManager.setScene(new UncompressScene(selectedFile));
            }      
        });
    }
    
}
