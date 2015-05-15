import java.awt.Graphics2D;

/// @brief Un Objecte del joc que es dibuixa a un Graphics2D

public interface ObjecteJoc {
	/// @pre --
	/// @post s'ha dibuixat l'ObjecteJoc a g2
	public void dibuixar(Graphics2D g2);
}
