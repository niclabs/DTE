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


package cl.nic.dte.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cl.nic.dte.VerifyResult;
import cl.nic.dte.util.Utilities;
import cl.nic.dte.util.XMLUtil;
import cl.sii.siiDte.libroguia.LibroGuiaDocument;
import cl.sii.siiDte.libroguia.SignatureType;




public class LibroGuiaDocumentExtensionHandler {

	public static byte[] getBytes(LibroGuiaDocument dte) throws IOException {

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}

	public static VerifyResult verifyXML(LibroGuiaDocument dte) {
		return XMLUtil.verifyXML(dte);

	}

	public static VerifyResult verifySignature(LibroGuiaDocument dte) {
		// Ojo, verifica la firma usando el keyValue, pero no verifica que el
		// value corresponda al certificado incluido, ni la validez del
		// certificado.

		SignatureType sign = dte.getLibroGuia().getSignature();

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveOuter();

		// Debido a que SII define Signature en una estructura interna, estoy
		// obligado a cambiar el namespace y a hacer cosas raras/feas
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("http://www.sii.cl/SiiDte",
				"http://www.w3.org/2000/09/xmldsig#");
		opts.setLoadSubstituteNamespaces(namespaces);

		// Find Signature element
		if (sign == null || sign.isNil()) {
			return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
		}

		try {
			cl.sii.siiDte.dsig.SignatureType sign2 = cl.sii.siiDte.dsig.SignatureType.Factory
					.parse(sign.newInputStream(opts), opts);
			// No quiero afectar el dte original, saco copia
			LibroGuiaDocument dte2 = (LibroGuiaDocument) dte.copy();
			dte2.getLibroGuia().getSignature().set(sign2);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			DocumentBuilder builder = dbf.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(dte2.newInputStream());

			NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS,
					"Signature");
			if (nl.getLength() == 0) {
				return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, Utilities.verificationLabels
								.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
			}

			return (XMLUtil.verifySignature(nl.item(0), dte.getLibroGuia()
					.getEnvioLibro().getTmstFirma().getTime()));
		} catch (XmlException e) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNKNOWN")
							+ ": " + e.getMessage()));
		} catch (IOException e) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNKNOWN")
							+ ": " + e.getMessage()));
		} catch (ParserConfigurationException e) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNKNOWN")
							+ ": " + e.getMessage()));
		} catch (SAXException e) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNKNOWN")
							+ ": " + e.getMessage()));
		}

	}

	public static byte[] sign(LibroGuiaDocument dte, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, SAXException, IOException,
			ParserConfigurationException, XmlException {

		String uri = "#"+dte.getLibroGuia().getEnvioLibro().getID();
		// Segun el esquema no estoy obligado a este formato de fecha, pero
		// prefiero

		XmlDateTime now = XmlDateTime.Factory
		.newValue(Utilities.fechaHoraFormat.format(new Date()));

		dte.getLibroGuia().getEnvioLibro().xsetTmstFirma(now);
		XMLUtil.signEmbeded(dte.getLibroGuia().getDomNode(), uri, pKey, cert);

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("http://www.w3.org/2000/09/xmldsig#",
				"http://www.sii.cl/SiiDte");

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		// Debido a que SII define Signature en una estructura interna, estoy
		// obligado a cambiar el namespace
		//opts.setLoadSubstituteNamespaces(namespaces);

		dte.set(LibroGuiaDocument.Factory.parse(dte.newInputStream(), opts));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);

		return out.toByteArray();

	}

	public static X509Certificate getCertificate(LibroGuiaDocument dte) {
		return XMLUtil.getCertificate( dte.getLibroGuia().getSignature()); 
	}

}
