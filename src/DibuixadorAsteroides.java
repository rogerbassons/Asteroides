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

// DibuixadorAsteroides dibuixa multiples ObjecteJoc a una finestra(f)
// f té una mida, un color de fons(c) i un titol determinat. Es pot afegir un KeyListener a f
// Es poden afegir ObjecteJoc a DibuixadorAsteroides
// Per poder dibuixar els ObjecteJoc afegits s'ha de crear una finestra
// La finestra es pot tornar a crear tantes vegades com es desitji, pero nomes s'utilitza la ultima
// que s'ha creat.
// La finestra es pot tancar en qualsevol moment

public class DibuixadorAsteroides {
	JFrame f_; // finestra principal
	Canvas c_; // superficie on es dibuixa
	LinkedList<ObjecteJoc> lo_; // llista d'ObjecteJoc

	int amplada_, altura_; // amplada i altura de c_

	//Pre: --
	//Post: No hi ha cap finestra
	//      No hi ha cap KeyListener
	//      No hi ha cap ObjecteJoc
	DibuixadorAsteroides()
	{
		
		lo_ = new LinkedList<ObjecteJoc>();
	}

	//Pre: --
	//Post: crea una finestra per al DibuixaAsteroides amb una superficie amplada x altura, el color de fons es fons
	//      i el titol és t
	public void crearFinestra(int amplada, int altura, Color fons, String t)
	{
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

	//Pre: oj != null
	//Post: Afageix oj al DibuixaAsteroides si oj no hi era previament altrament no fa res
	//      Excepcio: llança excepcio si oj == null
	public void afegir(ObjecteJoc oj) throws Exception
	{
		if (oj == null) {
			throw new Exception("l'objecte es null");
		}
		if (!lo_.contains(oj)) {
			lo_.add(oj);
		}
	}

	//Pre: oj != null
	//Post: Elimina oj del DibuixaAsteroides si hi era previament altrament no fa res
	//      Excepcio: llança excepcio si oj == null
	public void elimina(ObjecteJoc oj) throws Exception
	{
		if (oj == null) {
			throw new Exception("l'objecte es null");
		}
		lo_.remove(oj);
	}

	//Pre: l != null
	//Post: afageix l a la finestra del DibuixaAsteroides
	public void afegirKeyListener(KeyListener l)
	{
		f_.addKeyListener(l);
	}


	//Pre: finestra creada
	//Post: pinta a la superficie de la finestra del DibuixaAsteroides amb el color de fons i pinta tots els ObjecteJoc
	//      que s'han afegit al DibuixaAsteroides
	public void dibuixar(int puntuacio)
	{
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

	//Pre: el DibuixaAsteroides té finestra
	//Post: s'ha tancat la finestra del DibuixaAsteroides
	public void tancarFinestra()
	{
		f_.dispose();
	}

}
