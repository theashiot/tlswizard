package fly.wild.wizards.serverside;

import java.io.IOException;
import java.time.LocalTime;
import java.util.UUID;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.as.cli.ControllerAddress;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.impl.ModelControllerClientFactory;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;

import fly.wild.wizards.tlswizard.controller.OneWayTLSConfigurationConfiguration;
import fly.wild.wizards.tlswizard.controller.TLSConfiguration;

public class ServerConnnector {

	private String ipAddress;	
	private String distinguishedName;
	private String alias;
	private StringBuilder logMessage;
	private final String STRING_CLEAR_TEXT_VALUE_KEY_STORE = "keystorepass";
	private final String STRING_ALGORITHM_VALUE = "RSA";
	private final int INT_KEY_SIZE_VALUE = 2048;
	private final int INT_VALIDITY_VALUE = 365;
	private final String STRING_SERVER_VALUE = "default-server";
	private final String STRING_HTTPS_LISTENER_VALUE = "https";
	private String genKeyStoreName;
	private String genKeyManagerName;
	private String genServerSSLContext;
	private String padding;
	private String keyStoreFileName;	
	private ControllerAddress connectionAddress;
	private OneWayTLSConfigurationConfiguration oneWayTLSConfiguration;
	private TLSConfiguration tlsConfiguration;
	
