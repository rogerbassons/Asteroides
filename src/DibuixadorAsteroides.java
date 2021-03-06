import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.image.BufferStrategy;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.event.KeyListener;
import java.awt.Font;
import java.awt.FontMetrics;

/// @brief Dibuixa múltiples ObjecteJoc a una finestra
/// @author Roger Bassons Renart
///
/// La finestra(f) s'ha de crear amb crearFinestra(). f té una mida, un color de fons i un títol determinat.
///
/// Es pot afegir un KeyListener a f
///
/// Es poden afegir ObjecteJoc a DibuixadorAsteroides. Aquests ObjecteJoc es dibuixaran a f
///
/// La finestra es pot tornar a crear tantes vegades com es desitgi, però només s'utilitza la última
/// que s'ha creat. La finestra es pot tancar en qualsevol moment.

public class DibuixadorAsteroides {
	/// @var JFrame f_
	/// @brief Finestra principal
	
	/// @var Canvas c_
	/// @brief Superfície on es dibuixa

	/// @var LinkedList<ObjecteJoc> lo_
	/// @brief Llista d'ObjecteJoc

	/// @var int amplada_
	/// @brief Amplada de c_

	/// @var int altura_
	/// @brief Altura de c_

	private JFrame f_;
	private Canvas c_; 
	private LinkedList<ObjecteJoc> lo_; 
	private int amplada_, altura_; 

	/// @pre --
	/// @post No hi ha cap finestra
	///       No hi ha cap KeyListener
	///       No hi ha cap ObjecteJoc
	DibuixadorAsteroides() {
		
		lo_ = new LinkedList<ObjecteJoc>();
	}

	/// @pre --
	/// @post crea una finestra per al DibuixaAsteroides amb una superfície amplada x altura, el color de fons es _fons_
	///      i el títol és t
	public void crearFinestra(int amplada, int altura, Color fons, String t) {
		amplada_ = amplada;
		altura_ = altura;
		
		f_ = new JFrame(t);
		f_.setResizable(false);
		f_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f_.setIgnoreRepaint(true);

		c_ = new Canvas();
		c_.setPreferredSize(new Dimension(amplada_,altura_));
		c_.setIgnoreRepaint(true);
		c_.setBackground(fons);
		c_.setFocusable(false);

		f_.add(c_);
		f_.pack();

		f_.setFocusable(true);
		f_.setVisible(true);

		c_.createBufferStrategy(2);
	}

	/// @pre ---
	/// @post Si oj no era al DibuixadorAsteroides llavors l'afegeix, altrament no fa res
	public void afegir(ObjecteJoc oj) {
		if (oj != null && !lo_.contains(oj)) {
			lo_.add(oj);
		}
	}

	/// @pre ---
	/// @post Si oj era al DibuixadorAsteroides llavors l'elimina altrament no fa res
	public void elimina(ObjecteJoc oj) {
		if (oj != null) {
			lo_.remove(oj);
		}
	}

	/// @pre ---
	/// @post afegeix l a la finestra del DibuixaAsteroides
	public void afegirKeyListener(KeyListener l) {
		f_.addKeyListener(l);
	}


	/// @pre finestra creada
	/// @post pinta a la superficie de la finestra del DibuixaAsteroides amb el color de fons i pinta tots els ObjecteJoc
	///       que s'han afegit al DibuixaAsteroides. També mostra puntuació al costat esquerre superior.
	public void dibuixar(int puntuacio) {
		BufferStrategy buff = c_.getBufferStrategy();
		Graphics g = buff.getDrawGraphics();
		g.clearRect(0,0,amplada_,altura_);

		g.setColor(Color.WHITE);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 20)); 
		g.drawString(Integer.toString(puntuacio),amplada_ / 34,altura_ / 15);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		Iterator<ObjecteJoc> it = lo_.iterator();
		while (it.hasNext()) {
			it.next().dibuixar(g2);
		}
		
		if(!buff.contentsLost()) {
			buff.show();
		}
		g.dispose();
	}

	/// @pre finestra creada
	/// @post dibuixa a la superfície de la finestra del DibuixaAsteroides puntuació al marge esquerre superior.
	public void dibuixarPuntuacio(int puntuacio) {
		BufferStrategy buff = c_.getBufferStrategy();
		Graphics g = buff.getDrawGraphics();
	
		g.setColor(Color.WHITE);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 20)); 
		g.drawString(Integer.toString(puntuacio),amplada_ / 34,altura_ / 15);
			
		if(!buff.contentsLost()) {
			buff.show();
		}
		g.dispose();
	}

	/// @pre finestra creada
	/// @post pinta a la superfície de la finestra del DibuixaAsteroides amb el color de fons i mostra m de color blanc
	public void mostrarMissatge(String m) {
		BufferStrategy buff = c_.getBufferStrategy();
		Graphics g = buff.getDrawGraphics();
		g.clearRect(0,0,amplada_,altura_);
		g.setColor(Color.WHITE);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
		FontMetrics fm = g.getFontMetrics( g.getFont() );
		int width = fm.stringWidth(m);
		g.drawString(m,amplada_ / 2 - width / 2 ,altura_ / 2);
			
		if(!buff.contentsLost()) {
			buff.show();
		}
		g.dispose();
	}

	/// @pre el DibuixaAsteroides té finestra
	/// @post s'ha tancat la finestra del DibuixaAsteroides
	public void tancarFinestra() {
		f_.dispose();
	}
}
