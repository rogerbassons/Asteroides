import java.awt.geom.Path2D

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
	float [] puntsT = new float[6]; 
	PathIterator pi = triangle_.getPathIterator(null,0);
	float [] coordenades = new float[6];
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
	int centrex = (puntsT[0]+puntsT[2]+puntsT[4])/3;
	int centrey = (puntsT[1]+puntsT[3]+puntsT[5])/3;	
	AffineTransform a = new AffineTransform();
	a.translate(amplada/2, altura/2);
}