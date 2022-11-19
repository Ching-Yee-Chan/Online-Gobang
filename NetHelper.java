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
			JOptionPane.showMessageDialog(null, "用户不存在或密码错误", "验证失败", JOptionPane.WARNING_MESSAGE, null);
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
			Login.log.append("服务器网络线程启动成功！等待对手上线中...");
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "端口必须是1-60000间的整数！", "错误",JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "网络异常", "错误",JOptionPane.ERROR_MESSAGE);
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
						Login.log.append("收到新的网络请求，正在验证身份...");
						String command = cacheReader.readLine();
						switch(command){
						case "$CLIENT": {
							String username = cacheReader.readLine();
							String password = cacheReader.readLine();
							if(DataBase.getInstance().getInfo(username, password, Status.CILENT)&&!findClient){//验证对手身份
								out = cacheWriter;
								cacheWriter.println("$TRUE");
								cacheWriter.println(Control.getInstance().me.name);
								cacheWriter.println(Control.getInstance().opponent.name);
								//发送自己、对手段位
								watcherRadio.sendInitInfo();
								new GetMessage(cacheReader).start();
								findClient = true;
								Login.log.append("对手身份验证成功！");
								Control.getInstance().init(false);
							}
							else{
								Login.log.append("对手身份验证失败或已有配对对手！");
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
							cacheWriter.println(Control.getInstance().me.name);//无论是否已经开局，都先发送服务器端玩家信息
							//发送自己段位
							if(findClient){//已经在游戏中，旁观者半途插入
								cacheWriter.println(Control.getInstance().opponent.name);
								//发送对手段位
								//发送敌我两方其他信息
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
								Login.log.append("旁观者身份验证成功！");
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
			JOptionPane.showMessageDialog(null, "端口必须是1-60000间的整数！", "错误",JOptionPane.ERROR_MESSAGE);
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
				Login.log.append("连接服务器"+ip+":"+port+"成功！");
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
					Login.log.append("身份验证成功！");
					Control.getInstance().opponent = new UserInfo(reader.readLine(), 5, true);//改段位
					Control.getInstance().me = new UserInfo(reader.readLine(), 5, false);//改段位
					out = writer;
					new GetMessage(reader).start();
					mediaHelper = new ClientMediaHelper();
					((ClientMediaHelper) mediaHelper).start(ip);
					Control.getInstance().init(isWatcher);
				}
				else{
					JOptionPane.showMessageDialog(null, "用户名不存在或密码错误！", "错误",JOptionPane.ERROR_MESSAGE);
					sleep(3000);
					return;
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "连接失败！未找到相应IP地址及端口！", "错误",JOptionPane.ERROR_MESSAGE);
				Login.log.append("连接失败！未找到相应IP地址及端口！");
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
						int sel = JOptionPane.showConfirmDialog(null, "对方请求悔棋，您同意么？", "悔棋申请",JOptionPane.YES_NO_OPTION);
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
						int sel = JOptionPane.showConfirmDialog(null, "对方请求重开一局，您同意么？", "重开申请",JOptionPane.YES_NO_OPTION);
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
							String side = reader.readLine().equals("true")?"黑":"白";
							JOptionPane.showMessageDialog(null, side+"方成功请求了重开", "系统提示", JOptionPane.PLAIN_MESSAGE);
						}
						else{
							ChatBox.getInstance().addText("系统", "对方同意了您的重开请求！");
						}
						Model.getInstance().remake();
						watcherRadio.remake(Control.getInstance().me.color);
						break;
					}
					case "$REFUSE":{
						ChatBox.getInstance().addText("系统", "对方拒绝了您的请求！");
						JOptionPane.showMessageDialog(null, "对方拒绝了您的请求！", "请求失败！",JOptionPane.ERROR_MESSAGE);
						break;
					}
					case "$SURRENDER":{
						if(Login.status==Status.WATCHER){
							boolean whoSurrender = reader.readLine().equals("true");
							String side = whoSurrender?"黑":"白";
							JOptionPane.showMessageDialog(null, side+"方认输了", "系统提示", JOptionPane.PLAIN_MESSAGE);
							if(Control.getInstance().me.color==whoSurrender){
								Control.getInstance().opponent.score++;
							}
							else{
								Control.getInstance().me.score++;
							}
						}
						else{
							JOptionPane.showMessageDialog(null, "对方认输了！");
							Control.getInstance().me.score++;
						}
						WinWindow.getInstance().setVisible(true);
						watcherRadio.surrender(Control.getInstance().opponent.color);
						break;
					}
					case "$START?":{
						int choice = JOptionPane.showConfirmDialog(null, "对方已就绪，是否开始？", "开始申请",JOptionPane.YES_NO_OPTION);
						if(choice==JOptionPane.YES_OPTION){
							out.println("$START");
							watcherRadio.sendSimpleCommand("$START");
							if(Model.getInstance().period == GamePeriod.FREE){
								ChessTimer.getInstance().start(1200, 1200);
							}
							Model.getInstance().period = GamePeriod.ON;
							ButtonArea.pause.setText("暂停");
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
							ButtonArea.pause.setText("暂停");
						}
						watcherRadio.sendSimpleCommand("$START");
						JOptionPane.showMessageDialog(null, "游戏已开始！");
						break;
					}
					case "$PAUSE?":{
						int choice = JOptionPane.showConfirmDialog(null, "对方申请暂停，休息一会儿好不好？", "暂停申请",JOptionPane.YES_NO_OPTION);
						if(choice==JOptionPane.YES_OPTION){
							out.println("$PAUSE");
							watcherRadio.sendSimpleCommand("$PAUSE");
							Model.getInstance().period = GamePeriod.PAUSE;
							ButtonArea.pause.setText("继续");
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
							ButtonArea.pause.setText("继续");
						}
						watcherRadio.sendSimpleCommand("$PAUSE");
						JOptionPane.showMessageDialog(null, "游戏已暂停！");
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
		ChatBox.getInstance().addText("系统","正在等待对方玩家同意...");
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
		ChatBox.getInstance().addText("系统","正在等待对方玩家同意...");
	}
	public void sendPause() {
		out.println("$PAUSE?");
		ChatBox.getInstance().addText("系统","正在等待对方玩家同意...");
	}
	public void canBack() {
		out.println("$BACK?");
		ChatBox.getInstance().addText("系统","正在等待对方玩家同意...");
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
			//发送对方段位信息
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