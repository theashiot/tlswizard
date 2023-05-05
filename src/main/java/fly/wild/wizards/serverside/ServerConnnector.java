package fly.wild.wizards.serverside;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import fly.wild.wizards.tlswizard.controller.OneWayTLSConfigurationConfiguration;
import fly.wild.wizards.tlswizard.controller.TLSConfiguration;

public class ServerConnnector {

	private String ipAddress;
	private final int DMR_PORT = 9999;
	
	private String distinguishedName;
	private String alias;
	
	private ModelControllerClient client;
	
	
	private final String STRING_SUBSYSTEM = "subsystem";
	private final String STRING_SUBSYSTEM_ELYTRON = "elytron";
	private final String STRING_SUBSYSTEM_UNDERTOW = "undertow";
	private final String STRING_KEY_STORE_ATTRIBUTE = "key-store";
	private final String STRING_PATH_ATTRIBUTE = "path";
	private final String STRING_RELATIVE_TO_ATTRIBUTE = "relative-to";
	private final String STRING_RELATIVE_TO_VALUE = "jboss.server.config.dir";
	private final String STRING_KEY_STORE_TYPE_ATTRIBUTE = "type";
	private final String STRING_KEY_STORE_TYPE_VALUE = "PKCS12";
	private final String STRING_CREDENTIAL_REFERENCE_ATTRIBUTE = "credential-reference";
	private final String STRING_CLEAR_TEXT_ATTRIBUTE = "clear-text";
	private final String STRING_CLEAR_TEXT_VALUE_KEY_STORE = "keystorepass";
	private final String STRING_OPERATION_GENERATE_KEY_PAIR = "generate-key-pair";
	private final String STRING_ALIAS_ATTRIBUTE = "alias";
	private final String STRING_ALGORITHM_ATTRIBUTE = "algorithm";
	private final String STRING_ALGORITHM_VALUE = "RSA";
	private final String STRING_KEY_SIZE_ATTRIBUTE = "key-size";
	private final int INT_KEY_SIZE_VALUE = 2048;
	private final String STRING_VALIDITY_ATTRIBUTE = "validity";
	private final int INT_VALIDITY_VALUE = 365;
	private final String STRING_DISTINGUISHED_NAME_ATTRIBUTE = "distinguished-name";
	private final String STRING_OPERATION_STORE = "store";
	private final String STRING_KEY_MANAGER_ATTRIBUTE = "key-manager";
	private final String stringClearTextValueKeyManager = "secret";
	private final String STRING_SERVER_SSL_CONTEXT_ATTRIBUTE = "server-ssl-context";
	private final String STRING_SERVER_ATTRIBUTE = "server";
	private final String STRING_SERVER_VALUE = "default-server";
	private final String STRING_HTTPS_LISTENER_ATTRIBUTE = "https-listener";
	private final String STRING_HTTPS_LISTENER_VALUE = "https";
	private final String STRING_NAME_ATTRIBUTE = "name";
	private final String STRING_NAME_VALUE = "ssl-context";
	private final String STRING_VALUE_ATTRIBUTE = "value";
	private final String STRING_CORE_SERVICE_ATTRIBUTE = "core-service";
	private final String STRING_CORE_SERVICE_VALUE = "management";
	private final String STRING_MANAGEMENT_INTERFACE_ATTRIBUTE = "management-interface";
	private final String STRING_MANAGEMENT_INTERFACE_VALUE = "http-interface";
	private String genKeyStoreName;
	private String genKeyManagerName;
	private String genServerSSLContext;
	private String padding;
	private String keyStoreFileName;
	
	
	private OneWayTLSConfigurationConfiguration oneWayTLSConfiguration;
	private TLSConfiguration tlsConfiguration;
	
