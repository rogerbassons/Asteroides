import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
	
public class MostrarTriangle {
	PolygonDrawer pd_;
	JFrame f_;
	Polygon pol_;

	boolean sortir_;
	boolean ferPiu_;

	public static void main(String[] args) throws Exception
	{
		MostrarTriangle t = new MostrarTriangle();
		t.jugar();
	}
	
	MostrarTriangle() throws Exception
	{
		f_ = new JFrame("MostrarTriangle");
		f_.setSize(1024,768);
		f_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f_.getContentPane().setBackground(Color.BLACK);

		int xcenter = 1024/2;
		int ycenter = 768/2;
		pol_ = new Polygon(new int []{xcenter, xcenter+50, xcenter-50},new int []{ycenter-50, ycenter+50, ycenter+50},3);
		pd_ = new PolygonDrawer();
		KeyListener listener = new MyKeyListener();
		f_.addKeyListener(listener);
		f_.setFocusable(true);
		f_.setVisible(true);

		pd_.setPolygon(pol_);
		f_.add(pd_);

		sortir_ = ferPiu_ = false;
	}

	public void jugar() throws Exception
	{
		int xcenter = 1024/2;
		int ycenter = 768/2;
		File so = new File("../res/piu.wav");
		AudioInputStream a = AudioSystem.getAudioInputStream(so);
		Clip c = AudioSystem.getClip();
		c.open(a);
		while (!sortir_) {
			if (pol_.ypoints[0] < 0) {
				pol_ = new Polygon(new int []{xcenter, xcenter+50, xcenter-50},new int []{768-100, 768, 768},3);
				pd_.setPolygon(pol_);
			} else {
				pol_.translate(0,-1);
				pd_.repaint();
			}
			if (ferPiu_ && !c.isRunning()) {
				c.setFramePosition(0);
				c.start();
			}
			//Thread.sleep(1);
		}
		f_.dispose();
	}

	private static class PolygonDrawer extends JPanel {
		private Polygon p;

		PolygonDrawer() {
			setOpaque(false);
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillPolygon(p);
		}

		public void setPolygon(Polygon p) {
			this.p = p;
		}
	}

	public class MyKeyListener implements KeyListener {
		
		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e) 
		{
			switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				ferPiu_ = true;
				break;
			}
		}
		
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				ferPiu_ = false;
				break;
			case KeyEvent.VK_ESCAPE:
				sortir_ = true;
				break;
			}
		}
	}
}
