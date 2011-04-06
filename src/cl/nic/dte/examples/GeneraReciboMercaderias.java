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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;

import cl.nic.dte.VerifyResult;
import cl.sii.siiDte.EnvioRecibosDocument;
import cl.sii.siiDte.ReciboDefType;
import cl.sii.siiDte.ReciboDocument;
import cl.sii.siiDte.EnvioRecibosDocument.EnvioRecibos;
import cl.sii.siiDte.EnvioRecibosDocument.EnvioRecibos.SetRecibos;
import cl.sii.siiDte.ReciboDefType.DocumentoRecibo;

public class GeneraReciboMercaderias {

	
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err
					.println("Utilice: java cl.nic.dte.examples.GeneraReciboMercaderias "
							+ "<certDigital.p12> <password>");
			System.exit(-1);
		}
		
		
		ReciboDocument recibo = ReciboDocument.Factory.newInstance();

		
		ReciboDefType rec = recibo.addNewRecibo();
		
		DocumentoRecibo dr = rec.addNewDocumentoRecibo();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		dr.setFchEmis(cal);
		dr.setDeclaracion("El acuse de recibo que se declara en este acto, de acuerdo a lo dispuesto en la letra b) del Art. 4, y la letra c) del Art. 5 de la Ley 19.983, acredita que la entrega de mercaderias o servicio(s) prestado(s) ha(n) sido recibido(s).");
		dr.setFolio(12345);

		dr.setID("RM-33-12345-60910000-1");
		dr.setMntTotal(10000000);
		dr.setRecinto("El Tanguito");
		dr.setRUTEmisor("60910000-1");
		dr.setRUTRecep("55555555-5");
		
		
		// leo certificado y llave privada del archivo pkcs12
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(args[0]), args[1].toCharArray());
		String alias = ks.aliases().nextElement();
		System.out.println("Usando certificado " + alias
				+ " del archivo PKCS12: " + args[0]);

		X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
		PrivateKey key = (PrivateKey) ks.getKey(alias, args[1].toCharArray());
		
		dr.setRutFirma("12345678-9");
		dr.setTipoDoc(new BigInteger("33"));

		//rec.setDocumentoRecibo(dr);
		rec.setVersion(new BigDecimal("1.0"));

		
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setSaveImplicitNamespaces(namespaces);
		opts.setLoadSubstituteNamespaces(namespaces);
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);

		try {
			recibo = ReciboDocument.Factory.parse(recibo.newInputStream(opts), opts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// firma del recibo
		recibo.getRecibo().sign(key, cert);

		EnvioRecibosDocument erd = EnvioRecibosDocument.Factory
				.newInstance();
		EnvioRecibos er = EnvioRecibos.Factory.newInstance();
		er.setVersion(new BigDecimal("1.0"));

		cl.sii.siiDte.EnvioRecibosDocument.EnvioRecibos.SetRecibos.Caratula caratula = cl.sii.siiDte.EnvioRecibosDocument.EnvioRecibos.SetRecibos.Caratula.Factory
				.newInstance();
		caratula.setFonoContacto("9400000");
		caratula.setMailContacto("info@niclabs.cl");
		caratula.setNmbContacto("Contacto de Prueba");
		caratula.setRutRecibe("60910000-1");
		caratula.setRutResponde("55555555-5");
		caratula.setVersion(new BigDecimal("1.0"));

		SetRecibos sr = SetRecibos.Factory.newInstance();
		sr.setCaratula(caratula);
		sr.setID("SRM-33-1234-60910000-1");

		ReciboDefType[] recArray = new ReciboDefType[1];
		recArray[0] = recibo.getRecibo();//rec;
		sr.setReciboArray(recArray);

		er.setSetRecibos(sr);
		erd.setEnvioRecibos(er);

		XmlCursor cursor = erd.newCursor();
		if (cursor.toFirstChild()) {
			cursor.setAttributeText(new QName(
					"http://www.w3.org/2001/XMLSchema-instance",
					"schemaLocation"),
					"http://www.sii.cl/SiiDte EnvioRecibos_v10.xsd");
		}

		opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);
		opts.setUseDefaultNamespace();
		opts.setSaveSuggestedPrefixes(namespaces);
		
		
		try {
			erd = EnvioRecibosDocument.Factory.parse(erd
					.newInputStream(opts));
		} catch (Exception e) {
			e.printStackTrace();
		}

		erd.sign(key, cert);

		// validar schema
		
		VerifyResult resl = erd.verifyXML();
		if (!resl.isOk()) {
			System.out.println("Documento: Estructura XML Incorrecta: "
					+ resl.getMessage());
		} else {
			System.out.println("Documento: Estructura XML OK");
		}
		
		
		resl = erd.verifySignature();
		if (!resl.isOk()) {
			System.out.println("Documento: Firma Incorrecta: "
					+ resl.getMessage());
		} else {
			System.out.println("Documento: Firma OK");
		}
		// almacenar
		opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			erd.save(out, opts);
			out.flush();
			System.out.println("Recibo Mercaderias generado: "
					+ new String(out.toByteArray(), "ISO-8859-1"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
