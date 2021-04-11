import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHACrypto {
	
	public static String sha256(String msg)  throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(msg.getBytes());
		return byteToHexString(md.digest());
	}
	
	public static String byteToHexString(byte[] data) {
		StringBuilder builder = new StringBuilder();
		for(byte b : data) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
	
}