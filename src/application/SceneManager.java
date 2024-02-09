package application;

import java.awt.Desktop;
import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class SceneManager {

    // Reference to the primary stage of the application.
    private static Stage primaryStage;

    // Setter method to set the primary stage.
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Set the main scene using a custom MainScene class.
    public static void setMainScene() {
        primaryStage.setScene(new MainScene(primaryStage));
    }

    // Set a general scene for the primary stage.
    public static void setScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    // Display an alert with specified title and content
    // Because I used it in all the scenes i put it here to avoid repeating it in every scene
    public static void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
	public static void getUniquName(StringBuilder fileName){
	     // Create a File object based on the input file name.
	     File file = new File(fileName.toString());
	     // Initialize a counter and a flag for the while loop.
	     int number = 1, flag = 0;
	     // Loop to check if the file exists and modify the file name accordingly.
	     while (file.exists()) {
	         int lastDotIndex;
	         if (flag == 0) {
	             // Find the last dot (.) position to locate the extension.
	             lastDotIndex = fileName.lastIndexOf(".");
	             // Insert a number before the extension for the first time.
	             fileName.insert(lastDotIndex, "("+ (number++) +")");
	         }
	         else {
	             // For subsequent iterations, remove the old number and add a new one.
	             int startIndex = fileName.lastIndexOf("(");
	             int endIndex = fileName.lastIndexOf(")") + 1;
	             fileName.delete(startIndex, endIndex);
	             lastDotIndex = fileName.lastIndexOf(".");
	             fileName.insert(lastDotIndex, "(" + (number++) + ")");
	         }
	         // Update the file object with the new file name.
	         file = new File(fileName.toString()); 
	         // Set flag to 1 to indicate that the file name has been modified at least once.
	         flag = 1;
	     }
	 }
	
	public static String replaceExtension(String fileName, String newExtension){
	     // Find the last dot (.) position to locate the extension.
	     int lastDotIndex = fileName.lastIndexOf(".");
	     if (lastDotIndex == -1) {
	         // If there's no extension, simply append the new extension.
	         return fileName + "." + newExtension;
	     }
	     // Replace the old extension with the new extension.
	     return fileName.substring(0, lastDotIndex) + "." + newExtension;
	 }
	
	public static void openFolder(String path) {
		try {
			Desktop desktop = Desktop.getDesktop();
	        File directory = new File(path);
	        desktop.open(directory);
		} catch (Exception e) {
			SceneManager.showAlert("Error","Can't open file by java copy the path of file and go to open it.");
		}
	}

	public static void handleShowHeader(StringBuilder headerToShow) {

		// Copy the header string for manipulation
	    StringBuilder header = new StringBuilder(headerToShow);

	    // Create a scrollable pane
	    ScrollPane subScenePane = new ScrollPane();

	    // Horizontal box for layout with spacing of 80
	    HBox hBox = new HBox(80);
	    hBox.setAlignment(Pos.CENTER);
	    hBox.setPadding(new Insets(50));
	    hBox.setStyle("-fx-background-color: #64fc9c;");

	    // Vertical box for layout with spacing of 15
	    VBox background = new VBox(15);
	    background.setPadding(new Insets(40));
	    background.setStyle("-fx-background-color: #2b2b2b; " +
	            "-fx-border-color: #555555; " +
	            "-fx-border-width: 2px; " +
	            "-fx-padding: 10px;");

	    // Label for the header
	    Label headerLabel = new Label("Header (bits in red color it's a subset from the header)");
	    headerLabel.setFont(Font.font("Verdana", 15.5));
	    headerLabel.setStyle("-fx-text-fill: #FFFFFF;");

	    // Processing header string to extract and display extension in bits and chars
	    String extensionInbitString = "";
	    String extensionInCharString = "";
	    for (int i = 0; i < 8; i++) {
	        extensionInCharString += (char) Integer.parseInt(header.substring(i * 8, (i + 1) * 8), 2);
	        extensionInbitString += header.substring(i * 8, (i + 1) * 8) + " ";
	    }
	    header.delete(0, 64); // Remove processed part from header
	    TextFlow extensionInbitTextFlow = creatTextFlow("Extension in bits: ", extensionInbitString);

	    Label extensionInCharLabel = new Label("Extension in char: " + extensionInCharString);
	    extensionInCharLabel.setStyle("-fx-text-fill: #FFFFFF;");

	    // Display size of header in bits
	    TextFlow sizeInbitTextFlow = creatTextFlow("size of header in bits: ", header.substring(0, 32));
	    int sizeOfHeader = Integer.parseInt(header.substring(0, 32), 2); // Convert size from bits to integer
	    header.delete(0, 32); // Remove processed part from header
	    Label sizeInIntLabel = new Label("size of header in integer: " + sizeOfHeader);
	    sizeInIntLabel.setStyle("-fx-text-fill: #FFFFFF;");

	    // Label for post-order traversal
	    Label postOrderLabel = new Label("Post Order for binary tree: ");
	    postOrderLabel.setStyle("-fx-text-fill: #FFFFFF;");
	    background.getChildren().addAll(headerLabel, extensionInbitTextFlow, extensionInCharLabel, sizeInbitTextFlow, sizeInIntLabel, postOrderLabel);

	    // Process and display individual bits of the header
	    int counter = 0, numberOfExtrabit = 8 - sizeOfHeader % 8;
	    while (counter < sizeOfHeader) {
	        if (header.charAt(counter) == '0') {
	            counter++;
	            System.out.println();
	            Label label = new Label("0 " + header.substring(counter, counter + 8));
	            label.setStyle("-fx-text-fill: #FF0000;"); // Red text for the header bits
	            background.getChildren().add(label);
	            counter += 8;
	        } else {
	            counter++;
	            Label label = new Label("1");
	            label.setStyle("-fx-text-fill: #FF0000;"); // Red text for the header bits
	            background.getChildren().add(label);
	        }
	    }

	    // Handle case when no extra bit is added
	    if (numberOfExtrabit == 8) {
	        Label label = new Label("The size of the header is divided by 8 no extra bit added");
	        label.setStyle("-fx-text-fill: #FFFFFF;");
	        background.getChildren().add(label);
	    }
	    else {
	        TextFlow textFlow = creatTextFlow("Extra bit: ", header.substring(counter, counter + numberOfExtrabit));
	        background.getChildren().add(textFlow);
	    }

	    // Add the VBox to the HBox and set the scene
	    hBox.getChildren().addAll(background);
	    subScenePane.setContent(hBox);
	    Scene scene = new Scene(subScenePane, 700, 400);
	    Stage newStage = new Stage();
	    newStage.setScene(scene);
	    hBox.prefWidthProperty().bind(newStage.widthProperty());
	    hBox.prefHeightProperty().bind(newStage.heightProperty());
	    newStage.show();
	}
	
	
	// Helper method to create a TextFlow with two differently colored parts
	private static TextFlow creatTextFlow(String str1, String str2) {
		 Text text1 = new Text(str1);
		 text1.setFill(Color.WHITE); // Set the color of the first part to white

		 Text text2 = new Text(str2);
		 text2.setFill(Color.RED); // Set the color of the second part to red

		 // Create a TextFlow and add both Text nodes
		 return new TextFlow(text1, text2);
	}

	public static String byteToBinaryString(byte b) {
	    StringBuilder binaryString = new StringBuilder();
	    for (int i = 7; i >= 0; i--) {
	        int bit = (b >> i) & 1;
	        binaryString.append(bit);
	    }
	    return binaryString.toString();
	}
}

