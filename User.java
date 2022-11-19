import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class User extends JPanel{
	private JLabel label;
	public TableLabel time = new TableLabel("20:00");
	public TableLabel score = new TableLabel("0");
	public TableLabel huiqi = new TableLabel("3");
	User(boolean color, String userName){
		setPreferredSize(new Dimension(250,500));
		setOpaque(false);
		label = new JLabel();
		if(color){
			label.setIcon(new ImageIcon("src/images/black.png"));
		}
		else{
			label.setIcon(new ImageIcon("src/images/white.png"));
		}
		label.setPreferredSize(new Dimension(100, 100));
		add(label);
		add(new JLabel(userName){{
			setFont(new Font("华文新魏", Font.BOLD, 40));
		}});
		JPanel table = new JPanel();
		table.setOpaque(false);
		table.setLayout(new GridLayout(3, 2){{
			setHgap(30);
			setVgap(50);
		}});
		table.add(new TableLabel("  倒计时"));
		table.add(time);
		table.add(new TableLabel("当前积分"));
		table.add(score);
		table.add(new TableLabel("剩余悔棋"));
		table.add(huiqi);
		add(table);
		
	}
	class TableLabel extends JLabel{
		public TableLabel(String s) {
			super(s);
			setFont(new Font("华文新魏", Font.BOLD, 25));
		}
	}
	public void refresh(UserInfo info) {
		if(info.color){
			label.setIcon(new ImageIcon("src/images/black.png"));
		}
		else{
			label.setIcon(new ImageIcon("src/images/white.png"));
		}
		ChessTimer.getInstance().myTimer.reStart(1200);
		ChessTimer.getInstance().opponentTimer.reStart(1200);
		score.setText(String.valueOf(info.score));
		huiqi.setText("3");
	}
}
class UserInfo{
	public String name;
	public int level;
	public int huiqi = 3;
	public int score;
	public long time;
	public boolean color;
	
	public UserInfo(String name, int level, boolean isHost) {
		super();
		this.name = name;
		this.level = level;
		this.time = 60;
		this.color = isHost;
	}
}