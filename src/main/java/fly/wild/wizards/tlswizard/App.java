package fly.wild.wizards.tlswizard;

import java.io.IOException;

import fly.wild.wizards.tlswizard.controller.TLSConfiguration;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
	//private ObservableList personData = FXCollections.observableArrayList();
	
	private static Scene scene;
	public static TLSConfiguration tlsConfiguration = new TLSConfiguration ();

    /*
	public App () {
    	
    }
    */
	
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("TLSWizardView"));
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
    
    
}
