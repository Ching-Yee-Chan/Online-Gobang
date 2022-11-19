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
		super("����");
		setSize(500, 550);
		Model.getInstance().clear();
		add(Board.getReviewInstance());
		add(new JPanel(){{
			add(new JButton("��һ��"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(step==-1){
							JOptionPane.showMessageDialog(Review.this, "û����һ����~", "����", JOptionPane.ERROR_MESSAGE);
							return;
						}
						Model.getInstance().clearNode(Model.getInstance().record.get(step--));
					}
				});
			}});
			add(new JButton("��һ��"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(step==Model.getInstance().record.size()-1){
							JOptionPane.showMessageDialog(Review.this, "û����һ����~", "����", JOptionPane.ERROR_MESSAGE);
							return;
						}
						Model.getInstance().nextNode(Model.getInstance().record.get(++step));
					}
				});
			}});
		}},BorderLayout.SOUTH);
		Control.getInstance().color = !Control.getInstance().me.color;//�������幦��
		setVisible(true);
	}
}
