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
import java.io.InputStream;
import java.net.URL;
import java.awt.geom.Area;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.awt.Shape;
import java.util.concurrent.TimeUnit;

/// @brief Controla una Nau a l'espai evitant i atacant Meteorits i Naus Enemigues
///
/// El Joc conté quatre tipus d'ObjecteJoc:
///	- Nau: Nau pròpia controlada per l'usuari.
///	- NauEnemiga: Nau controlada per la màquina (AI).
///	- Meteorit: Objectes controlats per la màquina, que van a la deriva.
///	- RaigLaser: Raig disparat per Nau i NauEnemiga, que destrueix Meteorits i Naus.
///
/// Funcionament general del Joc:
/// ----------------------------
/// Nau:
///	- Rota sobre si mateixa. Té un coet propulsor que l'impulsa endavant.
///	- Quan abandoni l'espai visible per l'usuari, per algun dels costats de la finestra, apareixerà pel costat invers, conservant el moviment que portava.
///	- Té forma de triangle isòsceles.
///	- Si s'acaben les vides de la Nau, la partida s'ha acabat.
///	- Un cop s'ha començat a moure en una direcció, es continua movent en aquesta direcció durant un temps determinat mentre l'usuari no intervingui, simulant 
///	  la ingravidesa de l'espai, i al mateix temps, facilitant el control de la nau.
///
/// NauEnemiga:
///	- Hi ha una NauEnemiga que intenta, per qualsevol mitjà (disparant i col·lisionant), destruir la nau de l'usuari. 
///	- Té el mateix comportament que la Nau espacial controlada per l'usuari.
///	- Excepte: 
///		- L'objectiu d'aquesta nau és destruir la nau de l'usuari.
///		- Per a destruir la nau de l'usuari dispara rajos làser i la persegueix.
///
///	- La Nau i la NauEnemiga poden disparar RajosLaser.
///
/// RaigLaser:
///	- Els RaigLaser poden col·lisionar amb Meteorits o amb les Naus.
///
/// Meteorit:
///	- És un objecte que es mou amb una velocitat i direcció pseudoaleatòries (entre un rang determinat)
///	- Té una forma d'un polígon irregular, pseudoaleatòria
///	- Els Meteorits no col·lisionen entre si, s'atravessen.
///	- Quan un Meteorit gran col·lisiona amb un RaigLaser o una Nau, es divideix en dos Meteorits petits.
///	- Si xoca contra una nau, destrueix la nau amb la qual ha xocat.
///	- Quan un Meteorit petit col·lisiona amb un RaigLaser o una Nau, aquest desapareix.
///
/// Descripció general:
/// ------------------
/// Inicialment, hi ha diversos Meteorit grans dispersats per tot l'espai i la NauEnemiga a prop d'algun extrem de l'espai. La Nau controlada per l'usuari està al centre.
/// Hi ha un nombre màxim de Meteorit  que poden estar dins l'espai del joc al mateix temps. Quan es comença, hi ha un número determinat de meteorits grans, que es va
/// augmentant fins al límit. A partir de llavors, cada vegada que es destrueixi un meteorit n'apareix un de nou. 
///
/// Quan la Nau es destrueix i encara té vides, llavors tarda 5 segons a reaparèixer.
/// Només hi ha una NauEnemiga a l'espai. Cada vegada que sigui destruïda, despres de 5 segons n'apareix una de nova. 
///
/// El joc té un sistema de puntuació i de vides. L'usuari sempre comença amb 3 vides i 0 punts. A mesura que va destruint meteorits i naus enemigues, augmenta la seva
/// puntuació, segons aquestes ponderacions: meteorit gran 50 punts, meteorit petit 20 punts, nau enemiga 100 punts.
///
/// Quan s'acaben les vides de la Nau, es mostra "Game Over" juntament amb la puntuació.
///
/// Controls:
/// --------
///	- W: impulsar cap endavant
///	- A: rotar cap a l'esquerra
///	- D: rotar cap a la dreta
///	- Espai: disparar rajos làser
///	- ESC: sortir del joc

