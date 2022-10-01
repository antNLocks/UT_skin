package application;
	
import controller.ConfigController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;


public class Main extends Application {
	static String fileArg = null;
	public static Stage Stage;
	
	@Override
	public void start(Stage primaryStage) {
		Stage = primaryStage;
		try {
			
			//There are some issues with fonts and OS. It is therefore best to import a specific font explicitly.
			Font.loadFont(getClass().getClassLoader().getResource("resources/fonts/my_Arial_.ttf").toExternalForm(), 10);
			Font.loadFont(getClass().getClassLoader().getResource("resources/fonts/my_Arial_-Italic.ttf").toExternalForm(), 10);
			Font.loadFont(getClass().getClassLoader().getResource("resources/fonts/my_Arial_-Bold.ttf").toExternalForm(), 10);
			Font.loadFont(getClass().getClassLoader().getResource("resources/fonts/my_Arial_-Bold-Italic.ttf").toExternalForm(), 10);
			
			ConfigController configController = new ConfigController(fileArg);
			
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("FXML/Config.fxml"));
			loader.setController(configController);
			Parent root = loader.load();
			
			Scene scene = new Scene(root,400,710);
			primaryStage.setScene(scene);
			primaryStage.setAlwaysOnTop(true);			
			
			primaryStage.setTitle("Configuration");
			primaryStage.setResizable(false);
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/images/logo.png")));

			primaryStage.setOnCloseRequest(e -> Platform.exit());
			primaryStage.show();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length > 0)
			fileArg = args[0];
		
		launch(args);		
	}
}
