import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.geom.Path2D;
import java.awt.Canvas;
import java.awt.image.BufferStrategy;
import java.awt.RenderingHints;
	
public class FuncionamentNauActive {
	JFrame f_;
	Canvas c_;
	Nau n_;

	boolean sortir_;
	boolean ferPiu_;
	boolean rotarDreta_, rotarEsquerra_,gas_;
	
	public static void main(String[] args) throws Exception
	{
		FuncionamentNauActive t = new FuncionamentNauActive();
		t.jugar();
	}
	
	FuncionamentNauActive() throws Exception
	{
		f_ = new JFrame("FuncionamentNau");
		f_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f_.setResizable(false);
		f_.getContentPane().setBackground(Color.BLACK);
		f_.setIgnoreRepaint(true);

		int xcenter = 1024/2;
		int ycenter = 768/2;
		n_ = new Nau(50);//,20);
		n_.centrar(1024,768);
		
		KeyListener listener = new MyKeyListener();
		f_.addKeyListener(listener);

		c_ = new Canvas();
		c_.setSize(1024,768);
		c_.setIgnoreRepaint(true);
		c_.setBackground(Color.BLACK);
		
		f_.setFocusable(true);
		f_.setVisible(true);
		
		f_.add(c_);
		f_.pack();

		sortir_ = ferPiu_ = rotarEsquerra_ = rotarDreta_ = gas_ = false;
	}

	private Clip reprodueix(File f) throws Exception
	{
		AudioInputStream a = AudioSystem.getAudioInputStream(f);
		Clip c = AudioSystem.getClip();
		c.open(a);
		c.setFramePosition(0);
		return c;
	}

	public void jugar() throws Exception
	{
		File so = new File("../res/piu.wav");
		Clip piu = reprodueix(so);
		File background = new File("../res/soDeFons/background.wav");
		Clip b = reprodueix(background);
		b.loop(Clip.LOOP_CONTINUOUSLY);
				

		c_.createBufferStrategy(2);
		BufferStrategy buffer = c_.getBufferStrategy();
		
		while (!sortir_) {
			
			update();
		
			if (ferPiu_ && !piu.isRunning()) {
				piu.setFramePosition(0);
				piu.start();
			}

			Graphics g = buffer.getDrawGraphics();
			g.clearRect(0,0,1024,768);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			n_.dibuixar(g2);

			if(!buffer.contentsLost()) {
				buffer.show();
			}
						
			Thread.sleep(10);
			g.dispose();

		}
		f_.dispose();
	}

	private void update() {
		if (rotarDreta_) {
			n_.rotarDreta();
		} else if (rotarEsquerra_) {
			n_.rotarEsquerra();
		} else {
			n_.pararRotacio();
		}

		if (gas_) {
			n_.propulsarEndavant();
		}
		n_.moure(1024,768);
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
