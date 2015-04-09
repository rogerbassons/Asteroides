import java.awt.geom.Path2D;

public class Nau {
       	private Path2D triangle_;
	private float dx_, dy_;
	private int angleRotacio_;
	private int nvides_;
	
	Nau(int l, int a, int x, int y) {
		triangle_ = new Path2D.Float();
		triangle_.moveTo(a/2,0);
		triangle_.lineTo(0,l);
		triangle_.lineTo(a,l);
		triangle_.closePath();
	}

	public void centrar(int amplada, int altura) {
		float centrex, centrey;
		obtenirCentreTriangle(centrex,centrey);
		AffineTransform a = new AffineTransform();
		int tx = 0;
		int ty = 0;
	
		if (centrex != amplada/2) {
			tx = amplada/2 - centrex;
		}
	
		if (centrey != altura/2) {
			ty = altura/2 - centrey;
		}
		
		a.translate(tx, ty);
		triangle_.transform(a);
	}

	private void obtenirCentreTriangle(float centrex, float centrey) {
		float [] puntsT = new float[6]; 
		float [] coordenades = new float[6];
	
		PathIterator pi = triangle_.getPathIterator(null,0);
		pi.currentSegment(coordenades);
	
		puntsT[0] = coordenades[0];
		puntsT[1] = coordenades[1];
		int i = 2;
		while (!pi.isDone()) {
			pi.currentSegment(coordenades);
			puntsT[i] = coordenades[2];
			puntsT[i+1] = coordenades[3];
			pi.next();
		}
		centrex = (puntsT[0]+puntsT[2]+puntsT[4])/3;
		centrey = (puntsT[1]+puntsT[3]+puntsT[5])/3;
	}
	
