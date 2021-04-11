import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
 
public class RSACrypto {
	
	private Key publicKey;
	private Key privateKey;
	
    public RSACrypto(int keyBits) throws NoSuchAlgorithmException {
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keyBits);
        
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }
    
    public static String encrypt(Key publicKey, String plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {	 
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        byte[] encrypted = new byte[2048];
        encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        String enStr = new String(Base64.encodeBase64(encrypted));
        
        return enStr;
    }
    
    public static String decrypt(Key privateKey, String cryptText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    	Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    	cipher.init(Cipher.DECRYPT_MODE, privateKey);
    	
        byte[] byteStr = Base64.decodeBase64(cryptText.getBytes());
        String deStr = new String(cipher.doFinal(byteStr),"UTF-8");
        
        return deStr;
    }
    
    public Key getPrivate() {
    	return privateKey;
    }

	public Object getPublic() {
		return publicKey;
	}
}