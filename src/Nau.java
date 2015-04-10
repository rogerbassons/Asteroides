import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

// És una nau espacial triangular isòsceles que pot rotar sobre si mateixa, es pot propulsar endavant i disparar rajos làser.
// Quan la nau es propulsa endavant, s'accelera fins la seva velocitat màxima. Si la nau no és propulsada i està en moviment,
// cada certa distància es desaccelera el seu moviment fins a parar-se.
// La Nau té inicialment 3 vides, cada vegada que es destrueix(explota) perd una vida.

public class Nau {
	
       	private Path2D triangle_; // camí geometric amb forma de triangle isòceles que representa la Nau
	private int nvides_; // nombre de vides que te la Nau
	
	// Distancia que la Nau es mou en sentit horitzontal i vertical, respectivament, quan es crida el metode moure()
	private float dx_, dy_;
	private int angleRotacio_; // Angle que rota la Nau sobre el seu baricentre quan es crida el metode moure()
	
	// Distancia màxima que es pot moure la nau en qualsevol direccio amb una unica crida del metode moure()
	private distanciaMax_;
	
	
	//Pre: l > 0 i a > 0
	//Post: La Nau:
	//          - té una llargada l i una amplada màxima a. L
	//          - te la punta superior a la coordenada (a/2)
	//          - la part de darrera de la nau forma un angle de 90 graus amb l'eix vertical(la Nau apunta cap a dalt)
	Nau(int l, int a) {
		triangle_ = new Path2D.Float();
		triangle_.moveTo(a/2,0);
		triangle_.lineTo(0,l);
		triangle_.lineTo(a,l);
		triangle_.closePath();

		nvides_ = 3;
		distanciaMax_ = 2 * l // En una unica crida de moure() es pot desplaçar com a màxim una distancia de 2 cops la seva llargada
	}

	//Pre: amplada > 0 i altura > 0
	//Post: s'ha centrat el triangle al pla de mida amplada*altura
	public void centrar(int amplada, int altura) {
		float centrex = 0;
		float centrey = 0;
		obtenirCentreTriangle(centrex,centrey);
		AffineTransform a = new AffineTransform();
		float tx = 0;
		float ty = 0;
	
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
	//Post: centrex i centrey són les coordenades x i y del baricentre del triangle que forma la nau, respectivament
	private void obtenirCentreTriangle(float centrex, float centrey) {
		float [] puntsT = new float[6]; 
		float [] coordenades = new float[6];
	
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
	public float [] obtenirPuntsTriangle() {
		float [] puntsT = new float[6]; 
		float [] coordenades = new float[6];
	
		PathIterator pi = triangle_.getPathIterator(null,0);
		int i = 0;
		while (!pi.isDone() && i < 5) {
			pi.currentSegment(coordenades);
			puntsT[i] = coordenades[0];
			puntsT[i+1] = coordenades[1];
			i += 2;
			for (float j : coordenades) {
				System.out.println(j);
			}
			System.out.println("----------");
			pi.next();
		}
		return puntsT;
	}
}
	
