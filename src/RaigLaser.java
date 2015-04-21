import java.awt.geom.Path2D;

//És un raig làser amb forma circular
//És disparat per una Nau
//
//Comportament bàsic:
//	- Apareix en la posició (0,0), es pot posicionar amb situar()
//	- Té una mida donada al constructor
//	- Es mou en una direcció i velocitat fixes, donades al constructor
//	- És controlat per la màquina
//	- Si col·lisiona amb qualsevol objecte, el destrueix i desapareix
//	
// Supòsits sobre l'area(a) on es mou el RaigLaser:
//     Té una mida i no canvia mentre existeix el RaigLaser
//     És un pla amb:
//         - un eix horitzontal X que augmenta d'esquerra a dreta (dreta és més)
//         - un eix vertical Y que augmenta de dalt a baix (a baix és més)
//

public class RaigLaser {

	private Path2D cercle_;
	private double mida_;
	private double velocitat_;
	private double angleVelocitat_;
	
	//Pre: mida > 0, velocitat >= 0
	//Post: s'ha creat un RaigLaser amb mida_ = mida, velocitat_ = velocitat i angleVelocitat_ = angle,
	//	també s'ha generat un cercle que representa el RaigLaser
	RaigLaser(double mida, double velocitat, double angle){
		mida_ = mida;
		velocitat_ = velocitat;
		angleVelocitat_ = angle;
	}
	
}
	
	
	