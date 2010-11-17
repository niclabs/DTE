/**
 * 
 */
package cl.nic.dte.examples;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import cl.nic.dte.net.ConexionSii;

/**
 * Esta clase se encarga de realizar una prueba de autenticacion
 * 
 */
public class TestAutenticacion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.err
					.println("Utilice: java cl.nic.dte.examples.TestAutenticacion "
							+ "<certDigital.p12> <password>");
			System.exit(-1);
		}
		
		try {
			// leo certificado y llave privada del archivo pkcs12
			// leo certificado y llave privada del archivo pkcs12
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(args[0]), args[1].toCharArray());
			String alias = ks.aliases().nextElement();
			System.out.println("Usando certificado " + alias
					+ " del archivo PKCS12: " + args[0]);

			X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
			PrivateKey key = (PrivateKey) ks.getKey(alias, args[1].toCharArray());

			ConexionSii con = new ConexionSii();
			String token = con.getToken(key, cert);

			System.out.println("\n\nToken: " + token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
