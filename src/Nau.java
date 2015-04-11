import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.lang.Math;

// És una nau espacial triangular isòsceles que pot rotar sobre si mateixa, es pot propulsar endavant i disparar rajos làser.
// Quan la nau es propulsa endavant, s'accelera fins la seva velocitat màxima. Si la nau no és propulsada i està en moviment,
// cada certa distància es desaccelera el seu moviment fins a parar-se.
// La Nau té inicialment 3 vides, cada vegada que es destrueix(explota) perd una vida.

public class Nau {
	
       	private Path2D triangle_; // camí geomètric amb forma de triangle isòceles que representa la Nau
	private int angle_; // angle que forma la Nau respecte l'eix horitzontal
	private int nvides_; // nombre de vides que te la Nau
	
	// Distancia que la Nau es mou en sentit horitzontal i vertical, respectivament, quan es crida el metode moure()
	private double dx_, dy_;
	// Distancia màxima que es pot moure la nau en qualsevol direccio amb una unica crida del metode moure()
	private double distanciaMax_;
	private double acceleracio_; // Acceleracio amb la qual la velocitat de la nau augmenta o disminueix
	
	private int angleRotacio_; // Angle que rota la Nau sobre el seu baricentre
	private int rotar_;// Defineix si en el metode moure() la Nau ha de rotar sobre els seu baricentre
	// 0 -> no s'ha de rotar
	// 1 -> rotar en el sentit esquerra
	// 2 -> rotar en el sentit dret
	
	

	
	
	//Pre: l > 0 i a > 0
	//Post: La Nau:
	//          - té una llargada l i una amplada màxima a. L
	//          - te la punta superior a la coordenada (a/2)
	//          - la part de darrera de la nau forma un angle de 90 graus amb l'eix vertical(la Nau apunta cap a dalt)
	Nau(int l, int a) {
		triangle_ = new Path2D.Double();
		triangle_.moveTo(a/2,0);
		triangle_.lineTo(0,l);
		triangle_.lineTo(a,l);
		triangle_.closePath();
		angle_ = 180;

		nvides_ = 3;
		dx_ = dy_ = 0;
		distanciaMax_ = l/10;
		acceleracio_ = l/100;
		rotar_ = 0;
	}

	//Pre: amplada > 0 i altura > 0
	//Post: s'ha centrat el triangle al pla de mida amplada*altura
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

	//Pre: -- 
	//Post: s'augmenta la velocitat de la Nau en el sentit en el que apunta
	public void propulsarEndavant() {
		double seguentdx = dx_ + Math.sin(Math.toRadians(angle_))*acceleracio_;
		double seguentdy = dy_ + Math.cos(Math.toRadians(angle_))*acceleracio_;

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

	//Pre: --
	//Post: la velocitat rotacional de la Nau és màxima en el sentit contrari de les agulles del rellotge
	public void rotarEsquerra() {
		rotar_ = 1;
	}

	//Pre: --
	//Post: la velocitat rotacional de la Nau és màxima en el sentit de les agulles del rellotge
	public void rotarDreta() {
		rotar_ = 2;
	}

	//Pre: --
	//Post: la Nau no rota en cap sentit
	public void pararRotacio() {
		rotar_ = 0;
	}
	
	//Pre: --
	//Post: tenint en compte totes les velocitats actuals de la Nau, desplaça la nau a la posició determinada per aquestes velocitats. Altrament, si la nau està totalment parada, no fa res. 
	public void moure(){
		if (acceleracio_ != 0) {
			AffineTransform m = new AffineTransform();
			double dxmov = dx_;
			double dymov = dy_;
			if (dxmov > distanciaMax_) {
				dxmov = distanciaMax_;
			}
			if (dymov > distanciaMax_) {
				dymov = distanciaMax_;
			}
			m.translate(dxmov, dymov);
			triangle_.transform(m);
		}
		if (rotar_ != 0) {
			AffineTransform r = new AffineTransform();
			if (rotar_ == 1) {
				r.setToRotation(Math.toRadians(-angleRotacio_));
				angle_ = angle_ - angleRotacio_;
			} else {
				r.setToRotation(Math.toRadians(angleRotacio_));
				angle_ = angle_ + angleRotacio_;
			}
			triangle_.transform(r);
		}
	}
	
	//Pre: nombre de vides de la Nau>0
	//Post: la Nau perd una vida
	public void morir() throws Exception {
		if (nvides_ > 0) {
			nvides_--;
		} else {
			throw new Exception("La Nau no te vides");
		}
	}

	//Pre: --
	//Post: retorna cert (diu) si el nombre de vides de la Nau >0
	public boolean teVides() {
		return nvides_ > 0;
	}

	//Pre: --
	//Post: centrex i centrey són les coordenades x i y del baricentre del triangle que forma la nau, respectivament
	private void obtenirCentreTriangle(double centrex, double centrey) {
		double [] puntsT = new double[6]; 
		double [] coordenades = new double[6];
	
		PathIterator pi = triangle_.getPathIterator(null,0);
		int i = 0;
		while (!pi.isDone() && i < 5) {
			pi.currentSegment(coordenades);
			puntsT[i] = coordenades[0];
			puntsT[i+1] = coordenades[1];
			i += 2;
			pi.next();
		}
		centrex = (puntsT[0]+puntsT[2]+puntsT[4])/3;
		centrey = (puntsT[1]+puntsT[3]+puntsT[5])/3;
	}

	/* TEST */
	public double [] obtenirPuntsTriangle() {
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
	
