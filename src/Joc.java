import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.awt.geom.Area;

//Mòdul que gestiona la lògica, la física i la interfície del joc
//El Joc conté quatre tipus d'elements:
//	- Nau: Nau pròpia controlada per l'usuari
//	- NauEnemiga: Nau controlada per la màquina (AI)
//	- Meteorits: Objectes controlats per la màquina
//	- RaigLasers: raig disparat per Nau i NauEnemiga, que destrueix Meteorits i Naus

public class Joc {
	
	int amplada_;
	int altura_;
	Nau n_;
	NauEnemiga ne_;
	DibuixadorAsteroides d_;
	LinkedList<Meteorit> meteorits_;
	LinkedList<RaigLaser> rajosLaser_;
	
	boolean sortir_;
	boolean disparar_;
	boolean rotarEsquerra_, rotarDreta_, accelerar_;
	
	Clip piu_;
	
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
		
		ne_ = new NauEnemiga(50, 200, 600);
		
		d_ = new DibuixadorAsteroides();
		d_.crearFinestra(amplada_, altura_, Color.BLACK, "Joc");
		d_.afegirKeyListener(new MyKeyListener());
		d_.afegir(n_);
		d_.afegir(ne_);
	}
	
	//Pre: --
	//Post: es comença a jugar la partida
	public void jugar() throws Exception {
		
		File so = new File("../res/piu.wav");
		AudioInputStream a = AudioSystem.getAudioInputStream(so);
		piu_ = AudioSystem.getClip();
		piu_.open(a);

		while (!sortir_) {
			actualitzar();
			d_.dibuixar();
			Thread.sleep(10);
		}
		
		d_.tancarFinestra();
	
	}
	
	//Pre: --
	//Post: afegeix Meteorits al DibuixadorAsteroides i al Joc, fins a un màxim de 10
	private void generarMeteorits() throws Exception {
		while (meteorits_.size() < 10) {
			Random rand = new Random();
			Meteorit m = new Meteorit(0.8, rand.nextInt(360), 1, rand.nextInt(amplada_), rand.nextInt(altura_));
			meteorits_.add(m);
			d_.afegir(m);
		}
		//Falta comprovar que no es col·loquin on hi ha la nau
	}
	
	//Pre: --
	//Post: actualitza l'estat del joc, generant meteorits, movent els elements i tractant les col·lisions
	private void actualitzar() throws Exception {
		
		generarMeteorits();
		
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

		if (disparar_ && !piu_.isRunning()) { //SOLUCIÓ TEMPORAL
			piu_.setFramePosition(0);
			piu_.start();
			RaigLaser r = n_.disparar();
			rajosLaser_.add(r);
			d_.afegir(r);
		}

		n_.moure(amplada_,altura_);

		ne_.atacarNau(n_);
		ne_.moure(amplada_,altura_);
		
		Iterator<RaigLaser> it = rajosLaser_.iterator();

		while (it.hasNext()){
			RaigLaser r = it.next();
			if (!r.gastat())
				r.moure(amplada_, altura_);
			else{
				it.remove();
				d_.elimina(r);
			}
		}
		
		Iterator<Meteorit> it2 = meteorits_.iterator();
		while (it2.hasNext())
			it2.next().moure(amplada_, altura_);
		
		tractarColisions();
		
	}
	
	private void tractarColisions() throws Exception {
		
		//CODI PER NETEJAR
		//Col·lisions Nau - Meteorit
		Area n = new Area(n_.obtenirShape());
		tractarColisionsAmbMeteorits(n);
		
		//Col·lisions RaigLaser - Meteorit
		for (RaigLaser r : rajosLaser_) {
			Area rl = new Area(r.obtenirShape());
			tractarColisionsAmbMeteorits(rl);
		}
		
		
		//Falten col·lisions NauEnemiga amb Meteorits i RaigLaser amb NauEnemiga
		
		
	}
	
	private void tractarColisionsAmbMeteorits(Area a) throws Exception {
		LinkedList<Meteorit> meteoritsNous_ = new LinkedList<Meteorit>();
		ListIterator<Meteorit> it = meteorits_.listIterator();
		while (it.hasNext()){
			Meteorit m = it.next();
			Area am = new Area(m.obtenirShape());
			am.intersect(a);
			if (!am.isEmpty()) {
				if (m.divisible()) {
					Meteorit m2 = m.dividir();
					it.add(m);
					meteoritsNous_.add(m2);
					d_.afegir(m2);
				}
				else {
					it.remove();
					d_.elimina(m);
				}
				
			}
		}
		meteorits_.addAll(meteoritsNous_);
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