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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class Utilities {

	public static DateFormat fechaFormat = null;

	public static DateFormat fechaEstadoDte = null;

	public static DateFormat fechaMesFormat = null;

	public static DateFormat fechaHoraFormat = null;

	public static ResourceBundle verificationLabels = null;

	public static ResourceBundle netLabels = null;

	public static ResourceBundle exceptions = null;

	static {
		String prop1 = System.getProperty("sii.dte.ConfigurationFile");
		ResourceBundle conf1 = null;

		if (prop1 != null)
			conf1 = ResourceBundle.getBundle(prop1);
		ResourceBundle conf2 = ResourceBundle
				.getBundle("cl.nic.dte.resources.Configuration");

		Locale language;
		if (conf1 != null && conf1.getString("LOCALE") != null)
			language = new Locale(conf1.getString("LOCALE"));
		else
			language = new Locale(conf2.getString("LOCALE"));

		verificationLabels = ResourceBundle.getBundle(
				"cl.nic.dte.resources.VerifyResults", language);

		netLabels = ResourceBundle.getBundle("cl.nic.dte.resources.Net",
				language);

		exceptions = ResourceBundle.getBundle(
				"cl.nic.dte.resources.Exceptions", language);

		if (conf1 != null && conf1.getString("FECHA_TYPE_FORMAT") != null)
			fechaFormat = new SimpleDateFormat(conf1
					.getString("FECHA_TYPE_FORMAT"));
		else
			fechaFormat = new SimpleDateFormat(conf2
					.getString("FECHA_TYPE_FORMAT"));

		if (conf1 != null && conf1.getString("FECHA_MES_TYPE_FORMAT") != null)
			fechaMesFormat = new SimpleDateFormat(conf1
					.getString("FECHA_MES_TYPE_FORMAT"));
		else
			fechaMesFormat = new SimpleDateFormat(conf2
					.getString("FECHA_MES_TYPE_FORMAT"));

		if (conf1 != null && conf1.getString("FECHA_HORA_TYPE_FORMAT") != null)
			fechaHoraFormat = new SimpleDateFormat(conf1
					.getString("FECHA_HORA_TYPE_FORMAT"));
		else
			fechaHoraFormat = new SimpleDateFormat(conf2
					.getString("FECHA_HORA_TYPE_FORMAT"));

		if (conf1 != null && conf1.getString("FECHA_ESTADO_DTE") != null)
			fechaEstadoDte = new SimpleDateFormat(conf1
					.getString("FECHA_ESTADO_DTE"));
		else
			fechaEstadoDte = new SimpleDateFormat(conf2
					.getString("FECHA_ESTADO_DTE"));
	}

	public static PrivateKey readPrivateKeyFromFile(String fileName,
			String algo, char[] password) throws IOException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeySpecException, InvalidKeyException,
			InvalidAlgorithmParameterException {

		return Utilities.readPrivateKey(
				new FileInputStream(new File(fileName)), algo, password);

	}

	public static PrivateKey readPrivateKey(InputStream in, String algo,
			char[] password) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeySpecException,
			InvalidKeyException, InvalidAlgorithmParameterException {

		byte[] datos = new byte[in.available()];
		while (in.available() > 0)
			in.read(datos);
		return Utilities.readPrivateKey(datos, algo, password);
	}

	public static PrivateKey readPrivateKey(byte[] datos, String algo,
			char[] password) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeySpecException,
			InvalidKeyException, InvalidAlgorithmParameterException {

		PKCS8EncodedKeySpec pkcs8KeySpec = null;

		if (password != null) {
			EncryptedPrivateKeyInfo ekey = new EncryptedPrivateKeyInfo(datos);
			Cipher cip = Cipher.getInstance(ekey.getAlgName());
			PBEKeySpec pspec = new PBEKeySpec(password);
			SecretKeyFactory skfac = SecretKeyFactory.getInstance(ekey
					.getAlgName());

			Key pbeKey = skfac.generateSecret(pspec);

			AlgorithmParameters algParams = ekey.getAlgParameters();
			cip.init(Cipher.DECRYPT_MODE, pbeKey, algParams);

			pkcs8KeySpec = ekey.getKeySpec(cip);
		} else {
			pkcs8KeySpec = new PKCS8EncodedKeySpec(datos);
		}

		KeyFactory rsaKeyFac = KeyFactory.getInstance(algo);
		return (PrivateKey) rsaKeyFac.generatePrivate(pkcs8KeySpec);
	}

	public static boolean correspond(PublicKey pubKey, PrivateKey prvKey)
			throws NoSuchAlgorithmException {

		ResourceBundle labels = ResourceBundle
				.getBundle("cl.nic.dte.resources.Exceptions");

		if (!pubKey.getAlgorithm().equals(prvKey.getAlgorithm()))
			return false;

		if (pubKey.getAlgorithm().equals("RSA")) {
			if (!((RSAPrivateKey) prvKey).getModulus().equals(
					((RSAPublicKey) pubKey).getModulus()))
				return false;
			return true;

		} else if (pubKey.getAlgorithm().equals("DSA")) {
			if (!(((DSAPrivateKey) prvKey).getParams().getG()
					.equals(((DSAPublicKey) pubKey).getParams().getG())))
				return false;
			if (!(((DSAPrivateKey) prvKey).getParams().getP()
					.equals(((DSAPublicKey) pubKey).getParams().getP())))
				return false;
			if (!(((DSAPrivateKey) prvKey).getParams().getQ()
					.equals(((DSAPublicKey) pubKey).getParams().getQ())))
				return false;
			return true;
		} else
			throw new NoSuchAlgorithmException(labels
					.getString("ALG_NOT_SUPPORTED"));
	}

	public static Date getDate(String date) throws ParseException {

		return fechaFormat.parse(date);

	}

	public static void generatePDF(InputStream xmlFile, InputStream xslFile,
			OutputStream pdfFile) throws FOPException, FileNotFoundException,
			TransformerException {
		FopFactory fopFactory = FopFactory.newInstance();

		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		// configure foUserAgent as desired

		// Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent,
				pdfFile);

		// Setup XSLT
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(new StreamSource(
				xslFile));

		// Set the value of a <param> in the stylesheet
		transformer.setParameter("versionParam", "2.0");

		// Setup input for XSLT transformation
		Source src = new StreamSource(xmlFile);
		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());

		// res = new StreamResult(new File("data/generated/sample-fo.xml"));

		// Start XSLT transformation and FOP processing
		transformer.transform(src, res);
	}

	public static String getRutFormatted(String rut) {
		rut = rut.substring(0, rut.length() - 8) + "."
				+ rut.substring(rut.length() - 8, rut.length() - 5) + "."
				+ rut.substring(rut.length() - 5, rut.length());
		return rut;
	}

	/**
	 * Obtiene el RUT desde un certificado digital. Busca en la extension
	 * 2.5.29.17
	 * 
	 * @param x509
	 * @return
	 */
	public static String getRutFromCertificate(X509Certificate x509) {
		String rut = null;
		Pattern p = Pattern.compile("[\\d]{6,8}-[\\dkK]");
	
		Matcher m = p.matcher(new String(x509.getExtensionValue("2.5.29.17")));
		if (m.find())
			rut = m.group();
		
		return rut;

	}
	
}
