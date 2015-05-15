import java.awt.geom.Path2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.lang.Math;
import java.awt.geom.Point2D; //calcular la distancia entre dos punts
import java.awt.Graphics2D;
import java.awt.Color;

// És una nau espacial triangular isòsceles que pot:
//     - rotar sobre si mateixa
//     - propulsar-se endavant
//     - disparar rajos làser
//
// Comportament bàsic:
//
//     La Nau rota un angle definit(sempre es el mateix).
//
//     Quan la Nau es propulsa endavant, la velocitat s'augmenta en la direcció i sentit que té (fins la seva velocitat màxima).
//     Si la Nau està en moviment(alguna velocitat != 0), es frena el seu moviment degut a una resistencia.
//
//     La Nau té una vida, cada vegada que es destrueix(explota) mort. Es pot reanimar.
//
//     Degut a que la Nau es mou pot sortir d'una area(a) definida. Als parametres del mètode moure(..) queda definida a. En el cas
//     de que la Nau es mogui totalment fora de a, la Nau es teletransporta al marge/costat invers del qual ha sortit(superior, inferior,
//     esquerra, dreta).
//
// Supòsits sobre l'area(a) on es mou la Nau:
//     Té mida fixa i no canvia durant la vida de la Nau
//     És un pla amb:
//         - un eix horitzontal X que augmenta d'esquerra a dreta (dreta és més)
//         - un eix vertical Y que augmenta de dalt a baix (a baix és més)
//
// Altres:
//     Els mètodes propulsarEndavant(), rotarEsquerra(), rotarDreta(), paraRotacio() no mouen la Nau per si sols. Són com els comandaments
//     de la Nau amb la difrencia que despres d'actuar sobre aquests comandaments s'ha de cridar el metode moure(..) per a desplaçar la Nau
//     segons les modificacions/actuacions sobre els comandaments.
//
//     Excepte el constructor i el mètode esViva(), la Nau ha d'estar viva per poder utilitzar els altres mètodes.

public class Nau implements ObjecteJoc {
       	protected Path2D triangle_; // camí geomètric que sempre forma un triangle isòceles(representa gràficament la Nau)
	private Color c_; //Color de la Nau
	private int nombrePunts_; // nombre de punts que té el triangle_
	protected int l_; // llargada de la Nau
	protected int angle_; // angle que forma la Nau respecte l'eix horitzontal
	private boolean viva_; // defineix l'estat de la Nau. Cert -> Nau viva, Fals-> Nau morta

	protected double velocitat_; // Modul del vector velocitat de la Nau
	protected double angleVelocitat_; //angle_ que tenia la Nau a l'última propulsació
	private double velocitatMax_; // Velocitat màxima de la Nau
	private double acceleracio_; // Acceleracio amb la qual la velocitat de la nau augmenta o disminueix
		
	private int angleRotacio_; // Angle que rota la Nau sobre el seu baricentre(valor fix)
	private int rotar_;// Defineix si en el metode moure() la Nau ha de rotar sobre els seu baricentre
	// 0 -> no s'ha de rotar
	// 1 -> rotar en el sentit esquerra
	// 2 -> rotar en el sentit dret

	protected double velocitatRaig_; // velocitat amb la qual es mouen els Raigos Laser que dispara la Nau

	
	//Pre: l > 0
	//Post: La Nau:
	//          - està viva
	//          - té una llargada l i una amplada(a) màxima l/2
	//          - te la punta superior a la coordenada (a/2)
	//          - apunta cap a dalt
	//          - és de color c
	Nau(int l, Color c) {
		// Crear triangle
		int ampladaBase = l / 2;
		triangle_ = new Path2D.Double();
		triangle_.moveTo(ampladaBase/2,0);
		triangle_.lineTo(0,l);
		triangle_.lineTo(ampladaBase/2,l-l/5);
		triangle_.lineTo(ampladaBase,l);
		triangle_.closePath();
		nombrePunts_ = 4;
		angle_ = 90;
		c_ = c;
		l_ = l;

		// atributs de moviment
		velocitat_ = 0;
		angleVelocitat_ = 0;
		velocitatMax_ = l/15;
		acceleracio_ = l/50;
		rotar_ = 0;
		angleRotacio_ = 5;

		//La Nau està viva!
		viva_ = true;

		velocitatRaig_ = velocitatMax_*1.5;
	}

	//Pre: Nau viva, amplada > 0 i altura > 0
	//Post: s'ha centrat el triangle a l'area amplada*altura
	public void centrar(int amplada, int altura) {
		double [] t = obtenirCentreTriangle();
		double centrex = t[0];
		double centrey = t[1];
		AffineTransform a = new AffineTransform();
		double tx = 0;
		double ty = 0;
	
		if (centrex != amplada/2) {
			tx = amplada/2 - centrex;
		}
	
		if (centrey != altura/2) {
			ty = altura/2 - centrey;
		}
		
		a.translate(tx, ty);
		triangle_.transform(a);
	}

