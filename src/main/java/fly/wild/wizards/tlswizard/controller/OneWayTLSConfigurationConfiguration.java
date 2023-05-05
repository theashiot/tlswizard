package fly.wild.wizards.tlswizard.controller;

public class OneWayTLSConfigurationConfiguration {

	private String keyStoreFileNameValue;
	private String firstAndLastNameValue;
	private String organizationalUnitValue;
	private String organizationValue;
	private String cityOrLocalityValue;
	private String stateOrProvinceValue;
	private String countryCodeValue;
	
	public OneWayTLSConfigurationConfiguration () {
		
		this.keyStoreFileNameValue = "Unknown";
		this.firstAndLastNameValue = "Unknown";
		this.organizationalUnitValue = "Unknown";
		this.organizationValue = "Unknown";
		this.cityOrLocalityValue = "Unknown";
		this.stateOrProvinceValue = "Unknown";
		this.countryCodeValue = "Unknown";
		
	}
	
	public String getKeyStoreFileNameValue() {
		return keyStoreFileNameValue;
		
	}
	public void setKeyStoreFileNameValue(String keyStoreFileNameValue) {
		this.keyStoreFileNameValue = keyStoreFileNameValue;
		
	}
	public String getFirstAndLastNameValue() {
		return firstAndLastNameValue;
		
	}
	public void setFirstAndLastNameValue(String firstAndLastNameValue) {
		this.firstAndLastNameValue = firstAndLastNameValue;
		
	}
	public String getOrganizationalUnitValue() {
		return organizationalUnitValue;
		
	}
	public void setOrganizationalUnitValue(String organizationalUnitValue) {
		this.organizationalUnitValue = organizationalUnitValue;
		
	}
	public String getOrganizationValue() {
		return organizationValue;
		
	}
	public void setOrganizationValue(String organizationValue) {
		this.organizationValue = organizationValue;
		
	}
	public String getCityOrLocalityValue() {
		return cityOrLocalityValue;
		
	}
	public void setCityOrLocalityValue(String cityOrLocalityValue) {
		this.cityOrLocalityValue = cityOrLocalityValue;
		
	}
	public String getStateOrProvinceValue() {
		return stateOrProvinceValue;
		
	}
	public void setStateOrProvinceValue(String stateOrProvinceValue) {
		this.stateOrProvinceValue = stateOrProvinceValue;
		
	}
	public String getCountryCodeValue() {
		return countryCodeValue;
		
	}
	public void setCountryCodeValue(String countryCodeValue) {
		this.countryCodeValue = countryCodeValue;
		
	}
	
	@Override
	public String toString () {
		String result = "";
		
		result += this.keyStoreFileNameValue + ", " + this.firstAndLastNameValue +
				", " + this.organizationalUnitValue + ", " + this.organizationValue +
				", " + this.cityOrLocalityValue + ", " + this.stateOrProvinceValue +
				", " + this.countryCodeValue;
		
		return result;
	}
	
}
