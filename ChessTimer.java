import javax.swing.JOptionPane;

abstract class Timer extends Thread{
	protected int time;
	public Timer(int totalTime) {
		time = totalTime;
	}
	@Override
	public void run() {
		while(!exit()){
			try {
				sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(canRun()){
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setTime();
				if(--time==0){
					timeup();
					break;
				}
			}
		}
	}
	@Override
	public synchronized void start() {
		super.start();
		setTime();
	}
	public synchronized void reStart(int newTime){
		time = newTime;
		setTime();
	}
	protected boolean exit(){
		return false;
	}
	protected abstract void setTime();
	abstract protected void timeup();
	protected boolean canRun(){
		return true;
	};
}
public class ChessTimer {
	private static ChessTimer instance;
	public int myTime;
	public int opponentTime;
	public Timer myTimer;
	public Timer opponentTimer;
	private ChessTimer(){}
	public static ChessTimer getInstance(){
		if(instance==null){
			instance=new ChessTimer();
		}
		return instance;
	}
	public void start(int myTime, int opponentTime){
		myTimer = new Timer(myTime){//我方计时器
			@Override
			protected boolean canRun() {
				return Model.getInstance().period==GamePeriod.ON&&
						Control.getInstance().color==Control.getInstance().me.color;
			}

			@Override
			protected void timeup() {
				Model.getInstance().period=GamePeriod.FREE;
				if(Login.status==Status.WATCHER){
					WatcherEndDialog.getInstance().init(Control.getInstance().me.color);
				}
				else{
					LoseWindow.getInstance().setVisible(true);
					JOptionPane.showMessageDialog(null, "您的时间已到！");
				}
			}

			@Override
			protected void setTime() {
				View.getInstance().me.time.setText(time%60>=10?(time/60+":"+time%60):(time/60+":0"+time%60));
				ChessTimer.this.myTime = time;
			}
			
		};
		myTimer.start();
		opponentTimer = new Timer(opponentTime){//对方计时器
			@Override
			protected boolean canRun() {
				return Model.getInstance().period==GamePeriod.ON&&
						Control.getInstance().color==Control.getInstance().opponent.color;
			}

			@Override
			protected void timeup() {
				if(Login.status==Status.WATCHER){
					WatcherEndDialog.getInstance().init(Control.getInstance().opponent.color);
				}
				else{
					WinWindow.getInstance().setVisible(true);
					JOptionPane.showMessageDialog(null, "对方时间已到！");
					Control.getInstance().me.score++;
					//段位增加
				}
			}

			@Override
			protected void setTime() {
				View.getInstance().opponent.time.setText(time%60>=10?(time/60+":"+time%60):(time/60+":0"+time%60));
				ChessTimer.this.opponentTime = time;
			}
		};
		opponentTimer.start();
	}
}
