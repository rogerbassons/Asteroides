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
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.awt.Shape;

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
	
	Calendar tempsRaigEnemiga_;
	Calendar tempsRaigJugador_;
	
	int nVides_;
	int puntuacio_;
	
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
		
		n_ = new Nau(50,Color.GREEN);
		n_.centrar(amplada_, altura_);
		Random rand = new Random();
		
		ne_ = new NauEnemiga(50, Color.RED, rand.nextInt(amplada_-30)+30, rand.nextInt(altura_-30)+30);
		
		d_ = new DibuixadorAsteroides();
		d_.crearFinestra(amplada_, altura_, Color.BLACK, "Joc");
		d_.afegirKeyListener(new MyKeyListener());
		d_.afegir(n_);
		d_.afegir(ne_);

		tempsRaigEnemiga_ = new GregorianCalendar();
		tempsRaigJugador_ = new GregorianCalendar();

		nVides_ = 3;
		puntuacio_ = 0; 
	}
	
	//Pre: --
	//Post: es comença a jugar la partida
	public void jugar() throws Exception {
		
		File so = new File("../res/piu.wav");
		AudioInputStream a = AudioSystem.getAudioInputStream(so);
		piu_ = AudioSystem.getClip();
		piu_.open(a);
		
		generarMeteoritsInicials();
		
		while (!sortir_) {
			actualitzar();
			d_.dibuixar();
			Thread.sleep(10);
		}
		
		d_.tancarFinestra();
	
	}
	
	//Pre: --
	//Post: afegeix Meteorits al DibuixadorAsteroides i al Joc, fins a un màxim de 10
	private void generarMeteoritsInicials() throws Exception {
		while (meteorits_.size() < 10) {
			Random rand = new Random();
			Meteorit m;
			Area am, an;
			do {
				m = new Meteorit(0.8, rand.nextInt(360), 1, rand.nextInt(amplada_), rand.nextInt(altura_));
				am = new Area(m.obtenirShape());
				an = new Area(n_.obtenirShape());
				am.intersect(an);
			} while (!am.isEmpty()); //Comprovem que al col·locar els Meteorits inicials no estiguin sobre la Nau
			meteorits_.add(m);
			d_.afegir(m);
		}
	}
	
	//Pre: --
	//Post: afegeix Meteorits al DibuixadorAsteroides i al Joc, fins a un màxim de 10, i els fa sortir per les bandes de la pantalla
	private void generarMeteorits() throws Exception {
		while (meteorits_.size() < 10) {
			Random rand = new Random();
			Meteorit m = new Meteorit(0.8, rand.nextInt(360), 1, rand.nextInt(amplada_), rand.nextInt(altura_));
			m.situarAlCostatMesProper(amplada_, altura_);
			meteorits_.add(m);
			d_.afegir(m);
		}
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

		n_.moure(amplada_,altura_);

		if (disparar_) {
			Calendar tempsActual = new GregorianCalendar();
			if (tempsActual.getTimeInMillis() - tempsRaigJugador_.getTimeInMillis() > 441) {
				tempsRaigJugador_ = new GregorianCalendar();
				piu_.setFramePosition(0);
				piu_.start();
				RaigLaser r = n_.disparar();
				rajosLaser_.add(r);
				d_.afegir(r);
			}
		}
		
		RaigLaser ra = ne_.atacarNau(n_);
		if (ra!=null) {
			Calendar tempsActual = new GregorianCalendar();
			if (tempsActual.getTimeInMillis() - tempsRaigEnemiga_.getTimeInMillis() > 1000) {
				tempsRaigEnemiga_ = new GregorianCalendar();
				piu_.setFramePosition(0);
				piu_.start();
				d_.afegir(ra);
				rajosLaser_.add(ra);
			}
		}
		
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
	
	//Pre: --
	//Post: tracta les col·lisions entre els objectes del Joc
	private void tractarColisions() throws Exception {
		
		//Col·lisions Nau - NauEnemiga
		tractarColisionsEntreNaus();
		
		//Col·lisions Nau i NauEnemiga amb RaigLaser
		tractarColisionsNausRajosLaser();
		
		
		//Col·lisions Nau i NauEnemiga amb Meteorit
		tractarColisionsNausMeteorit();
		
		//Col·lisions RaigLaser - Meteorit
		tractarColisionsRaigLaserMeteorit();
		
	}
	
	//Pre: --
	//Post: si la nau està viva es centra la nau i se li resta una vida, altrament no fa res (de moment). Si té 0 vides, mor.
	private void xocarNauJugador() throws Exception {
		//if (n_.esViva()){
			n_.centrar(amplada_, altura_);
			nVides_--;
			//if (nVides_ == 0) 
				//n_.morir();
		//}
		//FALTA TRACTAR MORT
	}
	
	//Pre: --
	//Post: posiciona la NauEnemiga en un lloc pseudoaleatori dins l'espai de joc. DE MOMENT NO MOR
	private void xocarNauEnemiga() throws Exception {
		//MORIR, REACCIÓ PROVISIONAL
		//ne_.morir();
		puntuacio_ += 100;
		d_.elimina(ne_);
		Random rand = new Random();
		ne_ = new NauEnemiga(50, Color.RED,rand.nextInt(amplada_-50)+50, rand.nextInt(altura_-50)+50);
		d_.afegir(ne_);
	}
	
	//Pre: --
	//Post: si les naus colisionen la Nau del jugador es centra i la NauEnemiga es canvia de lloc altrament no fa res
	private void tractarColisionsEntreNaus() throws Exception {
		Area n = new Area(n_.obtenirShape());
		Area ne = new Area(ne_.obtenirShape());
		n.intersect(ne);
		if (!n.isEmpty()) {
			xocarNauJugador();
			xocarNauEnemiga();
		}
	}
	
	//Pre: --
	//Post: els RaigLaser que hagin col·lisionat amb un Meteorit s'han eliminat i els Meteorits s'han dividit o bé eliminat segons la seva mida
	private void tractarColisionsRaigLaserMeteorit() throws Exception {
		Iterator<RaigLaser> it = rajosLaser_.iterator();
		while (it.hasNext()) {
			RaigLaser r = it.next();
			Area rl = new Area(r.obtenirShape());
			boolean haXocat = tractarColisionsAmbMeteorits(rl);

			if (haXocat) {
				it.remove();
				d_.elimina(r);
			}
		}
	}
	
	//Pre: --
	//Post: retorna si l'Area a ha colisionat amb algun Meteorit
	private boolean tractarColisionsAmbMeteorits(Area a) throws Exception {
		boolean haXocat = false;
		LinkedList<Meteorit> meteoritsNous_ = new LinkedList<Meteorit>();
		ListIterator<Meteorit> it = meteorits_.listIterator();
		while (it.hasNext()) {
			Meteorit m = it.next();
			Area am = new Area(m.obtenirShape());
			am.intersect(a);
			if (!am.isEmpty()) {
				if (m.divisible()) {
					Meteorit m2 = m.dividir();
					it.add(m);
					meteoritsNous_.add(m2);
					d_.afegir(m2);
					puntuacio_ += 50;
				}
				else {
					it.remove();
					d_.elimina(m);
					puntuacio_ += 20;
				}
				haXocat = true;
			}
		}
		meteorits_.addAll(meteoritsNous_);
		return haXocat;
	}
	
	//Pre: --
	//Post: si la Nau del jugador ha xocat amb algun Meteorit, aquesta es centra i perd una vida, i el Meteorit es divideix o desapareix segons la mida
	//	si la NauEnemiga ha xocat amb algun Meteorit, aquesta es canvia de lloc aleatòriament i el Meteorit es divideix o desapareix segons la mida
	private void tractarColisionsNausMeteorit() throws Exception {
		
		Area n = new Area(n_.obtenirShape());
		Area ne = new Area(ne_.obtenirShape());
		
		boolean haXocatNau = tractarColisionsAmbMeteorits(n);
		boolean haXocatNE = tractarColisionsAmbMeteorits(ne);

		if (haXocatNau) 
			xocarNauJugador();
		
		if (haXocatNE) 
			xocarNauEnemiga();
	}
	
	//Pre: --
	//Post: si la Nau del jugador ha xocat amb algun RaigLaser, aquesta es centra i perd una vida, i el RaigLaser desapareix
	//	si la NauEnemiga ha xocat amb algun RaigLaser, aquesta es canvia de lloc aleatòriament, i el RaigLaser desapareix
	private void tractarColisionsNausRajosLaser() throws Exception {
		boolean haXocatNau = false;
		boolean haXocatNE = false;
		
		Area n = new Area(n_.obtenirShape());
		Area ne = new Area(ne_.obtenirShape());
		
		Iterator<RaigLaser> it = rajosLaser_.listIterator();
		while (it.hasNext()) {
			RaigLaser r = it.next();
			Shape s = r.obtenirShape();
			Area ar = new Area(s);
			ar.intersect(n);
			if (!ar.isEmpty()) {
				it.remove();
				d_.elimina(r);
				haXocatNau = true;
			}
			else {
				ar = new Area(s);
				ar.intersect(ne);
				if (!ar.isEmpty()) {
					it.remove();
					d_.elimina(r);
					haXocatNE = true;
				}
			}
		}
		
		if (haXocatNau) 
			xocarNauJugador();
		
		if (haXocatNE) 
			xocarNauEnemiga();
	}
	
	public class MyKeyListener implements KeyListener {
		
		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e) {
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
