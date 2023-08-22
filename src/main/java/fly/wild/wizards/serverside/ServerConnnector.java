package fly.wild.wizards.serverside;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
/*
*/
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
/*
*/

import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.ControllerAddress;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.impl.ModelControllerClientFactory;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.protocol.StreamUtils;
import org.jboss.dmr.ModelNode;
/*
 * 
 */
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
	
	private ControllerAddress connectionAddress;
	
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


		connectionAddress = new ControllerAddress("remote+http", "localhost", 9990);
		System.out.println (connectionAddress.getProtocol());
		System.out.println("connecting to " + connectionAddress.getProtocol() + "://" + connectionAddress.getHost() + ":" + connectionAddress.getPort());
	
		
	}
	
	
	public boolean configureOneWayTLS () {
		
		boolean success = true;
		
		/*
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

        /*
         * old code
		ModelNode keyStore = new ModelNode();
		keyStore.get(ClientConstants.OP).set(ClientConstants.ADD);
		keyStore.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		keyStore.get(ClientConstants.OP_ADDR).add(STRING_KEY_STORE_ATTRIBUTE, this.genKeyStoreName);
		keyStore.get(STRING_PATH_ATTRIBUTE).set(keyStoreFileName); 
		keyStore.get(STRING_RELATIVE_TO_ATTRIBUTE).set(STRING_RELATIVE_TO_VALUE);
		keyStore.get(STRING_CREDENTIAL_REFERENCE_ATTRIBUTE).get(STRING_CLEAR_TEXT_ATTRIBUTE).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);
		keyStore.get(STRING_KEY_STORE_TYPE_ATTRIBUTE).set(STRING_KEY_STORE_TYPE_VALUE);
		*/

		/*
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
		//Because we are not setting a different value for the password, we are reusing the already created ModelNode
		generateCertificateBuilder.getModelNode().get(Util.CREDENTIAL_REFERENCE).set(mn);
		generateCertificateBuilder.addProperty(Util.DISTINGUISHED_NAME,this.distinguishedName);

		/*
		 old code
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
		
		*/
		/*
		 * /subsystem=elytron/key-store=exampleKeyStore:store()
		 */
		
		DefaultOperationRequestBuilder storeCertificateBuilder = new DefaultOperationRequestBuilder();
		storeCertificateBuilder.setOperationName(Util.STORE);
		storeCertificateBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		storeCertificateBuilder.addNode(Util.KEY_STORE,this.genKeyStoreName);
		
		/*
		 * old code
		ModelNode storeCertificate = new ModelNode();
		storeCertificate.get(ClientConstants.OP).set(STRING_OPERATION_STORE);
		storeCertificate.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		storeCertificate.get(ClientConstants.OP_ADDR).add(STRING_KEY_STORE_ATTRIBUTE, this.genKeyStoreName);
		*/
		
		
		
		/*
		 * /subsystem=elytron/key-manager=exampleKeyManager:add(
		 * key-store=exampleKeyStore,credential-reference={clear-text=secret})
		 */
		
		DefaultOperationRequestBuilder keyManagerBuilder = new DefaultOperationRequestBuilder();
		keyManagerBuilder.setOperationName(Util.ADD);
		keyManagerBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		keyManagerBuilder.addNode(Util.KEY_MANAGER,this.genKeyManagerName);
		keyManagerBuilder.addProperty(Util.KEY_STORE,this.genKeyStoreName);	
		//Because we are not setting a different value for the password, we are reusing the already created ModelNode
		keyManagerBuilder.getModelNode().get(Util.CREDENTIAL_REFERENCE).set(mn);
	
		/*
		 * Old code
		ModelNode keyManager = new ModelNode ();
		keyManager.get(ClientConstants.OP).set(ClientConstants.ADD);
		keyManager.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		keyManager.get(ClientConstants.OP_ADDR).add(STRING_KEY_MANAGER_ATTRIBUTE, this.genKeyManagerName);
		keyManager.get(STRING_KEY_STORE_ATTRIBUTE).set(this.genKeyStoreName);
		keyManager.get(STRING_CREDENTIAL_REFERENCE_ATTRIBUTE).get(STRING_CLEAR_TEXT_ATTRIBUTE).set(STRING_CLEAR_TEXT_VALUE_KEY_STORE);
		*/
		
		/*
		 * /subsystem=elytron/server-ssl-context=examplehttpsSSC:add(key-manager=exampleKeyManager, protocols=["TLSv1.2"])
		 */
		DefaultOperationRequestBuilder serverSSLContextBuilder = new DefaultOperationRequestBuilder();
		serverSSLContextBuilder.setOperationName(Util.ADD);
		serverSSLContextBuilder.addNode(Util.SUBSYSTEM,Util.ELYTRON);
		serverSSLContextBuilder.addNode(Util.SERVER_SSL_CONTEXT,this.genServerSSLContext);
		serverSSLContextBuilder.addProperty(Util.KEY_MANAGER,this.genKeyManagerName);
		/*
		 * old code
		 
		ModelNode serverSSLContext = new ModelNode ();
		serverSSLContext.get(ClientConstants.OP).set(ClientConstants.ADD);
		serverSSLContext.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_ELYTRON);
		serverSSLContext.get(ClientConstants.OP_ADDR).add(STRING_SERVER_SSL_CONTEXT_ATTRIBUTE,this.genServerSSLContext);    		
		serverSSLContext.get(STRING_KEY_MANAGER_ATTRIBUTE).set(this.genKeyManagerName);		
		*/
		
		try {
			
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			final ModelNode keytore = client.execute(keyStoreBuilder.buildRequest());
			System.out.println ("Creating keystore");
			System.out.println(keytore.toString());
			
			final ModelNode cert = client.execute(generateCertificateBuilder.buildRequest());
			System.out.println ("Generating certificate");
			System.out.println(cert.toString());
			
			final ModelNode store = client.execute(storeCertificateBuilder.buildRequest());
			System.out.println ("Storing certificate");
			System.out.println(store.toString());
			
			final ModelNode keyMan = client.execute(keyManagerBuilder.buildRequest());
			System.out.println ("Creating keymanager");
			System.out.println(keyMan.toString());
			
			final ModelNode sslCon = client.execute(serverSSLContextBuilder.buildRequest());
			System.out.println ("Configuring SSLContext");
			System.out.println(sslCon.toString());
			/*
			 * old code
			this.client = ModelControllerClient.Factory.create (InetAddress.getByName(this.ipAddress), 
					DMR_PORT);
			
			client.execute(new OperationBuilder(keyStore).build());
			client.execute(new OperationBuilder(generateCertificate).build());
			client.execute(new OperationBuilder(storeCertificate).build());
			client.execute(new OperationBuilder(keyManager).build());
			client.execute(new OperationBuilder(serverSSLContext).build());
			*/
			
			
		} catch (IOException | OperationFormatException e) {
			success = false;
			e.printStackTrace();
		}
		
		
		if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.APPLICATIONS)) {
			System.out.println ("Applications selected");
			configureUndertow(); 
			reloadServer ();
		}
		
		else {
			System.out.println ("Management interface selected");
			configureManagementInterface ();
			reloadServer ();
		}
			

		return success;
	}
	
	private void configureUndertow () {
		
		/*
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
		/*
		 * old code
		ModelNode undertow = new ModelNode();
		undertow.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		undertow.get(ClientConstants.OP_ADDR).add(STRING_SUBSYSTEM, STRING_SUBSYSTEM_UNDERTOW);
		undertow.get(ClientConstants.OP_ADDR).add(STRING_SERVER_ATTRIBUTE,STRING_SERVER_VALUE);
		undertow.get(ClientConstants.OP_ADDR).add(STRING_HTTPS_LISTENER_ATTRIBUTE,STRING_HTTPS_LISTENER_VALUE);
		undertow.get(STRING_NAME_ATTRIBUTE).set(STRING_NAME_VALUE);
		undertow.get(STRING_VALUE_ATTRIBUTE).set(this.genServerSSLContext);
		 */
		try {
			/*
			 * old code
			this.client = ModelControllerClient.Factory.create (InetAddress.getByName(this.ipAddress), 
					DMR_PORT);
			
			client.execute(new OperationBuilder(undertow).build());
			*/
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			ModelNode undertow = client.execute(undertowBuilder.buildRequest());
			System.out.println ("Updating Undertow");
			System.out.println (undertow.toString());
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
		/*
		 *old code
		 
		ModelNode coreServiceSSLContext = new ModelNode();
		coreServiceSSLContext.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		coreServiceSSLContext.get(ClientConstants.OP_ADDR).add(STRING_CORE_SERVICE_ATTRIBUTE,STRING_CORE_SERVICE_VALUE);
		coreServiceSSLContext.get(ClientConstants.OP_ADDR).add(STRING_MANAGEMENT_INTERFACE_ATTRIBUTE,STRING_MANAGEMENT_INTERFACE_VALUE);
		coreServiceSSLContext.get(STRING_NAME_ATTRIBUTE).set("ssl-context");
		coreServiceSSLContext.get("value").set(this.genServerSSLContext);
		*/
		
		DefaultOperationRequestBuilder coreServiceSecureSocketBindingBuilder = new DefaultOperationRequestBuilder();
		coreServiceSecureSocketBindingBuilder.setOperationName(Util.WRITE_ATTRIBUTE);
		coreServiceSecureSocketBindingBuilder.addNode(Util.CORE_SERVICE,Util.MANAGEMENT);
		coreServiceSecureSocketBindingBuilder.addNode(Util.MANAGEMENT_INTERFACE,Util.HTTP_INTERFACE);
		coreServiceSecureSocketBindingBuilder.addProperty(Util.NAME,Util.SECURE_SOCKET_BINDING);
		coreServiceSecureSocketBindingBuilder.addProperty(Util.VALUE,Util.MANAGEMENT_HTTPS);
				
		/*
		 * old code
		ModelNode coreServiceSecureSocketBinding = new ModelNode();
		coreServiceSecureSocketBinding.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		coreServiceSecureSocketBinding.get(ClientConstants.OP_ADDR).add("core-service","management");
		coreServiceSecureSocketBinding.get(ClientConstants.OP_ADDR).add("management-interface","http-interface");
		
		coreServiceSecureSocketBinding.get("name").set("secure-socket-binding");
		coreServiceSecureSocketBinding.get("value").set("management-https");
		*/
		try {
			
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			ModelNode coreService = client.execute(coreServiceSSLContextBuilder.buildRequest());
			System.out.println ("Configuring Core services");
			System.out.println (coreService.toString());
			ModelNode secureSocketBinding = client.execute(coreServiceSecureSocketBindingBuilder.buildRequest());
			System.out.println ("Configuring secure socket binding");
			System.out.println (secureSocketBinding.toString());
			
			/*
			 * old code
			this.client = ModelControllerClient.Factory.create (InetAddress.getByName(this.ipAddress), 
					DMR_PORT);
			
			client.execute(new OperationBuilder(coreServiceSSLContext).build());
			client.execute(new OperationBuilder(coreServiceSecureSocketBinding).build());
			*/
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
			System.out.println ("Server reloaded");
		} catch (IOException | OperationFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public String getConfigurationDetails () {
		String configurationDetails = "";
		
		if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.APPLICATIONS)) {
			configurationDetails += "Check TLS by navigating to https://" + this.ipAddress +":8443\n";
					
		}
		else if (tlsConfiguration.getSecure().equals(TLSConfiguration.Secure.MANAGEMENT_INTERFACES)) {
			configurationDetails += "Check TLS by navigating to https://" + this.ipAddress + ":9990\n";
		}
			
		configurationDetails += "Configuration details:\n";
		configurationDetails += "Key store name: "+ this.genKeyStoreName + "\n" +
				"Key manager name: " + 	this.genKeyManagerName  +"\n" 
				+ "SSL context: " + this.genServerSSLContext;
		
		
		
		return configurationDetails;
	}
}