public class Joc {
	/// @var int amplada_ 
	/// @brief Amplada de l'espai del Joc
	
	/// @var int altura_
	/// @brief Altura de l'espai del Joc
	
	/// @var Nau n_
	/// @brief Nau controlada pel jugador, de color verd
	
	/// @var NauEnemiga ne_
	/// @brief Nau controlada per AI, de color vermell
	
	/// @var DibuixadorAsteroides d_
	/// @brief Dibuixa per pantalla els objectes del Joc
	
	/// @var LinkedList<Meteorit> meteorits_
	/// @brief Conté els Meteorits de la partida
	
	/// @var LinkedList<RaigLaser> rajosLaserNau_
	/// @brief Conté els RaigLaser disparats per la Nau n_
	
	/// @var LinkedList<RaigLaser> rajosLaserNE_
	/// @brief Conté els RaigLaser disparats per la NauEnemiga ne_
	
	/// @var Calendar tempsRaigEnemiga_
	/// @brief Instant de temps en què s'ha disparat l'últim RaigLaser de la NauEnemiga ne_
	
	/// @var Calendar tempsRaigNau_
	/// @brief Instant de temps en què s'ha disparat l'últim RaigLaser de la Nau n_
	
	/// @var Calendar tempsNauEnemiga_
	/// @brief Instant de temps en el qual la NauEnemiga ha mort

	/// @var Calendar tempsNau_
	/// @brief Instant de temps en el qual la Nau ha mort
	
	/// @var int nVides_
	/// @brief Vides restants de la Nau n_
	
	/// @var int puntuacio_
	/// @brief Puntuació de l'usuari
	
	/// @var boolean sortir_
	/// @brief Diu si s'ha de sortir del programa
	
	/// @var boolean partidaAcabada_
	/// @brief Diu si s'ha acabat la partida (el programa encara queda obert)
	
	/// @var boolean inhibirNau_
	/// @brief Diu si la Nau està protegida de col·lisions, no es pot controlar i no es pot mostrar
	
	/// @var boolean disparar_
	/// @brief Diu si la Nau ha de disparar
	
	/// @var boolean rotarEsquerra_
	/// @brief Diu si la Nau ha de rotar cap a l'esquerra
	
	/// @var boolean rotarDreta_
	/// @brief Diu si la Nau ha de rotar cap a la dreta
	
	/// @var boolean accelerar_
	/// @brief Diu si la Nau ha d'accelerar

	/// @var Clip piu_
	/// @brief So que es reprodueix quan es dispara un RaigLaser
	
	
	int amplada_;
	int altura_;
	Nau n_;
	NauEnemiga ne_;
	DibuixadorAsteroides d_;
	LinkedList<Meteorit> meteorits_;
	LinkedList<RaigLaser> rajosLaserNau_;
	LinkedList<RaigLaser> rajosLaserNE_;
	
	Calendar tempsRaigEnemiga_;
	Calendar tempsRaigNau_;
	Calendar tempsNauEnemiga_;
	Calendar tempsNau_;
	
	int nVides_;
	int puntuacio_;
	
	boolean sortir_;
	boolean partidaAcabada_;
	boolean inhibirNau_;
	boolean disparar_;
	boolean rotarEsquerra_, rotarDreta_, accelerar_;
	
	Clip piu_;
	
	public static void main(String[] args) throws Exception {
		Joc j = new Joc(1024, 768);
		j.jugar();
	}
	
	/// @pre amplada >= 800 i altura >= 600
	/// @post s'ha creat Joc amb un espai d'amplada*altura
	Joc(int amplada, int altura) throws Exception {
		
		inhibirNau_ = partidaAcabada_ = sortir_ = disparar_ = rotarEsquerra_ = rotarDreta_ = accelerar_ = false;
		
		meteorits_ = new LinkedList<Meteorit>();
		rajosLaserNau_ = new LinkedList<RaigLaser>();
		rajosLaserNE_ = new LinkedList<RaigLaser>();
		
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
		tempsRaigNau_ = new GregorianCalendar();
		tempsNauEnemiga_ = new GregorianCalendar();
		tempsNau_ = new GregorianCalendar();
		
		nVides_ = 3;
		puntuacio_ = 0; 
	}
	