	//Pre: Nau viva
	//Post: la Nau dispara un RaigLaser
	public RaigLaser disparar() {
		double [] puntsT = obtenirPuntsTriangle();
		double x = puntsT[0];
		double y = puntsT[1];
		double midaRaig = llargadaNau()/15;
		return new RaigLaser(x,y,velocitatRaig_,angle_,midaRaig);
	}

	//Pre: Nau viva
	//Post: s'augmenta la velocitat de la Nau en el sentit en el que apunta
	public void propulsarEndavant() {
		double seguentVelocitat = velocitat_ + acceleracio_;
		if (seguentVelocitat > velocitatMax_) {
			seguentVelocitat = velocitatMax_;
		}
		velocitat_ = seguentVelocitat;
		angleVelocitat_ = angle_;
	}

	//Pre: Nau viva
	//Post: Es frena el moviment de la Nau determinat per una resistencia que és directament proporcional a la velocitat de la Nau.
	private void frenar() {
		double seguentVelocitat = velocitat_ - acceleracio_ * 0.01;
		if (seguentVelocitat < 0) {
			seguentVelocitat = 0;
		}
		velocitat_ = seguentVelocitat;
	}

	//Pre: Nau viva
	//Post: la velocitat rotacional de la Nau és màxima en el sentit contrari de les agulles del rellotge
	public void rotarEsquerra() {
		rotar_ = 1;
	}

	//Pre: Nau viva
	//Post: la velocitat rotacional de la Nau és màxima en el sentit de les agulles del rellotge
	public void rotarDreta() {
		rotar_ = 2;
	}

	//Pre: Nau viva
	//Post: la velocitat rotaciona de la Nau és 0 (no rota en cap sentit)
	public void pararRotacio() {
		rotar_ = 0;
	}
	
	//Pre: Nau viva, amplada > 0 i altura > 0
	//Post: Desplaça la Nau a la posició(p) determinada per totes les velocitats de la Nau
	//      Es frena el seu moviment degut a una resistencia
	//      Si la Nau, situada a la posició p, està totalment fora de l'area amplada x altura llavors la Nau es teletransporta al
	//      marge/costat invers del qual ha sortit(superior, inferior, esquerra, dreta)
	public void moure(int amplada, int altura) {
		AffineTransform a = new AffineTransform(); //Tots els moviments es concatenen

		frenar();
		double dx = velocitat_*Math.cos(Math.toRadians(angleVelocitat_));
		double dy = velocitat_*-Math.sin(Math.toRadians(angleVelocitat_));
		a.translate(dx,dy); // desplaçar la Nau segons la velocitat

		double [] t = obtenirCentreTriangle();
		double centrex = t[0];
		double centrey = t[1];
		//rotar la Nau
		
		if (rotar_ == 1) {
			a.rotate(Math.toRadians(-angleRotacio_),centrex,centrey);
			angle_ = angle_ + angleRotacio_;
			if (angle_ >= 360) {
				angle_ -= 360;
			}
		} else if (rotar_ == 2) {
			a.rotate(Math.toRadians(angleRotacio_),centrex,centrey);
			angle_ = angle_ - angleRotacio_;
			if (angle_ < 0) {
				angle_ += 360;
			}
		}

		triangle_.transform(a); //aplicar els moviments al triangle que representa la Nau

		// Comprovar si ha sortit de amplada x altura (a)
		// Si ha sortit:
		//     Seleccionar el punt(p) del triangle_ més proper de a (últim de sortir)
		//     Comprovar per quin marge(m) de a ha ha sortit la Nau mitjançant segons p
		//     Seleccionar el punt(l) del triangle_ més llunya de a (primer de sortir)
		//     Desplaçar la Nau al marge invers(i) de m de manera que l està exactament a la coordenada del marge i 
		if (haSortit(amplada,altura)) {
			double [] p = puntProperAlCentreDeArea(amplada,altura); //unicament per saber per quin marge ha sortit
			double px = p[0];
			double py = p[1];
			double [] l = puntLlunyaAlCentreDeArea(amplada,altura); //per teletransportar
			double lx = l[0];
			double ly = l[1];
	

			//coordenades desti del punt més llunya
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
			triangle_.transform(a);
		}
	}