	public ServerConnnector (TLSConfiguration tlsConfiguration, OneWayTLSConfigurationConfiguration oneWayTLSConfiguration) {
		UUID uuid=UUID.randomUUID();
		
		this.oneWayTLSConfiguration = oneWayTLSConfiguration;
		this.tlsConfiguration = tlsConfiguration;
		this.ipAddress = this.tlsConfiguration.getServerIP();
		
		this.padding = uuid.toString();
		
		this.genKeyStoreName = "wizgenks" + this.padding;
		this.genKeyManagerName = "wizgenkm" + this.padding;
		this.genServerSSLContext = "wizgenssl" + this.padding;
		
		this.keyStoreFileName = this.oneWayTLSConfiguration.getKeyStoreFileNameValue();
		
		this.alias = this.oneWayTLSConfiguration.getFirstAndLastNameValue();
		this.distinguishedName = "CN=" + this.oneWayTLSConfiguration.getFirstAndLastNameValue() +
				";" + "OU=" + this.oneWayTLSConfiguration.getOrganizationalUnitValue() +
				";" + "O=" + this.oneWayTLSConfiguration.getOrganizationValue() +
				";" + "L=" + this.oneWayTLSConfiguration.getCityOrLocalityValue() +
				";" + "ST=" + this.oneWayTLSConfiguration.getStateOrProvinceValue() +
				";" + "C=" + this.oneWayTLSConfiguration.getCountryCodeValue();
	}
	
	
	public boolean configureOneWayTLS () {
		
		boolean success = true;
		
		/*
		 * /subsystem=elytron/key-store=exampleKeyStore:add(
		 * path=exampleserver.keystore.pkcs12, 
		 * relative-to=jboss.server.config.dir,
		 * credential-reference={clear-text=secret},type=PKCS12) 
		 */
		
		ModelNode keyStore = new ModelNode();
		keyStore.get(ClientConstants.OP).set(ClientConstants.ADD);
		keyStore.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		keyStore.get(ClientConstants.OP_ADDR).add(STRING_KEY_STORE_ATTRIBUTE, this.genKeyStoreName);
		keyStore.get(STRING_PATH_ATTRIBUTE).set(keyStoreFileName); 
		keyStore.get(STRING_RELATIVE_TO_ATTRIBUTE).set(STRING_RELATIVE_TO_VALUE);
		keyStore.get(STRING_CREDENTIAL_REFERENCE_ATTRIBUTE).get(STRING_CLEAR_TEXT_ATTRIBUTE).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);
		keyStore.get(STRING_KEY_STORE_TYPE_ATTRIBUTE).set(STRING_KEY_STORE_TYPE_VALUE);

		/*
		 * /subsystem=elytron/key-store=exampleKeyStore
		 * :generate-key-pair(alias=localhost,algorithm=RSA,
		 * key-size=2048,validity=365,credential-reference=
		 * {clear-text=secret},distinguished-name="CN=localhost")
		 */
		
