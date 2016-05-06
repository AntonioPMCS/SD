package pt.upa.crypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyManager {

	private boolean alreadyCleanedFolder = false;
	
	/**
	 *  Generates an assymetric key par
	 *  
	 *  @param the name of the entity folder ex:ca-ws, broker-ws, transporter-ws
	 *  @param publicKeyPath
	 *  @param privateKeyPath
	 */
	public void generateRSAKeys(String entity, int nr) throws Exception {

		// generate RSA key pair
		System.out.println("Generating RSA keys ...");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair key = keyGen.generateKeyPair();

		if(entity.equals("transporter-ws")){
			byte[] pubEncoded = key.getPublic().getEncoded();
			writeKey("ca-ws", "Transporter", nr, "public", pubEncoded);
			
			byte[] privEncoded = key.getPrivate().getEncoded();
			writeKey(entity, "Transporter", nr,"private", privEncoded);
			
		}else if(entity.equals("broker-ws")){
			byte[] pubEncoded = key.getPublic().getEncoded();
			writeKey("ca-ws", "Broker", nr, "public", pubEncoded);
			
			byte[] privEncoded = key.getPrivate().getEncoded();
			writeKey(entity, "Broker", nr, "private", privEncoded);
			
		}else if(entity.equals("ca-ws")){
			byte[] privEncoded = key.getPrivate().getEncoded();
			writeKey("ca-ws", "CertificateAuthority", nr, "private", privEncoded);
			
			byte[] pubEncoded = key.getPublic().getEncoded();
			writeKey("broker-ws", "CertificateAuthority", nr, "public", pubEncoded);
			writeKey("broker-ws", "CertificateAuthority", nr, "public", pubEncoded);
			writeKey("transporter-ws", "CertificateAuthority", nr, "public", pubEncoded);
			writeKey("transporter-ws", "CertificateAuthority", nr, "public", pubEncoded);
		}
	}
	
	/**
	 * Writes a key to a folder named as the service provider -> this.name
	 * 
	 * @param directory
	 * @param type
	 * @param content
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void writeKey(String folder, String entity, int nr, String type, byte[] content) throws FileNotFoundException, IOException {
		String directoryName = entity + type + String.valueOf(nr);
		
		File dir = new File("../"+folder+"/target/classes/"+directoryName);
		
		if (!dir.exists()) {
		    System.out.println("creating directory: " + directoryName);
		    try{ dir.mkdir();} 
		    catch(SecurityException se){
		        //handle it
		    }    
		}
		else if(!alreadyCleanedFolder){
			for(File file: dir.listFiles()){
				file.delete();
				alreadyCleanedFolder = true;
			}
		}
		
		File actualFile = new File (dir, type);
		
		FileOutputStream fos = new FileOutputStream(actualFile);
		fos.write(content);
		fos.close();
	}
	
	/**
	 * Returns the public key existent in the given directory
	 * 
	 * @param publicKeyPath
	 * @return pub The public key stored in the given path
	 * @throws Exception
	 */
	public PublicKey getPublicKeyFromDirectory(String directory) throws Exception {
		byte[] encoded = readKeyFromDir(directory);
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(encoded);
		KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
		PublicKey key = keyFacPub.generatePublic(pubSpec);
		return key;
	}
	
	/**
	 * Returns the public key existent in the given directory
	 * 
	 * @param publicKeyPath
	 * @return pub The public key stored in the given path
	 * @throws Exception
	 */
	public PrivateKey getPrivateKeyFromDirectory(String directory) throws Exception {
		byte[] encoded = readKeyFromDir(directory);
		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(encoded);
		KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
		PrivateKey key = keyFacPriv.generatePrivate(privSpec);
		return key;
	}
	
	/**
	 * Extracts the key in the given directory
	 * 
	 * @param directory
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public byte[] readKeyFromDir(String directory) throws FileNotFoundException, IOException {
		File dir = new File("target/classes/"+directory);
		File keyFile = new File (dir, "public");
		FileInputStream fis = new FileInputStream(keyFile);
		byte[] content = new byte[fis.available()];
		fis.read(content);
		fis.close();
		return content;
	}
	
	/*
	 * 
	 */
	public String getStringKeyFromFile(String filename) throws IOException {
	    // Read key from file
	    String strKeyPEM = "";
	    BufferedReader br = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = br.readLine()) != null) {
	        strKeyPEM += line + "\n"; // + \n??????
	    }
	    br.close();
	    return strKeyPEM;
	}
	
	/*
	 * 
	 */
	public PublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
	    String publicKeyPEM = key;
	    publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
	    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
	    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    PublicKey publicKey = (PublicKey) kf.generatePublic(new PKCS8EncodedKeySpec(encoded));
	    return publicKey;
	}
}
