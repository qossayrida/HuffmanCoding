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

public class UncompressScene extends Scene {
	
    File path; // Path of the file to be uncompressed
    TreeNode[] bytes; // Array to store TreeNode objects for Huffman tree
    long lengthFileBefore; // Length of the file before uncompression
    long lengthFileAfter; // Length of the file after uncompression
    StringBuilder headerToShow = new StringBuilder(""); // StringBuilder to construct and show header information
    StringBuilder nameOfUncompressedFile; // StringBuilder to store the name of the uncompressed file
    
    public UncompressScene(File path) {
        // Creating a new Scene with a StackPane as the root, and setting its dimensions to the screen size
        super(new StackPane(), Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
        StackPane layout = (StackPane) getRoot();
        layout.getStylesheets().add(getClass().getResource("application.css").toExternalForm()); // Adding CSS for styling
        layout.getStyleClass().add("root");
        this.path = path; // Assigning the path of the file to uncompress

        // Creating and configuring a label to indicate the uncompression process
        Label waitLabel = new Label("Please wait, the file will be uncompressed soon");
        waitLabel.setFont(Font.font("Century Gothic", 20));

        // Creating a progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(true);

        // VBox for arranging the progress indicator and the label vertically
        VBox vBox = new VBox(40);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(progressIndicator, waitLabel);

        // String array for button labels
        String[] strings = {
            "Copy path of uncompressed file",
            "Open uncompressed file",
            "Show header",
            "Show huffman table",
            "Back to main page"
        };


        // Adding the background image and VBox to the layout
        layout.getChildren().addAll(vBox);
        layout.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        layout.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());

        // Starting a new thread for the uncompression process
        new Thread(() -> {
            uncompress(); // Method call to start uncompressing the file
            
            // Updating the UI elements after the uncompression is completed
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    vBox.getChildren().remove(vBox.getChildren().size() - 1); // Removing the last child (progress indicator)
                    vBox.getChildren().remove(vBox.getChildren().size() - 1); // Removing the second last child (wait label)
                    vBox.setPadding(new Insets(60)); // Setting padding for the VBox

                    // Setting up various labels to display uncompression results and details
                    Label welcomeLabel = new Label("Results for uncompress the file");
                    welcomeLabel.setFont(Font.font("Century Gothic", FontWeight.BOLD, 30));
                    welcomeLabel.setStyle("-fx-text-fill: #000000;");

                    Label doneCorrectlyLabel = new Label(
                        "File size Before uncompress: " + lengthFileBefore + " bytes, " +
                        "File size After uncompress: " + lengthFileAfter + " bytes, " +
                        "Uncompression Ratio: " + String.format("%.5f", (double) lengthFileAfter / lengthFileBefore));
                    Label nameOfUncompressedFileLabel = new Label(
                        "The name of compressed file is: " + nameOfUncompressedFile);
                    doneCorrectlyLabel.setFont(Font.font("Century Gothic", 20));
                    doneCorrectlyLabel.setStyle("-fx-text-fill: #000000;");
                    nameOfUncompressedFileLabel.setFont(Font.font("Century Gothic", 20));
                    nameOfUncompressedFileLabel.setStyle("-fx-text-fill: #000000;");

                    // Creating buttons and setting them up
                    Button[] buttons = new Button[strings.length];
                    setupButtons(strings, buttons);
                    VBox ArrangementButtons = new VBox(45);
                    ArrangementButtons.setAlignment(Pos.TOP_CENTER);
                    ArrangementButtons.getChildren().addAll(buttons);

                    // Adding all elements to the VBox
                    vBox.setAlignment(Pos.TOP_CENTER);
                    vBox.getChildren().addAll(welcomeLabel, doneCorrectlyLabel, nameOfUncompressedFileLabel, ArrangementButtons);
                }
            });
        }).start(); // Starting the thread
    }
	
    public void uncompress() {
        try {
            // Record the file size before uncompression
            lengthFileBefore = path.length();

            // Create a FileInputStream to read the compressed file
            FileInputStream scan = new FileInputStream(path);

            // StringBuilder to store the file extension
            StringBuilder fileExtension = new StringBuilder();

            // Buffers for reading the file
            byte[] buffer = new byte[8]; // Buffer for general purpose
            byte[] sizeOfHeaderbBuffer = new byte[4]; // Buffer for the size of the header
            int sizeOfHeader = 0;

            // Read the first 8 bytes of the file to determine the file extension
            if (scan.read(buffer) != -1) {
                for (int i = 0; i < 8; i++) {
                    if (buffer[i] != 0) {
                        fileExtension.append((char) buffer[i]);
                    }
                    headerToShow.append(SceneManager.byteToBinaryString(buffer[i]));
                }

                // Read the next 4 bytes to determine the size of the header
                scan.read(sizeOfHeaderbBuffer);
                sizeOfHeader = byteArrayToInt(sizeOfHeaderbBuffer);

                // Append size of header to headerToShow StringBuilder
                for (int i = 0; i < sizeOfHeaderbBuffer.length; i++) {
                    headerToShow.append(SceneManager.byteToBinaryString(sizeOfHeaderbBuffer[i]));
                }
            } else {
                scan.close();
                throw new IllegalArgumentException("The input file cannot be read");
            }

            // Calculate the number of bytes for the header
            int numberOfBytesForHeader;
            if (sizeOfHeader % 8 == 0) {
                numberOfBytesForHeader = sizeOfHeader / 8;
            } else {
                numberOfBytesForHeader = (sizeOfHeader / 8) + 1;
            }
            
            // StringBuilders for header and the serialized data
            StringBuilder header = new StringBuilder();
            StringBuilder serialData = new StringBuilder();
            int numberOfBytesRead = 0, counterHowManyByteReadFromHeader = 0;

            // Read the rest of the file
            while ((numberOfBytesRead = scan.read(buffer)) != -1) {
                for (int i = 0; i < numberOfBytesRead; i++) {
                    if (counterHowManyByteReadFromHeader < numberOfBytesForHeader) {
                        header.append(SceneManager.byteToBinaryString(buffer[i]));
                        counterHowManyByteReadFromHeader++;
                    } else {
                        serialData.append(SceneManager.byteToBinaryString(buffer[i]));
                    }
                }
            }
            scan.close();
            headerToShow.append(header);

            // Huffman tree reconstruction
            Stack stack = new Stack(256);
            int counter = 0, numberOfLeafNode = 0;
            while (counter < sizeOfHeader) {
                if (header.charAt(counter) == '0') {
                    counter++;
                    stack.push(new TreeNode((byte) Integer.parseInt(header.substring(counter, counter + 8), 2)));
                    numberOfLeafNode++;
                    counter += 8;
                } else {
                    counter++;
                    TreeNode node = new TreeNode(0);
                    node.setRight(stack.pop());
                    node.setLeft(stack.pop());
                    stack.push(node);
                }
            }
            TreeNode rootTreeNode = stack.peek();

            // Setting the code for the root node if it's the only node
            if (rootTreeNode.getLeft() == null && rootTreeNode.getRight() == null)
                rootTreeNode.setCode("1");
            else
                TreeNode.gaveCodeForEachByte(rootTreeNode);

            // Get all leaf nodes from the Huffman tree
            bytes = new TreeNode[numberOfLeafNode];
            rootTreeNode.getLeafNodes(bytes);

            // Determine the name for the uncompressed file
            nameOfUncompressedFile = new StringBuilder(SceneManager.replaceExtension(path.getName(), fileExtension.toString()));
            SceneManager.getUniquName(nameOfUncompressedFile);

            // Write the uncompressed data to a file
            FileOutputStream out = new FileOutputStream(nameOfUncompressedFile.toString());
            int startIndex = serialData.length() - 8;
            int numberOfEffectiveBits = Integer.parseInt(serialData.substring(startIndex), 2);
            serialData.delete(startIndex + numberOfEffectiveBits - 8, serialData.length());

            // Buffer for writing to the output file
            byte[] bufferOut = new byte[8];
            int counterForBufferSerialData = 0, counterForBufferOut = 0;

            // Process the serialized data to extract the original content
            while (counterForBufferSerialData < serialData.length()) {
                TreeNode curr = rootTreeNode;

                // Traverse the Huffman tree to find the corresponding byte
                while (curr != null && counterForBufferSerialData < serialData.length()) {
                    if (serialData.charAt(counterForBufferSerialData) == '0' && curr.hasLeft())
                        curr = curr.getLeft();
                    else if (curr.hasRight())
                        curr = curr.getRight();
                    else if (rootTreeNode.getLeft() == null && rootTreeNode.getRight() == null) {
                        counterForBufferSerialData++;
                        break;
                    } else
                        break;

                    counterForBufferSerialData++;
                }

                // Write the byte to the output buffer
                bufferOut[counterForBufferOut++] = curr.getByteContent();
                if (counterForBufferOut == 8) {
                    out.write(bufferOut);
                    counterForBufferOut = 0;
                }
            }

            // Write any remaining bytes in the buffer to the file
            if (counterForBufferOut > 0)
                out.write(bufferOut, 0, counterForBufferOut);

            out.close();

            // Check the size of the uncompressed file
            File toCheck = new File(nameOfUncompressedFile.toString());
            lengthFileAfter = toCheck.length();

        } catch (Exception e) {
            // Show an alert in case of an error
            SceneManager.showAlert("Error", e.getMessage());
        }
    }
 
	
	// Method to convert a byte array to an integer
	public static int byteArrayToInt(byte[] b) {
	    // Combines 4 bytes into an integer, assuming big-endian order
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	// Method to setup buttons for the UI
	public void setupButtons(String[] strings, Button[] buttons) {
	    
	    // Loop through the strings array to create and configure buttons
	    for (int i = 0; i < strings.length; i++) {
	        Button button = new Button(strings[i]); // Create a new button with the text from the strings array
	        buttons[i] = button; // Store the button in the buttons array
	        button.getStyleClass().add("custom-button"); // Add CSS class for styling
	        button.setPrefHeight(50); // Set preferred height
	        button.setPrefWidth(300); // Set preferred width
	    }
	    
	    // Tooltip setup for showing clipboard copy confirmation
	    Tooltip tooltip = new Tooltip("Link copied to clipboard");
	    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
	        tooltip.hide(); // Hide the tooltip after 1.5 seconds
	    }));
	    
	    // Set an action for the first button - Copying path to clipboard
	    buttons[0].setOnAction(e -> {
	        Clipboard clipboard = Clipboard.getSystemClipboard();
	        ClipboardContent content = new ClipboardContent();
	        content.putString(System.getProperty("user.dir")); // Copying the current user directory path
	        clipboard.setContent(content);

	        // Show tooltip on button click
	        if (!tooltip.isShowing()) {
	            tooltip.show(buttons[0], 70, 70);
	            timeline.playFromStart();
	        }
	    });
	    
	    // Set an action for the second button - Open uncompressed file
	    buttons[1].setOnAction(e -> {
	        SceneManager.openFolder(nameOfUncompressedFile.toString());
	    });
	    
	    // Set an action for the third button - Show header information
	    buttons[2].setOnAction(e -> {
	        SceneManager.handleShowHeader(headerToShow);
	    });
	    
	    // Set an action for the fourth button - Show Huffman table
	    buttons[3].setOnAction(e -> {      	
	        handleShowHuffmanTable();      
	    });
	    
	    // Set an action for the fifth button - Return to the main page
	    buttons[4].setOnAction(e -> {      	
	        SceneManager.setMainScene();      
	    });
	}

	// Method to display the Huffman table
	private void handleShowHuffmanTable() {
	    ScrollPane subScenePane = new ScrollPane();
	    HBox hBox = new HBox(80);
	    hBox.setAlignment(Pos.CENTER);
	    hBox.setPadding(new Insets(50));
	    hBox.setStyle("-fx-background-color: #64fc9c;");
	    
	    GridPane gridPane = new GridPane();
	    gridPane.setVgap(10); // Set vertical gap between grid rows
	    gridPane.setHgap(35); // Set horizontal gap between grid columns
	    gridPane.setPadding(new Insets(15));
	    gridPane.setAlignment(Pos.CENTER);

	    // Create and style headers for the grid
	    Label[] headers = {
	        new Label("Byte"),
	        new Label("Huffman"),
	        new Label("Length")
	    };
	    int columnIndex = 0;
	    for (Label header : headers) {
	        header.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
	        GridPane.setConstraints(header, columnIndex, 0); // Set grid position
	        GridPane.setHalignment(header, HPos.CENTER); // Center alignment
	        gridPane.getChildren().add(header);
	        columnIndex++;
	    }

	    // Add TreeNode details (byte content, Huffman code, and code length) to the GridPane
	    int rowIndex = 1;
	    for (TreeNode node : bytes) {
	        if (node.getCode() != null) {
	            Label byteLabel = new Label(node.getByteContent() + "");
	            Label codeLabel = new Label(node.getCode());
	            Label lengthLabel = new Label(node.getCode().length() + "");

	            byteLabel.setStyle("-fx-text-fill: white;");
	            codeLabel.setStyle("-fx-text-fill: white;");
	            lengthLabel.setStyle("-fx-text-fill: white;");
	            GridPane.setHalignment(byteLabel, HPos.CENTER);
	            GridPane.setHalignment(codeLabel, HPos.CENTER);
	            GridPane.setHalignment(lengthLabel, HPos.CENTER);

	            gridPane.add(byteLabel, 0, rowIndex);
	            gridPane.add(codeLabel, 1, rowIndex);
	            gridPane.add(lengthLabel, 2, rowIndex);

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
	    background.getChildren().addAll(huffmanLabel, gridPane);
	    hBox.getChildren().addAll(background);
	    subScenePane.setContent(hBox);
	    
	    // Create a new scene with the ScrollPane as its root
	    Scene scene = new Scene(subScenePane, 400, 400);
	    Stage newStage = new Stage();
	    newStage.setScene(scene);
	    hBox.prefWidthProperty().bind(newStage.widthProperty());
	    hBox.prefHeightProperty().bind(newStage.heightProperty());
	    newStage.show();
	}

}

