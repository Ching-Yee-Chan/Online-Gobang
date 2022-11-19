import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

public class NetHelper {
	private static NetHelper helper;
	private ServerSocket ss;
	private Socket c;
	private PrintWriter out = null;
	private SetWatcher watcherRadio = new SetWatcher();
	private boolean findClient = false;
	public MediaHelper mediaHelper;
	private NetHelper(){};
	public static NetHelper getInstance(){
		if(helper == null){
			helper = new NetHelper();
		}
		return helper;
	}
	public void startServer(String port, String username, char[] password) {
		if(!DataBase.getInstance().getInfo(username, String.copyValueOf(password), Status.HOST)){
			JOptionPane.showMessageDialog(null, "�û������ڻ��������", "��֤ʧ��", JOptionPane.WARNING_MESSAGE, null);
			return;
		}
		try {
			int portNum = new Integer(port);
			if(portNum<=0||portNum>60000){
				throw new NumberFormatException();
			}
			ss = new ServerSocket(portNum);
			mediaHelper = new ServerMediaHelper();
			((ServerMediaHelper) mediaHelper).start();
			Login.log.append("�����������߳������ɹ����ȴ�����������...");
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "�˿ڱ�����1-60000���������", "����",JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "�����쳣", "����",JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
		new Thread() {
			public void run(){
				while(true) {
					Socket s = null;
					try {
						s = ss.accept();
						PrintWriter cacheWriter = new PrintWriter(s.getOutputStream(), true);
						BufferedReader cacheReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
						Login.log.append("�յ��µ���������������֤���...");
						String command = cacheReader.readLine();
						switch(command){
						case "$CLIENT": {
							String username = cacheReader.readLine();
							String password = cacheReader.readLine();
							if(DataBase.getInstance().getInfo(username, password, Status.CILENT)&&!findClient){//��֤�������
								out = cacheWriter;
								cacheWriter.println("$TRUE");
								cacheWriter.println(Control.getInstance().me.name);
								cacheWriter.println(Control.getInstance().opponent.name);
								//�����Լ������ֶ�λ
								watcherRadio.sendInitInfo();
								new GetMessage(cacheReader).start();
								findClient = true;
								Login.log.append("���������֤�ɹ���");
								Control.getInstance().init(false);
							}
							else{
								Login.log.append("���������֤ʧ�ܻ�������Զ��֣�");
								cacheWriter.println("$FALSE");
								cacheWriter.close();
								cacheReader.close();
							}
							break;
						}
						case "$WATCHER":{
							String username = cacheReader.readLine();
							String password = cacheReader.readLine();
							if(!DataBase.getInstance().getInfo(username, password, Status.WATCHER)){
								cacheWriter.println("$FALSE");
								break;
							}
							watcherRadio.watchers.addLast(cacheWriter);
							cacheWriter.println("$TRUE");
							cacheWriter.println(Control.getInstance().me.name);//�����Ƿ��Ѿ����֣����ȷ��ͷ������������Ϣ
							//�����Լ���λ
							if(findClient){//�Ѿ�����Ϸ�У��Թ��߰�;����
								cacheWriter.println(Control.getInstance().opponent.name);
								//���Ͷ��ֶ�λ
								//���͵�������������Ϣ
								for (Iterator<ChessNode> iterator = Model.getInstance().record.iterator(); iterator
										.hasNext();) {
									ChessNode node = (ChessNode) iterator.next();
									if(node.removeMark==2){
										cacheWriter.println("$BACK");
										cacheWriter.println(node.color);
									}
									else{
										cacheWriter.println("$PUTCHESS");
										cacheWriter.println(node.row);
										cacheWriter.println(node.col);
										cacheWriter.println(node.color);
									}
								}if(Model.getInstance().period==GamePeriod.ON){
									cacheWriter.println("$SETTIME");
									cacheWriter.println(ChessTimer.getInstance().myTime);
									cacheWriter.println(ChessTimer.getInstance().opponentTime);
								}
							}
							else{
								Login.log.append("�Թ��������֤�ɹ���");
							}
							new GetMessage(cacheReader).start();
							break;
						}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	public void startClient(String host, String port, String username,
			char[] password, boolean isWatcher) {
		int portNum = new Integer(port);
		if(portNum<=0||portNum>60000){
			JOptionPane.showMessageDialog(null, "�˿ڱ�����1-60000���������", "����",JOptionPane.ERROR_MESSAGE);
		}
		new ClientStart(host, portNum, username, password, isWatcher).start();
	}
	class ClientStart extends Thread{
		private String ip;
		private int port;
		private boolean isWatcher;
		private String name;
		private String key;
		public ClientStart(String IP, int portInput, String username,
				char[] password, boolean watcher) {
			ip = IP;
			port = portInput;
			isWatcher = watcher;
			name = username;
			key = String.valueOf(password);
		}
		@Override
		public void run() {
			super.run();
			try {
				c = new Socket(InetAddress.getByName(ip), port);
				Login.log.append("���ӷ�����"+ip+":"+port+"�ɹ���");
				PrintWriter writer = new PrintWriter(c.getOutputStream(), true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
				if(isWatcher){
					writer.println("$WATCHER");
				}
				else{
					writer.println("$CLIENT");
				}
				writer.println(name);
				writer.println(key);
				if(reader.readLine().equals("$TRUE")){
					Login.log.append("�����֤�ɹ���");
					Control.getInstance().opponent = new UserInfo(reader.readLine(), 5, true);//�Ķ�λ
					Control.getInstance().me = new UserInfo(reader.readLine(), 5, false);//�Ķ�λ
					out = writer;
					new GetMessage(reader).start();
					mediaHelper = new ClientMediaHelper();
					((ClientMediaHelper) mediaHelper).start(ip);
					Control.getInstance().init(isWatcher);
				}
				else{
					JOptionPane.showMessageDialog(null, "�û��������ڻ��������", "����",JOptionPane.ERROR_MESSAGE);
					sleep(3000);
					return;
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "����ʧ�ܣ�δ�ҵ���ӦIP��ַ���˿ڣ�", "����",JOptionPane.ERROR_MESSAGE);
				Login.log.append("����ʧ�ܣ�δ�ҵ���ӦIP��ַ���˿ڣ�");
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	class GetMessage extends Thread{
		private BufferedReader reader = null;
		public GetMessage(BufferedReader in) {
			reader = in;
		}
		@Override
		public void run() {
			while(true){
				try {
					String command = reader.readLine();
					System.out.println(command);
					switch(command){
					case "$PUTCHESS":{
						int row = new Integer(reader.readLine());
						int col = new Integer(reader.readLine());
						boolean color = false;
						if(reader.readLine().equals("true")){
							color = true;
						}
						Model.getInstance().putChess(row, col, color);
						watcherRadio.send(row, col, color);
						break;
					}
					case "$BACK?":{
						int sel = JOptionPane.showConfirmDialog(null, "�Է�������壬��ͬ��ô��", "��������",JOptionPane.YES_NO_OPTION);
						if(sel==JOptionPane.YES_OPTION){
							out.println("$BACK");
							out.println(Control.getInstance().opponent.color);
							Model.getInstance().back(Control.getInstance().opponent.color);
							watcherRadio.back(Control.getInstance().opponent.color);
						}
						else{
							out.println("$REFUSE");
						}
							break;
					}
					case "$BACK":{
						boolean color = false;
						if(reader.readLine().equals("true")){
							color = true;
						}
						Model.getInstance().back(color);
						watcherRadio.back(color);
						break;
					}
					case "$REMAKE?":{
						int sel = JOptionPane.showConfirmDialog(null, "�Է������ؿ�һ�֣���ͬ��ô��", "�ؿ�����",JOptionPane.YES_NO_OPTION);
						if(sel==JOptionPane.YES_OPTION){
							out.println("$REMAKE");
							Model.getInstance().remake();
							watcherRadio.remake(Control.getInstance().opponent.color);
						}
						if(sel==JOptionPane.NO_OPTION){
							out.println("$REFUSE");
						}
						break;
					}
					case "$REMAKE" :{
						if(Login.status==Status.WATCHER){
							String side = reader.readLine().equals("true")?"��":"��";
							JOptionPane.showMessageDialog(null, side+"���ɹ��������ؿ�", "ϵͳ��ʾ", JOptionPane.PLAIN_MESSAGE);
						}
						else{
							ChatBox.getInstance().addText("ϵͳ", "�Է�ͬ���������ؿ�����");
						}
						Model.getInstance().remake();
						watcherRadio.remake(Control.getInstance().me.color);
						break;
					}
					case "$REFUSE":{
						ChatBox.getInstance().addText("ϵͳ", "�Է��ܾ�����������");
						JOptionPane.showMessageDialog(null, "�Է��ܾ�����������", "����ʧ�ܣ�",JOptionPane.ERROR_MESSAGE);
						break;
					}
					case "$SURRENDER":{
						if(Login.status==Status.WATCHER){
							boolean whoSurrender = reader.readLine().equals("true");
							String side = whoSurrender?"��":"��";
							JOptionPane.showMessageDialog(null, side+"��������", "ϵͳ��ʾ", JOptionPane.PLAIN_MESSAGE);
							if(Control.getInstance().me.color==whoSurrender){
								Control.getInstance().opponent.score++;
							}
							else{
								Control.getInstance().me.score++;
							}
						}
						else{
							JOptionPane.showMessageDialog(null, "�Է������ˣ�");
							Control.getInstance().me.score++;
						}
						WinWindow.getInstance().setVisible(true);
						watcherRadio.surrender(Control.getInstance().opponent.color);
						break;
					}
					case "$START?":{
						int choice = JOptionPane.showConfirmDialog(null, "�Է��Ѿ������Ƿ�ʼ��", "��ʼ����",JOptionPane.YES_NO_OPTION);
						if(choice==JOptionPane.YES_OPTION){
							out.println("$START");
							watcherRadio.sendSimpleCommand("$START");
							if(Model.getInstance().period == GamePeriod.FREE){
								ChessTimer.getInstance().start(1200, 1200);
							}
							Model.getInstance().period = GamePeriod.ON;
							ButtonArea.pause.setText("��ͣ");
							watcherRadio.sendSimpleCommand("$START");
						}
						else{
							out.println("$REFUSE");
						}
						break;
					}
					case "$START":{
						if(Model.getInstance().period == GamePeriod.FREE){
							ChessTimer.getInstance().start(1200, 1200);
						}
						Model.getInstance().period = GamePeriod.ON;
						if(Login.status!=Status.WATCHER){
							ButtonArea.pause.setText("��ͣ");
						}
						watcherRadio.sendSimpleCommand("$START");
						JOptionPane.showMessageDialog(null, "��Ϸ�ѿ�ʼ��");
						break;
					}
					case "$PAUSE?":{
						int choice = JOptionPane.showConfirmDialog(null, "�Է�������ͣ����Ϣһ����ò��ã�", "��ͣ����",JOptionPane.YES_NO_OPTION);
						if(choice==JOptionPane.YES_OPTION){
							out.println("$PAUSE");
							watcherRadio.sendSimpleCommand("$PAUSE");
							Model.getInstance().period = GamePeriod.PAUSE;
							ButtonArea.pause.setText("����");
							watcherRadio.sendSimpleCommand("$PAUSE");
						}
						else{
							out.println("$REFUSE");
						}
						break;
					}
					case "$PAUSE":{
						Model.getInstance().period = GamePeriod.PAUSE;
						if(Login.status!=Status.WATCHER){
							ButtonArea.pause.setText("����");
						}
						watcherRadio.sendSimpleCommand("$PAUSE");
						JOptionPane.showMessageDialog(null, "��Ϸ����ͣ��");
						break;
					}
					case "$TEXT":{
						ChatBox.getInstance().addText(Control.getInstance().opponent.name, reader.readLine());
						break;
					}
					case "$SETTIME":{
						ChessTimer.getInstance().start(new Integer(reader.readLine()), new Integer(reader.readLine()));
						Model.getInstance().period = GamePeriod.ON;
						break;
					}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void putChessToOpponent(int row, int col, boolean color) {
		out.println("$PUTCHESS");
		out.println(row);
		out.println(col);
		out.println(color);
		watcherRadio.send(row, col, color);
	}
	public void canRemake(){
		out.println("$REMAKE?");
		ChatBox.getInstance().addText("ϵͳ","���ڵȴ��Է����ͬ��...");
	}
	public void surrenderToOpponent() {
		out.println("$SURRENDER");
		watcherRadio.surrender(Control.getInstance().me.color);
		
	}
	public void sendText(String text) {
		out.println("$TEXT");
		out.println(text);
	}
	public void sendStart() {
		out.println("$START?");
		ChatBox.getInstance().addText("ϵͳ","���ڵȴ��Է����ͬ��...");
	}
	public void sendPause() {
		out.println("$PAUSE?");
		ChatBox.getInstance().addText("ϵͳ","���ڵȴ��Է����ͬ��...");
	}
	public void canBack() {
		out.println("$BACK?");
		ChatBox.getInstance().addText("ϵͳ","���ڵȴ��Է����ͬ��...");
	}
}
class SetWatcher{
	public LinkedList<PrintWriter> watchers = new LinkedList<PrintWriter>();
	public void send(final int row,final int col, final boolean color){
		for (PrintWriter out : watchers) {
			out.println("$PUTCHESS");
			out.println(row);
			out.println(col);
			out.println(color);
		}
	}
	public void back(final boolean color){
		for (PrintWriter out : watchers) {
			out.println("$BACK");
			out.println(color);
		}
	}
	public void sendInitInfo(){
		for (PrintWriter out : watchers) {
			out.println(Control.getInstance().opponent.name);
			//���ͶԷ���λ��Ϣ
		}
	}
	public void remake(boolean color){
		for (PrintWriter out : watchers) {
			out.println("$REMAKE");
			out.println(color);
		}
	}
	public void surrender(boolean color){
		for (PrintWriter out : watchers) {
			out.println("$SURRENDER");
			out.println(color);
		}
	}
	public void sendSimpleCommand(String s){
		for (PrintWriter out : watchers) {
			out.println(s);
		}
	}
}


class MediaHelper{
	protected Socket audioSocket;
	protected Socket imageSocket;
	protected BufferedInputStream audioDown;
	protected BufferedInputStream imageDown;
	protected BufferedOutputStream audioUp;
	protected BufferedOutputStream imageUp;
	public byte[] audioData = new byte[3000000];
	public byte[] imageData = new byte[500000];
	private int audioPos;
	private int imagePos;
	synchronized protected void startReceiving(){
		new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						sleep(1);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						if(audioDown.available()>= 60000){
							audioDown.read(audioData, audioPos, 60000);
							audioPos += 60000;
							if(audioPos==3000000){
								audioPos = 0;
								new AudioReceiver().init(audioData);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						sleep(1);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						if(imageDown.available()>= 50000){
							imageDown.read(imageData, imagePos, 50000);
							imagePos += 50000;
							if(imagePos==500000){
								imagePos=0;
								new ImageReceiver().init(imageData);
							}
							
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	public void sendAudio(byte[] out){
		try {
			audioUp.write(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendImage(byte[] out){
		try {
			imageUp.write(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class ServerMediaHelper extends MediaHelper{
	private ServerSocket audioServerSocket;
	private ServerSocket imageServerSocket;
	private Thread audioThread;
	public void start(){
		try {
			audioServerSocket = new ServerSocket(10010);
			imageServerSocket = new ServerSocket(20010);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		audioThread = new Thread(){
			public void run() {
				try {
					audioSocket = audioServerSocket.accept();
					audioDown = new BufferedInputStream(audioSocket.getInputStream());
					audioUp = new BufferedOutputStream(audioSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		audioThread.start();
		new Thread(){
			public void run() {
				try {
					imageSocket = imageServerSocket.accept();
					imageDown = new BufferedInputStream(imageSocket.getInputStream());
					imageUp = new BufferedOutputStream(imageSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					if(audioThread.isAlive()){
						audioThread.join();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startReceiving();
			}
		}.start();
	}
}
class ClientMediaHelper extends MediaHelper{
	protected void start(String ip) {
		try {
			audioSocket = new Socket(InetAddress.getByName(ip), 10010);
			imageSocket = new Socket(InetAddress.getByName(ip), 20010);
			audioDown = new BufferedInputStream(audioSocket.getInputStream());
			audioUp = new BufferedOutputStream(audioSocket.getOutputStream());
			imageDown = new BufferedInputStream(imageSocket.getInputStream());
			imageUp = new BufferedOutputStream(imageSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startReceiving();
	}
}