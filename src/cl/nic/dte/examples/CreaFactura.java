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

public class CreaFactura {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int folio;
		DTEDocument doc;
		AutorizacionType caf;
		X509Certificate cert;
		PrivateKey key;

		if (args.length != 5) {
			System.err
					.println("Utilice: java cl.nic.dte.examples.CreaFactura <Nr folio> "
							+ "<caf.xml> <template.xml> <certDigital.p12> <password>");
			System.exit(-1);
		}

		// Leo el folio
		folio = Integer.valueOf(args[0]);

		// Leo Autorizacion
		// Debo meter el namespace porque SII no lo genera
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		caf = AUTORIZACIONDocument.Factory.parse(new File(args[1]), opts)
				.getAUTORIZACION();

		// Construyo base a partir del template
		doc = DTEDocument.Factory.parse(new File(args[2]));

		// leo certificado y llave privada del archivo pkcs12
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(args[3]), args[4].toCharArray());
		String alias = ks.aliases().nextElement();
		System.out.println("Usando certificado " + alias
				+ " del archivo PKCS12: " + args[3]);

		cert = (X509Certificate) ks.getCertificate(alias);
		key = (PrivateKey) ks.getKey(alias, args[4].toCharArray());

		// Agrego al doc datos inventados para pruebas

		// IdDoc
		IdDoc iddoc = doc.getDTE().getDocumento().getEncabezado().addNewIdDoc();
		iddoc.setFolio(folio);
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
		recp.setRznSocRecep("Servicio de Impuestos Internos");
		recp.setGiroRecep("GOBIERNO CENTRAL Y ADMINISTRACION PUB.");
		recp.setContacto("Director Impuestos Internos");
		recp.setDirRecep("Teatinos 120");
		recp.setCmnaRecep("Santiago");
		recp.setCiudadRecep("Santiago");

		// Totales
		Totales tot = doc.getDTE().getDocumento().getEncabezado()
				.addNewTotales();
		tot.setMntNeto(33900);
		tot.setTasaIVA(BigDecimal.valueOf(19));
		tot.setIVA(6441);
		tot.setMntTotal(40341);

		// Agrego detalles
		Detalle[] det = new Detalle[2];
		det[0] = Detalle.Factory.newInstance();
		det[0].setNroLinDet(1);
		det[0].setNmbItem("dominio sii");
		det[0].setQtyItem(BigDecimal.valueOf(1));
		det[0].setPrcItem(BigDecimal.valueOf(16949.584));
		det[0].setMontoItem(16950);

		det[1] = Detalle.Factory.newInstance();
		det[1].setNroLinDet(1);
		det[1].setNmbItem("dominio impuestosinternos");
		det[1].setQtyItem(BigDecimal.valueOf(1));
		det[1].setPrcItem(BigDecimal.valueOf(16949.584));
		det[1].setMontoItem(16950);

		doc.getDTE().getDocumento().setDetalleArray(det);

		// Timbro

		doc.getDTE().timbrar(caf.getCAF(), caf.getPrivateKey(null));

		// antes de firmar le doy formato a los datos
		opts = new XmlOptions();
		opts.setSaveImplicitNamespaces(namespaces);
		opts.setLoadSubstituteNamespaces(namespaces);
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);

		// releo el doc para que se reflejen los cambios de formato
		doc = DTEDocument.Factory.parse(doc.newInputStream(opts), opts);

		// firmo
		doc.getDTE().sign(key, cert);

		// Guardo
		opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);
		doc.save(System.out, opts);
	}

}