		ModelNode generateCertificate = new ModelNode();
		generateCertificate.get(ClientConstants.OP).set(STRING_OPERATION_GENERATE_KEY_PAIR);
		generateCertificate.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		generateCertificate.get(ClientConstants.OP_ADDR).add(STRING_KEY_STORE_ATTRIBUTE, this.genKeyStoreName);
		generateCertificate.get(STRING_ALIAS_ATTRIBUTE).set(this.alias);
		generateCertificate.get(STRING_ALGORITHM_ATTRIBUTE).set(STRING_ALGORITHM_VALUE);
		generateCertificate.get(STRING_KEY_SIZE_ATTRIBUTE).set(INT_KEY_SIZE_VALUE);
		generateCertificate.get(STRING_VALIDITY_ATTRIBUTE).set(INT_VALIDITY_VALUE);
		generateCertificate.get(STRING_CREDENTIAL_REFERENCE_ATTRIBUTE).get(STRING_CLEAR_TEXT_ATTRIBUTE).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);
		generateCertificate.get(STRING_DISTINGUISHED_NAME_ATTRIBUTE).set(this.distinguishedName);
		
		
		/*
		 * /subsystem=elytron/key-store=exampleKeyStore:store()
		 */
		ModelNode storeCertificate = new ModelNode();
		storeCertificate.get(ClientConstants.OP).set(STRING_OPERATION_STORE);
		storeCertificate.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		storeCertificate.get(ClientConstants.OP_ADDR).add(STRING_KEY_STORE_ATTRIBUTE, this.genKeyStoreName);
		
		
		
		
		/*
		 * /subsystem=elytron/key-manager=exampleKeyManager:add(
		 * key-store=exampleKeyStore,credential-reference={clear-text=secret})
		 */
		ModelNode keyManager = new ModelNode ();
		keyManager.get(ClientConstants.OP).set(ClientConstants.ADD);
		keyManager.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		keyManager.get(ClientConstants.OP_ADDR).add(STRING_KEY_MANAGER_ATTRIBUTE, this.genKeyManagerName);
		keyManager.get(STRING_KEY_STORE_ATTRIBUTE).set(this.genKeyStoreName);
		keyManager.get(STRING_CREDENTIAL_REFERENCE_ATTRIBUTE).get(STRING_CLEAR_TEXT_ATTRIBUTE).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);
		
		/*
		 * /subsystem=elytron/server-ssl-context=examplehttpsSSC:add(key-manager=exampleKeyManager, protocols=["TLSv1.2"])
		 */
		ModelNode serverSSLContext = new ModelNode ();
		serverSSLContext.get(ClientConstants.OP).set(ClientConstants.ADD);
		serverSSLContext.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		serverSSLContext.get(ClientConstants.OP_ADDR).add(STRING_SERVER_SSL_CONTEXT_ATTRIBUTE,this.genServerSSLContext);    		
		serverSSLContext.get(STRING_KEY_MANAGER_ATTRIBUTE).set(this.genKeyManagerName);		
				
		try {
			this.client = ModelControllerClient.Factory.create (InetAddress.getByName(this.ipAddress), 
					DMR_PORT);
			
			client.execute(new OperationBuilder(keyStore).build());
			client.execute(new OperationBuilder(generateCertificate).build());
			client.execute(new OperationBuilder(storeCertificate).build());
			client.execute(new OperationBuilder(keyManager).build());
			client.execute(new OperationBuilder(serverSSLContext).build());
			
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		
		
		if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.APPLICATIONS)) {
			System.out.println ("Applications selected");
			configureUndertow(); 
		}
		
		else {
			System.out.println ("Management interface selected");
			configureManagementInterface ();
		}
			

		return success;
	}
	
	private void configureUndertow () {
		
		/*
		 * /subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context, value=examplehttpsSSC) 
		 */
	
		ModelNode undertow = new ModelNode();
		undertow.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		undertow.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_UNDERTOW);
		undertow.get(ClientConstants.OP_ADDR).add(STRING_SERVER_ATTRIBUTE,STRING_SERVER_VALUE);
		undertow.get(ClientConstants.OP_ADDR).add(STRING_HTTPS_LISTENER_ATTRIBUTE,STRING_HTTPS_LISTENER_VALUE);
		undertow.get(STRING_NAME_ATTRIBUTE).set(STRING_NAME_VALUE);
		undertow.get(STRING_VALUE_ATTRIBUTE).set(this.genServerSSLContext);

		try {
			this.client = ModelControllerClient.Factory.create (InetAddress.getByName(this.ipAddress), 
					DMR_PORT);
			
			client.execute(new OperationBuilder(undertow).build());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void configureManagementInterface () {
		
		ModelNode coreServiceSSLContext = new ModelNode();
		ModelNode coreServiceSecureSocketBinding = new ModelNode();
		
		coreServiceSSLContext.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		coreServiceSSLContext.get(ClientConstants.OP_ADDR).add(STRING_CORE_SERVICE_ATTRIBUTE,STRING_CORE_SERVICE_VALUE);
		coreServiceSSLContext.get(ClientConstants.OP_ADDR).add(STRING_MANAGEMENT_INTERFACE_ATTRIBUTE,STRING_MANAGEMENT_INTERFACE_VALUE);
		coreServiceSSLContext.get(STRING_NAME_ATTRIBUTE).set("ssl-context");
		coreServiceSSLContext.get("value").set(this.genServerSSLContext);
		
		coreServiceSecureSocketBinding.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		coreServiceSecureSocketBinding.get(ClientConstants.OP_ADDR).add("core-service","management");
		coreServiceSecureSocketBinding.get(ClientConstants.OP_ADDR).add("management-interface","http-interface");
		
		coreServiceSecureSocketBinding.get("name").set("secure-socket-binding");
		coreServiceSecureSocketBinding.get("value").set("management-https");
		
		try {
			this.client = ModelControllerClient.Factory.create (InetAddress.getByName(this.ipAddress), 
					DMR_PORT);
			
			client.execute(new OperationBuilder(coreServiceSSLContext).build());
			client.execute(new OperationBuilder(coreServiceSecureSocketBinding).build());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public String getConfigurationDetails () {
		String configurationDetails = "";
		
		if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.APPLICATIONS)) {
			configurationDetails += "Reload the server now to secure your applications with TLS\n" +
									"Command to reload the server:\n *Nix: wildfly_home/bin/jboss-cli.sh --connect --command=\":reload\"\n" +
									"Windows: wildfly_home\\bin\\jboss-cli.sh --connect --command=\\\":reload\\\"" + "\n" +
									"After you reload the server, you can check TLS by navigating to https://" + this.ipAddress +":8443\n";
					
		}
		else if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.MANAGEMENT_INTERFACES)) {
			configurationDetails += "Reload the server now to secure the management interfaces with TLS\n" +
					"Command to reload the server:\n *Nix: wildfly_home/bin/jboss-cli.sh --connect --command=\":reload\"\n" +
					"Windows: wildfly_home\\bin\\jboss-cli.sh --connect --command=\\\":reload\\\"" + "\n" +
					"After you reload the server, you can check TLS by navigating to https://" + this.ipAddress + ":9993\n";
		}
			
		configurationDetails += "Configuration details:\n";
		configurationDetails += "Key store name: "+ this.genKeyStoreName + "\n" +
				"Key manager name: " + 	this.genKeyManagerName  +"\n" 
				+ "SSL context: " + this.genServerSSLContext;
		
		
		
		return configurationDetails;
	}
}
