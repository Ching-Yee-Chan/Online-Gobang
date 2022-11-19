import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

enum Status{
	HOST, CILENT, WATCHER
}
public class Login extends JFrame{
	private InputPanel username = new InputPanel(" �û��� ");
	private PasswordPanel password = new PasswordPanel("  ����  ");
	private InputPanel hostBox = new InputPanel(" ����IP ");
	private InputPanel portBox = new InputPanel("�����˿�");
	public static Status status = null;
	public static JTextArea log = new JTextArea(){
		synchronized public void append(String s){
			setText(getText()+"\n"+s);
		}
	};
	public static Font font = new Font("����", Font.PLAIN, 30);

	public Login() {
		super("������-��¼");
		setSize(428,600);
		JPanel main = new JPanel(){
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon initial = new ImageIcon("src/images/login.png");
				initial.setImage(initial.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT));
				g.drawImage(initial.getImage(), 0, 0, null);
			}
		};
		main.add(new JLabel("������������"){{
			setFont(new Font("�����п�", Font.BOLD, 50));
			setForeground(Color.red);
		}});
		JRadioButton host = new ChoiceButton("����"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					status = Status.HOST;
					try {
						hostBox.text.setText(InetAddress.getLocalHost().getHostAddress());
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					hostBox.text.setEditable(false);
					portBox.text.setText("8010");
				}
			});
		}};
		JRadioButton client = new ChoiceButton("�ͻ���"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					status = Status.CILENT;
					hostBox.text.setEditable(true);
					hostBox.text.setText("");
					portBox.text.setText("8010");
				}
			});
		}};
		JRadioButton watcher = new ChoiceButton("�Թ�ģʽ"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					status = Status.WATCHER;
					hostBox.text.setEditable(true);
					hostBox.text.setText("");
					portBox.text.setText("8010");
				}
			});
		}};
		ButtonGroup choice = new ButtonGroup();
		choice.add(host);
		choice.add(client);
		choice.add(watcher);
		main.add(host);
		main.add(client);
		main.add(watcher);
		main.add(username);
		main.add(password);
		main.add(hostBox);
		main.add(portBox);
		main.add(new JButton("��¼"){{
			setFont(Login.font);
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(status==null){
						JOptionPane.showMessageDialog(null, "��ѡ���¼���ͣ�", "����",JOptionPane.ERROR_MESSAGE);
					}
					switch(status){
					case HOST: NetHelper.getInstance().startServer(portBox.text.getText(),
							username.text.getText(), password.text.getPassword());break;
					case CILENT:NetHelper.getInstance().startClient(hostBox.text.getText(), 
							portBox.text.getText(), username.text.getText(), password.text.getPassword(), false);break;
					case WATCHER: NetHelper.getInstance().startClient(hostBox.text.getText(), 
							portBox.text.getText(), username.text.getText(), password.text.getPassword(), true);break;
					}
				}
			});
		}});
		main.add(new JButton("ע��"){{
			setFont(Login.font);
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					DataBase.getInstance().addInfo(username.text.getText(), String.copyValueOf(password.text.getPassword()));
				}
			});
		}});
		log.setPreferredSize(new Dimension(400, 120));
		log.setEditable(false);
		log.setAutoscrolls(true);
		main.add(log);
		add(main);
		main.repaint();
		setVisible(true);
	}
}
class ChoiceButton extends JRadioButton{
	public ChoiceButton(String s) {
		super(s);
		setFont(Login.font);
		setOpaque(false);
	}
}
class InputPanel extends JPanel{
	public JTextField text = new JTextField();
	public InputPanel(String s) {
		add(new JLabel(s){{
		setFont(Login.font);
		setOpaque(false);
		}});
		setOpaque(false);
		text.setPreferredSize(new Dimension(250, 50));
		text.setFont(Login.font);
		add(text);
	}
}
class PasswordPanel extends JPanel{
	JPasswordField text = new JPasswordField();
	public PasswordPanel(String s) {
		add(new JLabel(s){{
		setFont(Login.font);
		setOpaque(false);
		}});
		setOpaque(false);
		text.setPreferredSize(new Dimension(250, 50));
		text.setFont(new Font("����", Font.PLAIN, 30));
		add(text);
	}
}