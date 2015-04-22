import java.awt.*;
import java.awt.geom.Path2D;
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
//	- Si sobrepassa un dels marges de la pantalla desapareix
//	
// Supòsits sobre l'area(a) on es mou el RaigLaser:
//     Té una mida i no canvia mentre existeix el RaigLaser
//     És un pla amb:
//         - un eix horitzontal X que augmenta d'esquerra a dreta (dreta és més)
//         - un eix vertical Y que augmenta de dalt a baix (a baix és més)
//

public class RaigLaser {

	private Shape cercle_;
	private double mida_;
	private double velocitat_;
	private double angleVelocitat_;
	private boolean fora_;

	//Pre: mida > 0, velocitat >= 0
	//Post: s'ha creat un RaigLaser amb mida_ = mida, velocitat_ = velocitat i angleVelocitat_ = angle,
	//	també s'ha generat un cercle que representa el RaigLaser, el cercle_ està situat a (x,y)
	RaigLaser(double x, double y, double velocitat, double angle, double mida) {
		mida_ = mida;
		fora_ = false;
		velocitat_ = velocitat;
		angleVelocitat_ = angle;
		cercle_ = new Ellipse2D.Double(x, y, mida_, mida_); //Creem un el·lipse d'igual amplada que altura (cercle)
	}
	
	//Pre: --
	//Post: el RaigLaser s'ha mogut a la seguent posició determinada per la velocitat_ i l'angleVelocitat_
	//	si surt fora de l'àrea desapareix i posa fora_ a true
	public void moure(int amplada, int altura) {
		AffineTransform a = new AffineTransform();
		double dx = velocitat_ * Math.cos(Math.toRadians(angleVelocitat_));
		double dy = velocitat_ * -Math.sin(Math.toRadians(angleVelocitat_));
		a.translate(dx, dy); //desplaçar RaigLaser segons velocitat horitzontal i vertical
		
		cercle_ = a.createTransformedShape(cercle_);
	}
	
	/*TEST*/
	public void dibuixar(Graphics2D g2) {
		g2.setPaint(Color.WHITE);
		g2.fill(cercle_);
		g2.draw(cercle_);
	}
	
}