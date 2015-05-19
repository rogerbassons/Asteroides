import java.awt.geom.Path2D;
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Random;

/// @brief És un meteorit amb forma de polígon irregular que pot tenir quatre tipus de formes diferents. També pot tenir dues mides, gran o petit
///
/// Comportament bàsic:
/// ------------------
///	- Apareix en la posició donada al constructor, coordenada (x,y)
///	- Es mou en una direcció i velocitat fixes, la velocitat i la direcció són donades al constructor
///	- És controlat per la màquina
///	- Segons la mida (1 o 2) serà gran o petit, respectivament
///	- Quan un Meteorit es crea, té mida 1 (gran) i es rota el polígon un angle aleatori
///	- Si és un Meteorit gran, pot ser destruit en dos Meteorits de forma i direcció aleatòries, de mida petita
///	- Si es destrueix un Meteorit petit, desapareix
///
/// Supòsits sobre l'àrea(a) on es mou el Meteorit:
/// ----------------------------------------------
///	Té mida fixa i no canvia mentre existeix el Meteorit. 
///
///	És un pla amb:
///         - Un eix horitzontal X que augmenta d'esquerra a dreta (dreta és més)
///         - Un eix vertical Y que augmenta de dalt a baix (a baix és més)
///

public class Meteorit implements ObjecteJoc {
	/// @var Path2D poligon_
	/// @brief Camí geomètric que forma un polígon irregular tancat (pot contenir quatre formes diferents) i dues mides 
	
	/// @var int mida_
	/// @brief Indica la mida del Meteorit 
	/// 1 -> gran 
	/// 2 -> petit
	
	/// @var double velocitat_
	/// @brief Mòdul del vector velocitat del Meteorit
	
	/// @var double angleVelocitat_
	/// @brief Angle_ del vector velocitat del Meteorit
	
	/// @var int nVertexs_
	/// @brief Nombre de vèrtexs que té el Meteorit
	
	private Path2D poligon_;
	private int mida_; 
	private double velocitat_;
	private double angleVelocitat_;
	private int nVertexs_;
	
	/// @pre --
	/// @post el Meteorit té una velocitat v, una direcció angle i una mida m, està situat a (x,y)
	public Meteorit(double velocitat, double angle, int mida, double x, double y) {
		//Crear polígon
		mida_ = mida;
		angleVelocitat_ = angle;
		velocitat_ = velocitat;
		nVertexs_ = 8;
		Random rand = new Random();
		establirForma(0, 0, rand.nextInt(4)+1);
		rotar(rand.nextInt(360));
		
		double [] centre = puntCentrePoligon();

		AffineTransform a = new AffineTransform();
		a.translate(x - centre[0], y - centre[1]);
		poligon_.transform(a);
	}
	
	/// @pre --
	/// @post situa el Meteorit a la banda de la pantalla més propera d'on es troba el Meteorit. L'àrea de la pantalla és amplada*altura
	public void situarAlCostatMesProper(int amplada, int altura) {
		double [] puntCentre = puntCentrePoligon();
		double distMinX, distMinY;
		double [] puntCostat, puntAllunyat;
		if ( puntCentre[0] > amplada - puntCentre[0] ) //Costat esquerra
			puntCostat = new double [] {0, puntCentre[1]};
		else  //Costat dret
			puntCostat = new double [] {amplada, puntCentre[1]};

		puntAllunyat = puntMesAllunyat(puntCostat);
		distMinX = Point2D.distance(puntCostat[0], puntCostat[1], puntAllunyat[0], puntAllunyat[1]);
		
		if ( puntCentre[1] > altura - puntCentre[1] ) //Costat baix
			puntCostat = new double [] {puntCentre[0], 0};
		else //Costat dalt
			puntCostat = new double [] {puntCentre[0], altura};

		puntAllunyat = puntMesAllunyat(puntCostat);
		distMinY = Point2D.distance(puntCostat[0], puntCostat[1], puntAllunyat[0], puntAllunyat[1]);

		AffineTransform a = new AffineTransform();

		if ( distMinX < distMinY )
			a.translate(distMinX, 0);
		else
			a.translate(0, distMinY);

		poligon_.transform(a);
		
	}
	
