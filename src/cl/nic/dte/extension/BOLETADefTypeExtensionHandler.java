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
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.HashMap;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cl.nic.dte.TimbreException;
import cl.nic.dte.VerifyResult;
import cl.nic.dte.util.Utilities;
import cl.nic.dte.util.XMLUtil;
import cl.sii.siiDte.CAFType;
import cl.sii.siiDte.boletas.BOLETADefType;
import cl.sii.siiDte.boletas.BOLETADefType.Documento;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.Detalle;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.Encabezado.IdDoc;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.TED.DD;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.TED.FRMT;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.TED.DD.CAF;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.TED.DD.CAF.DA.DSAPK;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.TED.DD.CAF.DA.RSAPK;
import cl.sii.siiDte.boletas.BOLETADefType.Documento.TED.FRMT.Algoritmo;

public class BOLETADefTypeExtensionHandler {

	public static VerifyResult verifyXML(BOLETADefType dte) {
		return XMLUtil.verifyXML(dte);

	}

	public static VerifyResult verifySignature(BOLETADefType dte) {
		// Ojo, verifica la firma usando el keyValue, pero no verifica la
		// validez del
		// certificado.

		cl.sii.siiDte.dsig.SignatureType sign = dte.getSignature();
		// Find Signature element
		if (sign == null || sign.isNil()) {
			return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
		}

		Date date = dte.getDocumento().getTmstFirma().getTime();

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		XmlOptions opts = new XmlOptions();

		opts.setSaveOuter();
		opts.setSaveImplicitNamespaces(namespaces);

		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document dte2 = dbf.newDocumentBuilder().parse(
					dte.newInputStream(opts));
			NodeList nl = dte2.getElementsByTagNameNS(XMLSignature.XMLNS,
					"Signature");
			if (nl.getLength() == 0) {
				return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, Utilities.verificationLabels
								.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
			}

			return (XMLUtil.verifySignature(nl.item(0), date));

		} catch (SAXException e) {
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
		}
	}

	public static byte[] sign(BOLETADefType dte, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, SAXException, IOException,
			ParserConfigurationException, XmlException {

		String uri = "#"+dte.getDocumento().getID();

		XmlDateTime now = XmlDateTime.Factory
				.newValue(Utilities.fechaHoraFormat.format(new Date()));

		dte.getDocumento().xsetTmstFirma(now);

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XMLUtil.signEmbeded(dte.getDomNode(), uri, pKey, cert);
		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}

	public static VerifyResult verifyTimbre(BOLETADefType doc)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, SignatureException {
		
		DD data = doc.getDocumento().getTED().getDD();

		FRMT frmt = doc.getDocumento().getTED().getFRMT();
		CAF caf = data.getCAF();

		IdDoc iddoc = doc.getDocumento().getEncabezado().getIdDoc();

		Detalle[] det = doc.getDocumento().getDetalleArray();

		if (!data.getTD().equals(iddoc.getTipoDTE())
				|| data.getF() != iddoc.getFolio()
				|| !data.getRE().equals(
						doc.getDocumento().getEncabezado().getEmisor().getRUTEmisor())
				|| !data.getRR().equals(
						doc.getDocumento().getEncabezado().getReceptor().getRUTRecep())
				|| !data.getFE().equals(iddoc.getFchEmis())
				|| data.getMNT().longValue() != doc.getDocumento().getEncabezado()
						.getTotales().getMntTotal()
				|| !data
						.getRSR()
						.equals(
								doc
										.getDocumento().getEncabezado()
										.getReceptor()
										.getRznSocRecep()
										.substring(
												0,
												doc.getDocumento().getEncabezado()
														.getReceptor()
														.getRznSocRecep()
														.length() > 40 ? 40
														: doc
																.getDocumento().getEncabezado()
																.getReceptor()
																.getRznSocRecep()
																.length()))
				|| det == null
				|| det.length <= 0
				|| !data.getIT1().equals(
						det[0].getNmbItem().substring(
								0,
								(det[0].getNmbItem().length() > 40 ? 40
										: det[0].getNmbItem().length())))) {
			return new VerifyResult(VerifyResult.TED_CONTENTS_WRONG, false,
					Utilities.verificationLabels
							.getString("TED_WRONG_CONTENTS"));
		}

		if (data.getF() < caf.getDA().getRNG().getD()
				|| data.getF() > caf.getDA().getRNG().getH()
				|| !data.getRE().equals(caf.getDA().getRE())
				|| !data.getTD().equals(caf.getDA().getTD())) {
			return new VerifyResult(VerifyResult.TED_WRONG_CAF, false,
					Utilities.verificationLabels.getString("TED_WRONG_CAF"));

		}

		Signature sig = null;

		Algoritmo.Enum alg = frmt.getAlgoritmo();
		PublicKey pKey = null;

		if (alg.equals(Algoritmo.SHA_1_WITH_RSA)) {
			sig = Signature.getInstance("SHA1withRSA");

			RSAPK kk = caf.getDA()
					.getRSAPK();
			KeySpec keys = new RSAPublicKeySpec(new BigInteger(kk.getM()),
					new BigInteger(kk.getE()));
			pKey = KeyFactory.getInstance("RSA").generatePublic(keys);

		} else if (alg.equals(Algoritmo.SHA_1_WITH_DSA)) {
			sig = Signature.getInstance("SHA1withDSA");

			DSAPK kk = caf.getDA()
					.getDSAPK();

			KeySpec keys = new DSAPublicKeySpec(new BigInteger(kk.getY()),
					new BigInteger(kk.getP()), new BigInteger(kk.getQ()),
					new BigInteger(kk.getG()));
			pKey = KeyFactory.getInstance("DSA").generatePublic(keys);

		} else
			throw new NoSuchAlgorithmException(Utilities.exceptions
					.getString("ALG_NOT_SUPPORTED"));

		sig.initVerify(pKey);
		sig.update(XMLUtil.getCleaned(data));

		if (sig.verify(frmt.getByteArrayValue())) {
			return new VerifyResult(VerifyResult.TED_OK, true, null);
		} else {
			return new VerifyResult(VerifyResult.TED_BAD_SIGNATURE, false,
					Utilities.verificationLabels.getString("TED_BAD_SIGNATURE"));
		}
	}

	public static void timbrar(BOLETADefType dte, CAFType caf, PrivateKey pKey)
			throws TimbreException, NoSuchAlgorithmException,
			SignatureException, InvalidKeyException {

		PublicKey pubKey = null;

		try {
			pubKey = caf.getPublicKey();
		} catch (InvalidKeySpecException e1) {
			throw new InvalidKeyException(Utilities.exceptions
					.getString("TIMBRE_ERROR_BAD_KEY")
					+ ": " + e1.getMessage());
		}

		Documento doc = dte.getDocumento();
		IdDoc iddoc = doc.getEncabezado().getIdDoc();

		if (!iddoc.getTipoDTE().equals(caf.getDA().getTD()))
			throw new TimbreException(Utilities.exceptions
					.getString("CAF_NOT_CORRESPONDING"));

		long folio = iddoc.getFolio();

		if (folio < caf.getDA().getRNG().getD()
				|| folio > caf.getDA().getRNG().getH())
			throw new TimbreException(Utilities.exceptions
					.getString("TIMBRE_ERROR_BAD_FOLIO"));

		if (!Utilities.correspond(pubKey, pKey))
			throw new InvalidKeyException(Utilities.exceptions
					.getString("TIMBRE_ERROR_BAD_KEY"));

		Signature sig = null;
		Algoritmo.Enum siiAlg = null;

		String alg = pKey.getAlgorithm();

		if (alg.equals("RSA")) {
			sig = Signature.getInstance("SHA1withRSA");
			siiAlg = Algoritmo.SHA_1_WITH_RSA;
		} else if (alg.equals("DSA")) {
			sig = Signature.getInstance("SHA1withDSA");
			siiAlg = Algoritmo.SHA_1_WITH_DSA;
		} else
			throw new NoSuchAlgorithmException(Utilities.exceptions
					.getString("ALG_NOT_SUPPORTED"));

		BOLETADefType.Documento.TED ted = doc.addNewTED();
		XmlAnySimpleType version = XmlAnySimpleType.Factory.newInstance();
		version.setStringValue("1.0");
		ted.setVersion(version);
		BOLETADefType.Documento.TED.DD data = ted.addNewDD();
		data.addNewCAF().set(caf);
		data.setRE(doc.getEncabezado().getEmisor().getRUTEmisor());
		data.setTD(iddoc.getTipoDTE());
		data.setF(iddoc.getFolio());
		data.setFE(iddoc.getFchEmis());
		data.setRR(doc.getEncabezado().getReceptor().getRUTRecep());
		data.setRSR(doc.getEncabezado().getReceptor().getRznSocRecep());
		data.setMNT(BigInteger.valueOf(doc.getEncabezado().getTotales()
				.getMntTotal()));

		Detalle[] det = doc.getDetalleArray();
		if (det != null && det.length > 0)
			data.setIT1(det[0].getNmbItem().substring(
					0,
					(det[0].getNmbItem().length() > 40 ? 40 : det[0]
							.getNmbItem().length())));

		data.xsetTSTED(XmlDateTime.Factory.newValue(Utilities.fechaHoraFormat
				.format(new Date())));

		sig.initSign(pKey);

		sig.update(XMLUtil.getCleaned(data));
		BOLETADefType.Documento.TED.FRMT frmt = ted.addNewFRMT();
		frmt.setAlgoritmo(siiAlg);
		frmt.setByteArrayValue(sig.sign());
	}

	public static X509Certificate getCertificate(BOLETADefType dte) {
		return XMLUtil.getCertificate(dte.getSignature());
	}

	public static byte[] getBytes(BOLETADefType dte) throws IOException {
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}

}
