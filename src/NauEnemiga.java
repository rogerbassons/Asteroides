// Nau que té la capacitat d'atacar una Nau
import java.awt.geom.Path2D;
import java.lang.Math;
import java.awt.geom.AffineTransform;

public class NauEnemiga extends Nau {

	//Pre: l > 0, x >= 0, y >= 0
	//Post: La Nau:
	//          - està viva
	//          - té una llargada l i una amplada(a) màxima l/2
	//          - te la punta superior a la coordenada (a/2)
	//          - apunta cap a dalt
	//          - esta situada al punt (x,y)
	NauEnemiga(int l, double x, double y) {
		super(l);
		double [] c = obtenirCentreTriangle();
		AffineTransform a = new AffineTransform();
		a.translate(x - c[0], y - c[1]);
		triangle_.transform(a);
	}

	// De moment només apunta a n
	RaigLaser atacarNau(Nau n) {
		RaigLaser r = null;
		int angle = angleApuntar(n);
		//System.out.println(Double.toString(angle));
		//System.out.println(Double.toString(angle_));
		int dreta = angle_ - angle;
		int esquerra = angle - angle_;
		if (dreta > esquerra ) {
			rotarDreta();
		} else if (dreta < esquerra) {
			rotarEsquerra();
		} else {
			r = disparar();
			pararRotacio();
		}

		return r;
	}


	//Pre: --
	//Post: retorna l'angle respecte l'eix horitzontal entre la NauEnemiga i n
	private int angleApuntar(Nau n) {
		double [] cn = n.obtenirCentreTriangle();
		double [] ce = obtenirCentreTriangle();

		double angle = 180 / Math.PI * Math.atan2(-(cn[1] - ce[1]), cn[0] - ce[0]);
		// angle és l'angle respecte l'eix horitzontal que va de la Nau Enemiga a la Nau n
		// -180 < angle <= 180
		if (angle < 0) {
			angle = 360 + angle;
		}
		// 0 <= angle < 360
		return (int)angle;
	}
}
