import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class Review extends JFrame{
	int step = -1;
	Review(){
		super("复盘");
		setSize(500, 550);
		Model.getInstance().clear();
		add(Board.getReviewInstance());
		add(new JPanel(){{
			add(new JButton("上一步"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(step==-1){
							JOptionPane.showMessageDialog(Review.this, "没有上一步了~", "错误", JOptionPane.ERROR_MESSAGE);
							return;
						}
						Model.getInstance().clearNode(Model.getInstance().record.get(step--));
					}
				});
			}});
			add(new JButton("下一步"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(step==Model.getInstance().record.size()-1){
							JOptionPane.showMessageDialog(Review.this, "没有下一步了~", "错误", JOptionPane.ERROR_MESSAGE);
							return;
						}
						Model.getInstance().nextNode(Model.getInstance().record.get(++step));
					}
				});
			}});
		}},BorderLayout.SOUTH);
		Control.getInstance().color = !Control.getInstance().me.color;//禁用下棋功能
		setVisible(true);
	}
}
