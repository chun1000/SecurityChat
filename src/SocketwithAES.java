import java.net.Socket;
import java.util.Vector;

public class SocketwithAES {
	public Socket socket;
	public AESCrypto aes;

	public SocketwithAES(Socket socket, AESCrypto aes) {
		this.socket = socket;
		this.aes = aes;
	}
}
