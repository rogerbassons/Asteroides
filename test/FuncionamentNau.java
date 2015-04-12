import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.geom.Path2D;
	
public class FuncionamentNau {
	JFrame f_;
	NauDrawer nd_;
	Nau n_;

	boolean sortir_;
	boolean ferPiu_;
	boolean rotarDreta_, rotarEsquerra_,gas_;
	
	public static void main(String[] args) throws Exception
	{
		FuncionamentNau t = new FuncionamentNau();
		t.jugar();
	}
	
	FuncionamentNau() throws Exception
	{
		f_ = new JFrame("FuncionamentNau");
		f_.setSize(1024,768);
		f_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f_.setResizable(false);
		f_.getContentPane().setBackground(Color.BLACK);

		int xcenter = 1024/2;
		int ycenter = 768/2;
		n_ = new Nau(50,20);
		n_.centrar(1024,768);
		nd_ = new NauDrawer();
		KeyListener listener = new MyKeyListener();
		f_.addKeyListener(listener);
		f_.setFocusable(true);
		f_.setVisible(true);
		f_.add(nd_);

		sortir_ = ferPiu_ = rotarEsquerra_ = rotarDreta_ = gas_ = false;
	}

	public void jugar() throws Exception
	{
		int xcenter = 1024/2;
		int ycenter = 768/2;
		File so = new File("../res/piu.wav");
		AudioInputStream a = AudioSystem.getAudioInputStream(so);
		Clip c = AudioSystem.getClip();
		c.open(a);

		//Limitador de FPS
		//http://www.java-gaming.org/index.php?topic=24220.0
		long lastLoopTime = System.nanoTime();
		final int TARGET_FPS = 60;
		final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
		int fps = 0;
		int lastFpsTime = 0;
		
		while (!sortir_) {
			// work out how long its been since the last update, this
			// will be used to calculate how far the entities should
			// move this loop
			long now = System.nanoTime();
			long updateLength = now - lastLoopTime;
			lastLoopTime = now;
			double delta = updateLength / ((double)OPTIMAL_TIME);

			// update the frame counter
			lastFpsTime += updateLength;
			fps++;
      
			// update our FPS counter if a second has passed since
			// we last recorded
			if (lastFpsTime >= 1000000000) {
					System.out.println("(FPS: "+fps+")");
					lastFpsTime = 0;
					fps = 0;
			}
			
			if (rotarDreta_) {
				n_.rotarDreta();
			} else if (rotarEsquerra_) {
				n_.rotarEsquerra();
			} else {
				n_.pararRotacio();
			}

			if (gas_) {
				n_.propulsarEndavant();
			} else {
				n_.frenar();
			}
			n_.moure(1024,768);
			nd_.repaint();
			if (ferPiu_ && !c.isRunning()) {
				c.setFramePosition(0);
				c.start();
			}
			Thread.sleep( (lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000 );
		}
		f_.dispose();
	}

	private class NauDrawer extends JPanel {
		
		NauDrawer() {
			setOpaque(false);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g; 
			n_.dibuixar(g2);
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
			case KeyEvent.VK_UP:
				gas_ = true;
				break;
			case KeyEvent.VK_RIGHT:
				rotarDreta_ = true;
				break;
			case KeyEvent.VK_LEFT:
				rotarEsquerra_ = true;
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
			case KeyEvent.VK_UP:
				gas_ = false;
				break;
			case KeyEvent.VK_RIGHT:
				rotarDreta_ = false;
				break;
			case KeyEvent.VK_LEFT:
				rotarEsquerra_ = false;
				break;
			}
		}
	}
}
