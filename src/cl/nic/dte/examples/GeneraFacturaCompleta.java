package cl.nic.dte.examples;
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

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.xmlbeans.XmlOptions;

import cl.nic.dte.util.Utilities;
import cl.sii.siiDte.AUTORIZACIONDocument;
import cl.sii.siiDte.AutorizacionType;
import cl.sii.siiDte.DTEDocument;
import cl.sii.siiDte.FechaType;
import cl.sii.siiDte.MedioPagoType;
import cl.sii.siiDte.DTEDefType.Documento.Detalle;
import cl.sii.siiDte.DTEDefType.Documento.Encabezado.IdDoc;
import cl.sii.siiDte.DTEDefType.Documento.Encabezado.Receptor;
import cl.sii.siiDte.DTEDefType.Documento.Encabezado.Totales;
import jargs.gnu.CmdLineParser;

public class GeneraFacturaCompleta {

	private static void printUsage() {
		System.err
				.println("Utilice: java cl.nic.dte.examples.GeneraFacturaCompleta "
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
		
		System.out.println("******** Genera Documento ********");

		// Obtengo los datos del emisor desde un template
		DTEDocument doc = null;
		doc = DTEDocument.Factory.parse(new File(
				plantillaS));

		// IdDoc

		IdDoc iddoc = doc.getDTE().getDocumento().getEncabezado().addNewIdDoc();
		iddoc.setFolio(150);
		doc.getDTE().getDocumento().setID("N" + iddoc.getFolio());
		iddoc.setTipoDTE(BigInteger.valueOf(33));

		iddoc.xsetFchEmis(FechaType.Factory.newValue(Utilities.fechaFormat
				.format(new Date())));

		iddoc.setIndServicio(BigInteger.valueOf(3));
		iddoc.setFmaPago(BigInteger.valueOf(1));

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 45);
		iddoc.xsetFchCancel(FechaType.Factory.newValue(Utilities.fechaFormat
				.format(new Date())));

		iddoc.setMedioPago(MedioPagoType.Enum.forString("LT"));
		iddoc.setFmaPago(BigInteger.valueOf(2));
		
		// Receptor
		Receptor recp = doc.getDTE().getDocumento().getEncabezado()
				.addNewReceptor();
		recp.setRUTRecep("60803000-K");
		recp.setRznSocRecep("Warner Brothers");
		recp.setGiroRecep("Divertimento");
		recp.setContacto("Pinky y Cerebro");
		recp.setDirRecep("Embajada USA");
		recp.setCmnaRecep("Vitacura");
		recp.setCiudadRecep("Santiago");

		// Totales
		Totales tot = doc.getDTE().getDocumento().getEncabezado()
				.addNewTotales();
		tot.setMntNeto(1000000);
		tot.setTasaIVA(BigDecimal.valueOf(19));
		tot.setIVA(190000);
		tot.setMntTotal(1190000);

		// Agrego detalles
		Detalle[] det = new Detalle[2];
		det[0] = Detalle.Factory.newInstance();
		det[0].setNroLinDet(1);
		det[0].setNmbItem("Trampa correcaminos");
		det[0].setQtyItem(BigDecimal.valueOf(2));
		det[0].setPrcItem(BigDecimal.valueOf(350000));
		det[0].setMontoItem(700000);

		det[1] = Detalle.Factory.newInstance();
		det[1].setNroLinDet(2);
		det[1].setNmbItem("Insumos varios");
		det[1].setMontoItem(300000);

		doc.getDTE().getDocumento().setDetalleArray(det);

		// Leo la autorizacion y timbro
		// Debo meter el namespace porque SII no lo genera
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		AutorizacionType auth = AUTORIZACIONDocument.Factory.parse(
				new File(cafS), opts)
				.getAUTORIZACION();

		doc.getDTE().timbrar(auth.getCAF(), auth.getPrivateKey(null));

		// leo certificado y llave privada del archivo pkcs12
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(certS), passS
				.toCharArray());
		String alias = ks.aliases().nextElement();
		System.out.println("Usando certificado " + alias
				+ " del archivo PKCS12: " + certS);

		X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
		PrivateKey pKey = (PrivateKey) ks.getKey(alias, passS
				.toCharArray());

		// Le doy un formato bonito
		opts = new XmlOptions();
		opts.setSaveImplicitNamespaces(namespaces);
		opts.setLoadSubstituteNamespaces(namespaces);
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);

		doc = DTEDocument.Factory.parse(doc.newInputStream(opts), opts);

		// firmo
		doc.getDTE().sign(pKey, x509);

		opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);
		doc.save(new File(resultS), opts);
		
	}

}