	public ServerConnnector (TLSConfiguration tlsConfiguration, OneWayTLSConfigurationConfiguration oneWayTLSConfiguration) {
		LocalTime localTime = LocalTime.now();
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

		this.logMessage = new StringBuilder("This is the TLSWizard run of "+localTime.toString()+"\n");				
		connectionAddress = new ControllerAddress("remote+http",ipAddress,9990);	
		
	}
	
	
	public boolean configureOneWayTLS () {
		
		boolean success = true;
		
		// Create a key store		
		DefaultOperationRequestBuilder keyStoreBuilder = this.buildKeyStoreNode();
		// Generate a key pair
		DefaultOperationRequestBuilder generateCertificateBuilder = buildGenerateCertificateBuilder ();
		// Persist the key pair		
		DefaultOperationRequestBuilder storeCertificateBuilder = buildStoreCertificateBuilder ();
		//Create a key manager
		DefaultOperationRequestBuilder keyManagerBuilder = buikdKeyManagerBuilder ();
		//Configure SSL Context
		DefaultOperationRequestBuilder serverSSLContextBuilder = buildServerSSLContextBuilder ();
		
		try {
			
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			
			final ModelNode keytore = client.execute(keyStoreBuilder.buildRequest());
			this.logMessage.append("Creating a key store\n");
			this.logMessage.append(keytore.toString()+"\n");
			
			final ModelNode cert = client.execute(generateCertificateBuilder.buildRequest());
			this.logMessage.append("Generating certificate\n");
			this.logMessage.append(cert.toString()+"\n");
			
			final ModelNode store = client.execute(storeCertificateBuilder.buildRequest());
			this.logMessage.append("Storing certificate\n");
			this.logMessage.append(store.toString()+"\n");
			
			final ModelNode keyMan = client.execute(keyManagerBuilder.buildRequest());
			this.logMessage.append("Creating key manager\n");
			this.logMessage.append(keyMan.toString()+"\n");
			
			final ModelNode sslCon = client.execute(serverSSLContextBuilder.buildRequest());
			this.logMessage.append("Configuring SSLContext\n");
			this.logMessage.append(sslCon.toString()+"\n");			
			
		} catch (IOException | OperationFormatException e) {
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
		reloadServer ();
			
		System.out.println(this.logMessage.toString());
		return success;
	}
	
	private void configureUndertow () {
		
		/*
		 * Sample management CLI commands
		 * /subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context, value=examplehttpsSSC) 
		 */
	
		DefaultOperationRequestBuilder undertowBuilder = new DefaultOperationRequestBuilder();
		undertowBuilder.setOperationName(Util.WRITE_ATTRIBUTE);
		undertowBuilder.addNode(Util.SUBSYSTEM,Util.UNDERTOW);
		undertowBuilder.addNode(Util.SERVER,STRING_SERVER_VALUE);
		undertowBuilder.addNode(Util.HTTPS_LISTENER,STRING_HTTPS_LISTENER_VALUE);
		undertowBuilder.addProperty(Util.NAME,Util.SSL_CONTEXT);
		undertowBuilder.addProperty(Util.VALUE,this.genServerSSLContext);
		System.out.println (undertowBuilder.toString());

		try {
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			ModelNode undertow = client.execute(undertowBuilder.buildRequest());
			this.logMessage.append("Updating Undertow\n");
			this.logMessage.append(undertow.toString()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OperationFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void configureManagementInterface () {
	
		DefaultOperationRequestBuilder coreServiceSSLContextBuilder = new DefaultOperationRequestBuilder();
		coreServiceSSLContextBuilder.setOperationName(Util.WRITE_ATTRIBUTE);
		coreServiceSSLContextBuilder.addNode(Util.CORE_SERVICE,Util.MANAGEMENT);
		coreServiceSSLContextBuilder.addNode(Util.MANAGEMENT_INTERFACE,Util.HTTP_INTERFACE);
		coreServiceSSLContextBuilder.addProperty(Util.NAME,Util.SSL_CONTEXT);
		coreServiceSSLContextBuilder.addProperty(Util.VALUE,this.genServerSSLContext);
		
		DefaultOperationRequestBuilder coreServiceSecureSocketBindingBuilder = new DefaultOperationRequestBuilder();
		coreServiceSecureSocketBindingBuilder.setOperationName(Util.WRITE_ATTRIBUTE);
		coreServiceSecureSocketBindingBuilder.addNode(Util.CORE_SERVICE,Util.MANAGEMENT);
		coreServiceSecureSocketBindingBuilder.addNode(Util.MANAGEMENT_INTERFACE,Util.HTTP_INTERFACE);
		coreServiceSecureSocketBindingBuilder.addProperty(Util.NAME,Util.SECURE_SOCKET_BINDING);
		coreServiceSecureSocketBindingBuilder.addProperty(Util.VALUE,Util.MANAGEMENT_HTTPS);

		try {
			
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			ModelNode coreService = client.execute(coreServiceSSLContextBuilder.buildRequest());
			this.logMessage.append("Configuring coreServiceSSLContext services\n");
			this.logMessage.append(coreService.toString()+"\n");
			ModelNode secureSocketBinding = client.execute(coreServiceSecureSocketBindingBuilder.buildRequest());
			this.logMessage.append("Configuring secure socket binding\n");
			this.logMessage.append(secureSocketBinding.toString()+"\n");
			
		} catch (IOException | OperationFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public void reloadServer () {
		DefaultOperationRequestBuilder reloadBuilder = new DefaultOperationRequestBuilder();
		reloadBuilder.setOperationName(Util.RELOAD);
		
		ModelControllerClient client;
		try {
			client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			client.execute(reloadBuilder.buildRequest());
			this.logMessage.append("Server reloaded\n");
		} catch (IOException | OperationFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getConfigurationDetails () {
		StringBuilder configurationDetails = new StringBuilder();
		
		if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.APPLICATIONS)) {
			configurationDetails.append("Check TLS by navigating to https://" + this.ipAddress +":8443\n");
					
		}
		else if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.MANAGEMENT_INTERFACES)) {
			configurationDetails.append("Check TLS by navigating to https://" + this.ipAddress + ":9990\n");
		}
			
		configurationDetails.append("Configuration details:\n");
		configurationDetails.append("Key store name: "+ this.genKeyStoreName + "\n" +
				"Key manager name: " + 	this.genKeyManagerName  +"\n" 
				+ "SSL context: " + this.genServerSSLContext);
		
		return configurationDetails.toString();
	}
	
	DefaultOperationRequestBuilder buildKeyStoreNode () {
	
		/*
		 * Sample management CLI commands
		 * /subsystem=elytron/key-store=exampleKeyStore:add(
		 * path=exampleserver.keystore.pkcs12, 
		 * relative-to=jboss.server.config.dir,
		 * credential-reference={clear-text=keystorepass},type=PKCS12) 
		 */
		
		DefaultOperationRequestBuilder keyStoreBuilder = new DefaultOperationRequestBuilder();
		keyStoreBuilder.setOperationName(Util.ADD);
		keyStoreBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		keyStoreBuilder.addNode(Util.KEY_STORE,this.genKeyStoreName);
		keyStoreBuilder.addProperty(Util.PATH,keyStoreFileName);
		keyStoreBuilder.addProperty(Util.RELATIVE_TO,Util.JBOSS_SERVER_CONFIG_DIR);
        
		ModelNode mn = new ModelNode();
        mn.get(Util.CLEAR_TEXT).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);            
        // Because credential reference is not a String property, we need to  set its value this way
        keyStoreBuilder.getModelNode().get(Util.CREDENTIAL_REFERENCE).set(mn);
        keyStoreBuilder.addProperty(Util.TYPE,"PKCS12");
        
        return keyStoreBuilder;
	}
	
	DefaultOperationRequestBuilder buildGenerateCertificateBuilder () {
		
		/*
		 * Sample management CLI commands		 * 
		 * /subsystem=elytron/key-store=exampleKeyStore
		 * :generate-key-pair(alias=localhost,algorithm=RSA,
		 * key-size=2048,validity=365,credential-reference=
		 * {clear-text=keystorepass},distinguished-name="CN=localhost")
		 */

		DefaultOperationRequestBuilder generateCertificateBuilder = new DefaultOperationRequestBuilder();
		generateCertificateBuilder.setOperationName(Util.GENERATE_KEY_PAIR);
		generateCertificateBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		generateCertificateBuilder.addNode(Util.KEY_STORE,this.genKeyStoreName);
		generateCertificateBuilder.addProperty(Util.ALIAS,this.alias);
		generateCertificateBuilder.addProperty(Util.ALGORITHM,STRING_ALGORITHM_VALUE);
		generateCertificateBuilder.addProperty(Util.KEY_SIZE,Integer.toString(INT_KEY_SIZE_VALUE));
		generateCertificateBuilder.addProperty(Util.VALIDITY,Integer.toString(INT_VALIDITY_VALUE));
		ModelNode mn = new ModelNode();
        mn.get(Util.CLEAR_TEXT).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);            
        // Because credential reference is not a String property, we need to  set its value this way
		generateCertificateBuilder.getModelNode().get(Util.CREDENTIAL_REFERENCE).set(mn);
		generateCertificateBuilder.addProperty(Util.DISTINGUISHED_NAME,this.distinguishedName);
		
		return generateCertificateBuilder;
	}
	
	DefaultOperationRequestBuilder buildStoreCertificateBuilder () {

		/*
		 * Sample management CLI commands
		 * /subsystem=elytron/key-store=exampleKeyStore:store()
		 */

		DefaultOperationRequestBuilder storeCertificateBuilder = new DefaultOperationRequestBuilder();
		storeCertificateBuilder.setOperationName(Util.STORE);
		storeCertificateBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		storeCertificateBuilder.addNode(Util.KEY_STORE,this.genKeyStoreName);
		
		return storeCertificateBuilder;
	}

	DefaultOperationRequestBuilder buikdKeyManagerBuilder () {

		/*
		 * Sample management CLI commands
		 * /subsystem=elytron/key-manager=exampleKeyManager:add(
		 * key-store=exampleKeyStore,credential-reference={clear-text=secret})
		 */
		DefaultOperationRequestBuilder keyManagerBuilder = new DefaultOperationRequestBuilder();
		keyManagerBuilder.setOperationName(Util.ADD);
		keyManagerBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		keyManagerBuilder.addNode(Util.KEY_MANAGER,this.genKeyManagerName);
		keyManagerBuilder.addProperty(Util.KEY_STORE,this.genKeyStoreName);	
		ModelNode mn = new ModelNode();
		mn.get(Util.CLEAR_TEXT).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);
    	keyManagerBuilder.getModelNode().get(Util.CREDENTIAL_REFERENCE).set(mn);
    	
    	return keyManagerBuilder;
	}
	
	DefaultOperationRequestBuilder buildServerSSLContextBuilder () {
		/*
		 * Sample management CLI commands
		 * /subsystem=elytron/server-ssl-context=examplehttpsSSC:add(key-manager=exampleKeyManager, protocols=["TLSv1.2"])
		 */
		DefaultOperationRequestBuilder serverSSLContextBuilder = new DefaultOperationRequestBuilder();
		serverSSLContextBuilder.setOperationName(Util.ADD);
		serverSSLContextBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		serverSSLContextBuilder.addNode(Util.SERVER_SSL_CONTEXT,this.genServerSSLContext);
		serverSSLContextBuilder.addProperty(Util.KEY_MANAGER,this.genKeyManagerName);
		
		return serverSSLContextBuilder;
	}
}
