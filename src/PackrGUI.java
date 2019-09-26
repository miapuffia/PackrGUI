import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class PackrGUI extends Application {
	String mainclass, executable, output, jvm, params;
	TextArea consoleTextArea = new TextArea();
	Button runButton = new Button("RUN");
	
	Service<Void> service = new Service<Void>() {
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", params);
					builder.redirectErrorStream(true);
					
					try {
						Process p = builder.start();
						
						BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line;
						
						while (true) {
							line = r.readLine();
							
							if(line == null) {
								runButton.setDisable(false);
								cancel();
								return null;
							}
							
							consoleTextArea.setText(consoleTextArea.getText() + "\n" + line);
						}
					} catch(IOException ioe) {
						cancel();
						return null;
					}
				}
			};
		}
	};
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		Label platformsLabel = new Label("Platform:");
		platformsLabel.setFont(new Font(15));
		
		Region platformRegion = new Region();
		HBox.setHgrow(platformRegion, Priority.ALWAYS);
		
		ObservableList<String> platforms = FXCollections.observableArrayList(
		    "windows32",
		    "windows64",
		    "linux32",
		    "linux64",
		    "mac"
		);
		
		ComboBox<String> platformsComboBox = new ComboBox<String>(platforms);
		platformsComboBox.setPrefWidth(200);
		platformsComboBox.setStyle("-fx-font-size: 15px;");
		
		HBox platformHBox = new HBox(10, platformsLabel, platformRegion, platformsComboBox);
		platformHBox.setAlignment(Pos.CENTER);
		
		Button jdkDirButton = new Button("Browse for JDK directory (root folder)");
		jdkDirButton.setFont(new Font(15));
		
		Button jdkZipButton = new Button("Browse for JDK zip file");
		jdkZipButton.setFont(new Font(15));
		
		Label jdkURL = new Label("You can also type the URL of a JDK zip file");
		jdkURL.setFont(new Font(15));
		
		VBox jdkVBox = new VBox(10, jdkDirButton, jdkZipButton, jdkURL);
		
		Region jdkRegion = new Region();
		HBox.setHgrow(jdkRegion, Priority.ALWAYS);
		
		TextField jdkTextField = new TextField();
		jdkTextField.setPrefWidth(400);
		jdkTextField.setFont(new Font(15));
		jdkTextField.setPromptText("Location of JDK");
		
		jdkDirButton.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			
			File selectedDirectory = directoryChooser.showDialog(primaryStage);
			
			if(selectedDirectory != null) {
				jdkTextField.setText(selectedDirectory.getAbsolutePath());
			}
		});
		
		jdkZipButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Zip archive file", "*.zip"));
			
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			
			if(selectedFile != null) {
				jdkTextField.setText(selectedFile.getAbsolutePath());
			}
		});
		
		HBox jdkHBox = new HBox(10, jdkVBox, jdkRegion, jdkTextField);
		jdkHBox.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2))));
		jdkHBox.setPadding(new Insets(10, 10, 10, 10));
		jdkHBox.setAlignment(Pos.CENTER);
		
		Button classpathButton = new Button("Browse for jar to pack");
		classpathButton.setFont(new Font(15));
		
		Region classpathRegion = new Region();
		HBox.setHgrow(classpathRegion, Priority.ALWAYS);
		
		TextField classpathTextField = new TextField();
		classpathTextField.setPrefWidth(400);
		classpathTextField.setFont(new Font(15));
		classpathTextField.setPromptText("Location of jar");
		
		classpathButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Java-archive", "*.jar"));
			
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			
			if(selectedFile != null) {
				classpathTextField.setText(selectedFile.getAbsolutePath());
			}
		});
		
		HBox classpathHBox = new HBox(classpathButton, classpathRegion, classpathTextField);
		classpathHBox.setAlignment(Pos.CENTER);
		
		CheckBox removeLibsCheckBox = new CheckBox("Remove libraries for other platforms from jar");
		removeLibsCheckBox.setFont(new Font(15));
		
		Label mainclassLabel = new Label("Name of the main class ('.' as deliminator: )");
		mainclassLabel.setFont(new Font(15));
		
		Region mainclassRegion = new Region();
		HBox.setHgrow(mainclassRegion, Priority.ALWAYS);
		
		TextField mainclassTextField = new TextField();
		mainclassTextField.setPrefWidth(400);
		mainclassTextField.setFont(new Font(15));
		mainclassTextField.setPromptText("Main class name");
		
		HBox mainclassHBox = new HBox(mainclassLabel, mainclassRegion, mainclassTextField);
		mainclassHBox.setAlignment(Pos.CENTER);
		
		VBox jarVBox = new VBox(10, classpathHBox, removeLibsCheckBox, mainclassHBox);
		jarVBox.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2))));
		jarVBox.setPadding(new Insets(10, 10, 10, 10));
		
		Label executableLabel = new Label("Name of executable (without extension):");
		executableLabel.setFont(new Font(15));
		
		Region executableRegion = new Region();
		HBox.setHgrow(executableRegion, Priority.ALWAYS);
		
		TextField executableTextField = new TextField();
		executableTextField.setFont(new Font(15));
		executableTextField.setPrefWidth(250);
		executableTextField.setPromptText("Name of executable");
		
		HBox executableHBox = new HBox(executableLabel, executableRegion, executableTextField);
		executableHBox.setAlignment(Pos.CENTER);
		
		Button outputButton = new Button("Browse for output directory");
		outputButton.setFont(new Font(15));
		
		Region outputRegion = new Region();
		HBox.setHgrow(outputRegion, Priority.ALWAYS);
		
		TextField outputTextField = new TextField();
		outputTextField.setPrefWidth(400);
		outputTextField.setFont(new Font(15));
		outputTextField.setPromptText("Location of output");
		
		outputButton.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			
			File selectedDirectory = directoryChooser.showDialog(primaryStage);
			
			if(selectedDirectory != null) {
				outputTextField.setText(selectedDirectory.getAbsolutePath());
			}
		});
		
		HBox outputHBox = new HBox(outputButton, outputRegion, outputTextField);
		outputHBox.setAlignment(Pos.CENTER);
		
		Label executableWarningLabel = new Label("WARNING! Packr will try to 'clean up' files and folders after running.\nTo prevent any data loss, the output location should be a folder that doesn't exist yet\nPackr will try to make the non-existant folder and proceed.\nE.g. C:\\Users\\user\\Documents\\FolderThatDoesntExistYet");
		executableWarningLabel.setFont(new Font(15));
		executableWarningLabel.setTextFill(Color.RED);
		
		VBox outputVBox = new VBox(10, executableHBox, outputHBox, executableWarningLabel);
		outputVBox.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2))));
		outputVBox.setPadding(new Insets(10, 10, 10, 10));
		
		Label vmargsLabel = new Label("JVM arguments list (without leading dashes):");
		vmargsLabel.setFont(new Font(15));
		
		Region vmargsRegion = new Region();
		HBox.setHgrow(vmargsRegion, Priority.ALWAYS);
		
		TextField vmargsTextField = new TextField();
		vmargsTextField.setFont(new Font(15));
		vmargsTextField.setPrefWidth(250);
		vmargsTextField.setPromptText("JVM arguments list (optional)");
		
		HBox vmargsHBox = new HBox(vmargsLabel, vmargsRegion, vmargsTextField);
		vmargsHBox.setAlignment(Pos.CENTER);
		
		runButton.setFont(new Font(20));
		
		StackPane runStackPane = new StackPane(runButton);
		
		VBox tab1VBox = new VBox(10, platformHBox, jdkHBox, jarVBox, outputVBox, vmargsHBox, runStackPane);
		tab1VBox.setPadding(new Insets(10, 10, 10, 10));
		
		Tab mainTab = new Tab("Main", tab1VBox);
		mainTab.setClosable(false);
		mainTab.setStyle("-fx-font-size: 15px;");
		
		consoleTextArea.setEditable(false);
		
		Tab consoleTab = new Tab("Output", consoleTextArea);
		consoleTab.setClosable(false);
		consoleTab.setStyle("-fx-font-size: 15px;");
		
		Label aboutLabel = new Label();
		aboutLabel.setFont(new Font(15));
		aboutLabel.setWrapText(true);
		
		Hyperlink libgdxGitHub = new Hyperlink("libgdx GitHub");
		libgdxGitHub.setFont(new Font(15));
		
		libgdxGitHub.setOnAction(e -> {
			getHostServices().showDocument("https://github.com/libgdx");
		});
		
		Hyperlink myGitHub = new Hyperlink("Robert D. Rioja GitHub");
		myGitHub.setFont(new Font(15));
		
		myGitHub.setOnAction(e -> {
			getHostServices().showDocument("https://github.com/miapuffia");
		});
		
		Label regionLabel = new Label("");
		regionLabel.setFont(new Font(15));
		
		Hyperlink packrGitHub = new Hyperlink("Packr documentation");
		packrGitHub.setFont(new Font(15));
		
		packrGitHub.setOnAction(e -> {
			getHostServices().showDocument("https://github.com/libgdx/packr");
		});
		
		VBox aboutVBox = new VBox(aboutLabel, libgdxGitHub, myGitHub, regionLabel, packrGitHub);
		aboutVBox.setPadding(new Insets(10, 10, 10, 10));
		
		Tab aboutTab = new Tab("About", aboutVBox);
		aboutTab.setClosable(false);
		aboutTab.setStyle("-fx-font-size: 15px;");
		
		TabPane mainTabPane = new TabPane(mainTab, consoleTab, aboutTab);
		
		Scene scene = new Scene(mainTabPane);
		
		runButton.setOnAction(e -> {
			if(!service.isRunning()) {
				consoleTextArea.setText("");
				
				if(platformsComboBox.getValue() == null) {
					QuickAlert.show(AlertType.WARNING, "No platform selected", "You must select which platform to use.");
					return;
				}
				
				if(jdkTextField.getText().equals("")) {
					QuickAlert.show(AlertType.WARNING, "No JDK provided", "You must provide a location to a JDK directory, zip file, or URL.");
					return;
				}
				
				if(classpathTextField.getText().equals("")) {
					QuickAlert.show(AlertType.WARNING, "No jar file provided", "You must provide a location to the jar file you wish to pack.");
					return;
				}
				
				if(mainclassTextField.getText().equals("")) {
					QuickAlert.show(AlertType.WARNING, "No main class given", "You must provide the full qualified name of the main class using '.' to delimit package names.");
					return;
				}
				
				if(executableTextField.getText().equals("")) {
					QuickAlert.show(AlertType.WARNING, "No executable name given", "You must provide a name for the executable without the platform specific file extension.");
					return;
				}
				
				if(outputTextField.getText().equals("")) {
					QuickAlert.show(AlertType.WARNING, "No output location given", "You must provide a directory to save the outputed files to. WARNING! Make certain that the output folder is a new folder (doesn't exist yet)! Packr will make the folder. Otherwise Packr will 'clean up' unrelated files and data will be lost!");
					return;
				}
				
				params = "cd res && java -jar packr.jar --platform " + platformsComboBox.getValue() + " --jdk \"" + jdkTextField.getText() + "\" --executable \"" + executableTextField.getText() + "\" --classpath \"" + classpathTextField.getText() + "\"";
				
				if(removeLibsCheckBox.isSelected()) {
					params += " --removelibs \"" + classpathTextField.getText() + "\"";
				}
				
				params += " --mainclass \"" + mainclassTextField.getText() + "\"";
				
				if(!vmargsTextField.getText().equals("")) {
					params += " --vmargs \"" + vmargsTextField.getText() + "\"";
				}
				
				params += " --output \"" + outputTextField.getText() + "\"";
				System.out.println(params);
				
				runButton.setDisable(true);
				
				mainTabPane.getSelectionModel().select(consoleTab);
				
				if(service.getState() == Worker.State.READY) {
					service.start();
				} else if(service.getState() != Worker.State.SCHEDULED) {
					service.restart();
				}
			}
		});
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("PackrGUI");
		primaryStage.show();
		
		aboutLabel.setText("This little GUI application uses Packr.jar to take a jar java program and turn it into a native system executable. The executable still requires the JRE, but since Packr includes the it with the executable the result is a seemlessly native, portable program.\n\nPackr.jar credit:\tlibgdx\nPackrGUI credit:\tRobert D. Rioja\n\n");
	}
}
