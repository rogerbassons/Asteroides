import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

//És un raig làser amb forma circular
//És disparat per una Nau
//
//Comportament bàsic:
//	- Apareix en la posició (0,0), es pot posicionar amb situar()
//	- Té una mida donada al constructor
//	- Es mou en una direcció i velocitat fixes, donades al constructor
//	- És controlat per la màquina
//	- Si col·lisiona amb qualsevol objecte, el destrueix i desapareix
//	- Si sobrepassa un dels marges de la pantalla apareix pel costat oposat en la mateixa velocitat i direcció
//	- Si recorre més que la distància màxima establerta segons la mida de la pantalla, desapareix
//	
// Supòsits sobre l'area(a) on es mou el RaigLaser:
//     Té una mida i no canvia mentre existeix el RaigLaser
//     És un pla amb:
//         - un eix horitzontal X que augmenta d'esquerra a dreta (dreta és més)
//         - un eix vertical Y que augmenta de dalt a baix (a baix és més)
//

public class RaigLaser implements ObjecteJoc {

	private Ellipse2D cercle_;
	private double mida_;
	private double velocitat_;
	private double angleVelocitat_;
	private double distRecorreguda_;
	private double maxDist_;

	//Pre: mida > 0, velocitat >= 0
	//Post: s'ha creat un RaigLaser amb mida_ = mida, velocitat_ = velocitat i angleVelocitat_ = angle,
	//	també s'ha generat un cercle que representa el RaigLaser, el cercle_ està situat a (x,y)
	//	maxDist_ és y - 2*llargadaNau per evitar que col·lisioni amb la Nau que ha disparat el RaigLaser
	RaigLaser(double x, double y, double amplada, double altura, double velocitat, double angle, double mida, double llargadaNau) {
		//Calculem la distància màxima que pot recòrrer el RaigLaser
		if (altura < amplada) {
			maxDist_ = altura - (2 * llargadaNau);
		} else {
			maxDist_ = amplada - (2 * llargadaNau);
		}
		
		distRecorreguda_ = 0; //El raig no s'ha mogut
		mida_ = mida;
		velocitat_ = velocitat;
		angleVelocitat_ = angle;

		cercle_ = new Ellipse2D.Double(x, y, mida_, mida_); //Creem un el·lipse d'igual amplada que altura (cercle)
	}
	
	//Pre: --
	//Post: el RaigLaser s'ha mogut a la seguent posició determinada per la velocitat_ i l'angleVelocitat_
	//	si surt fora de l'àrea apareix en el costat oposat del que ha sortit
	public void moure(int amplada, int altura) {
		double dx = velocitat_ * Math.cos(Math.toRadians(angleVelocitat_));
		double dy = velocitat_ * -Math.sin(Math.toRadians(angleVelocitat_));
		
		AffineTransform a = new AffineTransform();
		a.translate(dx, dy); //desplaçar RaigLaser segons velocitat horitzontal i vertical
		
		Point2D.Double pos = new Point2D.Double(cercle_.getX(), cercle_.getY()); //Posició actual
		
		Point2D antPos = (Point2D) pos.clone(); //Desem la posició anterior per calcular la distància recorreguda
		
		a.transform(pos, pos); //Apliquem la transformació a pos i hi desem la nova posició
		cercle_.setFrame(pos.getX(), pos.getY(), mida_, mida_);
		
		distRecorreguda_ += antPos.distance(pos); //Actualitzem distància recorreguda
		
		if (haSortit(amplada, altura)) {
			double x = cercle_.getX();
			double y = cercle_.getY();

			if ( x < 0 ) {
				a.translate(amplada, 0); 
			} else if ( x > amplada ) {
				a.translate(-amplada, 0);
			} else if ( y < 0 ) {
				a.translate(0, altura);
			} else { //if ( y > altura )
				a.translate(0, -altura);
			}

			pos = new Point2D.Double(cercle_.getX(), cercle_.getY());
			a.transform(pos, pos);
			cercle_.setFrame(pos.getX(), pos.getY(), mida_, mida_);
		}
	}
	
	//Pre: --
	//Post: retorna si distRecorreguda_ < maxDist_
	public boolean existeix() {
		return distRecorreguda_ < maxDist_;
	}
	
	//Pre: --
	//Post: retorna si el RaigLaser ha sortit de l'àrea on es mou
	private boolean haSortit(double amplada, double altura) {
		double x = cercle_.getX();
		double y = cercle_.getY();
		return x < 0 || x > amplada || y < 0 || y > altura;
	}
	
	
	
	//Pre: --
	//Post: dibuixa el RaigLaser blanc a g2
	public void dibuixar(Graphics2D g2) {
		g2.setPaint(Color.WHITE);
		g2.fill(cercle_);
		g2.draw(cercle_);
	}
	
}