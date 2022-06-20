package application;
	
import controller.ConfigController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {
	static String fileArg = null;
	public static Stage Stage;
	
	@Override
	public void start(Stage primaryStage) {
		Stage = primaryStage;
		try {
			ConfigController configController = new ConfigController(fileArg);
			
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Config.fxml"));
			loader.setController(configController);
			Parent root = loader.load();
			
			Scene scene = new Scene(root,400,700);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setAlwaysOnTop(true);			
			
			primaryStage.setTitle("Configuration");
			primaryStage.setResizable(false);
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("al.png")));

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
