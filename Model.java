import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

enum GamePeriod{
	FREE, ON, PAUSE;
}
class ChessNode{
	public int row;
	public int col;
	public boolean color;
	public int removeMark = 0;//正常棋为0；被悔棋为1，悔棋记录为2
	public ChessNode(int row, int col, boolean color) {
		super();
		this.row = row;
		this.col = col;
		this.color = color;
	}
}

public class Model {
	private static Model model;
	public GamePeriod period = GamePeriod.FREE;
	public static int winLength = 5;	//后期可改n子棋
	private int[][] board = new int[16][16];
	public LinkedList<ChessNode> record = new LinkedList<ChessNode>();
	AudioClip chessAudio;
	private Model(){
		try {
			chessAudio = Applet.newAudioClip(new File("src/music/putChess.wav").toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	public static Model getInstance() {
		if(model==null){
			model=new Model();
		}
		return model;
	}
	public boolean putChess(int row, int col, boolean color){
		if(board[row][col]!=0) return false;
		record.add(new ChessNode(row, col, color));
		if(color){
			board[row][col] = 1;
		}
		else{
			board[row][col] = -1;
		}
		Board.getInstance().repaint();
		chessAudio.play();
		int winside = win();
		if(winside!=0){
			if(winside==1&&Control.getInstance().me.color||
					winside == -1&&!Control.getInstance().me.color){
				if(Login.status == Status.WATCHER){
					WatcherEndDialog.getInstance().init(false);
				}
				else{
					WinWindow.getInstance().setVisible(true);
				}
				Control.getInstance().me.score++;
				//段位积分增加
			}
			else{
				Control.getInstance().opponent.score++;
				if(Login.status == Status.WATCHER){
					WatcherEndDialog.getInstance().init(true);
				}
				else{
					LoseWindow.getInstance().setVisible(true);
				}
			}
		}
		Control.getInstance().color = !Control.getInstance().color;
		return true;
	}
	public int getPos(int row, int col){
		return board[row][col];
	}
	public int win() {
		final int c = Model.getInstance().record.getLast().color?1:-1;//1为黑胜，-1为白胜，0为继续
		WinSearch oneDirection = new RowSearch();
		if(oneDirection.search(c)){
			return c;
		}
		oneDirection = new ColSearch();
		if(oneDirection.search(c)){
			return c;
		}
		oneDirection = new MainRectangleSearch();
		if(oneDirection.search(c)){
			return c;
		}
		oneDirection = new AssistantRectangleSearch();
		if(oneDirection.search(c)){
			return c;
		}
		return 0;
	}
	public boolean canBack(boolean color){
		ChessNode node = getLastNode(color);
		if(node==null){
			JOptionPane.showMessageDialog(null, "棋盘上已经无棋可悔啦！(ÒωÓױ)", "错误",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(Login.status!=Status.WATCHER&&color!=Control.getInstance().color){
			JOptionPane.showMessageDialog(null, "现在您不能悔棋~", "错误",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	public void back(boolean color) {
		ChessNode node = getLastNode(color);
		board[node.row][node.col] = 0;
		node.removeMark = 1;
		ChessNode backMark = new ChessNode(node.row, node.col, color);
		backMark.removeMark = 2;
		record.addLast(backMark);
		if(color==Control.getInstance().me.color){
			Control.getInstance().me.huiqi--;
			View.getInstance().me.huiqi.setText(String.valueOf(Control.getInstance().me.huiqi));
		}
		else{
			Control.getInstance().opponent.huiqi--;
			View.getInstance().opponent.huiqi.setText(String.valueOf(Control.getInstance().opponent.huiqi));
		}
		Board.getInstance().repaint();
	}
	public ChessNode getLastNode(boolean color){
		for (int i = record.size()-1; i >=0 ; i--) {
			if(record.get(i).color==color&&record.get(i).removeMark==0){
				return record.get(i);
			}
		}
		return null;
	}
	public void remake() {
		WinWindow.getInstance().setVisible(false);
		LoseWindow.getInstance().setVisible(false);
		Control.getInstance().me.color = !Control.getInstance().me.color;
		Control.getInstance().me.huiqi = 3;
		View.getInstance().me.refresh(Control.getInstance().me);
		Control.getInstance().opponent.color = !Control.getInstance().opponent.color;
		Control.getInstance().me.huiqi = 3;
		View.getInstance().opponent.refresh(Control.getInstance().opponent);
		record.clear();
		clear();
		Board.getInstance().repaint();
		Control.getInstance().color = true;
	}
	public void clear() {
		for (int row = 1; row <= 15; row++) {
			for (int col = 1; col <= 15; col++) {
				board[row][col] = 0;
			}
		}
	}
	public void clearNode(ChessNode node) {
		if(node.removeMark==2){//悔棋记录，返回原色
			board[node.row][node.col] = node.color?1:-1;
		}
		else board[node.row][node.col] = 0;
		Board.getReviewInstance().repaint();
	}
	public void nextNode(ChessNode node) {
		if(node.removeMark==2){//悔棋记录，置空
			board[node.row][node.col] = 0;
		}
		else board[node.row][node.col] = node.color?1:-1;
		Board.getReviewInstance().repaint();
	}
}
abstract class WinSearch{						//田字格四方向公用判断父类
	protected int row = Model.getInstance().record.getLast().row;
	protected int col = Model.getInstance().record.getLast().col;
	public boolean search(int c){				//公用遍历函数
		int num = 0;
		for(int i=-Model.winLength;i<=Model.winLength;i++){
			if(outOfBound(i)){					//越界条件不同，使用抽象方法outOfBound()
				continue;
			}
			else if(judge(i, c)){				//判断条件不同，使用抽象方法judge()
				num++;
			}
			else{
				num=0;
			}
			if(num==Model.winLength){
				return true;
			}
		}
		return false;
	}
	abstract protected boolean judge(int i, int c);
	abstract protected boolean outOfBound(int i);	
}

class RowSearch extends WinSearch{				//按行遍历的子类

	@Override
	protected boolean judge(int i, int c) {
		return Model.getInstance().getPos(row, col+i)==c;
	}

	@Override
	protected boolean outOfBound(int i) {
		return col+i<=0||col+i>15;
	}
	
}

class ColSearch extends WinSearch{				//按列遍历的子类

	@Override
	protected boolean judge(int i, int c) {
		return Model.getInstance().getPos(row+i, col)==c;
	}

	@Override
	protected boolean outOfBound(int i) {
		return row+i<=0||row+i>15;
	}
	
}

class MainRectangleSearch extends WinSearch{	//按左上到右下对角线遍历的子类

	@Override
	protected boolean judge(int i, int c) {
		return Model.getInstance().getPos(row+i, col+i)==c;
	}

	@Override
	protected boolean outOfBound(int i) {
		return row+i<=0||row+i>15||col+i<=0||col+i>15;
	}
	
}

class AssistantRectangleSearch extends WinSearch{//按右上到左下对角线遍历的子类

	@Override
	protected boolean judge(int i, int c) {
		return Model.getInstance().getPos(row+i, col-i)==c;
	}

	@Override
	protected boolean outOfBound(int i) {
		return row+i<=0||row+i>15||col-i<=0||col-i>15;
	}
	
}
