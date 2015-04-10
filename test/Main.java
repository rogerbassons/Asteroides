import java.io.*;
public class Main {
	public static void main(String[] args) {
		Nau n = new Nau(8,4);
		double [] punts = n.obtenirPuntsTriangle();

		for (double i : punts) {
			System.out.println(i);
		}
	}
}
