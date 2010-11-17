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

package cl.nic.dte.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.transform.TransformerException;

import org.apache.commons.ssl.Base64;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptionCharEscapeMap;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import cl.nic.dte.VerifyResult;
import cl.sii.siiDte.AUTORIZACIONDocument;
import cl.sii.siiDte.CAFType;

/**
 * Clase con varios m&eacute;todos de apoyo al manejo del XML.
 * 
 * @author Tom&aacute;s Barros <tbarros@nic.cl>
 * 
 */

public class XMLUtil {

	/**
	 * Verifica la estructura XML de un nodo, es decir, si cumple lo exigido por
	 * su XML Schema.
	 * 
	 * @param xml
	 *            El nodo a verificar.
	 * @return El resultado de la verificaci&oacute;n.
	 * @see cl.nic.dte.VerifyResult
	 */
	public static VerifyResult verifyXML(XmlObject xml) {

		XmlOptions validateOptions = new XmlOptions();
		ArrayList<XmlError> errorList = new ArrayList<XmlError>();
		validateOptions.setErrorListener(errorList);

		if (!xml.validate(validateOptions)) {
			String message = "";
			for (XmlError error : errorList) {
				message = Utilities.verificationLabels.getString(
						"XML_BAD_STRUCTURE").replaceAll("%1",
						error.getMessage());
			}
			return (new VerifyResult(VerifyResult.XML_STRUCTURE_WRONG, false,
					message));

		}
		return (new VerifyResult(VerifyResult.XML_STRUCTURE_OK, true, null));

	}

	/**
	 * Firma digitalmente usando la forma "enveloped signature" seg&uacute;n el
	 * est&aacute;ndar de la W3C (<a
	 * href="http://www.w3.org/TR/xmldsig-core/">http://www.w3.org/TR/xmldsig-core/</a>).
	 * <p>
	 * 
	 * Este m&eacute;todo adem&aacute;s incorpora la informaci&oacute;n del
	 * certificado a la secci&oacute;n &lt;KeyInfo&gt; opcional del
	 * est&aacute;ndar, seg&uacute;n lo exige SII.
	 * <p>
	 * 
	 * @param doc
	 *            El documento a firmar
	 * @param uri
	 *            La referencia dentro del documento que debe ser firmada
	 * @param pKey
	 *            La llave privada para firmar
	 * @param cert
	 *            El certificado digital correspondiente a la llave privada
	 * @throws NoSuchAlgorithmException
	 *             Si el algoritmo de firma de la llave no est&aacute; soportado
	 *             (Actualmente soportado RSA+SHA1, DSA+SHA1 y HMAC+SHA1).
	 * @throws InvalidAlgorithmParameterException
	 *             Si los algoritmos de canonizaci&oacute;n (parte del
	 *             est&aacute;ndar XML Signature) no son soportados (actaulmente
	 *             se usa el por defecto)
	 * @throws KeyException
	 *             Si hay problemas al incluir la llave p&uacute;blica en el
	 *             &lt;KeyValue&gt;.
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 * 
	 * @see javax.xml.crypto.dsig.XMLSignature#sign(javax.xml.crypto.dsig.XMLSignContext)
	 */
	public static void signEmbeded(Node doc, String uri, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException {

		// Create a DOM XMLSignatureFactory that will be used to generate the
		// enveloped signature
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// Create a Reference to the enveloped document (in this case we are
		// signing the whole document, so a URI of "" signifies that) and
		// also specify the SHA1 digest algorithm and the ENVELOPED Transform.

		Reference ref = fac.newReference(uri, fac.newDigestMethod(
				DigestMethod.SHA1, null), Collections.singletonList(fac
				.newTransform(Transform.ENVELOPED,
						(TransformParameterSpec) null)), null, null);

		// Create the SignedInfo
		String method = SignatureMethod.RSA_SHA1; // default by SII

		if ("DSA".equals(cert.getPublicKey().getAlgorithm()))
			method = SignatureMethod.DSA_SHA1;
		else if ("HMAC".equals(cert.getPublicKey().getAlgorithm()))
			method = SignatureMethod.HMAC_SHA1;

		SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(
				CanonicalizationMethod.INCLUSIVE, // Default canonical and
				// default by SII
				(C14NMethodParameterSpec) null), fac.newSignatureMethod(method,
				null), Collections.singletonList(ref));

		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kv = kif.newKeyValue(cert.getPublicKey());

		// Create a KeyInfo and add the KeyValue to it
		List<XMLStructure> kidata = new ArrayList<XMLStructure>();
		kidata.add(kv);
		kidata.add(kif.newX509Data(Collections.singletonList(cert)));
		KeyInfo ki = kif.newKeyInfo(kidata);

		// Create a DOMSignContext and specify the PrivateKey and
		// location of the resulting XMLSignature's parent element
		DOMSignContext dsc = new DOMSignContext(pKey, doc);

		// Create the XMLSignature (but don't sign it yet)
		XMLSignature signature = fac.newXMLSignature(si, ki);

		// Marshal, generate (and sign) the enveloped signature
		signature.sign(dsc);

	}

