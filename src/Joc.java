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

//Mòdul que gestiona la lògica, la física i la interfície del joc.
//El Joc conté quatre tipus d'elements:
//	- Nau: Nau pròpia controlada per l'usuari.
//	- NauEnemiga: Nau controlada per la màquina (AI).
//	- Meteorits: Objectes controlats per la màquina, que van a la deriva.
//	- RaigLasers: raig disparat per Nau i NauEnemiga, que destrueix Meteorits i Naus.
//
//Funcionament general del Joc:
//Nau:
//	- Rota sobre si mateixa. Té un coet propulsor que l'impulsa endavant.
//	- Quan abandoni l'espai visible per l'usuari, per algun dels costats de la finestra, apareixerà pel costat invers, conservant el moviment que portava.
//	- Té forma de triangle isòsceles.
//	- Si s'acaben les vides de la Nau, la partida s'ha acabat.
//	- Un cop s'ha començat a moure en una direcció, es continua movent en aquesta direcció durant un temps determinat mentre l'usuari no intervingui, simulant 
//	  la ingravidesa de l'espai, i al mateix temps, facilitant el control de la nau.
//
//NauEnemiga:
//	- Hi ha una NauEnemiga que intenta, per qualsevol mitjà (disparant i col·lisionant), destruir la nau de l'usuari. 
//	- Té el mateix comportament que la Nau espacial controlada per l'usuari.
//	- Excepte: 
//		- L'objectiu d'aquesta nau és destruir la nau de l'usuari.
//		- Per a destruir la nau de l'usuari dispara rajos làser i la persegueix.
//
//	- La Nau i la NauEnemiga poden disparar RajosLaser.
//
//RaigLaser:
//	- Els RaigLaser poden col·lisionar amb Meteorits o amb les Naus.
//
//Meteorit:
//	- És un objecte que es mou amb una velocitat i direcció pseudoaleatòries (entre un rang determinat)
//	- Té una forma d'un polígon irregular, pseudoaleatòria
//	- Els Meteorits no col·lisionen entre si, s'atravessen.
//	- Quan un Meteorit gran col·lisiona amb un RaigLaser o una Nau, es divideix en dos Meteorits petits.
//	- Si xoca contra una nau, destrueix la nau amb la qual ha xocat.
//	- Quan un Meteorit petit col·lisiona amb un RaigLaser o una Nau, aquest desapareix.
//
//Descripció general:
//Inicialment, hi ha meteorits grans dispersats per tot l'espai i la nau enemiga a prop d'algun extrem de l'espai. La nau controlada per l'usuari està al centre
//i cap meteorit està a sobre seu. Hi ha un límit definit de meteorits que poden estar dins l'espai del joc. Quan es comença, hi ha un número determinat de meteorits grans, 
//que es va augmentant fins al límit. A partir de llavors, cada vegada que es destrueixi un meteorit n'apareix un de nou. 
//
//Només hi ha una nau enemiga a l'espai. Cada vegada que sigui destruïda, n'apareix una de nova. 
//
//El joc té un sistema de puntuació i de vides. L'usuari sempre comença amb 3 vides i 0 punts. A mesura que va destruint meteorits i naus enemigues, augmenta la seva puntuació, 
//segons aquestes ponderacions: meteorit gran 50 punts, meteorit petit 20 punts, nau enemiga 100 punts. 
//Els controls són els següents:
//	- W: impulsar cap endavant
//	- A: rotar cap a l'esquerra
//	- D: rotar cap a la dreta
//	- Espai: disparar rajos làser
//	- ESC: sortir del joc

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
	Calendar tempsNauEnemiga_;
	
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
		
		ne_ = null; 
		
		d_ = new DibuixadorAsteroides();
		d_.crearFinestra(amplada_, altura_, Color.BLACK, "Joc");
		d_.afegirKeyListener(new MyKeyListener());
		d_.afegir(n_);
		
		
		tempsRaigEnemiga_ = new GregorianCalendar();
		tempsRaigJugador_ = new GregorianCalendar();
		tempsNauEnemiga_ = new GregorianCalendar();
		
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
			d_.dibuixar(puntuacio_);
			Thread.sleep(10);
		}
		
		d_.tancarFinestra();
		
	}
	
	//Pre: --
	//Post: afegeix Meteorits al DibuixadorAsteroides i al Joc, fins a un màxim de 8, evitant que estiguin sobre la Nau
	private void generarMeteoritsInicials() throws Exception {
		while (meteorits_.size() < 8) {
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
		while (meteorits_.size() < 8) {
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
		
		generarMeteorits(); //Generem Meteorits
		
		moureNaus(); //Movem tots els objectes
		
		dispararNaus(); //Disparen les Naus (si cal)
		
		moureMeteoritsIRajosLaser(); //Movem els RajosLaser i els Meteorits
		
		tractarColisions(); //Tractem les possibles col·lisions
	}
	
	//Pre: --
	//Post: si la Nau ha de disparar, dispara i afegeix el RaigLaser a la llista de RaigLaser del Joc, fent el mateix amb la NauEnemiga
	private void dispararNaus() throws Exception {
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
		
		if (ne_ != null) {
			RaigLaser ra = ne_.atacarNau(n_,meteorits_);
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
		}
	}
	
	//Pre: --
	//Post: mou les Naus del Joc
	private void moureNaus() throws Exception {
		
		//Tractem el moviment de la Nau
		if (rotarDreta_)
			n_.rotarDreta();
		else if (rotarEsquerra_)
			n_.rotarEsquerra();
		else
			n_.pararRotacio();
		
		if (accelerar_)
			n_.propulsarEndavant();
		
		n_.moure(amplada_,altura_); //Movem la Nau
		if (ne_ != null)
			ne_.moure(amplada_,altura_); //Movem la NauEnemiga

		else { //Si no és viva
			Calendar tempsActual = new GregorianCalendar();
			if (tempsActual.getTimeInMillis() - tempsNauEnemiga_.getTimeInMillis() > 5000) { //Mirem si han passat 5 segons des de la última mort
				Random rand = new Random();
				boolean haXocat;
				do {
					ne_ = new NauEnemiga(50, Color.RED, rand.nextInt(amplada_-30)+30, rand.nextInt(altura_-30)+30);
					Area a = new Area(ne_.obtenirShape());
					haXocat = tractarColisionsAmbMeteorits(a);
				} while (haXocat);
				d_.afegir(ne_);
			}
		}
	}
	
	//Pre: --
	//Post: mou els Meteorits i els RajosLaser del Joc
	private void moureMeteoritsIRajosLaser() throws Exception {
		Iterator<RaigLaser> it = rajosLaser_.iterator();
		
		while (it.hasNext()){ //Iterem per tots els RajosLaser per a moure'ls
			RaigLaser r = it.next();
			if (!r.gastat())
				r.moure(amplada_, altura_);
			else{
				it.remove();
				d_.elimina(r);
			}
		}
		
		for (Meteorit m : meteorits_) //Iterem per tots els Meteorits per a moure'ls
			m.moure(amplada_, altura_);
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
		if (n_.esViva()){
			n_.reanimar();
			n_.centrar(amplada_, altura_);
			nVides_--;
		}
		//if (nVides_ == 0) 
		//	n_.morir();
		//FALTA MOSTRAR ALGUN MISSATGE AL MORIR I ACABAR LA PARTIDA
	}
	
	//Pre: --
	//Post: posiciona la NauEnemiga en un lloc pseudoaleatori dins l'espai de joc.
	private void xocarNauEnemiga() throws Exception {
		puntuacio_ += 100;
		d_.elimina(ne_);
		ne_ = null;
		tempsNauEnemiga_ = new GregorianCalendar();
	}
	
	//Pre: --
	//Post: si les naus colisionen la Nau del jugador es centra i la NauEnemiga es canvia de lloc altrament no fa res
	private void tractarColisionsEntreNaus() throws Exception {
		if (ne_!=null) {
			Area n = new Area(n_.obtenirShape());
			Area ne = new Area(ne_.obtenirShape());
			n.intersect(ne);
			if (!n.isEmpty()) {
				xocarNauJugador();
				xocarNauEnemiga();
			}
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
		boolean haXocatNau = tractarColisionsAmbMeteorits(n);
		if (haXocatNau) 
			xocarNauJugador();
		
		if (ne_ != null) {
			Area ne = new Area(ne_.obtenirShape());
			boolean haXocatNE = tractarColisionsAmbMeteorits(ne);
			if (haXocatNE) 
				xocarNauEnemiga();
		}
	}
	
	//Pre: --
	//Post: si la Nau del jugador ha xocat amb algun RaigLaser, aquesta es centra i perd una vida, i el RaigLaser desapareix
	//	si la NauEnemiga ha xocat amb algun RaigLaser, aquesta desapareix i torna a aparèixer al cap de 10 segons, i el RaigLaser desapareix
	private void tractarColisionsNausRajosLaser() throws Exception {
		Area n = new Area(n_.obtenirShape());
		boolean haXocatNau = tractarColisionsAmbRajosLaser(n);
		if (haXocatNau)
			xocarNauJugador();
		
		if (ne_ != null) { //Si la nau enemiga existeix
			Area ne = new Area(ne_.obtenirShape());
			boolean haXocatNE = tractarColisionsAmbRajosLaser(ne);
			if (haXocatNE) 
				xocarNauEnemiga();
		}
	}
	
	//Pre: --
	//Post: si la Nau del jugador ha xocat amb algun RaigLaser, aquesta es centra i perd una vida, i el RaigLaser desapareix
	//	si la NauEnemiga ha xocat amb algun RaigLaser, aquesta es canvia de lloc aleatòriament, i el RaigLaser desapareix
	private boolean tractarColisionsAmbRajosLaser(Area a) throws Exception {
		boolean haXocat = false;
		
		Iterator<RaigLaser> it = rajosLaser_.listIterator();
		while (it.hasNext()) {
			RaigLaser r = it.next();
			Shape s = r.obtenirShape();
			Area ar = new Area(s);
			ar.intersect(a);
			if (!ar.isEmpty()) {
				it.remove();
				d_.elimina(r);
				haXocat = true;
			}
		}
		
		return haXocat;
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
