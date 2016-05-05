package pt.upa.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
	
	public byte[] digestMessage(String msg) throws NoSuchAlgorithmException{
		final byte[] plainBytes = msg.getBytes();
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		messageDigest.update(plainBytes);

		byte[] digest = messageDigest.digest();
		return digest;
	}
}