	/**
	 * @see #getCertificate(XMLSignature)
	 */
	public static X509Certificate getCertificate(
			cl.sii.siiDte.dsig.SignatureType xml) {
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// Unmarshal the signature
		XMLSignature signature;
		try {
			signature = fac.unmarshalXMLSignature(new DOMStructure(xml
					.getDomNode()));
		} catch (MarshalException e) {
			return null;
		}
		return (getCertificate(signature));
	}
	/**
	 * @see #getCertificate(XMLSignature)
	 */
	public static X509Certificate getCertificate(
			cl.sii.siiDte.libroguia.SignatureType xml) {
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// Unmarshal the signature
		XMLSignature signature;
		try {
			signature = fac.unmarshalXMLSignature(new DOMStructure(xml
					.getDomNode()));
		} catch (MarshalException e) {
			return null;
		}
		return (getCertificate(signature));
	}

	/**
	 * @see #getCertificate(XMLSignature)
	 */
	public static X509Certificate getCertificate(
			cl.sii.siiDte.libroboletas.SignatureType xml) {
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// Unmarshal the signature
		XMLSignature signature;
		try {
			signature = fac.unmarshalXMLSignature(new DOMStructure(xml
					.getDomNode()));
		} catch (MarshalException e) {
			return null;
		}
		return (getCertificate(signature));
	}

	/**
	 * Obtiene el certificado digital contenido en un nodo XML Sinature (<a
	 * href="http://www.w3.org/TR/xmldsig-core/">http://www.w3.org/TR/xmldsig-core/</a>)
	 * 
	 * @param signature
	 *            el nodo con el tag &lt;Signature&gt;.
	 * @return El certificado digital contenido en el &lt;KeyInfo&gt; o
	 *         <code>null</code> en caso que el &lt;Signature&gt; no contenga
	 *         tal informaci&oacute;n.
	 */
	@SuppressWarnings("unchecked")
	public static X509Certificate getCertificate(XMLSignature signature) {

		String alg = signature.getSignedInfo().getSignatureMethod()
				.getAlgorithm();
		KeyInfo kinf = signature.getKeyInfo();

		// Check for keyinfo
		if (kinf == null) {
			return null;
		}

		PublicKey pKey = null;
		List<X509Certificate> x509 = new ArrayList<X509Certificate>();

		// I look for the public key and the certificates
		for (XMLStructure xst : (List<XMLStructure>) kinf.getContent()) {
			if (xst instanceof KeyValue) {
				PublicKey pk;
				try {
					pk = ((KeyValue) xst).getPublicKey();
					if (algEquals(alg, pk.getAlgorithm()))
						pKey = pk;
				} catch (KeyException e) {
					// nothing
				}
			}
			if (xst instanceof X509Data) {
				for (Object cont : ((X509Data) xst).getContent())
					if (cont instanceof X509Certificate)
						x509.add((X509Certificate) cont);
			}
		}

		// return of the certificates that matchs the public key.
		for (X509Certificate cert : x509) {
			if (cert.getPublicKey().equals(pKey)) {
				return cert;
			}
		}

		return null;
	}

