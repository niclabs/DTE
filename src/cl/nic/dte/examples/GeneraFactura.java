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

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.apache.xmlbeans.XmlOptions;

import cl.sii.siiDte.AUTORIZACIONDocument;
import cl.sii.siiDte.AutorizacionType;
import cl.sii.siiDte.DTEDocument;

public class GeneraFactura {

	private static void printUsage() {
		System.err
				.println("Utilice: java cl.nic.dte.examples.GeneraFactura "
						+ "-a <caf.xml> -p <plantilla.xml> -c <certDigital.p12> -s <password> -o <resultado.xml>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option certOpt = parser.addStringOption('c', "cert");
		CmdLineParser.Option passOpt = parser.addStringOption('s', "password");
		CmdLineParser.Option resultOpt = parser.addStringOption('o', "output");
		CmdLineParser.Option cafOpt = parser.addStringOption('a',
				"autorizacion");
		CmdLineParser.Option plantillaOpt = parser.addStringOption('p',
				"plantilla");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}

		String certS = (String) parser.getOptionValue(certOpt);
		String passS = (String) parser.getOptionValue(passOpt);
		String resultS = (String) parser.getOptionValue(resultOpt);
		String cafS = (String) parser.getOptionValue(cafOpt);
		String plantillaS = (String) parser.getOptionValue(plantillaOpt);

		if (certS == null || passS == null || resultS == null || cafS == null
				|| plantillaS == null) {
			printUsage();
			System.exit(2);
		}

		// Leo Autorizacion
		// Debo meter el namespace porque SII no lo genera
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		AutorizacionType caf = AUTORIZACIONDocument.Factory.parse(
				new File(cafS), opts).getAUTORIZACION();

		// Construyo base a partir del template
		DTEDocument doc = DTEDocument.Factory.parse(new File(plantillaS), opts);

		// leo certificado y llave privada del archivo pkcs12
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(certS), passS.toCharArray());
		String alias = ks.aliases().nextElement();
		System.out.println("Usando certificado " + alias
				+ " del archivo PKCS12: " + certS);

		X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
		PrivateKey key = (PrivateKey) ks.getKey(alias, passS.toCharArray());

		// Timbro
		doc.getDTE().timbrar(caf.getCAF(), caf.getPrivateKey(null));
		
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);

		// Releo formateado
		doc = DTEDocument.Factory.parse(doc.newInputStream(opts), opts);
		
		// firmo
		doc.getDTE().sign(key, cert);
		
		//(new FileOutputStream(resultS)).write(doc.getDTE().sign(key, cert));
		

		// Guardo
		opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);
		doc.save(new File(resultS), opts);
	}

}
