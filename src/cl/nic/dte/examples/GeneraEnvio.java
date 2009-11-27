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
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;

import cl.nic.dte.util.Utilities;
import cl.sii.siiDte.DTEDefType;
import cl.sii.siiDte.DTEDocument;
import cl.sii.siiDte.EnvioDTEDocument;
import cl.sii.siiDte.EnvioDTEDocument.EnvioDTE.SetDTE.Caratula.SubTotDTE;

/**
 * Esta clase se encarga de generar un EnvioDTE segun las exigencias del SII. El
 * RUT del enviador lo deduce desde el certificado digital. El subtotal de DTE
 * se calcula a partir de los DTE de entrada.
 * 
 */
public class GeneraEnvio {

	private static void printUsage() {
		System.err
				.println("Utilice: java cl.nic.dte.examples.GeneraEnvio "
						+ "-r <RUT receptor> --id <id de envio> -p <plantilla.xml> -c <certDigital.p12> "
						+ "-s <password> -o <resultado.xml> [documentos.xml]*");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option certOpt = parser.addStringOption('c', "cert");
		CmdLineParser.Option passOpt = parser.addStringOption('s', "password");
		CmdLineParser.Option resultOpt = parser.addStringOption('o', "output");
		CmdLineParser.Option recepOpt = parser.addStringOption('r', "receptor");
		CmdLineParser.Option idOpt = parser.addStringOption("id");
		CmdLineParser.Option plantillaOpt = parser.addStringOption('p',
				"plantilla");
		// CmdLineParser.Option tipoOpt = parser.addIntegerOption('t', "tipo");
		// CmdLineParser.Option enviadorOpt = parser.addStringOption('e',
		// "enviador");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}

		String certS = (String) parser.getOptionValue(certOpt);
		String passS = (String) parser.getOptionValue(passOpt);
		String resultS = (String) parser.getOptionValue(resultOpt);
		String recepS = (String) parser.getOptionValue(recepOpt);
		// String enviadorS = (String) parser.getOptionValue(enviadorOpt);
		String plantillaS = (String) parser.getOptionValue(plantillaOpt);
		String idS = (String) parser.getOptionValue(idOpt);
		// Integer tipo = (Integer) parser.getOptionValue(tipoOpt);

		if (certS == null || passS == null || resultS == null || recepS == null
				|| plantillaS == null || idS == null) {
			printUsage();
			System.exit(2);
		}

		String[] otherArgs = parser.getRemainingArgs();

		if (otherArgs.length < 1) {
			printUsage();
			System.exit(2);
		}

		// Construyo Envio
		EnvioDTEDocument envio = EnvioDTEDocument.Factory
				.parse(new FileInputStream(plantillaS));

		// Debo agregar el schema location (Sino SII rechaza)
		XmlCursor cursor = envio.newCursor();
		if (cursor.toFirstChild()) {
			cursor.setAttributeText(new QName(
					"http://www.w3.org/2001/XMLSchema-instance",
					"schemaLocation"),
					"http://www.sii.cl/SiiDte EnvioDTE_v10.xsd");
		}

		// leo certificado y llave privada del archivo pkcs12
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(certS), passS.toCharArray());
		String alias = ks.aliases().nextElement();
		System.out.println("Usando certificado " + alias
				+ " del archivo PKCS12: " + certS);

		X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
		String enviadorS = Utilities.getRutFromCertificate(x509);
		PrivateKey pKey = (PrivateKey) ks.getKey(alias, passS.toCharArray());

		// Asigno un ID
		envio.getEnvioDTE().getSetDTE().setID(idS);

		cl.sii.siiDte.EnvioDTEDocument.EnvioDTE.SetDTE.Caratula car = envio
				.getEnvioDTE().getSetDTE().getCaratula();

		car.setRutReceptor(recepS);
		car.setRutEnvia(enviadorS);

		// documentos a enviar
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		DTEDefType[] dtes = new DTEDefType[otherArgs.length];
		HashMap<Integer, Integer> hashTot = new HashMap<Integer, Integer>();
		for (int i = 0; i < otherArgs.length; i++) {

			dtes[i] = DTEDocument.Factory.parse(
					new FileInputStream(otherArgs[i]), opts).getDTE();
			// armar hash para totalizar por tipoDTE
			if (hashTot.get(dtes[i].getDocumento().getEncabezado().getIdDoc()
					.getTipoDTE().intValue()) != null) {
				hashTot.put(dtes[i].getDocumento().getEncabezado().getIdDoc()
						.getTipoDTE().intValue(), hashTot.get(dtes[i]
						.getDocumento().getEncabezado().getIdDoc().getTipoDTE()
						.intValue()) + 1);
			} else {
				hashTot.put(dtes[i].getDocumento().getEncabezado().getIdDoc()
						.getTipoDTE().intValue(), 1);
			}

			// if
			// (!dtes[i].getDocumento().getEncabezado().getIdDoc().getTipoDTE()
			// .equals(BigInteger.valueOf(tipo))) {
			// System.err.println("Documento folio: "
			// + dtes[i].getDocumento().getEncabezado().getIdDoc()
			// .getFolio() + " no corresponde al tipo");
			// System.exit(3);
			// }
		}

		SubTotDTE[] subtDtes = new SubTotDTE[hashTot.size()];
		int i = 0;
		for (Integer tipo : hashTot.keySet()) {
			SubTotDTE subt = SubTotDTE.Factory.newInstance();
			subt.setTpoDTE(new BigInteger(tipo.toString()));
			subt.setNroDTE(new BigInteger(hashTot.get(tipo).toString()));
			subtDtes[i] = subt;
			i++;
		}

		// cl.sii.siiDte.EnvioDTEDocument.EnvioDTE.SetDTE.Caratula.SubTotDTE
		// subt =
		// cl.sii.siiDte.EnvioDTEDocument.EnvioDTE.SetDTE.Caratula.SubTotDTE.Factory
		// .newInstance();
		// subt.setTpoDTE(BigInteger.valueOf(tipo));
		// subt.setNroDTE(BigInteger.valueOf(otherArgs.length));
		car.setSubTotDTEArray(subtDtes);

		// Le doy un formato bonito (debo hacerlo antes de firmar para no
		// afectar los DTE internos)
		opts = new XmlOptions();
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);
		envio = EnvioDTEDocument.Factory.parse(envio.newInputStream(opts));

		envio.getEnvioDTE().getSetDTE().setDTEArray(dtes);

		// firmo
		envio.sign(pKey, x509);

		opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		envio.save(new File(resultS), opts);
	}

}