	/// @pre --
	/// @post mentre no es surti del Joc, captura l'entrada de l'usuari, actualitza el moviment dels objectes del joc i ho mostra per pantalla
	public void jugar() throws Exception {
		
		URL so = getClass().getResource("/res/piu.wav");
		AudioInputStream a = AudioSystem.getAudioInputStream(so);
		piu_ = AudioSystem.getClip();
		piu_.open(a);
		
		generarMeteoritsInicials();

		actualitzar();
		while (!sortir_) {
			if (!partidaAcabada_) {
				d_.dibuixar(puntuacio_);
				actualitzar();
			}
			Thread.sleep(10);
		}

		
		
		d_.tancarFinestra();
		
	}
	
	/// @pre --
	/// @post afegeix a posicions aleatòries el nombre de meteorits necessaris al Joc, perquè hi hagi un total de 8 Meteorits.
	///       Cap Meteorit colisiona amb la Nau 
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
	
	/// @pre --
	/// @post afegeix el nombre de meteorits necessaris als marges de l'espai del Joc, perquè hi hagi un total de 8 Meteorits
	private void generarMeteorits() throws Exception {
		while (meteorits_.size() < 8) {
			Random rand = new Random();
			Meteorit m = new Meteorit(0.8, rand.nextInt(360), 1, rand.nextInt(amplada_), rand.nextInt(altura_));
			m.situarAlCostatMesProper(amplada_, altura_);
			meteorits_.add(m);
			d_.afegir(m);
		}
	}
	
	/// @pre --
	/// @post actualitza l'estat del joc, generant meteorits i movent les Naus, Meteorits i Raigs Laser (tractant les col·lisions)
	///       Si han passat més de 5 segons respecte l'última vegada que la Nau ha mort llavors l'afegeix al Joc.
	private void actualitzar() throws Exception {
		
		generarMeteorits(); //Generem Meteorits
		
		moureDispararNaus(); //Movem tots els objectes
		
		moureMeteoritsIRajosLaser(); //Movem els RajosLaser i els Meteorits
		
		tractarColisions(); //Tractem les possibles col·lisions

		
	}

	/// @pre --
	/// @post mou NauEnemiga i Nau en el Joc i si disparen llavors afegeix els RaigLaser que disparen al Joc
	private void moureDispararNaus() throws Exception {
		
		//Tractem el moviment de la Nau
		Calendar t = new GregorianCalendar();
		if (!inhibirNau_) {
			if (rotarDreta_)
				n_.rotarDreta();
			else if (rotarEsquerra_)
				n_.rotarEsquerra();
			else
				n_.pararRotacio();
			if (accelerar_)
				n_.propulsarEndavant();
			n_.moure(amplada_,altura_); //Movem la Nau

			if (disparar_ && t.getTimeInMillis() - tempsRaigNau_.getTimeInMillis() > 441) {
				tempsRaigNau_ = new GregorianCalendar();
				piu_.setFramePosition(0);
				piu_.start();
				RaigLaser r = n_.disparar();
				rajosLaserNau_.add(r);
				d_.afegir(r);
			}
			
		} else if (t.getTimeInMillis() - tempsNau_.getTimeInMillis() > 1000) {
			inhibirNau_ = false;
			d_.afegir(n_);
		}
		
		
		if (ne_ != null) {
			RaigLaser ra = ne_.atacarNau(n_,meteorits_);
			if (ra != null && t.getTimeInMillis() - tempsRaigEnemiga_.getTimeInMillis() > 1000) {
				tempsRaigEnemiga_ = new GregorianCalendar();
				piu_.setFramePosition(0);
				piu_.start();
				d_.afegir(ra);
				rajosLaserNE_.add(ra);
			}
			ne_.moure(amplada_,altura_); //Movem la NauEnemiga
		} else { //Si no existeix
			Calendar tempsActual = new GregorianCalendar();
			if (tempsActual.getTimeInMillis() - tempsNauEnemiga_.getTimeInMillis() > 5000) { //Mirem si han passat 5 segons des de la última mort
				Random rand = new Random();
				boolean haXocat;
				do {
					ne_ = new NauEnemiga(50, Color.RED, rand.nextInt(amplada_-30)+30, rand.nextInt(altura_-30)+30);
					Area a = new Area(ne_.obtenirShape());
					haXocat = tractarColisionsAmbMeteorits(a, false);
				} while (haXocat);
				d_.afegir(ne_);
			}
		}
	}
	
