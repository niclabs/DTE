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

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;

import cl.nic.dte.VerifyResult;
import cl.nic.dte.util.Utilities;
import cl.nic.dte.util.XMLUtil;
import cl.sii.siiDte.EnvioDTEDocument;
import cl.sii.siiDte.FechaHoraType;

public class EnvioDTEDocumentExtensionHandler {

	public static byte[] getBytes(EnvioDTEDocument dte) throws IOException {

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}

	public static VerifyResult verifyXML(EnvioDTEDocument dte) {
		return XMLUtil.verifyXML(dte);

	}

	public static VerifyResult verifySignature(EnvioDTEDocument dte) {
		// Ojo, verifica la firma usando el keyValue, pero no verifica que el
		// value corresponda al certificado incluido, ni la validez del
		// certificado.

		cl.sii.siiDte.dsig.SignatureType sign = dte.getEnvioDTE().getSignature();

		// Find Signature element
		if (sign == null || sign.isNil()) {
			return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
		}

		return (XMLUtil.verifySignature(sign.getDomNode(), dte.getEnvioDTE()
				.getSetDTE().getCaratula().getTmstFirmaEnv().getTime()));
	}

	public static byte[] sign(EnvioDTEDocument dte, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, SAXException, IOException,
			ParserConfigurationException, XmlException {

		String uri = "#"+dte.getEnvioDTE().getSetDTE().getID();
		// Segun el esquema no estoy obligado a este formato de fecha, pero
		// prefiero

		dte.getEnvioDTE().getSetDTE().getCaratula().xsetTmstFirmaEnv(
				FechaHoraType.Factory.newValue(Utilities.fechaHoraFormat
						.format(new Date())));
		XMLUtil.signEmbededApache(dte.getEnvioDTE().getDomNode().getOwnerDocument(), uri, pKey, cert);

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);

		return out.toByteArray();

	}

	public static X509Certificate getCertificate(EnvioDTEDocument dte) {
		return XMLUtil.getCertificate(dte.getEnvioDTE().getSignature());
	}

}