	/**
	 * Verifica si una firma XML embedida es v&aacute;lida seg&uacute;n define
	 * el est&aacute;ndar XML Signature (<a
	 * href="http://www.w3.org/TR/xmldsig-core/#sec-CoreValidation">Core
	 * Validation</a>), y si el certificado era v&aacute;lido en la fecha dada.
	 * <p>
	 * 
	 * Esta rutina <b>NO</b> verifica si el certificado embedido en
	 * &lt;KeyInfo&gt; es v&aacute;lido (eso debe verificarlo con la autoridad
	 * certificadora que emiti&oacute; el certificado), pero si verifica que la
	 * llave utilizada para verificar corresponde a la contenida en el
	 * certificado.
	 * 
	 * @param xml
	 *            el nodo &lt;Signature&gt;
	 * @param date
	 *            una fecha en la que se verifica la validez del certificado
	 * @return el resultado de la verificaci&oacute;n
	 * 
	 * @see javax.xml.crypto.dsig.XMLSignature#sign(javax.xml.crypto.dsig.XMLSignContext)
	 * @see cl.nic.dte.VerifyResult
	 * @see cl.nic.dte.extension.DTEDefTypeExtensionHandler
	 * @see #getCertificate(XMLSignature)
	 */
	public static VerifyResult verifySignature(Node xml, Date date) {

		try {

			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
			KeyValueKeySelector ksel = new KeyValueKeySelector();

			DOMValidateContext valContext = new DOMValidateContext(ksel, xml);

			// Unmarshal the signature
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);

			X509Certificate x509 = getCertificate(signature);

			// Verifica que un certificado bien embedido
			if (x509 == null) {
				return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, Utilities.verificationLabels
								.getString("XML_SIGNATURE_ERROR_NO509")));
			}

			try {
				// Valida que en la fecha dada el certificado era va'lido
				x509.checkValidity(date);
			} catch (CertificateExpiredException e) {
				String message = Utilities.verificationLabels
						.getString("XML_SIGNATURE_ERROR_NOTVALID");
				message = message.replaceAll("%1", DateFormat.getDateInstance()
						.format(date));
				message = message.replaceAll("%2", DateFormat.getDateInstance()
						.format(x509.getNotBefore()));
				message = message.replaceAll("%3", DateFormat.getDateInstance()
						.format(x509.getNotAfter()));
				return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, message));
			} catch (CertificateNotYetValidException e) {
				String message = Utilities.verificationLabels
						.getString("XML_SIGNATURE_ERROR_NOTVALID");
				message = message.replaceAll("%1", DateFormat.getDateInstance()
						.format(date));
				message = message.replaceAll("%2", DateFormat.getDateInstance()
						.format(x509.getNotBefore()));
				message = message.replaceAll("%3", DateFormat.getDateInstance()
						.format(x509.getNotAfter()));
				return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, message));
			}

			return (verifySignature(signature, valContext));
		} catch (MarshalException e1) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNMARSHAL")
							+ ": " + e1.getMessage()));
		}

	}

	/**
	 * Verifica si una firma XML embedida es v&aacute;lida seg&uacute;n define
	 * el est&aacute;ndar XML Signature (<a
	 * href="http://www.w3.org/TR/xmldsig-core/#sec-CoreValidation">Core
	 * Validation</a>), y si el certificado era v&aacute;lido en la fecha dada.
	 * <p>
	 * 
	 * Esta rutina <b>NO</b> verifica si el certificado embedido en
	 * &lt;KeyInfo&gt; es v&aacute;lido (eso debe verificarlo con la autoridad
	 * certificadora que emiti&oacute; el certificado), pero si verifica que la
	 * llave utilizada para verificar corresponde a la contenida en el
	 * certificado.
	 * 
	 * @param xml
	 *            el nodo &lt;Signature&gt;
	 * @param date
	 *            una fecha en la que se verifica la validez del certificado
	 * @return el resultado de la verificaci&oacute;n
	 * 
	 * @see javax.xml.crypto.dsig.XMLSignature#sign(javax.xml.crypto.dsig.XMLSignContext)
	 * @see cl.nic.dte.VerifyResult
	 * @see cl.nic.dte.extension.DTEDefTypeExtensionHandler
	 * @see #getCertificate(XMLSignature)
	 */
	public static VerifyResult verifySignature(Node xml) {

		try {

			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
			KeyValueKeySelector ksel = new KeyValueKeySelector();

			DOMValidateContext valContext = new DOMValidateContext(ksel, xml);

			// Unmarshal the signature
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);

			X509Certificate x509 = getCertificate(signature);

			// Verifica que un certificado bien embedido
			if (x509 == null) {
				return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, Utilities.verificationLabels
								.getString("XML_SIGNATURE_ERROR_NO509")));
			}

			return (verifySignature(signature, valContext));
		} catch (MarshalException e1) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNMARSHAL")
							+ ": " + e1.getMessage()));
		}

	}

	/**
	 * Verifica si una firma XML embedida es v&aacute;lida seg&uacute;n define
	 * el est&aacute;ndar XML Signature (<a
	 * href="http://www.w3.org/TR/xmldsig-core/#sec-CoreValidation">Core
	 * Validation</a>), y si el certificado era v&aacute;lido en la fecha dada.
	 * <p>
	 * 
	 * Esta rutina <b>NO</b> verifica si el certificado embedido en
	 * &lt;KeyInfo&gt; es v&aacute;lido (eso debe verificarlo con la autoridad
	 * certificadora que emiti&oacute; el certificado), pero si verifica que la
	 * llave utilizada para verificar corresponde a la contenida en el
	 * certificado.
	 * 
	 * @param xml
	 *            el nodo &lt;Signature&gt;
	 * @param date
	 *            una fecha en la que se verifica la validez del certificado
	 * @return el resultado de la verificaci&oacute;n
	 * 
	 * @see javax.xml.crypto.dsig.XMLSignature#sign(javax.xml.crypto.dsig.XMLSignContext)
	 * @see cl.nic.dte.VerifyResult
	 * @see cl.nic.dte.extension.DTEDefTypeExtensionHandler
	 * @see #getCertificate(XMLSignature)
	 */
	@SuppressWarnings("unchecked")
	public static VerifyResult verifySignature(XMLSignature signature,
			DOMValidateContext valContext) {

		try {

			KeyValueKeySelector ksel = (KeyValueKeySelector)valContext.getKeySelector();
			X509Certificate x509 = getCertificate(signature);

			
			// Verifica que un certificado bien embedido
			if (x509 == null) {
				return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, Utilities.verificationLabels
								.getString("XML_SIGNATURE_ERROR_NO509")));
			}

			// Validate the XMLSignature
			boolean coreValidity = signature.validate(valContext);

			// Check core validation status
			if (coreValidity == false) {
				boolean sv = signature.getSignatureValue().validate(valContext);
				if (!sv)
					return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
							false, Utilities.verificationLabels
									.getString("XML_SIGNATURE_BAD_VALUE"));

				// check the validation status of each Reference
				String message = "";

				for (Reference ref : (List<Reference>) signature
						.getSignedInfo().getReferences()) {
					if (!ref.validate(valContext)) {
						message += Utilities.verificationLabels
								.getString("XML_SIGNATURE_BAD_REFERENCE");
						message = message.replaceAll("%1", new String(Base64
								.encodeBase64(ref.getCalculatedDigestValue())));
						message = message.replaceAll("%2", new String(Base64
								.encodeBase64(ref.getDigestValue())));
						message += "\n";
					}
				}
				return new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, message);
			}
			
			// Verifica que la llave del certificado corresponde a la usada para
			// la firma
			if (!ksel.getPk().equals(x509.getPublicKey())) {
				String message = Utilities.verificationLabels
						.getString("XML_SIGNATURE_ERROR_BADKEY");
				return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG,
						false, message));
			}

			return new VerifyResult(VerifyResult.XML_SIGNATURE_OK, true, null);
		} catch (XMLSignatureException e) {
			return (new VerifyResult(VerifyResult.XML_SIGNATURE_WRONG, false,
					Utilities.verificationLabels
							.getString("XML_SIGNATURE_ERROR_UNKNOWN")
							+ ": " + e.getMessage()));
		}

	}

	private static boolean algEquals(String algURI, String algName) {
		if (algName.equalsIgnoreCase("DSA")
				&& algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
			return true;
		} else if (algName.equalsIgnoreCase("RSA")
				&& algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Obtiene una representaci&oacute;n "limpia" de un elemento XML, esto
	 * quiere decir, sin espacios ni nuevas l&iacute;neas entre tags. Este
	 * m&eacute;todo se utiliza en la generaci&oacute;n PDF del &lt;TED&gt;.
	 * 
	 * @param xml
	 *            El nodo XML
	 * @return El arreglo de bytes codificado con ISO-8859-1 (norma exigida por
	 *         SII) del contenido del nodo (incluyendo los tags y caracteres
	 *         especiales de XML como &amp;).
	 */
	public static byte[] getCleaned(XmlObject xml) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			XmlOptionCharEscapeMap escapes = new XmlOptionCharEscapeMap();
		    escapes.addMapping('\'', XmlOptionCharEscapeMap.PREDEF_ENTITY);
		    escapes.addMapping('&', XmlOptionCharEscapeMap.PREDEF_ENTITY);
			
			
			XmlOptions opts = new XmlOptions();
			HashMap<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("", "http://www.sii.cl/SiiDte");
			opts.setCharacterEncoding("ISO-8859-1");
			opts.setSaveImplicitNamespaces(namespaces);
			opts.setSaveOuter();
			opts.setSavePrettyPrint();
			opts.setSavePrettyPrintIndent(0);
			opts.setSaveNoXmlDecl();
			
			opts.setSaveSubstituteCharacters(escapes);
			
			xml.save(out, opts);

			return out.toString("ISO-8859-1").replaceAll("\n", "").getBytes("ISO-8859-1");
			// return out.toString("ISO-8859-1").getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// Nunca debe invocarse
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// Nunca debe invocarse
			e.printStackTrace();
			return null;
		} catch (XmlException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] getBytesXML(XmlObject xml) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			XmlOptions opts = new XmlOptions();
			HashMap<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("", "http://www.sii.cl/SiiDte");
			opts.setCharacterEncoding("ISO-8859-1");
			opts.setSaveImplicitNamespaces(namespaces);
			opts.setSaveOuter();
			opts.setSavePrettyPrint();
			opts.setSavePrettyPrintIndent(0);
			opts.setSaveNoXmlDecl();
			
			xml.save(out, opts);

			return out.toString("ISO-8859-1").getBytes(
					"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// Nunca debe invocarse
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// Nunca debe invocarse
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * KeySelector which retrieves the public key out of the KeyValue element
	 * and returns it. NOTE: If the key algorithm doesn't match signature
	 * algorithm, then the public key will be ignored.
	 */
	private static class KeyValueKeySelector extends KeySelector {
		PublicKey pk;

		@SuppressWarnings("unchecked")
		public KeySelectorResult select(KeyInfo keyInfo,
				KeySelector.Purpose purpose, AlgorithmMethod method,
				XMLCryptoContext context) throws KeySelectorException {
			if (keyInfo == null) {
				throw new KeySelectorException("Null KeyInfo object!");
			}
			SignatureMethod sm = (SignatureMethod) method;
			List<XMLStructure> list = keyInfo.getContent();

			for (int i = 0; i < list.size(); i++) {
				XMLStructure xmlStructure = list.get(i);
				if (xmlStructure instanceof KeyValue) {
					try {
						setPk(((KeyValue) xmlStructure).getPublicKey());
					} catch (KeyException ke) {
						throw new KeySelectorException(ke);
					}
					// make sure algorithm is compatible with method
					if (algEquals(sm.getAlgorithm(), getPk().getAlgorithm())) {
						return new SimpleKeySelectorResult(getPk());
					}
				}
			}
			throw new KeySelectorException("No KeyValue element found!");
		}

		// @@@FIXME: this should also work for key types other than DSA/RSA
		static boolean algEquals(String algURI, String algName) {
			if (algName.equalsIgnoreCase("DSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
				return true;
			} else if (algName.equalsIgnoreCase("RSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
				return true;
			} else {
				return false;
			}
		}

		public PublicKey getPk() {
			return pk;
		}

		public void setPk(PublicKey pk) {
			this.pk = pk;
		}
	}

	private static class SimpleKeySelectorResult implements KeySelectorResult {
		private PublicKey pk;

		SimpleKeySelectorResult(PublicKey pk) {
			this.pk = pk;
		}

		public Key getKey() {
			return pk;
		}
	}

	public static AUTORIZACIONDocument generateAuthorization(
			AUTORIZACIONDocument template, PrivateKey pKey)
			throws NoSuchAlgorithmException, SignatureException,
			TransformerException, InvalidKeyException, IOException {
		// Generation of keys

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024);
		KeyPair kp = kpg.generateKeyPair();

		CAFType caf = template.getAUTORIZACION().getCAF();
		CAFType.DA.RSAPK rsapk = caf.getDA().addNewRSAPK();

		rsapk.setM(((RSAPublicKey) kp.getPublic()).getModulus().toByteArray());
		rsapk.setE(((RSAPublicKey) kp.getPublic()).getPublicExponent()
				.toByteArray());

		ResourceBundle labels = ResourceBundle
				.getBundle("cl.nic.dte.resources.VerifyResults");

		Signature sig = null;
		if (pKey.getAlgorithm().equals("RSA")) {
			sig = Signature.getInstance("SHA1withRSA");
			caf.addNewFRMA().setAlgoritmo("SHA1withRSA");
		} else if (pKey.getAlgorithm().equals("DSA")) {
			sig = Signature.getInstance("SHA1withDSA");
			caf.addNewFRMA().setAlgoritmo("SHA1withDSA");
		} else {
			throw new NoSuchAlgorithmException(labels.getString(
					"ALGORITHM_NOT_SUPPORTED").replaceAll("%1",
					pKey.getAlgorithm()));
		}

		template.getAUTORIZACION().setRSASK(
				"-----BEGIN RSA PRIVATE KEY-----\n"
						+ new String(Base64.encodeBase64(kp.getPrivate()
								.getEncoded(), true))
						+ "-----END RSA PRIVATE KEY-----\n");

		template.getAUTORIZACION().setRSAPUBK(
				"-----BEGIN RSA PUBLIC KEY-----\n"
						+ new String(Base64.encodeBase64(kp.getPublic()
								.getEncoded(), true))
						+ "-----END RSA PUBLIC KEY-----\n");

		sig.initSign(pKey);
		sig.update(XMLUtil.getCleaned(caf.getDA()));

		caf.getFRMA().setByteArrayValue(Base64.encodeBase64(sig.sign()));
		return template;
	}

	/**
	 * Obtiene una representaci&oacute;n "limpia" de un elemento XML, esto
	 * quiere decir, sin espacios ni nuevas l&iacute;neas entre tags. Este
	 * m&eacute;todo se utiliza en la generaci&oacute;n PDF del &lt;TED&gt;.
	 * 
	 * @param xml
	 *            El nodo XML
	 * @return El arreglo de bytes codificado con ISO-8859-1 (norma exigida por
	 *         SII) del contenido del nodo (incluyendo los tags y caracteres
	 *         especiales de XML como &amp;).
	 */
	public static byte[] getCleanedII(XmlObject xml) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	
			XmlOptions opts = new XmlOptions();
			HashMap<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("", "http://www.sii.cl/SiiDte");
			opts.setCharacterEncoding("ISO-8859-1");
			opts.setSaveImplicitNamespaces(namespaces);
			opts.setSaveOuter();
			//opts.setSavePrettyPrint();
			//opts.setSavePrettyPrintIndent(0);
			opts.setSaveNoXmlDecl();
			xml.save(out, opts);
	
			return out.toByteArray();
		} catch (UnsupportedEncodingException e) {
			// Nunca debe invocarse
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// Nunca debe invocarse
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Firma digitalmente usando la forma "enveloped signature" seg&uacute;n el
	 * est&aacute;ndar de la W3C (<a
	 * href="http://www.w3.org/TR/xmldsig-core/">http://www.w3.org/TR/xmldsig-core/</a>).
	 * <p>
	 * 
	 * Este m&eacute;todo adem&aacute;s incorpora la informaci&oacute;n del
	 * certificado a la secci&oacute;n &lt;KeyInfo&gt; opcional del
	 * est&aacute;ndar, seg&uacute;n lo exige SII.
	 * <p>
	 * 
	 * @param doc
	 *            El documento a firmar
	 * @param uri
	 *            La referencia dentro del documento que debe ser firmada
	 * @param pKey
	 *            La llave privada para firmar
	 * @param cert
	 *            El certificado digital correspondiente a la llave privada
	 * @throws NoSuchAlgorithmException
	 *             Si el algoritmo de firma de la llave no est&aacute; soportado
	 *             (Actualmente soportado RSA+SHA1, DSA+SHA1 y HMAC+SHA1).
	 * @throws InvalidAlgorithmParameterException
	 *             Si los algoritmos de canonizaci&oacute;n (parte del
	 *             est&aacute;ndar XML Signature) no son soportados (actaulmente
	 *             se usa el por defecto)
	 * @throws KeyException
	 *             Si hay problemas al incluir la llave p&uacute;blica en el
	 *             &lt;KeyValue&gt;.
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 * 
	 * @see javax.xml.crypto.dsig.XMLSignature#sign(javax.xml.crypto.dsig.XMLSignContext)
	 */
	public static void signEmbededApache(Document doc, String uri, PrivateKey pKey,
			X509Certificate cert) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException {
	
		try {
			org.apache.xml.security.signature.XMLSignature sig = new org.apache.xml.security.signature.XMLSignature(doc, uri,
			         org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA);
			
			doc.getDocumentElement().appendChild(sig.getElement());
			
			//ObjectContainer obj = new ObjectContainer(doc);
			//obj.setId(uri);
	         //sig.appendObject(obj);
	         Transforms transforms = new Transforms(doc);
	         transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
			sig.addDocument(uri,transforms);
	         sig.addKeyInfo(cert.getPublicKey());
	         sig.addKeyInfo(cert);
			//	sig.setXPathNamespaceContext("xmlns", "http://www.w3.org/2000/09/xmldsig#");
	         sig.sign(pKey);
			
		} catch (XMLSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	static {
	      org.apache.xml.security.Init.init();
	   }
}
