import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

// 키보드로 전송문자열 입력받아 서버로 전송하는 스레드

class WriteThread {

	Socket socket;
	ClientFrame cf;
	String str;
	String user_id;
	String user_password;
	AESCrypto aes;
	boolean cipher_check = false;

	public WriteThread(ClientFrame cf) {
		this.cf = cf;
		this.socket = cf.socket;
		this.aes = cf.aes;
	}

	public void sendMsg() throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(socket.getOutputStream(), true);
			if (cf.isFirst == true) {
				System.out.println("First session Connected");
				InetAddress iaddr = socket.getLocalAddress();
				String ip = iaddr.getHostAddress();
				this.getUserStatus();
				System.out.println(user_password);
				pw.println("LoginRequest_" + user_id + "_" + user_password);
				System.out.println("ip:" + ip + "id:" + user_id);
				str = "[" + user_id + "] 님 로그인 (" + ip + ")";
			} else {
				String plainText = cf.txtF.getText();
				if (this.cipher_check == false) {
					str = "[" + user_id + "]: " + plainText;
					pw.println("Message_" + str);
				} else if (this.cipher_check == true) {
					String cryptText = aes.aesEncode(plainText);
					str = "[" + user_id + "]: " + cryptText;
					pw.println("Cipher_" + str);
				}
			}

		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ie) {
				System.out.println(ie.getMessage());
			}
		}
	}

	public void getUserStatus() {
		user_id = Id.getId();
		user_password = Id.getPW();

	}
}

class ReadThread extends Thread {

	Socket socket;
	ClientFrame cf;
	AESCrypto aes;

	public ReadThread(Socket socket, ClientFrame cf, AESCrypto aes) {
		this.cf = cf;
		this.socket = socket;
		this.aes = aes;
	}

	public void run() {

		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (true) {
				Date today = new Date();
				SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
				SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
				String date_time = "[" + date.format(today) + " " + time.format(today) + "] ";
				String str = br.readLine();
				if (str == null) {
					System.out.println(date_time + "Disconnected");
					break;
				} else if (str.equals("success")) {
					cf.isFirst = false;
					cf.setVisible(true);
					cf.id.dispose();
					String id = Id.getId();
					cf.lblMyId.setText("내 아이디: " + id);
				} else if (str.equals("fail")) {
					System.out.println(date_time + "login failed");
				} else {
					// 전송받은 문자열 화면에 출력
					String[] splstr = str.split(" ", 3);
					
					if (splstr[0].equals("Cipher")) {
						String decoded = aes.aesDecode(splstr[2]);
						cf.txtA.append(date_time + splstr[1] + "[cipherMSG]" + splstr[2] + "\n");
						cf.txtA.append(date_time + splstr[1] + " " + decoded + "\n");
						System.out.println(date_time + "[server] " + splstr[1] + " " + splstr[2]);
					}else{
						System.out.println(date_time + "[server] " + splstr[1] + " " + splstr[2]);
						cf.txtA.append(date_time + splstr[1] + " " + splstr[2] + "\n");
					}
					
				}

			}

		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (socket != null)
					socket.close();
			} catch (IOException ie) {
			}

		}

	}

}

public class MultiChatClient {

	public static void main(String[] args) throws ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Socket socket = null;
		ClientFrame cf;

		ObjectInputStream ois;
		ObjectOutputStream oos;

		try {
			socket = new Socket("127.0.0.1", 3000);
			System.out.println("연결성공!");

			StringBuffer sb = new StringBuffer();
			Random rnd = new Random();
			for (int i = 0; i < 16; i++) {
				sb.append((char) ((int) rnd.nextInt(94) + 33));
			}

			ois = new ObjectInputStream(socket.getInputStream());
			Key publicKey = (Key) ois.readObject();

			String key = sb.toString();
			AESCrypto aes128 = new AESCrypto(key);
			key = RSACrypto.encrypt(publicKey, key);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(key);
			oos.flush();

			cf = new ClientFrame(socket, aes128);
			new ReadThread(socket, cf, aes128).start();
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}

	}

}
