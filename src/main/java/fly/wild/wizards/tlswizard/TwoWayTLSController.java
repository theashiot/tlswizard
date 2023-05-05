package fly.wild.wizards.tlswizard;

import java.io.IOException;

import javafx.fxml.FXML;

public class TwoWayTLSController {

	@FXML
	private void handleButtonPreviousEvent () {
		
		try {
			App.setRoot("TLSWizardView");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
