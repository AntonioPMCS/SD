package pt.upa.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {

	private boolean alreadyCleanedFolder = false;
	
	/**
	 *  Generates an assymetric key par
	 *  
	 *  @param publicKeyPath
	 *  @param privateKeyPath
	 */
	public void generateRSAKeys(String name) throws Exception {

		// generate RSA key pair
		System.out.println("Generating RSA keys ...");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair key = keyGen.generateKeyPair();

		System.out.println("Writing public key ...");
		System.out.println(key.getPublic().toString());
		byte[] pubEncoded = key.getPublic().getEncoded();
		writeKey(name, "public", pubEncoded);

		System.out.println("---");
		
		System.out.println("Writing private key ...");
		byte[] privEncoded = key.getPrivate().getEncoded();
		writeKey(name, "private", privEncoded);
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
	public void writeKey(String directory, String type, byte[] content) throws FileNotFoundException, IOException {
		String directoryName = directory;
		File dir = new File("target/classes/"+directoryName);
		
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

		byte[] pubEncoded = readKeyFromDir(directory);

		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
		KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
		PublicKey pub = keyFacPub.generatePublic(pubSpec);
		System.out.println(pub);
		
		return pub;
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
}
