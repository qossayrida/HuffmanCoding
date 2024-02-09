package application;

import java.io.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CompressScene extends Scene {
	
	// Class member variables
	File path; // The file to be compressed
	TreeNode[] bytes; // Array to store TreeNode objects for Huffman tree
	long lengthFileBefore; // Length of the file before compression
	long lengthFileAfter; // Length of the file after compression
	StringBuilder headerToShow = new StringBuilder(""); // StringBuilder to build and display header information
	StringBuilder nameOfCompressedFile; // Name of the compressed file

	// Constructor for CompressScene
	public CompressScene(File path) {
	    // Setting up the scene with StackPane as the root and adjusting its size to the screen's dimensions
	    super(new StackPane(), Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
	    StackPane layout = (StackPane) getRoot();
	    layout.getStylesheets().add(getClass().getResource("application.css").toExternalForm()); // Adding CSS for styling
	    layout.getStyleClass().add("root");
	    this.path = path; // Assigning the file path

	    // Creating and configuring a label to indicate the compression process
	    Label waitLabel = new Label("Please wait, the file will be compressed soon");
	    waitLabel.setFont(Font.font("Century Gothic", 20)); // Setting the font

	    // Creating a progress indicator
	    ProgressIndicator progressIndicator = new ProgressIndicator();
	    progressIndicator.setVisible(true);

	    // VBox for arranging the progress indicator and label vertically
	    VBox vBox = new VBox(40); // 40 is the spacing between children
	    vBox.setAlignment(Pos.CENTER);
	    vBox.getChildren().addAll(progressIndicator, waitLabel);

	    // String array for button labels
	    String[] strings = {
	        "Copy path of compressed file",
	        "Open folder of compressed file",
	        "Show header",
	        "Show huffman table",
	        "Back to main page"
	    };
	    
	    // Adding background image and VBox to the layout
	    layout.getChildren().addAll(vBox);
	    layout.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
	    layout.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());

	    // Starting a new thread for the compression process
	    new Thread(() -> {
	        compress(); // Compressing the file

	        // Updating the UI elements after the compression is completed
	        Platform.runLater(new Runnable() {
	            @Override
	            public void run() {
	                // Removing progress indicator and label
	                vBox.getChildren().remove(vBox.getChildren().size() - 1);
	                vBox.getChildren().remove(vBox.getChildren().size() - 1);
	                vBox.setPadding(new Insets(60));

	                // Checking if the file was compressed successfully
	                if (lengthFileBefore > 0) {
	                    // Displaying results of the compression
	                    Label welcomeLabel = new Label("Results for compress the file");
	                    welcomeLabel.setFont(Font.font("Century Gothic", FontWeight.BOLD, 30));
	                    welcomeLabel.setStyle("-fx-text-fill: #000000;");

	                    Label doneCorrectlyLabel = new Label(
	                        "File size Before compress: " + lengthFileBefore + " bytes, " +
	                        "File size After compress: " + lengthFileAfter + " bytes, " +
	                        "Compression Ratio: " + String.format("%.5f", (double) lengthFileAfter / lengthFileBefore));
	                    Label nameOfCompressedFileLabel = new Label(
	                        "The name of compressed file is: " + nameOfCompressedFile);
	                    doneCorrectlyLabel.setFont(Font.font("Century Gothic", 20));
	                    doneCorrectlyLabel.setStyle("-fx-text-fill: #000000;");
	                    nameOfCompressedFileLabel.setFont(Font.font("Century Gothic", 20));
	                    nameOfCompressedFileLabel.setStyle("-fx-text-fill: #000000;");

	                    // Creating buttons and setting them up
	                    Button[] buttons = new Button[strings.length];
	                    setupButtons(strings, buttons);
	                    VBox ArrangementButtons = new VBox(45);
	                    ArrangementButtons.setAlignment(Pos.TOP_CENTER);
	                    ArrangementButtons.getChildren().addAll(buttons);

	                    // Adding all elements to the VBox
	                    vBox.setAlignment(Pos.TOP_CENTER);
	                    vBox.getChildren().addAll(welcomeLabel, doneCorrectlyLabel, nameOfCompressedFileLabel, ArrangementButtons);
	                } else {
	                    // Handling the case where the file is empty
	                    Label failLabel = new Label("The file is empty,Can't be compressed");
	                    failLabel.setFont(Font.font("Century Gothic", FontWeight.BOLD, 22));
	                    failLabel.setStyle("-fx-text-fill: #000000;");

	                    Button button = new Button("Back to main page");
	                    button.setOnAction(e -> SceneManager.setMainScene());
	                    button.getStyleClass().add("custom-button");

	                    vBox.setAlignment(Pos.CENTER);
	                    vBox.getChildren().addAll(failLabel, button);
	                }
	            }
	        });
	    }).start(); // Starting the thread
	}

	public void compress() {
	    try {
	        // Initialize an array of TreeNode objects representing all possible byte values
	        bytes = new TreeNode[256];
	        for (int i = 0; i < 256; i++) {
	            bytes[i] = new TreeNode((byte) i);
	        }

	        // Record the file size before compression
	        lengthFileBefore = path.length();

	        // If the file is empty, exit the method
	        if (lengthFileBefore == 0)
	            return;

	        // Buffer for reading the file
	        byte[] bufferIn = new byte[8];

	        // Read the file and count the frequency of each byte
	        try (FileInputStream scan = new FileInputStream(path)) {
	            int countOfBytesInBuffer;
	            while ((countOfBytesInBuffer = scan.read(bufferIn)) != -1) {
	                for (int i = 0; i < countOfBytesInBuffer; i++) {
	                    if (bufferIn[i] < 0)
	                        bytes[bufferIn[i] + 256].increment();
	                    else
	                        bytes[bufferIn[i]].increment();
	                }
	            }
	        } catch (Exception e) {
	            SceneManager.showAlert("Error", e.getMessage());
	        }

	        // Create a heap to build the Huffman tree
	        Heap heap = new Heap(256);
	        for (TreeNode byteNode : bytes) {
	            if (byteNode.getFrequency() != 0)
	                heap.insert(byteNode);
	        }

	        // Build the Huffman tree
	        while (heap.getSize() > 1) {
	            TreeNode x = heap.remove();
	            TreeNode y = heap.remove();
	            TreeNode z = new TreeNode(x.getFrequency() + y.getFrequency());
	            z.setLeft(x);
	            z.setRight(y);
	            heap.insert(z);
	        }
	        TreeNode rootTreeNode = heap.remove();

	        // Set the code for the root node if it's the only node
	        if (rootTreeNode.getLeft() == null && rootTreeNode.getRight() == null)
	            rootTreeNode.setCode("1");
	        else
	            TreeNode.gaveCodeForEachByte(rootTreeNode);

	        // Build the header for the Huffman tree
	        StringBuilder header = new StringBuilder(rootTreeNode.traverse());
	        
	        // Get the file extension
	        String fileExtension = path.getName().substring(path.getName().lastIndexOf(".") + 1);
	        for (int i = 0; i < bufferIn.length; i++) {
	            if (i < fileExtension.length()) {
	                bufferIn[i] = (byte) fileExtension.charAt(i);
	                headerToShow.append(SceneManager.byteToBinaryString(bufferIn[i]));
	            } else {
	                bufferIn[i] = (byte) 0;
	                headerToShow.append("00000000");
	            }
	        }

	        // Convert the header length to a byte array
	        byte[] bufferForHeaderSize = {
	                (byte) (header.length() >> 24),
	                (byte) (header.length() >> 16),
	                (byte) (header.length() >> 8),
	                (byte) header.length()
	            };
	        for (byte b : bufferForHeaderSize) {
	            headerToShow.append(SceneManager.byteToBinaryString(b));
	        }

	        // Padding the header to make its length a multiple of 8
	        if (header.length() % 8 != 0) {
	            int paddingLength = 8 - header.length() % 8;
	            for (int i = 0; i < paddingLength; i++) {
	                header.append("0");
	            }
	        }
	        headerToShow.append(header);

	        // Prepare the name for the compressed file
	        nameOfCompressedFile = new StringBuilder(SceneManager.replaceExtension(path.getName(), "huf"));
	        SceneManager.getUniquName(nameOfCompressedFile);
	        FileOutputStream out = new FileOutputStream(nameOfCompressedFile.toString());

	        // Write the file extension and header size to the compressed file
	        out.write(bufferIn);
	        out.write(bufferForHeaderSize);

	        // Write the header to the compressed file
	        int numOfBytes = header.length() / 8;
	        int sizeForLastBuffer = numOfBytes % 8;
	        for (int i = 0; i < numOfBytes; i++) {
	            String byteString = header.substring(i * 8, (i + 1) * 8);
	            bufferIn[i % 8] = (byte) Integer.parseInt(byteString, 2);
	            if (i % 8 == 7)
	                out.write(bufferIn);
	        }
	        if (sizeForLastBuffer > 0)
	            out.write(bufferIn, 0, sizeForLastBuffer);

	        // Compress the data using the Huffman codes
	        StringBuilder data = new StringBuilder();
	        byte[] bufferOut = new byte[8];
	        int bufferLength;
	        try (FileInputStream scan = new FileInputStream(path)) {
	            while ((bufferLength = scan.read(bufferIn)) != -1) {
	                for (int k = 0; k < bufferLength; k++) {
	                    if (bufferIn[k] < 0)
	                        data.append(bytes[bufferIn[k] + 256].getCode());
	                    else
	                        data.append(bytes[bufferIn[k]].getCode());

	                    if (data.length() >= 64) {
	                        for (int i = 0; i < 8; i++) {
	                            bufferOut[i] = (byte) Integer.parseInt(data.substring(0, 8), 2);
	                            data.delete(0, 8);
	                        }
	                        out.write(bufferOut);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            SceneManager.showAlert("Error", e.getMessage());
	        }

	        // Handle the remaining bits
	        int numberOfEffectiveBits = data.length() % 8;
	        if (numberOfEffectiveBits % 8 != 0) {
	            int paddingLength = 8 - numberOfEffectiveBits;
	            for (int i = 0; i < paddingLength; i++)
	                data.append("0");
	        } else {
	            numberOfEffectiveBits = 8;
	        }

	        if (data.length() > 0) {
	            int remainsBytes = data.length() / 8;
	            byte[] bufferOut1 = new byte[remainsBytes];
	            for (int i = 0; i < remainsBytes; i++) {
	                bufferOut1[i] = (byte) Integer.parseInt(data.substring(0, 8), 2);
	                data.delete(0, 8);
	            }
	            out.write(bufferOut1);
	        }

	        // Write the number of effective bits in the last byte
	        out.write((byte) numberOfEffectiveBits);
	        out.close();

	        // Check the size of the compressed file
	        File toCheck = new File(nameOfCompressedFile.toString());
	        lengthFileAfter = toCheck.length();

	    } catch (Exception e) {
	        SceneManager.showAlert("Error", e.getMessage());
	    }
	}

	public void setupButtons(String[] strings, Button[] buttons) {
	    
	    // Loop through each string in the 'strings' array to create and configure buttons
	    for (int i = 0; i < strings.length; i++) {
	        Button button = new Button(strings[i]); // Create a new button with label from the strings array
	        buttons[i] = button; // Store the button in the 'buttons' array
	        button.getStyleClass().add("custom-button"); // Add a CSS class for styling
	        button.setPrefHeight(50); // Set the preferred height for the button
	        button.setPrefWidth(300); // Set the preferred width for the button
	    }

	    // Tooltip for indicating clipboard copy action
	    Tooltip tooltip = new Tooltip("Link copied to clipboard");
	    // Timeline to hide the tooltip after 1.5 seconds
	    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> tooltip.hide()));

	    // Set action for the first button: Copy the current directory's path to clipboard
	    buttons[0].setOnAction(e -> {
	        Clipboard clipboard = Clipboard.getSystemClipboard();
	        ClipboardContent content = new ClipboardContent();
	        content.putString(System.getProperty("user.dir")); // Put the current directory's path
	        clipboard.setContent(content); // Copy to clipboard

	        // Show tooltip temporarily when button is clicked
	        if (!tooltip.isShowing()) {
	            tooltip.show(buttons[0], 70, 70);
	            timeline.playFromStart();
	        }
	    });

	    // Set action for the second button: Open the folder containing the compressed file
	    buttons[1].setOnAction(e -> SceneManager.openFolder(System.getProperty("user.dir")));

	    // Set action for the third button: Show header information
	    buttons[2].setOnAction(e -> SceneManager.handleShowHeader(headerToShow));

	    // Set action for the fourth button: Show Huffman table
	    buttons[3].setOnAction(e -> handleShowHuffmanTable());

	    // Set action for the fifth button: Return to the main scene
	    buttons[4].setOnAction(e -> SceneManager.setMainScene());
	}
    
    private void handleShowHuffmanTable() {
    	ScrollPane subScenePane = new ScrollPane();
        HBox hBox = new HBox(80);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(50));
        hBox.setStyle("-fx-background-color: #64fc9c;");
        
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10); // Vertical gap between rows
        gridPane.setHgap(35); // Horizontal gap between columns
        gridPane.setPadding(new Insets(15));
        gridPane.setAlignment(Pos.CENTER);

        // Header
        Label[] headers = {
            new Label("Byte"),
            new Label("Huffman"),
            new Label("Frequency"),
            new Label("Length")
        };
        int columnIndex = 0;
        for (Label header : headers) {
            header.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            GridPane.setConstraints(header, columnIndex, 0); // column, row
            GridPane.setHalignment(header, HPos.CENTER); // Center alignment
            gridPane.getChildren().add(header);
            columnIndex++;
        }

        // Add TreeNode details to the GridPane
        int rowIndex = 1;
        for (TreeNode node : bytes) {
            if (node.getCode() != null) {
                Label byteLabel = new Label(node.getByteContent() + "");
                Label codeLabel = new Label(node.getCode());
                Label frequencyLabel = new Label(node.getFrequency() + "");
                Label lengthLabel = new Label(node.getCode().length() + "");

                byteLabel.setStyle("-fx-text-fill: white;");
                codeLabel.setStyle("-fx-text-fill: white;");
                frequencyLabel.setStyle("-fx-text-fill: white;");
                lengthLabel.setStyle("-fx-text-fill: white;");
                GridPane.setHalignment(byteLabel, HPos.CENTER);
                GridPane.setHalignment(codeLabel, HPos.CENTER);
                GridPane.setHalignment(frequencyLabel, HPos.CENTER);
                GridPane.setHalignment(lengthLabel, HPos.CENTER);

                // Add labels to grid
                gridPane.add(byteLabel, 0, rowIndex);
                gridPane.add(codeLabel, 1, rowIndex);
                gridPane.add(frequencyLabel, 2, rowIndex);
                gridPane.add(lengthLabel, 3, rowIndex);

                rowIndex++;
            }
        }

        // Create a new VBox to act as the main container for the contents
        VBox background = new VBox(30);
        Label huffmanLabel = new Label("Huffman Tabel");
        huffmanLabel.setFont(Font.font("Verdana", 15.5));
        huffmanLabel.setStyle("-fx-text-fill: #FFFFFF;");
        background.setStyle("-fx-background-color: #2b2b2b; " +
                "-fx-border-color: #555555; " +
                "-fx-border-width: 2px; " +
                "-fx-padding: 10px;");
        background.setAlignment(Pos.CENTER);
        background.setPadding(new Insets(40));
        background.getChildren().addAll(huffmanLabel,gridPane);
        
        hBox.getChildren().addAll(background);
        subScenePane.setContent(hBox);
        Scene scene = new Scene(subScenePane, 500, 400);
        Stage newStage = new Stage();
        newStage.setScene(scene);
        hBox.prefWidthProperty().bind(newStage.widthProperty());
        hBox.prefHeightProperty().bind(newStage.heightProperty());
        newStage.show();
    }
    
}