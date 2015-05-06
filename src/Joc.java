import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Random;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class Joc {
	
	int amplada_;
	int altura_;
	Nau n_;
	//NauEnemiga ne_;
	DibuixadorAsteroides d_;
	LinkedList<Meteorit> meteorits_;
	LinkedList<RaigLaser> rajosLaser_;
	
	boolean sortir_;
	boolean disparar_;
	boolean rotarEsquerra_, rotarDreta_, accelerar_;
	
	public static void main(String[] args) throws Exception {
		Joc j = new Joc(1024, 768);
		j.jugar();
	}
	
	//Pre: amplada >= 800 i altura >= 600
	//Post: s'ha creat una nova partida en un espai d'amplada x altura
	Joc(int amplada, int altura) throws Exception {
		
		sortir_ = disparar_ = rotarEsquerra_ = rotarDreta_ = accelerar_ = false;
		
		meteorits_ = new LinkedList<Meteorit>();
		rajosLaser_ = new LinkedList<RaigLaser>();
		
		amplada_ = amplada;
		altura_ = altura;
		
		n_ = new Nau(50);
		n_.centrar(amplada_, altura_);
		
		//ne_ = new NauEnemiga(50);
		//ne_.centrar(amplada_, altura_);
		
		d_ = new DibuixadorAsteroides();
		d_.crearFinestra(amplada_, altura_, Color.BLACK, "Joc");
		d_.afegirKeyListener(new MyKeyListener());
		d_.afegir(n_);
		//d_.afegir(ne_);
	}
	
	public void jugar() throws Exception {
		
		File so = new File("../res/piu.wav");
		AudioInputStream a = AudioSystem.getAudioInputStream(so);
		Clip c = AudioSystem.getClip();
		c.open(a);
		
		while (meteorits_.size() < 10) 
			generarMeteorit();
		
		while (!sortir_) {
			actualitzar();
			
			if (disparar_ && !c.isRunning()) {
				c.setFramePosition(0);
				c.start();
				RaigLaser r = n_.disparar();
				rajosLaser_.add(r);
				d_.afegir(r);
				
				if (meteorits_.size() < 10){
					generarMeteorit();
				}
			}
			d_.dibuixar();
			
			Thread.sleep(10);
		}
		
		d_.tancarFinestra();
	
	}
	
	private void generarMeteorit() throws Exception {
		Random rand = new Random();
		Meteorit m = new Meteorit(0.8, rand.nextInt(360), 1, rand.nextInt(amplada_), rand.nextInt(altura_));
		meteorits_.add(m);
		d_.afegir(m);
	}
	
	private void actualitzar() {
		if (rotarDreta_) {
			n_.rotarDreta();
		} else if (rotarEsquerra_) {
			n_.rotarEsquerra();
		} else {
			n_.pararRotacio();
		}

		if (accelerar_) {
			n_.propulsarEndavant();
		}

		n_.moure(amplada_,altura_);

		//ne_.atacarNau(n_);
		//ne_.moure(amplada_,altura_);
		
		Iterator<RaigLaser> it = rajosLaser_.iterator();
		while (it.hasNext()){
			RaigLaser r = it.next();
			if (!r.gastat())
				r.moure(amplada_, altura_);
			else{
				it.remove();
				//d_.eliminar(r);
			}
		}
		
		Iterator<Meteorit> it2 = meteorits_.iterator();
		while (it2.hasNext())
			it2.next().moure(amplada_, altura_);
		
	}
	
	public class MyKeyListener implements KeyListener {
		
		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e) 
		{
			switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				disparar_ = true;
				break;
			case KeyEvent.VK_W:
				accelerar_ = true;
				break;
			case KeyEvent.VK_D:
				rotarDreta_ = true;
				break;
			case KeyEvent.VK_A:
				rotarEsquerra_ = true;
				break;
			}
		}
		
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				disparar_ = false;
				break;
			case KeyEvent.VK_ESCAPE:
				sortir_ = true;
				break;
			case KeyEvent.VK_W:
				accelerar_ = false;
				break;
			case KeyEvent.VK_D:
				rotarDreta_ = false;
				break;
			case KeyEvent.VK_A:
				rotarEsquerra_ = false;
				break;
			}
		}
	}
}