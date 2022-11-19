import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.stream.FileImageInputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ChatBox extends JPanel{
	private static ChatBox chatBox;
	private JTextArea box;
	private JScrollPane scroll;
	private JTextField input;
	private JButton send;
	private ChatBox(){
		setPreferredSize(new Dimension(300, 612));
		setLayout(new BorderLayout());
		box = new JTextArea();
		box.setEditable(false);
		box.setLineWrap(true);
        box.setWrapStyleWord(true); 
        box.setFont(View.font);
		scroll = new JScrollPane(box);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(300, 450));
		add(scroll);
		JPanel below = new JPanel();
		below.setPreferredSize(new Dimension(300, 100));
		below.add(new JPanel(){{
			add(new JButton("·¢ËÍÍ¼Æ¬"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						new Thread(){
							@Override
							public void run() {
								JFileChooser chooser = new JFileChooser();
								int i = chooser.showOpenDialog(null);
								if(i==JFileChooser.APPROVE_OPTION){
									File pos = chooser.getSelectedFile();
									try {
										byte[] data = new byte[500000];
										FileImageInputStream image = new FileImageInputStream(pos);
										image.read(data);
										NetHelper.getInstance().mediaHelper.sendImage(data);
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							}
						}.start();
					}
				});
			}});
			add(new JButton("·¢ËÍÓïÒôÏûÏ¢"){{
				addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						AudioChat.getInstance().init();
					}
				});
			}});
			setPreferredSize(new Dimension(300,40));
		}});
		input = new JTextField();
		input.setPreferredSize(new Dimension(230, 40));
		send = new JButton("·¢ËÍ"){{
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					NetHelper.getInstance().sendText(input.getText());
					ChatBox.getInstance().addText(Control.getInstance().me.name,input.getText());
					ChatBox.getInstance().input.setText("");
				}
			});
		}};
		below.add(new JPanel(){{
			add(input);
			add(send);
			setPreferredSize(new Dimension(300,40));
		}});
		add(below, BorderLayout.SOUTH);
	}
	public static ChatBox getInstance(){
		if(chatBox == null){
			chatBox = new ChatBox();
			}
		return chatBox;
	}
	public void addText(String name, String s){
		StringBuilder cache = new StringBuilder(box.getText());
		cache.append('\n');
		cache.append(name);
		Date date = new Date();
		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
		cache.append(dateFormat.format(date)+"\n");
		cache.append(s);
		box.setText(cache.toString());
		Point p = new Point();
		p.setLocation(0,box.getLineCount()*30);
		scroll.getViewport().setViewPosition(p);
	}
}
