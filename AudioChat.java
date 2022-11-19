import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class Audio extends JFrame{
	protected JButton listener;
	protected JButton stop;
	protected AudioFormat format = new AudioFormat(22050, 16, 1, true, false);
	protected SourceDataLine play;
	protected byte[] data;
	protected JPanel buttons = new JPanel();
	protected void init(){
		listener = new JButton("开始播放"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.bgm.stop();
					stop.setEnabled(true);
					try {
						Audio.this.play = AudioSystem.getSourceDataLine(format);
						Audio.this.play.open(format);
						Audio.this.play.start();
					} catch (LineUnavailableException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					new Thread(){
						synchronized public void run() {
							Audio.this.play.write(data, 0, 3000000);
							Control.bgm.loop();
						};
					}.start();
				}
			});
		}};
		stop = new JButton("停止"){{
			setEnabled(false);
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Audio.this.play.stop();
					Control.bgm.loop();
					setEnabled(false);
				}
			});
		}};
		buttons.add(listener);
		buttons.add(stop);
		add(buttons, BorderLayout.SOUTH);
		setSize(400, 300);
		setVisible(true);
	}
}

public class AudioChat extends Audio{
	public JLabel timer;
	private JButton recorder;
	private JButton send;
	public boolean isRecording = false;
	private static AudioChat instance;
	private TargetDataLine record;
	private AudioChat(){}
	public static AudioChat getInstance(){
		if(instance==null){
			instance=new AudioChat();
		}
		return instance;
	}
	protected void init(){
		super.init();
		timer = new JLabel("00:30");
		timer.setFont(new Font("Times New Roman", Font.BOLD, 70));
		timer.setForeground(Color.green);
		add(timer);
		recorder = new JButton("按住说话"){{
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					isRecording=true;
					setText("松开结束");
					Control.bgm.stop();
					new Timer(30){
						@Override
						protected void setTime() {
							if(time<10){
								timer.setForeground(Color.red);
								timer.setText("00:0"+time);
							}
							else{
								timer.setText("00:"+time);
							}
						}
						@Override
						protected void timeup() {
							
						}
						@Override
						protected boolean exit() {
							return !isRecording;
						}
					}.start();
					try {
						AudioChat.this.record = AudioSystem.getTargetDataLine(format);
						AudioChat.this.record.open(format);
						AudioChat.this.record.start();
					} catch (LineUnavailableException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					new Thread(){
						synchronized public void run() {
							data = new byte[3000000];
							AudioChat.this.record.read(data, 0, 3000000);
						};
					}.start();
				};
				@Override
				public void mouseReleased(MouseEvent e) {
					super.mouseReleased(e);
					isRecording=false;
					AudioChat.this.record.flush();
					AudioChat.this.record.close();
					setText("按住重新录制");
					listener.setEnabled(true);
					send.setEnabled(true);
					Control.bgm.loop();
				}
			});
		}};
		send = new JButton("发送"){{
			setEnabled(false);
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					NetHelper.getInstance().mediaHelper.sendAudio(data);
					ChatBox.getInstance().addText("系统", "语音消息发送成功！");
			}});
		}};
		listener.setEnabled(false);
		buttons.add(recorder);
		buttons.add(send);
		add(buttons, BorderLayout.SOUTH);
	}
}

class AudioReceiver extends Audio{
	protected void init(byte[] in) {
		super.init();
		add(new JLabel("您收到了一条新的语音消息！"){{
			setFont(new Font("黑体", Font.BOLD, 25));
		}});
		data = in;
	}
}
