package fly.wild.wizards.tlswizard;

import java.io.IOException;

import fly.wild.wizards.serverside.ServerConnnector;
import fly.wild.wizards.tlswizard.controller.OneWayTLSConfigurationConfiguration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class OneWayTLSViewController {

	@FXML
	TextField KeyStoreFileName;
	@FXML
	TextField FirstAndLastName;
	@FXML
	TextField OrganizationalUnit;
	@FXML
	TextField Organization;
	@FXML
	TextField CityOrLocality;
	@FXML
	TextField StateOrProvince;
	@FXML
	TextField CountryCode;
	@FXML
	Label errorLabelKeyStore;
	@FXML
	TextArea result;
	@FXML
	Button configureTLSButton;
	
	OneWayTLSConfigurationConfiguration oneWayTLSConfiguration;
	
	@FXML
	private void handleButtonPreviousEvent () {
		
		try {
			App.setRoot("TLSWizardView");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@FXML
	private void handleButtonNextEvent () {
		
		oneWayTLSConfiguration = new OneWayTLSConfigurationConfiguration();
		
		if (validateOneWayTLSViewForm()) {
			
			oneWayTLSConfiguration.setKeyStoreFileNameValue(KeyStoreFileName.getText());
			
			if (!FirstAndLastName.getText().isBlank()) {
				oneWayTLSConfiguration.setFirstAndLastNameValue(FirstAndLastName.getText());
			}
			
			if (!OrganizationalUnit.getText().isBlank()) {
				oneWayTLSConfiguration.setOrganizationalUnitValue(OrganizationalUnit.getText());
			}
			
			if (!Organization.getText().isBlank()) {
				oneWayTLSConfiguration.setOrganizationValue(Organization.getText());
			}
			
			if (!CityOrLocality.getText().isBlank()) {
				oneWayTLSConfiguration.setCityOrLocalityValue(CityOrLocality.getText());
			}
			
			if (!StateOrProvince.getText().isBlank()) {
				oneWayTLSConfiguration.setStateOrProvinceValue(StateOrProvince.getText());
			}
			
			if (!CountryCode.getText().isBlank()) {
				oneWayTLSConfiguration.setCountryCodeValue(CountryCode.getText());
			}
			
			System.out.println (oneWayTLSConfiguration.toString());
			System.out.println (App.tlsConfiguration.getServerIP());
			
			ServerConnnector serverConnector = new ServerConnnector(App.tlsConfiguration, oneWayTLSConfiguration);
			boolean success = serverConnector.configureOneWayTLS();
			if (success) {
				result.setText(serverConnector.getConfigurationDetails());
				result.setVisible(true);
			}
			else {
				result.setText("Sorry something went wrong.\nCheck if your server is running and the IP addess you provided is correct.");
				result.setVisible(true);
			}
			
			configureTLSButton.setDisable(true);
		}
		
	}
	
	private boolean validateOneWayTLSViewForm () {
		
		boolean isValid = false;
		if (KeyStoreFileName.getText().isBlank()) {
			
			errorLabelKeyStore.setVisible(true);
			
		}
		
		else {
			isValid = true;
		}
		
		return isValid;
	}
}
