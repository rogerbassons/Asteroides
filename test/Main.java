import java.io.*;
public class Main {
	public static void main(String[] args) {
		Nau n = new Nau(8,4);
		float [] punts = n.obtenirPuntsTriangle();

		for (float i : punts) {
			System.out.println(i);
		}

		float centrex = 0;
		float centrey = 0;
		n.obtenirCentreTriangle(centrex,centrey);
		System.out.println(centrex);
		System.out.println(centrey);
	}
}
