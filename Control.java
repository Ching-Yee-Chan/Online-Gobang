import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;

public class Control {
	private static Control control;
	public boolean color=true;
	public UserInfo me;
	public UserInfo opponent;
	public static Music bgm = new Music();
	public boolean isPlaying = true;
	private static Login login;
	private Control(){}

	public static void main(String[] args) {
		login = new Login();
	}

	public static Control getInstance() {
		if(control==null){
			control = new Control();
		}
		return control;
	}

	public void init(boolean isWatcher) {
		login.setVisible(false);
		if(isWatcher){
			View.getInstance().watcherInit();
		}
		else{
			View.getInstance().init();			
		}
		bgm.loop();
	}
}
class Music{
	private AudioClip music;
	public boolean isPlaying = false;
	public Music() {
		try {
			music = Applet.newAudioClip(new File("src/music/bgm.wav").toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void loop(){
		if(isPlaying){
			return;
		}
		isPlaying = true;
		bgmController.getInstance().setText("Õ£÷π±≥æ∞“Ù¿÷");
		music.loop();
	}
	void stop(){
		if(isPlaying){
			isPlaying = false;
			bgmController.getInstance().setText("≤•∑≈±≥æ∞“Ù¿÷");
			music.stop();
		}
	}
}