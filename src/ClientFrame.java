import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//login 페이지 UI
class Id extends JFrame implements ActionListener {

	static JTextField tf_id = new JTextField(8);
	static JPasswordField tf_pw = new JPasswordField(8);
	public static boolean check = false;

	JButton btn = new JButton("입력");
	WriteThread wt;
	ClientFrame cf;
	public Id() {

	}

	public Id(WriteThread wt, ClientFrame cf) {

		super("정보보호 12조 채팅 프로그램");
		setResizable(false);
		this.wt = wt;
		this.cf = cf;
		setLayout(null);

		JLabel lblLogin = new JLabel("아이디:");
		lblLogin.setBounds(12, 27, 50, 15);
		add(lblLogin);

		tf_id.setBounds(86, 24, 120, 21);
		add(tf_id);

		JLabel lblPassword = new JLabel("비밀번호: ");
		lblPassword.setBounds(12, 52, 72, 29);
		add(lblPassword);

		tf_pw.setBounds(86, 55, 120, 21);
		add(tf_pw);

		btn.setBounds(12, 91, 91, 23);
		add(btn);
		btn.addActionListener(this);
		setBounds(100, 100, 242, 180);
		setVisible(true);

		JButton button = new JButton("나가기");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		button.setBounds(115, 91, 91, 23);
		add(button);
	}

	// login에서 버튼 누르면 바로 다음화면
	public void actionPerformed(ActionEvent e) {
		try {
			wt.sendMsg();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| InvalidParameterSpecException | BadPaddingException | IllegalBlockSizeException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	}
	static public String getId() {
		return tf_id.getText();
	}

	@SuppressWarnings("deprecation")
	static public String getPW() {
		return tf_pw.getText();
	}
}


//채팅 페이지 UI
public class ClientFrame extends JFrame implements ActionListener {

	JTextArea txtA = new JTextArea();
	JTextField txtF = new JTextField(15);
	JButton btnTransfer = new JButton("전송");
	JButton btnExit = new JButton("닫기");
	JLabel lblMyId = new JLabel("MY ID: ");
	boolean isFirst = true;
	JPanel p1 = new JPanel();
	Socket socket;
	AESCrypto aes;
	WriteThread wt;
	Id id;
	JCheckBoxMenuItem chckbxmntmCryptomode;

	public ClientFrame(Socket socket, AESCrypto aes) {
		super("정보보호 12조 채팅방");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 514, 443);
		p1.setLayout(null);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnDebug = new JMenu("테스트");
		menuBar.add(mnDebug);

		chckbxmntmCryptomode = new JCheckBoxMenuItem("암호화 적용");
		
		mnDebug.add(chckbxmntmCryptomode);
		
		this.aes = aes;
		
		this.socket = socket;
		wt = new WriteThread(this);
		id=new Id(wt, this);

		txtA.setBounds(12, 10, 476, 291);
		add(txtA);

		txtF.setBounds(73, 337, 327, 21);
		p1.add(txtF);

		JLabel lblEnter = new JLabel("입력:");
		lblEnter.setBounds(22, 340, 42, 15);
		add(lblEnter);

		lblMyId.setBounds(22, 311, 466, 15);
		add(lblMyId);

		btnTransfer.setBounds(409, 336, 79, 23);
		p1.add(btnTransfer);
		p1.add(btnExit);
		add(p1);
		// 메세지를 전송하는 클래스 생성.

		btnTransfer.addActionListener(this);
		// btnExit.addActionListener(this);
		setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {

		String id = Id.getId();
		if (e.getSource() == btnTransfer) {// 전송버튼 눌렀을 경우
			// 메세지 입력없이 전송버튼만 눌렀을 경우
			if (txtF.getText().equals("")) {
				return;
			}else{
			    //출력할 시간 만들기
				Date today = new Date();
				SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
			    SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
			        
			    System.out.println("Date: "+date.format(today));
			    System.out.println("Time: "+time.format(today));
			    String date_time="["+date.format(today)+" "+time.format(today)+"]";
				
			    if(chckbxmntmCryptomode.getState()==true){
					//암호화 선택을 한 경우
					txtA.append(date_time+"[" + id + "]: " + txtF.getText() + "\n");
					wt.cipher_check=true;
					try {
						wt.sendMsg();
					} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
							| NoSuchPaddingException | InvalidParameterSpecException | BadPaddingException
							| IllegalBlockSizeException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					txtF.setText("");
				}else if(chckbxmntmCryptomode.getState()==false){
					//암호화 선택을 안한 경우
					txtA.append(date_time+"[" + id + "]: " + txtF.getText() + "\n");
					wt.cipher_check=false;
					try {
						wt.sendMsg();
					} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
							| NoSuchPaddingException | InvalidParameterSpecException | BadPaddingException
							| IllegalBlockSizeException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					txtF.setText("");
				}
			}
			
		} else {
			this.dispose();
		}
	}
}
