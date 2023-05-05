package fly.wild.wizards.tlswizard;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class WizardApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	private AnchorPane homeLayout;
	private AnchorPane oneWayTlSLayout;
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("TLS Wizard");

        initRootLayout();
        showHomeScene();

	}

	public static void main(String[] args) {
		launch(args);
	}
	
    /**
     * Initializes the root layout.
     */
	
	 public void initRootLayout() {
		try {
	        // Load root layout from fxml file.
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(WizardApp.class.getResource("RootLayout.fxml"));
	        rootLayout = (BorderPane) loader.load();
	        
	        // Show the scene containing the root layout.
	        Scene scene = new Scene(rootLayout);
	        primaryStage.setScene(scene);
	        primaryStage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	 }
	
    public void showHomeScene () {
        
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WizardApp.class.getResource("TLSWizardView.fxml"));
            homeLayout = (AnchorPane) loader.load();
            
            rootLayout.setCenter(homeLayout);
            
        } catch (IOException e) {
        	
            e.printStackTrace();
        }
    }
    
    public void showOneWayTLSLayout () {
    	try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WizardApp.class.getResource("OneWayTLSView.fxml"));
            oneWayTlSLayout = (AnchorPane) loader.load();
            
            rootLayout.setCenter(oneWayTlSLayout);
            
        } catch (IOException e) {
        	
            e.printStackTrace();
        }
    }
}
