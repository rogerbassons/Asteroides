import java.awt.geom.Path2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Random;

//És un meteorit amb forma de polígon irregular
//Pot tenir dos tipus de formes diferents, sempre com a polígon irregular
//També pot tenir dues mides, gran o petit
//
//Comportament bàsic:
//	- Apareix en una posició donada al constructor
// 	- Es mou en una direcció i velocitat fixes, donades també al constructor
// 	- És controlat per la màquina
// 	- Segons la mida (1 o 2) serà petit o gran
// 	- Si és un meteorit gran, pot ser destruit en dos meteorits de la mateixa forma, de mida petita
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
	double dx_;
	double dy_;
	int mida_; //Si és 2 és petit, si és 1 és gran
	int nPunts_; //Nombre de punts del polígon

	//Pre: --
	//Post: el Meteorit té una posició (x,y), una velocitat horitzontal dx i una velocitat vertical dy i una mida m
	Meteorit(int x, int y, double dx, double dy, int m) {
		//Crear polígon
		dx_ = dx;
		dy_ = dy;
		mida_ = m;
		Random rand = new Random();
		int forma = 1;//rand.nextInt(5);
		establirForma(x, y, forma);
	}
	
	//Pre: mida_ val 1 o 2
	//Post: poligon_ conté la forma sent gran si mida_=1 i petit si mida_=2
	private void establirForma(int x, int y, int forma) {
		poligon_ = new Path2D.Double();
		poligon_.moveTo(x, y);
		if ( forma == 1 ) {
			poligon_.lineTo(x + (80/mida_), y + (10/mida_));
			poligon_.lineTo(x + (80/mida_), y + (10/mida_));
			poligon_.lineTo(x + (110/mida_), y + (50/mida_));
			poligon_.lineTo(x + (110/mida_), y + (90/mida_));
			poligon_.lineTo(x + (80/mida_), y + (120/mida_));
			poligon_.lineTo(x, y + (120/mida_));
			poligon_.lineTo(x - (30/mida_), y + (90/mida_));
		} else if ( forma == 2 ) {
			poligon_.lineTo(x + (50/mida_), y);
			poligon_.lineTo(x + (60/mida_), y + (30/mida_));
			poligon_.lineTo(x + (50/mida_), y + (40/mida_));
			poligon_.lineTo(x + (70/mida_), y + (50/mida_));
			poligon_.lineTo(x + (70/mida_), y + (60/mida_));
			poligon_.lineTo(x + (30/mida_), y + (70/mida_));
			poligon_.lineTo(x - (10/mida_), y + (60/mida_));
		} else if ( forma == 3 ) {
			poligon_.lineTo(x + (30/mida_), y - (10/mida_));
			poligon_.lineTo(x + (40/mida_), y + (70/mida_));
			poligon_.lineTo(x, y + (60/mida_));
			poligon_.lineTo(x - (10/mida_), y + (80/mida_));
			poligon_.lineTo(x - (30/mida_), y + (70/mida_));
			poligon_.lineTo(x - (30/mida_), y + (30/mida_));
			poligon_.lineTo(x - (10/mida_), y + (20/mida_));
		} else {
			poligon_.lineTo(x + (30/mida_), y);
			poligon_.lineTo(x + (50/mida_), y + (30/mida_));
			poligon_.lineTo(x + (50/mida_), y + (80/mida_));
			poligon_.lineTo(x, y + (90/mida_));
			poligon_.lineTo(x, y + (70/mida_));
			poligon_.lineTo(x - (20/mida_), y + (40/mida_));
			poligon_.lineTo(x - (20/mida_), y + (30/mida_));
		}
		poligon_.closePath();
	}
	
	//Pre: amplada > 0 i altura > 0
	//Post: Desplaça el Meteorit a la posició(p) determinada per totes les velocitats del Meteorit
	//      Si el Meteorit, situat a la posició p, està totalment fora de l'area amplada x altura llavors el Meteorit es teletransporta al
	//      marge/costat invers del qual ha sortit(superior, inferior, esquerra, dreta)
	public void moure(int amplada, int altura) {
		AffineTransform a = new AffineTransform(); //Moviments es concatenats
		a.translate(dx_, dy_); //desplaçar Meteorit segons velocitat horitzontal i vertical

		poligon_.transform(a); //aplicar els moviments al poligon que representa el Meteorit

		// Comprovar si ha sortit de amplada x altura (a)
		// Si ha sortit:
		//     Seleccionar el punt(p) del poligon_ més proper de a (últim de sortir)
		//     Comprovar per quin marge(m) de a ha ha sortit el Meteorit segons p
		//     Seleccionar el punt(l) del poligon_ més llunya de a (primer de sortir)
		//     Desplaçar el Meteorit al marge invers(i) de m de manera que l està exactament a la coordenada del marge i 
		if (haSortit(amplada,altura)) {
			double [] p = puntProperAlCentreDeArea(amplada,altura); //unicament per saber per quin marge ha sortit
			double px = p[0];
			double py = p[1];
			double [] l = puntLlunyaAlCentreDeArea(amplada,altura); //per teletransportar
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
		double distMin = Point2D.distance(x,y,amplada/2,altura/2);
		for (int i = 2; i <= 12; i += 2) {
			double dist = Point2D.distance(puntsT[i],puntsT[i+1],amplada/2,altura/2);
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
		double distMax = Point2D.distance(x,y,amplada/2,altura/2);
		for (int i = 2; i <= 12; i += 2) {
			double dist = Point2D.distance(puntsT[i],puntsT[i+1],amplada/2,altura/2);
			if (dist > distMax) {
				distMax = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}
	
	//Pre: --
	//Post: retorna una taula(t) que conte els punts del poligon
	//      coordenada (t[i],t[i+1]) per i = 0 fins a 12 increment 2
	private double [] obtenirPuntsPoligon() {
		double [] puntsT = new double[14]; 
		double [] coordenades = new double[14];
	
		PathIterator pi = poligon_.getPathIterator(null,0);
		int i = 0;
		while (!pi.isDone() && i < 13) {
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