// Nau que té la capacitat d'atacar una Nau
import java.awt.geom.Path2D;
import java.lang.Math;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;

public class NauEnemiga extends Nau {

	//Pre: l > 0, x >= 0, y >= 0
	//Post: La Nau:
	//          - està viva
	//          - té una llargada l i una amplada(a) màxima l/2
	//          - te la punta superior a la coordenada (a/2)
	//          - apunta cap a dalt
	//          - esta situada al punt (x,y)
	//          - és de color c
	NauEnemiga(int l, Color c, double x, double y) {
		super(l,c);
		double [] centre = obtenirCentreTriangle();
		AffineTransform a = new AffineTransform();
		a.translate(x - centre[0], y - centre[1]);
		triangle_.transform(a);
	}

	// De moment només apunta a n
	RaigLaser atacarNau(Nau n, LinkedList<Meteorit> lm) {
		RaigLaser r = null;

		if (!evitarMeteorits(lm)) {
			double [] pos = n.obtenirCentreTriangle();
			if (distancia(pos[0],pos[1]) <= l_ * 10) {
				r = apuntaDispara(n);
			} else {
				movimentObjectiu(pos);
			}
		}
		return r;
	}

	//Pre: NauEnemiga Viva
	//Post: la NauEnemiga apunta a n i si està ben apuntada llavors dispara
	RaigLaser apuntaDispara(Nau n) {
		RaigLaser r = null;

		int angle = angleApuntarMoviment(n);

		alinearse(angle);

		if (Math.abs(angle_ - angle) <= 1) {
			r = disparar();
		}

		return r;
	}

	//Pre: 0 <= a < 360
	//Post: la NauEnemiga realitza un moviment de manera que tendeix a tenir un angle a
	private void alinearse(int a) {
		int dif = angle_ - a;
		if (a > 270 && angle_ < 90) {
			rotarDreta();
		} else if (angle_ > 270 && a < 90) {
			rotarEsquerra();
		} else if (dif > 0) {
			rotarDreta();
		} else if (dif < 0) {
			rotarEsquerra();
		} else {
			pararRotacio();
		}
	}

	//Pre: --
	//Post: retorna l'angle perque la NauEnemiga apunti al punt (p[0],p[1])
	private int angleApuntar(double [] p) {
		double [] ce = obtenirCentreTriangle();

		double angle = 180 / Math.PI * Math.atan2(-(p[1] - ce[1]), p[0] - ce[0]);
		// angle és l'angle respecte l'eix horitzontal que va de la Nau Enemiga a la Nau n
		// -180 < angle <= 180
		if (angle < 0) {
			angle = 360 + angle;
		}
		// 0 <= angle < 360
		return (int)angle;
	}

	//Pre: --
	//Post: retorna l'angle que ha d'apuntar la NauEnemiga(e) per aconseguir que un RaigLaser disparat per e
	//      colisioni amb n
	private int angleApuntarMoviment(Nau n) {
		double [] cn = n.obtenirCentreTriangle();
		double [] ce = obtenirCentreTriangle();

		double dist = Math.hypot(Math.abs(cn[0] - ce[0]), Math.abs(cn[1] - ce[1]));
		double temps = dist / velocitatRaig_;
		double [] previsio = preveurePosicio(n, temps);
		
		double angle = angleApuntar(previsio);
		return (int)angle;
	}

	//Pre: temps > 0
	//Post: Retorna una taula(t). t[0] i t[1] són les coordenades de n despres de que hagui passat temps iteracions
	double [] preveurePosicio(Nau n, double temps) {
		double dx = temps * n.velocitat_*Math.cos(Math.toRadians(n.angleVelocitat_));
		double dy = temps * n.velocitat_*-Math.sin(Math.toRadians(n.angleVelocitat_));

		double [] t = n.obtenirCentreTriangle();
		double centrex = t[0];
		double centrey = t[1];

		double [] previsio = {centrex + dx, centrey + dy};
		return previsio;
	}

	//Pre: --
	//Post: la NauEnemiga realitza un moviment que tendeix a aproparse
	//       a la posicio (pos[0],pos[1])
	private void movimentObjectiu(double [] pos) {
		int angle = angleApuntar(pos);
		alinearse(angle);
		propulsarEndavant();
	}

	//Pre: --
	//Post: Si NauEnemiga està aprop d'algun Meteorit de lm llavors l'evita i retorna cert altrament no fa res i retorna fals
	private boolean evitarMeteorits(LinkedList<Meteorit> lm) {
		double [] c = obtenirCentreTriangle();

		double [] v = null;
		double distMin = l_ * 2;
		
		Iterator<Meteorit> it = lm.iterator();
		while (it.hasNext()) {
			double [] aux = it.next().puntVertexMesProper(c[0],c[1]);
			double distAct = distancia(aux[0],aux[1]);
				
			if (distAct < distMin) {
				v = aux;
				distMin = distAct;
			}
		}
		if (v != null) {
			int angle = angleApuntar(v);
			angle += 180;
			if (angle >= 360) {
				angle -= 360;
			}
			alinearse(angle);
			propulsarEndavant();
		}

		return v != null;
	}
}
