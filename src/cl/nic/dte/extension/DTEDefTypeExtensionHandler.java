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
import java.math.BigDecimal;
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
import cl.sii.siiDte.DTEDefType;
import cl.sii.siiDte.FechaHoraType;
import cl.sii.siiDte.DTEDefType.Documento;
import cl.sii.siiDte.DTEDefType.Exportaciones;
import cl.sii.siiDte.DTEDefType.Liquidacion;
import cl.sii.siiDte.DTEDefType.Documento.Detalle;
import cl.sii.siiDte.DTEDefType.Documento.Encabezado.IdDoc;
import cl.sii.siiDte.DTEDefType.Documento.TED.FRMT.Algoritmo;
import cl.sii.siiDte.dsig.SignatureType;

public class DTEDefTypeExtensionHandler {

	public static VerifyResult verifyXML(DTEDefType dte) {
		return XMLUtil.verifyXML(dte);

	}

	public static VerifyResult verifySignature(DTEDefType dte) {
		// Ojo, verifica la firma usando el keyValue, pero no verifica la
		// validez del
		// certificado.

		SignatureType sign = dte.getSignature();
		// Find Signature element
		if (sign == null || sign.isNil()) {
			return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_NOTFOUND"));
		}

		Date date = null;
		if (dte.isSetDocumento())
			date = dte.getDocumento().getTmstFirma().getTime();
		if (dte.isSetLiquidacion())
			date = dte.getLiquidacion().getTmstFirma().getTime();
		if (dte.isSetExportaciones())
			date = dte.getExportaciones().getTmstFirma().getTime();

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

	public static byte[] sign(DTEDefType dte, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, SAXException, IOException,
			ParserConfigurationException, XmlException {

		String uri = "";

		FechaHoraType now = FechaHoraType.Factory
				.newValue(Utilities.fechaHoraFormat.format(new Date()));

		if (dte.isSetDocumento()) {
			uri = dte.getDocumento().getID();
			dte.getDocumento().xsetTmstFirma(now);
		} else if (dte.isSetLiquidacion()) {
			uri = dte.getLiquidacion().getID();
			dte.getLiquidacion().xsetTmstFirma(now);
		} else if (dte.isSetExportaciones()) {
			uri = dte.getExportaciones().getID();
			dte.getExportaciones().xsetTmstFirma(now);
		}

		uri = "#" + uri;

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XMLUtil.signEmbededApache(dte.getDomNode().getOwnerDocument(), uri, pKey, cert);
		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}

	public static void timbrar(DTEDefType dte, CAFType caf, PrivateKey pKey)
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

		if (dte.isSetDocumento())
			timbrar(dte.getDocumento(), caf.getCAFforDocument(), pubKey, pKey);
		else if (dte.isSetLiquidacion())
			timbrar(dte.getLiquidacion(), caf.getCAFforLiquidacion(), pubKey,
					pKey);
		else if (dte.isSetExportaciones())
			timbrar(dte.getExportaciones(), caf.getCAFforExportacion(), pubKey,
					pKey);

	}

	public static VerifyResult verifyTimbre(DTEDefType dte)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, SignatureException {

		if (dte.isSetDocumento())
			return verifyTimbre(dte.getDocumento());
		if (dte.isSetLiquidacion())
			return verifyTimbre(dte.getLiquidacion());
		if (dte.isSetExportaciones())
			return verifyTimbre(dte.getExportaciones());
		return null;
	}

	private static VerifyResult verifyTimbre(Exportaciones doc)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, SignatureException {

		DTEDefType.Exportaciones.TED.DD data = doc.getTED().getDD();
		DTEDefType.Exportaciones.TED.FRMT frmt = doc.getTED().getFRMT();
		DTEDefType.Exportaciones.TED.DD.CAF caf = data.getCAF();

		cl.sii.siiDte.DTEDefType.Exportaciones.Encabezado.IdDoc iddoc = doc
				.getEncabezado().getIdDoc();

		cl.sii.siiDte.DTEDefType.Exportaciones.Detalle[] det = doc
				.getDetalleArray();

		if (!data.getTD().equals(iddoc.getTipoDTE())
				|| data.getF() != iddoc.getFolio()
				|| !data.getRE().equals(
						doc.getEncabezado().getEmisor().getRUTEmisor())
				|| !data.getRR().equals(
						doc.getEncabezado().getReceptor().getRUTRecep())
				|| !data.getFE().equals(iddoc.getFchEmis())
				|| data.getMNT().longValue() != doc.getEncabezado()
						.getTotales().getMntTotal().longValue()
				|| !data
						.getRSR()
						.equals(
								doc
										.getEncabezado()
										.getReceptor()
										.getRznSocRecep()
										.substring(
												0,
												doc.getEncabezado()
														.getReceptor()
														.getRznSocRecep()
														.length() > 40 ? 40
														: doc
																.getEncabezado()
																.getReceptor()
																.getRznSocRecep()
																.length()))
				|| det == null
				|| det.length > 0
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

		cl.sii.siiDte.DTEDefType.Exportaciones.TED.FRMT.Algoritmo.Enum alg = frmt
				.getAlgoritmo();
		PublicKey pKey = null;

		if (alg.equals(Algoritmo.SHA_1_WITH_RSA)) {
			sig = Signature.getInstance("SHA1withRSA");

			DTEDefType.Exportaciones.TED.DD.CAF.DA.RSAPK kk = caf.getDA()
					.getRSAPK();
			KeySpec keys = new RSAPublicKeySpec(new BigInteger(kk.getM()),
					new BigInteger(kk.getE()));
			pKey = KeyFactory.getInstance("RSA").generatePublic(keys);

		} else if (alg.equals(Algoritmo.SHA_1_WITH_DSA)) {
			sig = Signature.getInstance("SHA1withDSA");

			DTEDefType.Exportaciones.TED.DD.CAF.DA.DSAPK kk = caf.getDA()
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

	private static VerifyResult verifyTimbre(Documento doc)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, SignatureException {

		DTEDefType.Documento.TED.DD data = doc.getTED().getDD();
		DTEDefType.Documento.TED.FRMT frmt = doc.getTED().getFRMT();
		DTEDefType.Documento.TED.DD.CAF caf = data.getCAF();

		IdDoc iddoc = doc.getEncabezado().getIdDoc();

		Detalle[] det = doc.getDetalleArray();

		if (!data.getTD().equals(iddoc.getTipoDTE())
				|| data.getF() != iddoc.getFolio()
				|| !data.getRE().equals(
						doc.getEncabezado().getEmisor().getRUTEmisor())
				|| !data.getRR().equals(
						doc.getEncabezado().getReceptor().getRUTRecep())
				|| !data.getFE().equals(iddoc.getFchEmis())
				|| data.getMNT().longValue() != doc.getEncabezado()
						.getTotales().getMntTotal()
				|| !data
						.getRSR()
						.equals(
								doc
										.getEncabezado()
										.getReceptor()
										.getRznSocRecep()
										.substring(
												0,
												doc.getEncabezado()
														.getReceptor()
														.getRznSocRecep()
														.length() > 40 ? 40
														: doc
																.getEncabezado()
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

			DTEDefType.Documento.TED.DD.CAF.DA.RSAPK kk = caf.getDA()
					.getRSAPK();
			KeySpec keys = new RSAPublicKeySpec(new BigInteger(1, kk.getM()),
					new BigInteger(1, kk.getE()));
			pKey = KeyFactory.getInstance("RSA").generatePublic(keys);

		} else if (alg.equals(Algoritmo.SHA_1_WITH_DSA)) {
			sig = Signature.getInstance("SHA1withDSA");

			DTEDefType.Documento.TED.DD.CAF.DA.DSAPK kk = caf.getDA()
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

	private static VerifyResult verifyTimbre(Liquidacion doc)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, SignatureException {

		DTEDefType.Liquidacion.TED.DD data = doc.getTED().getDD();
		DTEDefType.Liquidacion.TED.FRMT frmt = doc.getTED().getFRMT();
		DTEDefType.Liquidacion.TED.DD.CAF caf = data.getCAF();

		cl.sii.siiDte.DTEDefType.Liquidacion.Encabezado.IdDoc iddoc = doc
				.getEncabezado().getIdDoc();

		cl.sii.siiDte.DTEDefType.Liquidacion.Detalle[] det = doc
				.getDetalleArray();

		if (!data.getTD().equals(iddoc.getTipoDTE())
				|| data.getF() != iddoc.getFolio()
				|| !data.getRE().equals(
						doc.getEncabezado().getEmisor().getRUTEmisor())
				|| !data.getRR().equals(
						doc.getEncabezado().getReceptor().getRUTRecep())
				|| !data.getFE().equals(iddoc.getFchEmis())
				|| data.getMNT().longValue() != doc.getEncabezado()
						.getTotales().getMntTotal()
				|| !data
						.getRSR()
						.equals(
								doc
										.getEncabezado()
										.getReceptor()
										.getRznSocRecep()
										.substring(
												0,
												doc.getEncabezado()
														.getReceptor()
														.getRznSocRecep()
														.length() > 40 ? 40
														: doc
																.getEncabezado()
																.getReceptor()
																.getRznSocRecep()
																.length()))
				|| det == null
				|| det.length > 0
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

		cl.sii.siiDte.DTEDefType.Liquidacion.TED.FRMT.Algoritmo.Enum alg = frmt
				.getAlgoritmo();
		PublicKey pKey = null;

		if (alg.equals(Algoritmo.SHA_1_WITH_RSA)) {
			sig = Signature.getInstance("SHA1withRSA");

			DTEDefType.Liquidacion.TED.DD.CAF.DA.RSAPK kk = caf.getDA()
					.getRSAPK();
			KeySpec keys = new RSAPublicKeySpec(new BigInteger(kk.getM()),
					new BigInteger(kk.getE()));
			pKey = KeyFactory.getInstance("RSA").generatePublic(keys);

		} else if (alg.equals(Algoritmo.SHA_1_WITH_DSA)) {
			sig = Signature.getInstance("SHA1withDSA");

			DTEDefType.Liquidacion.TED.DD.CAF.DA.DSAPK kk = caf.getDA()
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

	private static void timbrar(Documento doc,
			DTEDefType.Documento.TED.DD.CAF caf, PublicKey pubKey,
			PrivateKey pKey) throws TimbreException, NoSuchAlgorithmException,
			SignatureException, InvalidKeyException {

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

		DTEDefType.Documento.TED ted = doc.addNewTED();
		XmlAnySimpleType version = XmlAnySimpleType.Factory.newInstance();
		version.setStringValue("1.0");
		ted.setVersion(version);
		DTEDefType.Documento.TED.DD data = ted.addNewDD();
		data.addNewCAF().set(caf);
		data.setRE(doc.getEncabezado().getEmisor().getRUTEmisor());
		data.setTD(iddoc.getTipoDTE());
		data.setF(iddoc.getFolio());
		data.setFE(iddoc.getFchEmis());
		data.setRR(doc.getEncabezado().getReceptor().getRUTRecep());
		data.setRSR(doc.getEncabezado().getReceptor().getRznSocRecep()
				.substring(
						0,
						(doc.getEncabezado().getReceptor().getRznSocRecep()
								.length() > 40 ? 40 : doc.getEncabezado()
								.getReceptor().getRznSocRecep().length())));
		data.setMNT(BigInteger.valueOf(doc.getEncabezado().getTotales()
				.getMntTotal()));

		Detalle[] det = doc.getDetalleArray();
		if (det != null && det.length > 0)
			data.setIT1(det[0].getNmbItem().substring(
					0,
					(det[0].getNmbItem().length() > 40 ? 40 : det[0]
							.getNmbItem().length())));

		data.xsetTSTED(FechaHoraType.Factory.newValue(Utilities.fechaHoraFormat
				.format(new Date())));

		sig.initSign(pKey);

		sig.update(XMLUtil.getCleaned(data));	
		
		// sig.update(XMLUtil.getBytesXML(data));
		
		DTEDefType.Documento.TED.FRMT frmt = ted.addNewFRMT();
		frmt.setAlgoritmo(siiAlg);
		frmt.setByteArrayValue(sig.sign());
		
		
	}

	private static void timbrar(Exportaciones doc,
			DTEDefType.Exportaciones.TED.DD.CAF caf, PublicKey pubKey,
			PrivateKey pKey) throws TimbreException, NoSuchAlgorithmException,
			SignatureException, InvalidKeyException {

		cl.sii.siiDte.DTEDefType.Exportaciones.Encabezado.IdDoc iddoc = doc
				.getEncabezado().getIdDoc();

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
		DTEDefType.Exportaciones.TED.FRMT.Algoritmo.Enum siiAlg = null;

		String alg = pKey.getAlgorithm();

		if (alg.equals("RSA")) {
			sig = Signature.getInstance("SHA1withRSA");
			siiAlg = DTEDefType.Exportaciones.TED.FRMT.Algoritmo.SHA_1_WITH_RSA;
		} else if (alg.equals("DSA")) {
			sig = Signature.getInstance("SHA1withDSA");
			siiAlg = DTEDefType.Exportaciones.TED.FRMT.Algoritmo.SHA_1_WITH_DSA;
		} else
			throw new NoSuchAlgorithmException(Utilities.exceptions
					.getString("ALG_NOT_SUPPORTED"));

		DTEDefType.Exportaciones.TED ted = doc.addNewTED();
		XmlAnySimpleType version = XmlAnySimpleType.Factory.newInstance();
		version.setStringValue("1.0");
		ted.setVersion(version);
		DTEDefType.Exportaciones.TED.DD data = ted.addNewDD();
		data.addNewCAF().set(caf);
		data.setRE(doc.getEncabezado().getEmisor().getRUTEmisor());
		data.setTD(iddoc.getTipoDTE());
		data.setF(iddoc.getFolio());
		data.setFE(iddoc.getFchEmis());
		data.setRR(doc.getEncabezado().getReceptor().getRUTRecep());
		data.setRSR(doc.getEncabezado().getReceptor().getRznSocRecep());
		data.setMNT(new BigDecimal(doc.getEncabezado().getTotales()
				.getMntTotal().unscaledValue()));
		cl.sii.siiDte.DTEDefType.Exportaciones.Detalle[] det = doc
				.getDetalleArray();
		if (det != null && det.length > 0)
			data.setIT1(det[0].getNmbItem().substring(
					0,
					(det[0].getNmbItem().length() > 40 ? 40 : det[0]
							.getNmbItem().length())));

		data.xsetTSTED(FechaHoraType.Factory.newValue(Utilities.fechaHoraFormat
				.format(new Date())));

		sig.initSign(pKey);
		sig.update(XMLUtil.getCleaned(data));
		DTEDefType.Exportaciones.TED.FRMT frmt = ted.addNewFRMT();
		frmt.setAlgoritmo(siiAlg);
		frmt.setByteArrayValue(sig.sign());
	}

	private static void timbrar(Liquidacion doc,
			DTEDefType.Liquidacion.TED.DD.CAF caf, PublicKey pubKey,
			PrivateKey pKey) throws TimbreException, NoSuchAlgorithmException,
			SignatureException, InvalidKeyException {

		cl.sii.siiDte.DTEDefType.Liquidacion.Encabezado.IdDoc iddoc = doc
				.getEncabezado().getIdDoc();

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
		DTEDefType.Liquidacion.TED.FRMT.Algoritmo.Enum siiAlg = null;

		String alg = pKey.getAlgorithm();

		if (alg.equals("RSA")) {
			sig = Signature.getInstance("SHA1withRSA");
			siiAlg = DTEDefType.Liquidacion.TED.FRMT.Algoritmo.SHA_1_WITH_RSA;
		} else if (alg.equals("DSA")) {
			sig = Signature.getInstance("SHA1withDSA");
			siiAlg = DTEDefType.Liquidacion.TED.FRMT.Algoritmo.SHA_1_WITH_DSA;
		} else
			throw new NoSuchAlgorithmException(Utilities.exceptions
					.getString("ALG_NOT_SUPPORTED"));

		DTEDefType.Liquidacion.TED ted = doc.addNewTED();
		XmlAnySimpleType version = XmlAnySimpleType.Factory.newInstance();
		version.setStringValue("1.0");
		ted.setVersion(version);
		DTEDefType.Liquidacion.TED.DD data = ted.addNewDD();
		data.addNewCAF().set(caf);
		data.setRE(doc.getEncabezado().getEmisor().getRUTEmisor());
		data.setTD(iddoc.getTipoDTE());
		data.setF(iddoc.getFolio());
		data.setFE(iddoc.getFchEmis());
		data.setRR(doc.getEncabezado().getReceptor().getRUTRecep());
		data.setRSR(doc.getEncabezado().getReceptor().getRznSocRecep());
		data.setMNT(BigInteger.valueOf(doc.getEncabezado().getTotales()
				.getMntTotal()));
		cl.sii.siiDte.DTEDefType.Liquidacion.Detalle[] det = doc
				.getDetalleArray();
		if (det != null && det.length > 0)
			data.setIT1(det[0].getNmbItem().substring(
					0,
					(det[0].getNmbItem().length() > 40 ? 40 : det[0]
							.getNmbItem().length())));

		data.xsetTSTED(FechaHoraType.Factory.newValue(Utilities.fechaHoraFormat
				.format(new Date())));

		sig.initSign(pKey);
		sig.update(XMLUtil.getCleaned(data));
		DTEDefType.Liquidacion.TED.FRMT frmt = ted.addNewFRMT();
		frmt.setAlgoritmo(siiAlg);
		frmt.setByteArrayValue(sig.sign());
	}

	public static X509Certificate getCertificate(DTEDefType dte) {
		return XMLUtil.getCertificate(dte.getSignature());
	}

	public static byte[] getBytes(DTEDefType dte) throws IOException {
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
