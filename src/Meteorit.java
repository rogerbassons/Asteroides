import java.awt.geom.Path2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Random;

//És un meteorit amb forma de polígon irregular
//Pot tenir quatre tipus de formes diferents
//També pot tenir dues mides, gran o petit
//
//Comportament bàsic:
//	- Apareix en la posició donada al mètode situar(), per defecte està a la coordenada (0,0)
// 	- Es mou en una direcció i velocitat fixes, la velocitat és donada al constructor, la direcció és aleatòria
// 	- És controlat per la màquina
// 	- Segons la mida (1 o 2) serà gran o petit, respectivament
// 	- Quan un Meteorit es crea, té mida 1 (gran) i es rota el polígon un angle aleatori
// 	- Si és un Meteorit gran, pot ser destruit en dos Meteorits de forma i direcció aleatòries, de mida petita
//	- Si es destrueix un meteorit petit, desapareix
//
// Supòsits sobre l'area(a) on es mou el Meteorit:
//     Té mida fixa i no canvia mentre existeix el Meteorit
//     És un pla amb:
//         - un eix horitzontal X que augmenta d'esquerra a dreta (dreta és més)
//         - un eix vertical Y que augmenta de dalt a baix (a baix és més)
//

public class Meteorit {

	private Path2D poligon_;
	private int mida_; //Si és 1 és gran, si és 2 és petit
	private double velocitat_;
	private double angleVelocitat_;
	private int nVertexs_;
	
	
	//Pre: --
	//Post: this conté una còpia de m, angleVelocitat_ és aleatori
	Meteorit(Meteorit m) {
		poligon_ = new Path2D.Double(m.poligon_);
		mida_ = m.mida_;
		velocitat_ = m.velocitat_;
		nVertexs_ = m.nVertexs_;
		Random rand = new Random();
 		angleVelocitat_ = rand.nextInt(360);
	}
	
	//Pre: --
	//Post: el Meteorit té el primer punt a la coordenada (0,0), una velocitat v, una direcció angle i una mida m
	Meteorit(double velocitat, double angle, int mida) {
		//Crear polígon
		mida_ = mida;
		angleVelocitat_ = angle;
		velocitat_ = velocitat;
		nVertexs_ = 8;
		Random rand = new Random();
		establirForma(0, 0, rand.nextInt(4)+1);
		rotar(rand.nextInt(360));
	}
	
	public void situar(double x, double y) {
		double [] centre = puntCentrePoligon();

		AffineTransform a = new AffineTransform();
		a.translate(x - centre[0], y - centre[1]);
		poligon_.transform(a);
	}
		
	//Pre: --
	//Post: rota el Meteorit un angle graus
	private void rotar(int graus) {
		AffineTransform a = new AffineTransform();
		a.rotate(Math.toRadians(graus));
		poligon_.transform(a);
	}

	//Pre: mida_ val 1 o 2
	//Post: poligon_ conté el dibuix 1, 2, 3 o 4 segons forma, sent gran si mida_=1 i petit si mida_=2
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

