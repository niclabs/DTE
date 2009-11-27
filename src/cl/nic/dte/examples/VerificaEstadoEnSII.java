/**
 * Copyright [2009] [NIC Labs]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the 	License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or 
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 **/

package cl.nic.dte.examples;

import jargs.gnu.CmdLineParser;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.apache.xmlbeans.XmlOptions;

import cl.nic.dte.net.ConexionSii;
import cl.nic.dte.util.Utilities;
import cl.sii.siiDte.DTEDocument;
import cl.sii.xmlSchema.RESPUESTADocument;

public class VerificaEstadoEnSII {

	private static void printUsage() {
		System.err
				.println("Utilice: java cl.nic.dte.examples.VerificaEstadoEnSII "
						+ "-c <certDigital.p12> -s <password>  <documento.xml>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option certOpt = parser.addStringOption('c', "cert");
		CmdLineParser.Option passOpt = parser.addStringOption('s', "password");
//		CmdLineParser.Option enviadorOpt = parser.addStringOption('e',
//				"consultador");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}

		String certS = (String) parser.getOptionValue(certOpt);
		String passS = (String) parser.getOptionValue(passOpt);
		//String enviadorS = (String) parser.getOptionValue(enviadorOpt);

		if (certS == null || passS == null ) {
			printUsage();
			System.exit(2);
		}

		String[] otherArgs = parser.getRemainingArgs();

		if (otherArgs.length != 1) {
			printUsage();
			System.exit(2);
		}

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		DTEDocument doc = DTEDocument.Factory.parse(new FileInputStream(
				otherArgs[0]), opts);

		ConexionSii con = new ConexionSii();

		// leo certificado y llave privada del archivo pkcs12
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(certS), passS.toCharArray());
		String alias = ks.aliases().nextElement();
		System.out.println("Usando certificado " + alias
				+ " del archivo PKCS12: " + certS);

		X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
		PrivateKey pKey = (PrivateKey) ks.getKey(alias, passS.toCharArray());

		String token = con.getToken(pKey, x509);

		System.out.println("Token: " + token);

		String enviadorS = Utilities.getRutFromCertificate(x509);
		
		RESPUESTADocument resp = con.getEstadoDTE(enviadorS, doc.getDTE()
				.getDocumento(), token);
		opts.setSavePrettyPrintIndent(2);
		opts.setSavePrettyPrint();
		resp.save(System.out, opts);
	}

}