	//Pre: --
	//Post: diu si la Nau ha sortit de l'area amplada x altura
	private boolean haSortit(int amplada, int altura) {
		double [] puntsT = obtenirPuntsTriangle();
		boolean sortit = false;
	        int i = 0;
		while (!sortit && i < nombrePunts_ * 2 - 1) {
			double x = puntsT[i];
			double y = puntsT[i+1];
			sortit = x < 0 || x > (double)amplada || y < 0 || y > (double)altura;
			i++;
		}
		return sortit;
	}
	
	//Pre: amplada > 0 i altura > 0
	//Post: retorna una taula t on t[0] i t[1] són les coordenades x i y del punt del triangle(que forma la Nau) més proper al centre de l'area amplada x altura
	private double [] puntProperAlCentreDeArea(int amplada, int altura) {
		double [] puntsT = obtenirPuntsTriangle();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMin = Point2D.distance(x,y,amplada/2,altura/2);
		for (int i = 2; i < nombrePunts_ * 2 - 1; i += 2) {
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
	//Post: retorna una taula t on t[0] i t[1] són les coordenades x i y del punt del triangle(que forma la Nau) més llunya al centre de l'area amplada x altura
	private double [] puntLlunyaAlCentreDeArea(int amplada, int altura) {
		double [] puntsT = obtenirPuntsTriangle();
		double x = puntsT[0];
		double y = puntsT[1];
		double distMax = Point2D.distance(x,y,amplada/2,altura/2);
		for (int i = 2; i < nombrePunts_ * 2 - 1; i += 2) {
			double dist = Point2D.distance(puntsT[i],puntsT[i+1],amplada/2,altura/2);
			if (dist > distMax) {
				distMax = dist;
				x = puntsT[i];
				y = puntsT[i+1];
			}
		}
		return new double[] {x,y};
	}

	//Pre: Nau viva
	//Post: la Nau mort(RIP)
	public void morir() throws Exception {
		if (!viva_) {
			throw new Exception("La Nau no te vides");
		} 
		viva_ = false;
	}

	//Pre: --
	//Post: Diu si la Nau és viva
	public boolean esViva() {
		return viva_;
	}

	//Pre: --
	//Post: La Nau és viva, apunta cap a dalt, està parada i te la punta superior a la coordenada (a/2)
	public void reanimar() {
		viva_ = true;
		velocitat_ = 0;
		angleVelocitat_ = 0;
		rotar_ = 0;
		int ampladaBase = l_ / 2;
		triangle_ = new Path2D.Double();
		triangle_.moveTo(ampladaBase/2,0);
		triangle_.lineTo(0,l_);
		triangle_.lineTo(ampladaBase/2,l_-l_/5);
		triangle_.lineTo(ampladaBase,l_);
		triangle_.closePath();
	}

	//Pre: --
	//Post: retorna una taula t on t[0] i t[1] són les coordenades x i y del baricentre del triangle que forma la Nau, respectivament
	protected double [] obtenirCentreTriangle() {
		double [] puntsT = obtenirPuntsTriangle();
		double centrex = (puntsT[0]+puntsT[2]+puntsT[6])/3;
		double centrey = (puntsT[1]+puntsT[3]+puntsT[7])/3;
		return new double[] {centrex,centrey};
	}

	//Pre: --
	//Post: retorna la llargada de la Nau
	private double llargadaNau() {
		double [] puntsT = obtenirPuntsTriangle();
		double llargada = Math.hypot(puntsT[2] - puntsT[0], puntsT[3] - puntsT[1]);
		return llargada;
	}

	//Pre: --
	//Post: retorna una taula(t[0..nombrePunts_*2-1) que conte els punts del triangle 
	private double [] obtenirPuntsTriangle() {
		double [] puntsT = new double[nombrePunts_*2];
		double [] coordenades = new double[6];
	
		PathIterator pi = triangle_.getPathIterator(null,0);
		int i = 0;
		while (!pi.isDone() && i < nombrePunts_ * 2 - 1) {
			pi.currentSegment(coordenades);
			puntsT[i] = coordenades[0];
			puntsT[i+1] = coordenades[1];
			i += 2;
			pi.next();
		}
		return puntsT;
	}

	//Pre: x >= 0 i y >= 0
	//Post: retorna la distancia entre el centre de la Nau i el punt (x,y)
	protected double distancia(double x, double y) {
		double [] c = obtenirCentreTriangle();
		return Math.hypot(Math.abs(c[0] - x), Math.abs(c[1] -y));
	}

	//Pre: --
	//Post: s'ha dibuixat la Nau de color verd a g2
	public void dibuixar(Graphics2D g2) {
		g2.setColor(c_);
		g2.draw(triangle_);
	}
	
	//Pre: --
	//Post: retorna el polígon de la Nau
	public Shape obtenirShape() {
		return triangle_;
	}
}
	