	/// @pre --
	/// @post mou els Meteorits i els RajosLaser del Joc
	private void moureMeteoritsIRajosLaser() throws Exception {
		Iterator<RaigLaser> it = rajosLaserNau_.iterator();
		
		while (it.hasNext()) { //Iterem per tots els RajosLaser de la Nau per a moure'ls
			RaigLaser r = it.next();
			if (!r.gastat())
				r.moure(amplada_, altura_);
			else{
				it.remove();
				d_.elimina(r);
			}
		}
		
		it = rajosLaserNE_.iterator();
		
		while (it.hasNext()) { //Iterem per tots els RajosLaser de la NauEnemiga per a moure'ls
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
	
	/// @pre --
	/// @post tracta les col·lisions entre els objectes del Joc
	private void tractarColisions() throws Exception {
		
		//Col·lisions Nau - NauEnemiga
		tractarColisionsEntreNaus();
		
		//Col·lisions Nau i NauEnemiga amb RaigLaser
		tractarColisionsNausRajosLaser();
		
		//Col·lisions Nau i NauEnemiga amb Meteorit
		tractarColisionsNausMeteorit();
		
		//Col·lisions RaigLaser de la Nau - Meteorit
		tractarColisionsRaigLaserNauMeteorit();
		
		//Col·lisions RaigLaser de la NauEnemiga - Meteorit
		tractarColisionsRaigLaserNEMeteorit();
		
	}
	
	/// @pre --
	/// @post si la nau està viva es centra la nau i se li resta una vida, altrament s'acaba la partida i es mostra un missatge. Si té 0 vides, mor.
	private void xocarNauJugador() throws Exception {
		if (n_.esViva()) {
			tempsNau_ = new GregorianCalendar();
			d_.elimina(n_);
			n_.reanimar();
			n_.centrar(amplada_, altura_);
			inhibirNau_ = true;
			nVides_--;
			if (nVides_ == 0) {
				n_.morir();
				partidaAcabada_ = true;
				d_.mostrarMissatge("GAME OVER");
				d_.dibuixarPuntuacio(puntuacio_);
			}
		}
	}
	
	/// @pre  --
	/// @post la NauEnemiga desapareix del Joc durant 5 segons, la puntuació incrementa 100 punts
	private void xocarNauEnemiga() throws Exception {
		puntuacio_ += 100;
		d_.elimina(ne_);
		ne_ = null;
		tempsNauEnemiga_ = new GregorianCalendar();
	}
	
	/// @pre  --
	/// @post si les naus col·lisionen la Nau del jugador es centra, la NauEnemiga desapareix durant 5 segons i s'actualitza la puntuació, altrament no fa res
	private void tractarColisionsEntreNaus() throws Exception {
		if (ne_!=null && !inhibirNau_) {
			Area n = new Area(n_.obtenirShape());
			Area ne = new Area(ne_.obtenirShape());
			n.intersect(ne);
			if (!n.isEmpty()) {
				xocarNauJugador();
				xocarNauEnemiga();
			}
		}
	}
	
	/// @pre  --
	/// @post els RaigLaser de la Nau que hagin col·lisionat amb un Meteorit s'han eliminat i 
	///	els Meteorits s'han dividit o bé eliminat segons la seva mida, s'ha actualitzat la puntuació
	private void tractarColisionsRaigLaserNauMeteorit() throws Exception {
		Iterator<RaigLaser> it = rajosLaserNau_.iterator();
		while (it.hasNext()) {
			RaigLaser r = it.next();
			Area rl = new Area(r.obtenirShape());
			boolean haXocat = tractarColisionsAmbMeteorits(rl, true);
			
			if (haXocat) {
				it.remove();
				d_.elimina(r);
			}
		}
	}
	
	/// @pre  --
	/// @post els RaigLaser que hagin col·lisionat amb un Meteorit s'han eliminat i els Meteorits 
	// 	s'han dividit o bé eliminat segons la seva mida
	private void tractarColisionsRaigLaserNEMeteorit() throws Exception {
		Iterator<RaigLaser> it = rajosLaserNE_.iterator();
		while (it.hasNext()) {
			RaigLaser r = it.next();
			Area rl = new Area(r.obtenirShape());
			boolean haXocat = tractarColisionsAmbMeteorits(rl, false);
			
			if (haXocat) {
				it.remove();
				d_.elimina(r);
			}
		}
	}
	
	
	/// @pre  --
	/// @post retorna si l'Area a ha colisionat amb algun Meteorit i actualitza la puntuació
	private boolean tractarColisionsAmbMeteorits(Area a, boolean puntuar) throws Exception {
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
					if (puntuar) {
						puntuacio_ += 50;
					}
				}
				else {
					it.remove();
					d_.elimina(m);
					if (puntuar) {
						puntuacio_ += 20;
					}
				}
				haXocat = true;
			}
		}
		meteorits_.addAll(meteoritsNous_);
		return haXocat;
	}
	
	/// @pre  --
	/// @post si la Nau del jugador ha xocat amb algun Meteorit, aquesta es centra i perd una vida, i el Meteorit es divideix o desapareix segons la mida
	///	si la NauEnemiga ha xocat amb algun Meteorit, aquesta es canvia de lloc aleatòriament i el Meteorit es divideix o desapareix segons la mida
	private void tractarColisionsNausMeteorit() throws Exception {
		if (!inhibirNau_) {
			Area n = new Area(n_.obtenirShape());
			boolean haXocatNau = tractarColisionsAmbMeteorits(n, true);
			if (haXocatNau) 
				xocarNauJugador();
		}
		
		if (ne_ != null) {
			Area ne = new Area(ne_.obtenirShape());
			boolean haXocatNE = tractarColisionsAmbMeteorits(ne, false);
			if (haXocatNE) 
				xocarNauEnemiga();
		}
	}
	
	/// @pre  --
	/// @post si la Nau del jugador ha xocat amb algun RaigLaser, aquesta es centra i perd una vida, i el RaigLaser desapareix
	///	si la NauEnemiga ha xocat amb algun RaigLaser, aquesta desapareix i torna a aparèixer al cap de 10 segons, i el RaigLaser desapareix
	private void tractarColisionsNausRajosLaser() throws Exception {
		if (!inhibirNau_) {
			Area n = new Area(n_.obtenirShape());
			boolean haXocatNau = tractarColisionsAmbRajosLaser(n);
			if (haXocatNau)
				xocarNauJugador();
		}
		if (ne_ != null) { //Si la nau enemiga existeix
			Area ne = new Area(ne_.obtenirShape());
			boolean haXocatNE = tractarColisionsAmbRajosLaser(ne);
			if (haXocatNE) 
				xocarNauEnemiga();
		}
	}
	
	/// @pre  --
	/// @post si a ha xocat amb algun RaigLaser, retorna cert i elimina el RaigLaser, altrament retorna fals
	private boolean tractarColisionsAmbRajosLaser(Area a) throws Exception {
		boolean haXocat = false;
		
		Iterator<RaigLaser> it = rajosLaserNau_.listIterator();
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
		
		it = rajosLaserNE_.listIterator();
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
	
	///KeyListener per a actualitzar els booleans segons les pulsacions del teclat
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
