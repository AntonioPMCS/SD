package pt.upa.crypt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureRandomGen {

	public byte[] getRandomNumber() throws NoSuchAlgorithmException{
		
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	
		final byte array[] = new byte[16];
		random.nextBytes(array);
	
		return array;
	}
}
