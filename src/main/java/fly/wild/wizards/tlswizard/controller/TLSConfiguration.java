package fly.wild.wizards.tlswizard.controller;

public class TLSConfiguration {

	public enum TLSTypes {
		ONEWAYTLS,
		TWOWAYTLS
	}
	
	public enum Secure {
		APPLICATIONS,
		MANAGEMENT_INTERFACES
	}
	
	private String serverIP;
	private TLSTypes tlsType;
	private Secure secure;

	public TLSConfiguration () {
		
	}
	
	public TLSConfiguration (String serverIP, TLSTypes tlsType, Secure secure) {
		
		this.serverIP = serverIP;
		this.tlsType = tlsType;
		this.secure = secure;
		
	}
	
	public String getServerIP () {
		return serverIP;
		
	}

	public void setServerIP (String serverIP) {
		this.serverIP = serverIP;
		
	}

	public TLSTypes getTlsType () {
		return tlsType;
		
	}

	public void setTlsType (TLSTypes tlsType) {
		this.tlsType = tlsType;
		
	}

	public Secure getSecure() {
		return secure;
		
	}

	public void setSecure(Secure secure) {
		this.secure = secure;
		
	}
	
	
}