		if (mida_ == 2){
			AffineTransform a = new AffineTransform();
			a.scale(0.5, 0.5); //Reduim la mida a la meitat
			a.translate(x,y); //Necessari reposicionar degut al scale
			poligon_.transform(a);
		}

	}

	//Pre: mida_ == 1
	//Post: el Meteorit s'ha reduit a mida_ = 2 i es situa al centre del Meteorit inicial, es retorna un altre Meteorit també de mida_ = 2 
	//	amb forma i direcció aleatòries, situat també al centre del Meteorit inicial
	public Meteorit dividir(int amplada, int altura) {
		mida_ = 2;

		double [] punt = puntCentrePoligon();

		Random rand = new Random();
		angleVelocitat_ = rand.nextInt(360);
		establirForma(punt[0], punt[1], rand.nextInt(4)+1); //Donem una forma aleatòria al poligon_
		Meteorit m = new Meteorit(this); //Creem una còpia de this
		m.establirForma(punt[0], punt[1], rand.nextInt(4)+1); //Donem una forma aleatòria a m
		return m;
	}

	//Pre: --
	//Post: retorna si el Meteorit és divisible
	public boolean divisible() {
		return mida_ == 1;
	}

	//Pre: amplada > 0 i altura > 0
	//Post: Desplaça el Meteorit a la posició(p) determinada per totes les velocitats del Meteorit
	//      Si el Meteorit, situat a la posició p, està totalment fora de l'area amplada x altura llavors el Meteorit es teletransporta al
	//      marge/costat invers del qual ha sortit(superior, inferior, esquerra, dreta)
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
		//     Seleccionar el punt(l) del poligon_ més llunya de a (primer de sortir)
		//     Desplaçar el Meteorit al marge invers(i) de m de manera que l està exactament a la coordenada del marge i 
		if (haSortit(amplada,altura)) {
			double [] p = puntProperAlCentreDeArea(amplada, altura); //unicament per saber per quin marge ha sortit
			double px = p[0];
			double py = p[1];
			double [] l = puntLlunyaAlCentreDeArea(amplada, altura); //per teletransportar
			double lx = l[0];
			double ly = l[1];
	
			//moviment de translació que s'aplicarà
			double tx = 0;
			double ty = 0;
			System.out.println("(" + Double.toString(px) + "," + Double.toString(py) + ")");
			//coordenades desti del punt més llunya
			double xdesti = 0;
			double ydesti = 0;
			if (py < 0) { // surt pel marge superior
				xdesti = lx;
				ydesti = altura;
			} else if (py > altura) { // surt pel marge inferior
				xdesti = lx;
				ydesti = 0;
			} else if (px < 0) { // surt pel marge esquerra
				xdesti = amplada;
				ydesti = ly;
			} else if (px > amplada) { // surt pel marge dret
				xdesti = 0;
				ydesti = ly;
			}
			tx = xdesti - lx;
			ty = ydesti - ly;
			System.out.println("(" + Double.toString(xdesti) + "," + Double.toString(ydesti) + ")");
			a = new AffineTransform();
			a.translate(tx, ty);
			poligon_.transform(a);
		}
	}

	//Pre: --
	//Post: diu si el Meteorit ha sortit de l'area amplada x altura
	private boolean haSortit(int amplada, int altura) {
		double [] puntsT = obtenirPuntsPoligon();
		boolean hiHaUnPuntDins = false;
	        int i = 0;
		while (!hiHaUnPuntDins && i < 13) {
			hiHaUnPuntDins = puntsT[i] >= 0 && puntsT[i] <= amplada && puntsT[i+1] >= 0 && puntsT[i+1] <= altura;
			i++;
		}
		return !hiHaUnPuntDins;
	}

	//Pre: amplada > 0 i altura > 0
	//Post: retorna una taula t on t[0] i t[1] són les coordenades x i y del punt del poligon més proper al centre de l'area amplada x altura
	private double [] puntProperAlCentreDeArea(int amplada, int altura) {
		double [] puntsT = obtenirPuntsPoligon();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMin = Point2D.distance(x, y, amplada/2, altura/2);
		for (int i = 2; i < nVertexs_*2-1; i += 2) {
			double dist = Point2D.distance(puntsT[i], puntsT[i+1], amplada/2, altura/2);
			if (dist < distMin) {
				distMin = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}

	//Pre: amplada > 0 i altura > 0
	//Post: retorna una taula t on t[0] i t[1] són les coordenades x i y del punt del poligon més llunya al centre de l'area amplada x altura
	private double [] puntLlunyaAlCentreDeArea(int amplada, int altura) {
		double [] puntsT = obtenirPuntsPoligon();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMax = Point2D.distance(x, y, amplada/2, altura/2);
		for (int i = 2; i < nVertexs_*2-1; i += 2) {
			double dist = Point2D.distance(puntsT[i], puntsT[i+1], amplada/2, altura/2);
			if (dist > distMax) {
				distMax = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}

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

	//Pre: --
	//Post: retorna una taula(t) que conte els punts del poligon
	//      coordenada (t[i],t[i+1]) per i = 0 fins a 14 increment 2
	private double [] obtenirPuntsPoligon() {
		double [] puntsT = new double[nVertexs_*2]; 
		double [] coordenades = new double[6];
	
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

	/*TEST*/
	public void dibuixar(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.draw(poligon_);
	}

}