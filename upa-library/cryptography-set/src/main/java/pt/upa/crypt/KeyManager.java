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
