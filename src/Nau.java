import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.lang.Math;

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
//     Si la Nau està en moviment(alguna velocitat != 0), es frena el seu moviment degut a una resistencia que és directament proporcional
//     a la velocitat de la Nau.
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

public class Nau {
	
       	private Path2D triangle_; // camí geomètric que sempre forma un triangle isòceles(representa gràficament la Nau)
	private int angle_; // angle que forma la Nau respecte l'eix horitzontal
	private boolean viva_; // defineix l'estat de la Nau. Cert -> Nau viva, Fals-> Nau morta
	
	// Distancia que la Nau es mou en sentit horitzontal i vertical, respectivament, quan es crida el metode moure(...)
	private double dx_, dy_;
	// Distancia màxima que es pot moure la nau en qualsevol direccio amb una unica crida del metode moure(...)
	private double distanciaMax_;
	private double acceleracio_; // Acceleracio amb la qual la velocitat de la nau augmenta o disminueix
	private double coefFrenada_; // La Nau es frena un coeficient coefFrenada_ de la velocitat de la Nau
	
	private int angleRotacio_; // Angle que rota la Nau sobre el seu baricentre(valor fix)
	private int rotar_;// Defineix si en el metode moure() la Nau ha de rotar sobre els seu baricentre
	// 0 -> no s'ha de rotar
	// 1 -> rotar en el sentit esquerra
	// 2 -> rotar en el sentit dret

	
	//Pre: l > 0 i a > 0
	//Post: La Nau:
	//          - està viva
	//          - té una llargada l i una amplada màxima a
	//          - te la punta superior a la coordenada (a/2)
	//          - la part de darrera de la nau forma un angle de 90 graus amb l'eix vertical(la Nau apunta cap a dalt)
	Nau(int l, int a) {
		// Crear triangle
		triangle_ = new Path2D.Double();
		triangle_.moveTo(a/2,0);
		triangle_.lineTo(0,l);
		triangle_.lineTo(a,l);
		triangle_.closePath();
		angle_ = 270; // l'eix Y està invertit respecte a l'eix Y tradicional

		nvides_ = 3;

		// atributs de moviment
		dx_ = dy_ = 0;
		distanciaMax_ = l/10;
		acceleracio_ = l/100;
		rotar_ = 0;
		angleRotacio_ = 5;
	}

	//Pre: Nau viva, amplada > 0 i altura > 0
	//Post: s'ha centrat el triangle a l'area amplada*altura
	public void centrar(int amplada, int altura) {
		double centrex = 0;
		double centrey = 0;
		obtenirCentreTriangle(centrex,centrey);
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
	//Post: s'augmenta la velocitat de la Nau en el sentit en el que apunta
	public void propulsarEndavant() {
		double seguentdx = dx_ + Math.sin(Math.toRadians(angle_))*acceleracio_;
		double seguentdy = dy_ - Math.cos(Math.toRadians(angle_))*acceleracio_;

		if (seguentdx < distanciaMax_) {
			dx_ = seguentdx;
		} else {
			dx_ = distanciaMax_;
		}
		if (seguentdy < distanciaMax_) {
			dy_ = seguentdy;
		} else {
			dy_ = distanciaMax_;
		}
	}

	//Pre: Nau té vides
	//Post: es disminueix la velocitat de la Nau en el sentit contrari al moviment
	public void frenar() {
		if (dx_ > 0) {
		        dx_ -= dx_*coefFrenada_;
		} else {
			dx_ += dx_*coefFrenada_;
		}

		if (dy_ > 0) {
			dy_ -= dy_*coefFrenada_;
		} else {
			dy_ += dy_*coefFrenada_;
		}
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
	//      Es frena el moviment de la Nau determinat per una resistencia que és directament proporcional a la velocitat de la Nau.
	//      Si la Nau, situada a la posició p, està totalment fora de l'area amplada x altura llavors la Nau es teletransporta al
	//      marge/costat invers del qual ha sortit(superior, inferior, esquerra, dreta)
	public void moure(int amplada, int altura) {
		AffineTransform a = new AffineTransform();
		a.translate(dx_, dy_);
		
		if (rotar_ == 1) {
			a.setToRotation(Math.toRadians(angleRotacio_));
			angle_ = angle_ + angleRotacio_;
		} else if (rotar_ == 2) {
			a.setToRotation(Math.toRadians(-angleRotacio_));
			angle_ = angle_ - angleRotacio_;
		}
		
		double [] puntsT = obtenirPuntsTriangle();
		boolean trobat = false;
	        int i = 0;
		while (!trobat && i < 5) {
			trobat = puntsT[i] >= 0 && puntsT[i] <= amplada && puntsT[i+1] >= 0 && puntsT[i+1] <= altura;
		}
		if (!trobat) {
			//ha sortit a fora a pendre l'aire
		}
		
		triangle_.transform(a);
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
		return nvides_ > 0;
	}

	//Pre: --
	//Post: centrex i centrey són les coordenades x i y del baricentre del triangle que forma la Nau, respectivament
	private void obtenirCentreTriangle(double centrex, double centrey) {
		double [] puntsT = obtenirPuntsTriangle();
		centrex = (puntsT[0]+puntsT[2]+puntsT[4])/3;
		centrey = (puntsT[1]+puntsT[3]+puntsT[5])/3;
	}

	//Pre: --
	//Post: retorna una taula(t) que conte els punts del triangle
	//      coordenada (t[i],t[i+1]) per i = 0 fins a 4 increment 2
	private double [] obtenirPuntsTriangle() {
		double [] puntsT = new double[6]; 
		double [] coordenades = new double[6];
	
		PathIterator pi = triangle_.getPathIterator(null,0);
		int i = 0;
		while (!pi.isDone() && i < 5) {
			pi.currentSegment(coordenades);
			puntsT[i] = coordenades[0];
			puntsT[i+1] = coordenades[1];
			i += 2;
			for (double j : coordenades) {
				System.out.println(j);
			}
			System.out.println("----------");
			pi.next();
		}
		return puntsT;
	}
}
	
