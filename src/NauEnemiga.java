// Nau que té la capacitat d'atacar una Nau
import java.awt.geom.Path2D;

public class NauEnemiga extends Nau {

	//Pre: l > 0
	//Post: La Nau:
	//          - està viva
	//          - té una llargada l i una amplada(a) màxima l/2
	//          - te la punta superior a la coordenada (a/2)
	//          - La Nau apunta cap a dalt
	NauEnemiga(int l) {
		super(l);
	}
	
	RaigLaser atacarNau(Nau n) {
		double [] cn = n.obtenirCentreTriangle();
		double [] ce = obtenirCentreTriangle();

		double tangentAngle = (ce[1] - cn[1]) / (ce[0] - cn[0]);
		double angle = Math.atan(Math.toRadians(tangentAngle));
		if (angle_ < angle) {
			rotarDreta();
		} else if (angle_ > angle) {
			rotarEsquerra();
		} else {
			pararRotacio();
		}
			
		return null;
	}
}
