import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Board extends JPanel{
	private static Board board;
	private static Board reviewBoard;
	private int width;
	private int height;
	public BufferedImage image;
	private Board(){
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(canPutChess()){
					int row = 0;
					int col = 0;
					if(e.getX()%width<=0.2*width){
						col = e.getX()/width;
					}
					else if(e.getX()%width>=0.8*width){
						col = e.getX()/width+1;
					}
					if(e.getY()%height<=0.2*height){
						row = e.getY()/height;
					}
					else if(e.getY()%height>=0.8*height){
						row= e.getY()/height+1;
					}
					if(row!=0&&col!=0){
						if(Model.getInstance().putChess(row, col, Control.getInstance().me.color)){
							NetHelper.getInstance().putChessToOpponent(row, col, Control.getInstance().me.color);
						}
					}
				}
				super.mouseClicked(e);
			}
		});
	}
	public static Board getInstance(){
		if(board == null){
			board = new Board();
		}
		return board;
	}
	public static Board getReviewInstance(){
		if(reviewBoard == null){
			reviewBoard = new Board();
		}
		return reviewBoard;
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		ImageIcon initial = new ImageIcon("src/images/board.png");
		initial.setImage(initial.getImage().getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_DEFAULT));
		g.drawImage(initial.getImage(), 0, 0, null);
		width = getWidth()/16;
		height = getHeight()/16;
		for (int i = 0; i < 15; i++) {
			g.drawLine(width*(i+1), height, width*(i+1), height*15);
			g.drawLine(width, height*(i+1), width*15, height*(i+1));
		}
		g.fillOval((int)(7.9*width), (int)(7.9*height), (int)(0.2*width), (int)(0.2*height));
		g.fillOval((int)(3.9*width), (int)(3.9*height), (int)(0.2*width), (int)(0.2*height));
		g.fillOval((int)(11.9*width), (int)(3.9*height), (int)(0.2*width), (int)(0.2*height));
		g.fillOval((int)(3.9*width), (int)(11.9*height), (int)(0.2*width), (int)(0.2*height));
		g.fillOval((int)(11.9*width), (int)(11.9*height), (int)(0.2*width), (int)(0.2*height));
		for (int row=1;row<=15;row++) {
			for(int col = 1;col<=15;col++){
				if(Model.getInstance().getPos(row, col)==1){
					if(width>45){
						g.drawImage(new ImageIcon("src/images/blackBig.png").getImage(), width*col-25, height*row-25, null);
					}
					else{
						g.drawImage(new ImageIcon("src/images/blackNode.png").getImage(), width*col-15, height*row-15, null);
					}
				}
				else if(Model.getInstance().getPos(row, col)==-1){
					if(width>45){
						g.drawImage(new ImageIcon("src/images/whiteBig.png").getImage(), width*col-25, height*row-25, null);
					}
					else{
						g.drawImage(new ImageIcon("src/images/whiteNode.png").getImage(), width*col-15, height*row-15, null);
					}
				}
			}
		}
	}
	protected boolean canPutChess(){
		if(Login.status==Status.WATCHER){
			return false;
		}
		if(Model.getInstance().period!=GamePeriod.ON){
			return false;
		}
		return Control.getInstance().color == Control.getInstance().me.color;
	}
	public RenderedImage getImage() {
		if(image==null) image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
        paint(g);
        g.dispose();
		return image;
	}
}