	/// @pre --
	/// @post retorna el punt més allunyat del Meteorit respecte punt
	private double [] puntMesAllunyat(double [] punt) {
		double [] puntsT = obtenirPuntsPoligon();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMax = Math.abs(Point2D.distance(x, y, punt[0], punt[1]));
		for (int i = 2; i < nVertexs_*2-1; i += 2) {
			double dist = Math.abs(Point2D.distance(puntsT[i], puntsT[i+1], punt[0], punt[1]));
			if (dist > distMax) {
				distMax = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}
	
	/// @pre --
	/// @post rota el Meteorit un angle graus
	private void rotar(int graus) {
		AffineTransform a = new AffineTransform();
		a.rotate(Math.toRadians(graus));
		poligon_.transform(a);
	}

	/// @pre la mida és 1 o 2
	/// @post el Meteorit conté el dibuix 1, 2, 3 o 4 segons forma, aplicant-li la seva mida
	private void establirForma(double x, double y, int forma) {

		poligon_ = new Path2D.Double();
		poligon_.moveTo(x, y);

		//Hi ha quatre formes diferents de Meteorit
		if ( forma == 1 ) {
			poligon_.lineTo(x + 80, y + 10);
			poligon_.lineTo(x + 80, y + 10);
			poligon_.lineTo(x + 110, y + 50);
			poligon_.lineTo(x + 110, y + 90);
			poligon_.lineTo(x + 80, y + 120);
			poligon_.lineTo(x, y + 120);
			poligon_.lineTo(x - 30, y + 90);
		} else if ( forma == 2 ) {
			poligon_.lineTo(x + 75, y);
			poligon_.lineTo(x + 90, y + 45);
			poligon_.lineTo(x + 75, y + 60);
			poligon_.lineTo(x + 105, y + 75);
			poligon_.lineTo(x + 105, y + 90);
			poligon_.lineTo(x + 45, y + 105);
			poligon_.lineTo(x - 15, y + 90);
		} else if ( forma == 3 ) {
			poligon_.lineTo(x + 45, y - 25);
			poligon_.lineTo(x + 60, y + 105);
			poligon_.lineTo(x, y + 90);
			poligon_.lineTo(x - 15, y + 120);
			poligon_.lineTo(x - 45, y + 105);
			poligon_.lineTo(x - 45, y + 45);
			poligon_.lineTo(x - 15, y + 30);
		} else {
			poligon_.lineTo(x + 45, y);
			poligon_.lineTo(x + 75, y + 45);
			poligon_.lineTo(x + 75, y + 120);
			poligon_.lineTo(x, y + 135);
			poligon_.lineTo(x, y + 105);
			poligon_.lineTo(x - 30, y + 60);
			poligon_.lineTo(x - 30, y + 45);
		}

		poligon_.closePath();

		if (mida_ == 2) {
			AffineTransform a = new AffineTransform();
			a.scale(0.5, 0.5); //Reduim la mida a la meitat
			a.translate(x,y); //Necessari reposicionar degut al scale
			poligon_.transform(a);
		}

	}

	/// @pre el Meteorit és gran
	/// @post el Meteorit s'ha reduit a petit i es situa al centre del Meteorit inicial, es retorna un altre Meteorit també de mida petita 
	///	amb forma i direcció aleatòries, situat també al centre del Meteorit inicial, l'angle de velocitat del Meteorit retornat és 
	///	diferent en 45 graus com a mínim respecte l'angle de velocitat de m
	public Meteorit dividir() {
		
		mida_ = 2;
		double [] punt = puntCentrePoligon(); //Obtenim el punt del centre del polígon

		Random rand = new Random();
		establirForma(punt[0], punt[1], rand.nextInt(4)+1); //Canviem la mida i la forma del Meteorit
		
		angleVelocitat_ = rand.nextInt(360); //Canviem la direcció cap on va el Meteorit
		
		//Busquem un angle aleatori que tingui una diferència mínima de 45 graus respecte l'angle del Meteorit actual
		int angle2 = rand.nextInt(360); 
		while ((180 - Math.abs(Math.abs(angle2 - angleVelocitat_) - 180)) < 45)
			angle2 = rand.nextInt(360);
		
		Meteorit m = new Meteorit(velocitat_, angle2, 2, punt[0], punt[1]); //Creem el nou meteorit de mida petita

		return m;
	}

	/// @pre --
	/// @post retorna si el Meteorit és divisible
	public boolean divisible() {
		return mida_ == 1;
	}

	/// @pre amplada > 0 i altura > 0
	/// @post Desplaça el Meteorit a la posició(p) determinada per totes les velocitats del Meteorit
	///	Si el Meteorit, situat a la posició p, està totalment fora de l'area amplada*altura llavors el Meteorit es teletransporta al
	///	marge/costat invers del qual ha sortit(superior, inferior, esquerra, dreta)
	public void moure(int amplada, int altura) {
		AffineTransform a = new AffineTransform(); //Moviments concatenats
		double dx = velocitat_ * Math.cos(Math.toRadians(angleVelocitat_));
		double dy = velocitat_ * -Math.sin(Math.toRadians(angleVelocitat_));
		a.translate(dx, dy); //desplaçar Meteorit segons velocitat horitzontal i vertical

		poligon_.transform(a); //aplicar els moviments al poligon que representa el Meteorit

		// Comprovar si ha sortit de amplada x altura (a)
		// Si ha sortit:
		//     Seleccionar el punt(p) del poligon_ més proper de a (últim de sortir)
		//     Comprovar per quin marge(m) de a ha ha sortit el Meteorit segons p
		//     Seleccionar el punt(l) del poligon_ més llunyà de a (primer de sortir)
		//     Desplaçar el Meteorit al marge invers(i) de m de manera que l està exactament a la coordenada del marge i 
		if (haSortit(amplada,altura)) {
			double [] p = puntProperAlCentreDeArea(amplada,altura); //unicament per saber per quin marge ha sortit
			double px = p[0];
			double py = p[1];
			double [] l = puntLlunyaAlCentreDeArea(amplada,altura); //per teletransportar
			double lx = l[0];
			double ly = l[1];
	

			//coordenades desti del punt més llunyà
			double xdesti = lx;
			double ydesti = ly;
			if (py < 0) { // surt pel marge superior
				ydesti = altura;
			} else if (py > altura) { // surt pel marge inferior
				ydesti = 0;
			}

			if (px < 0) { // surt pel marge esquerra
				xdesti = amplada;
			} else if (px > amplada) { // surt pel marge dret
				xdesti = 0;
			}

			//moviment de translació que s'aplicarà
			double tx = xdesti - lx;
			double ty = ydesti - ly;
			a = new AffineTransform();
			a.translate(tx, ty);
			poligon_.transform(a);
		}
	}

	/// @pre amplada > 0 i altura > 0
	/// @post diu si el Meteorit ha sortit de l'àrea amplada*altura
	private boolean haSortit(int amplada, int altura) {
		double [] puntsT = obtenirPuntsPoligon();
		boolean hiHaUnPuntDins = false;
		int i = 0;
		while (!hiHaUnPuntDins && i < 13) {
			hiHaUnPuntDins = puntsT[i] >= 0 && puntsT[i] <= amplada && puntsT[i+1] >= 0 && puntsT[i+1] <= altura;
			i+=2;
		}
		return !hiHaUnPuntDins;
	}

	/// @pre amplada > 0 i altura > 0
	/// @post retorna una taula t on t[0] i t[1] són les coordenades x i y del punt del polígon més proper al centre de l'àrea amplada*altura
	private double [] puntProperAlCentreDeArea(int amplada, int altura) {
		double [] puntsT = obtenirPuntsPoligon();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMin = Math.abs(Point2D.distance(x, y, amplada/2, altura/2));
		for (int i = 2; i < nVertexs_*2-1; i += 2) {
			double dist = Math.abs(Point2D.distance(puntsT[i], puntsT[i+1], amplada/2, altura/2));
			if (dist < distMin) {
				distMin = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}

	/// @pre amplada > 0 i altura > 0
	/// @post retorna una taula t on t[0] i t[1] són les coordenades x i y del punt del polígon més llunyà al centre de l'àrea amplada*altura
	private double [] puntLlunyaAlCentreDeArea(int amplada, int altura) {
		double [] puntsT = obtenirPuntsPoligon();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMax = Math.abs(Point2D.distance(x, y, amplada/2, altura/2));
		for (int i = 2; i < nVertexs_*2-1; i += 2) {
			double dist = Math.abs(Point2D.distance(puntsT[i], puntsT[i+1], amplada/2, altura/2));
			if (dist > distMax) {
				distMax = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}

	/// @pre --
	/// @post retorna una taula t on t[0] i t[1] són les coordenades x i y del centre del polígon
	private double [] puntCentrePoligon() {
		double x = 0;
		double y = 0;
		double [] punts = obtenirPuntsPoligon();
		for (int i = 0; i < nVertexs_*2-1; i+=2) {
			x += punts[i];
			y += punts[i+1];
		}
		
		x = x/nVertexs_;
		y = y/nVertexs_;
		
		return new double [] {x,y};
	}
	
	/// @pre --
	/// @post retorna la distància entre el punt (x,y) i el punt més proper del Meteorit
	public double [] puntVertexMesProper(double x, double y) {
		double [] puntsP = obtenirPuntsPoligon();
		double xMin = puntsP[0];
		double yMin = puntsP[1];
		double distMin = Math.abs(Point2D.distance(puntsP[0], puntsP[1], x, y));
		for (int i = 2; i < nVertexs_*2-1; i += 2) {
			double dist = Math.abs(Point2D.distance(puntsP[i], puntsP[i+1], x, y));
			if (dist < distMin) {
				xMin = puntsP[i];
				yMin = puntsP[i+1];
				distMin = dist;
			}
		}
		return new double [] {xMin,yMin};
	}
	
	/// @pre --
	/// @post retorna una taula(t) que conte els punts del polígon
	///      coordenada (t[i],t[i+1]) per i = 0 fins al nombre de vèrtexs*2-2 increment 2
	private double [] obtenirPuntsPoligon() {
		double [] puntsT = new double[nVertexs_*2]; 
		double [] coordenades = new double[6]; //Per obtenir el segment
	
		PathIterator pi = poligon_.getPathIterator(null,0);
		int i = 0;
		while (!pi.isDone() && i < nVertexs_*2-1) {
			pi.currentSegment(coordenades);
			puntsT[i] = coordenades[0];
			puntsT[i+1] = coordenades[1];
			i += 2;
			pi.next();
		}
		return puntsT;
	}

	/// @pre --
	/// @post dibuixa el Meteorit de contorn blanc a g2
	public void dibuixar(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.draw(poligon_);
	}
	
	/// @pre --
	/// @post retorna el polígon del Meteorit
	public Shape obtenirShape() {
		return poligon_;
	}

}
