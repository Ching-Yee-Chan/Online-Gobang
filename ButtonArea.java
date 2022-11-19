import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class ButtonArea extends JPanel{
	public static JButton pause;
	public ButtonArea() {
		setPreferredSize(new Dimension(1000, 80));
		setOpaque(false);
		pause = new JButton("ø™æ÷"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					switch(Model.getInstance().period){
					case PAUSE: case FREE:{
						NetHelper.getInstance().sendStart();
						break;
					}
					case ON:{
						NetHelper.getInstance().sendPause();
						break;
					}
					}
				}
			});
		}};
		add(pause);
		add(new JButton("ª⁄∆Â"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!Model.getInstance().canBack(Control.getInstance().me.color)){
						return;
					}
					NetHelper.getInstance().canBack();
					if(Control.getInstance().me.huiqi==0){
						setEnabled(false);
					}
				}
			});
		}});
		add(new JButton("»œ ‰"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getInstance().opponent.score++;
					//∂ŒŒª‘ˆº”
					NetHelper.getInstance().surrenderToOpponent();
					LoseWindow.getInstance().setVisible(true);
				}
			});
		}});
		add(new JButton("∫Õ∆Â"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					NetHelper.getInstance().canRemake();
				}
			});
		}});
		add(new JButton("∑‚∆Â"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					int i = chooser.showSaveDialog(null);
					if(i==JFileChooser.APPROVE_OPTION){
						File pos = chooser.getSelectedFile();
						try {
							ImageIO.write(Board.getInstance().getImage(), "PNG", new FileOutputStream(pos));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}});
		add(bgmController.getInstance());
	}
}
class bgmController extends JButton{
	private static bgmController instance;
	private bgmController() {
		setText("Õ£÷π±≥æ∞“Ù¿÷");
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(Control.bgm.isPlaying){
					Control.bgm.stop();
				}
				else{
					Control.bgm.loop();
				}
			}
		});
	}
	public static bgmController getInstance(){
		if(instance==null){
			instance = new bgmController();
		}
		return instance;
	}
}