import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class View {
	private static View view;
	public static Font font = new Font("����", Font.PLAIN, 17);
	public JFrame window;
	public User me;
	public User opponent;
	private JPanel main;
	private View(){}
	public static View getInstance(){
		if(view==null){
			view = new View();
		}
		return view;
	}
	private void commonInit(){
		window = new JFrame("������");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main = new JPanel(){
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon initial = new ImageIcon("src/images/backGround.png");
				initial.setImage(initial.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT));
				g.drawImage(initial.getImage(), 0, 0, null);
			}
		};
		main.setLayout(new BorderLayout());
		main.add(Board.getInstance(), BorderLayout.CENTER);
		opponent = new User(Control.getInstance().opponent.color, Control.getInstance().opponent.name);
		me = new User(Control.getInstance().me.color, Control.getInstance().me.name);
		main.add(opponent, BorderLayout.WEST);
		main.add(me, BorderLayout.EAST);
		window.getContentPane().add(main, BorderLayout.CENTER);
	}
	public void watcherInit(){
		commonInit();
		window.setSize(1000, 612);
		main.add(new JPanel(){{
			add(bgmController.getInstance());
			setOpaque(false);
		}}, BorderLayout.SOUTH);
		main.repaint();
		window.setVisible(true);
	}
	public void init(){
		commonInit();
		window.setSize(1300, 612);
		main.add(new ButtonArea(), BorderLayout.SOUTH);
		window.getContentPane().add(main, BorderLayout.CENTER);
		window.getContentPane().add(ChatBox.getInstance(), BorderLayout.EAST);
		main.repaint();
		window.setVisible(true);
	}
}

class EndWindow extends JFrame{
	public EndWindow() {
		setSize(600, 400);
		setTitle("��Ϸ����");
		add(new JPanel(){{
			add(new JButton("����һ��"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						NetHelper.getInstance().canRemake();
					}
				});
			}});
			add(new JButton("����"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						new Review();
					}
				});
			}});
		}}, BorderLayout.SOUTH);
		repaint();
	}
}
class WinWindow extends EndWindow{
	private static WinWindow instance;
	private WinWindow(){}
	public static WinWindow getInstance(){
		if(instance==null){
			instance = new WinWindow();
		}
		return instance;
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		ImageIcon initial = new ImageIcon("src/images/win.png");
		initial.setImage(initial.getImage().getScaledInstance(getWidth(), getHeight()-100, Image.SCALE_DEFAULT));
		g.drawImage(initial.getImage(), 0, 0, null);
	}
}
class LoseWindow extends EndWindow{
	private static LoseWindow instance;
	private LoseWindow(){}
	public static LoseWindow getInstance(){
		if(instance==null){
			instance = new LoseWindow();
		}
		return instance;
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		ImageIcon initial = new ImageIcon("src/images/lose.jpg");
		initial.setImage(initial.getImage().getScaledInstance(getWidth(), getHeight()-50, Image.SCALE_DEFAULT));
		g.drawImage(initial.getImage(), 0, 0, null);
	}
}
class WatcherEndDialog extends JFrame{
	private static WatcherEndDialog instance;
	private WatcherEndDialog(){}
	public static WatcherEndDialog getInstance(){
		if(instance==null){
			instance=new WatcherEndDialog();
		}
		return instance;
	}
	void init(boolean winSide){
		setSize(600, 400);
		String side = winSide?"��":"��";
		add(new JLabel(side+"��ʤ��"){{
			setFont(new Font("���Ĳ���", Font.BOLD, 30));
			setForeground(Color.red);
		}});
		add(new JButton("����"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new Review();
				}
			});
		}}, BorderLayout.SOUTH);
		setVisible(true);
	}
}
class ImageReceiver extends JFrame{
	void init(byte[] image){
		setSize(500, 550);
		add(new JLabel("�Է�������һ��ͼƬ��"){{
			setFont(Login.font);
		}}, BorderLayout.NORTH);
		add(new JLabel(new ImageIcon(image)));
		setVisible(true);
	}
}