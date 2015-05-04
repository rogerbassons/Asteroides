import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
	
public class FuncionamentDibuixador {

	int amplada_;
	int altura_;
	Nau n_;

	DibuixadorAsteroides dib_;
	
	boolean sortir_;
	boolean ferPiu_;
	boolean rotarDreta_, rotarEsquerra_,gas_;
	
	public static void main(String[] args) throws Exception
	{
		FuncionamentDibuixador t = new FuncionamentDibuixador();
		t.jugar();
	}
	
	FuncionamentDibuixador() throws Exception
	{
		sortir_ = ferPiu_ = rotarEsquerra_ = rotarDreta_ = gas_ = false;
		
		amplada_ = 800;
		altura_ = 600;
		
		n_ = new Nau(50);
		n_.centrar(amplada_,altura_);
		
		

		dib_ = new DibuixadorAsteroides();
		dib_.crearFinestra(amplada_,altura_,Color.BLACK,"Funcionament Dibuixador");
		
		dib_.afegirKeyListener(new MyKeyListener());

		dib_.afegir(n_);
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
				

		while (!sortir_) {
			
			update();
		
			if (ferPiu_ && !piu.isRunning()) {
				piu.setFramePosition(0);
				piu.start();
			}

			dib_.dibuixar();
						
			Thread.sleep(10);

		}
		dib_.tancarFinestra();
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
		n_.moure(amplada_,altura_);
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
