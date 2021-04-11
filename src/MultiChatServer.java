import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.InvalidKeyException;

//클라이언트로 부터 전송된 문자열을 받아서 다른 클라이언트에게 문자열을
//보내주는 스레드

class EchoThread extends Thread {

	Socket socket;
	AESCrypto aes;
	SocketwithAES swa;
	Vector<SocketwithAES> swas;

	public EchoThread(Socket socket, AESCrypto aes, Vector<SocketwithAES> swas) {
		this.socket = socket;
		this.aes = aes;
		this.swa = new SocketwithAES(socket, aes);
		this.swas = swas;
	}

	public void run() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String str = null;
			while (true) {
				// 클라이언트로 부터 문자열 받기
				str = br.readLine();
				// 상대가 접속을 끊으면 break;
				if (str == null) {
					// 벡터에서 없애기
					swas.remove(swa);
					break;
				}
				Date today = new Date();
				SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
				SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
				String date_time = "[" + date.format(today) + " " + time.format(today) + "] ";
				
				StringTokenizer st = new StringTokenizer(str,"_");
				String type=st.nextToken().toString();
				if(type.equals("LoginRequest")){
					String id=st.nextToken().toString();
					String password= DBManager.get_salt_from_DB(id)+st.nextToken().toString();
					password = SHACrypto.sha256(password);
					int result=DBManager.get_table_from_DB(id, password);
					if(result>=1){
						//login시 메세지를 서버가 클라이언트로 보냅니다.
						System.out.println(date_time+"["+type+"]"+"Client "+id +"'s RSA password : "+password);
						sendMsg_login("success");
						continue;
					}else{
						System.out.println(date_time+"Database login fail");
						//실패시 action
						sendMsg_login("fail");
						continue;
					}
				}
				else if(type.equals("Message")){
					//일반 메세지를 넘겨줌
					String msg=st.nextToken().toString();
					System.out.println(date_time+"["+type+"]"+msg);
					sendMsg(msg,false);
				}else if(type.equals("Cipher")){
					//cipher text인 경우 
					String msg=st.nextToken().toString();
					System.out.println(date_time+"["+type+"]"+msg);
					sendMsg(msg,true);
				}
				// 연결된 소켓들을 통해서 다른 클라이언트에게 문자열 보내주기
			}
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (socket != null)
					socket.close();
			} catch (IOException ie) {
				System.out.println(ie.getMessage());
			}
		}
	}
	public void check_login(String id, String password){
		
	}

	// 전송받은 문자열 다른 클라이언트들에게 보내주는 메서드
	public void sendMsg(String str, boolean crypted) throws Exception {
		try {
			for (SocketwithAES swa : swas) {
				// for를 돌되 현재의 socket이 데이터를 보낸 클라이언트인 경우를 제외하고
				// 나머지 socket들에게만 데이터를 보낸다.
				if (swa.socket != this.socket) {
					PrintWriter pw = new PrintWriter(swa.socket.getOutputStream(), true);
					String sendText;
					if(crypted) {
						String[] splstr = str.split(" ",2);
						splstr[1] = aes.aesDecode(splstr[1]);
						String cryptText = swa.aes.aesEncode(splstr[1]);
						sendText = "Cipher " + splstr[0] + " " + cryptText;
					}
					else {
						sendText = "Message " + str;
					}
					pw.println(sendText);
					pw.flush();
					// 단,여기서 얻어온 소켓들은 남의것들이기 때문에 여기서 닫으면 안된다.
				}
			}
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}
	
	public void sendMsg_login(String str) {
		try {
			for (SocketwithAES swa : swas) {
				// for를 돌되 현재의 socket이 데이터를 보낸 클라이언트인 경우 데이터를 보낸다.
				if (swa.socket == this.socket) {
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
					pw.println(str);
					pw.flush();
					break;
				}
			}
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}
}

public class MultiChatServer {
	
	public static void main(String[] args) throws NoSuchAlgorithmException, ClassNotFoundException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		ServerSocket server = null;
		Socket socket = null;
		// 클라이언트와 연결된 소켓들을 배열처럼 저장할 벡터객체 생성
		Vector<SocketwithAES> swa = new Vector<SocketwithAES>();
		RSACrypto rsa = new RSACrypto(2048);
		
		ObjectInputStream ois;
		ObjectOutputStream oos;
		
		try {
			server = new ServerSocket(3000);
			while (true) {
				System.out.println("Waiting Clients..");
				socket = server.accept();
				// 클라이언트와 연결된 소켓을 벡터에 담기
				oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(rsa.getPublic());
	            oos.flush();
	            
				ois = new ObjectInputStream(socket.getInputStream());
				String key = (String)ois.readObject();
				key = RSACrypto.decrypt(rsa.getPrivate(), key);
				AESCrypto aes = new AESCrypto(key);
				SocketwithAES tmp = new SocketwithAES(socket, aes);
				swa.add(tmp);
				
				// 스레드 구동
				new EchoThread(socket, aes, swa).start();
			}
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}
}