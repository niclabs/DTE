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

package cl.nic.dte.net;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;

import cl.nic.dte.util.Utilities;
import cl.sii.siiDte.GetTokenDocument;
import cl.sii.siiDte.RECEPCIONDTEDocument;
import cl.sii.siiDte.DTEDefType.Documento;
import cl.sii.xmlSchema.RESPUESTADocument;

public class ConexionSii {

	public boolean autentifica(PrivateKey pKey) {

		return false;
	}

	@SuppressWarnings("unchecked")
	public String getToken(PrivateKey pKey, X509Certificate cert)
			throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, SAXException, IOException,
			ParserConfigurationException, XmlException,
			UnsupportedOperationException, SOAPException, ConexionSiiException {

		String urlSolicitud = Utilities.netLabels
				.getString("URL_SOLICITUD_TOKEN");

		String semilla = getSemilla();

		GetTokenDocument req = GetTokenDocument.Factory.newInstance();

		req.addNewGetToken().addNewItem().setSemilla(semilla);

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();

		opts = new XmlOptions();
		opts.setSaveImplicitNamespaces(namespaces);
		opts.setLoadSubstituteNamespaces(namespaces);
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(0);

		req = GetTokenDocument.Factory.parse(req.newInputStream(opts), opts);

		// firmo
		req.sign(pKey, cert);

		SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection con = scFactory.createConnection();
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		SOAPPart soapPart = message.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPHeader header = envelope.getHeader();
		SOAPBody body = envelope.getBody();
		header.detachNode();

		Name bodyName = envelope.createName("getToken", "m", urlSolicitud);
		SOAPBodyElement gltp = body.addBodyElement(bodyName);

		Name toKname = envelope.createName("pszXml");

		SOAPElement toKsymbol = gltp.addChildElement(toKname);

		opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		opts.setSaveImplicitNamespaces(namespaces);

		toKsymbol.addTextNode(req.xmlText(opts));

		message.getMimeHeaders().addHeader("SOAPAction", "");

		URL endpoint = new URL(urlSolicitud);
		
		message.writeTo(System.out);

		SOAPMessage responseSII = con.call(message, endpoint);

		SOAPPart sp = responseSII.getSOAPPart();
		SOAPBody b = sp.getEnvelope().getBody();

		cl.sii.xmlSchema.RESPUESTADocument resp = null;
		for (Iterator<SOAPBodyElement> res = b.getChildElements(sp
				.getEnvelope().createName("getTokenResponse", "ns1",
						urlSolicitud)); res.hasNext();) {
			for (Iterator<SOAPBodyElement> ret = res.next().getChildElements(
					sp.getEnvelope().createName("getTokenReturn", "ns1",
							urlSolicitud)); ret.hasNext();) {

				namespaces = new HashMap<String, String>();
				namespaces.put("", "http://www.sii.cl/XMLSchema");
				opts.setLoadSubstituteNamespaces(namespaces);

				resp = RESPUESTADocument.Factory.parse(ret.next().getValue(),
						opts);
			}
		}

		if (resp != null && resp.getRESPUESTA().getRESPHDR().getESTADO() == 0) {
			return resp.getRESPUESTA().getRESPBODY().getTOKEN();
		} else {
			throw new ConexionSiiException("No obtuvo Semilla: Codigo: "
					+ resp.getRESPUESTA().getRESPHDR().getESTADO()
					+ "; Glosa: " + resp.getRESPUESTA().getRESPHDR().getGLOSA());
		}

	}

	@SuppressWarnings("unchecked")
	private String getSemilla() throws UnsupportedOperationException,
			SOAPException, IOException, XmlException, ConexionSiiException {
		SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection con = scFactory.createConnection();
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		SOAPPart soapPart = message.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPHeader header = envelope.getHeader();
		SOAPBody body = envelope.getBody();
		header.detachNode();

		String urlSolicitud = Utilities.netLabels
				.getString("URL_SOLICITUD_SEMILLA");

		Name bodyName = envelope.createName("getSeed", "m", urlSolicitud);

		message.getMimeHeaders().addHeader("SOAPAction", "");

		body.addBodyElement(bodyName);

		URL endpoint = new URL(urlSolicitud);

		SOAPMessage responseSII = con.call(message, endpoint);

		SOAPPart sp = responseSII.getSOAPPart();
		SOAPBody b = sp.getEnvelope().getBody();

		cl.sii.xmlSchema.RESPUESTADocument resp = null;
		for (Iterator<SOAPBodyElement> res = b.getChildElements(sp
				.getEnvelope().createName("getSeedResponse", "ns1",
						urlSolicitud)); res.hasNext();) {
			for (Iterator<SOAPBodyElement> ret = res.next().getChildElements(
					sp.getEnvelope().createName("getSeedReturn", "ns1",
							urlSolicitud)); ret.hasNext();) {

				HashMap<String, String> namespaces = new HashMap<String, String>();
				namespaces.put("", "http://www.sii.cl/XMLSchema");
				XmlOptions opts = new XmlOptions();
				opts.setLoadSubstituteNamespaces(namespaces);

				resp = RESPUESTADocument.Factory.parse(ret.next().getValue(),
						opts);

			}
		}

		if (resp != null && resp.getRESPUESTA().getRESPHDR().getESTADO() == 0) {
			return resp.getRESPUESTA().getRESPBODY().getSEMILLA();
		} else {
			throw new ConexionSiiException("No obtuvo Semilla: Codigo: "
					+ resp.getRESPUESTA().getRESPHDR().getESTADO()
					+ "; Glosa: " + resp.getRESPUESTA().getRESPHDR().getGLOSA());
		}
	}

	/**
	 * Consulta el estado de un DTE en el ambiente de produccion del SII
	 * 
	 * @param rutConsultante
	 * @param dte
	 * @param token
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws SOAPException
	 * @throws MalformedURLException
	 * @throws XmlException
	 */
	public RESPUESTADocument getEstadoDTEProduccion(String rutConsultante,
			Documento dte, String token) throws UnsupportedOperationException,
			SOAPException, MalformedURLException, XmlException {
		String urlSolicitud = Utilities.netLabels
				.getString("URL_CONSULTA_ESTADO_DTE_PRODUCCION");
		return getEstadoDTE(rutConsultante, dte, token, urlSolicitud);
	}
	
	/**
	 * Consulta el estado de un DTE en el ambiente de del SII
	 * 
	 * @param rutConsultante
	 * @param dte
	 * @param token
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws SOAPException
	 * @throws MalformedURLException
	 * @throws XmlException
	 */
	public RESPUESTADocument getEstadoDTECertificacion(String rutConsultante,
			Documento dte, String token) throws UnsupportedOperationException,
			SOAPException, MalformedURLException, XmlException {
		String urlSolicitud = Utilities.netLabels
				.getString("URL_CONSULTA_ESTADO_DTE_CERTIFICACION");
		return getEstadoDTE(rutConsultante, dte, token, urlSolicitud);
	}

	@SuppressWarnings("unchecked")
	private RESPUESTADocument getEstadoDTE(String rutConsultante, Documento dte,
			String token, String urlSolicitud)
			throws UnsupportedOperationException, SOAPException,
			MalformedURLException, XmlException {

		String rutEmisor = dte.getEncabezado().getEmisor().getRUTEmisor();
		String rutReceptor = dte.getEncabezado().getReceptor().getRUTRecep();
		Integer tipoDTE = dte.getEncabezado().getIdDoc().getTipoDTE()
				.intValue();
		long folioDTE = dte.getEncabezado().getIdDoc().getFolio();
		String fechaEmision = Utilities.fechaEstadoDte.format(dte
				.getEncabezado().getIdDoc().getFchEmis().getTime());
		long montoTotal = dte.getEncabezado().getTotales().getMntTotal();

		SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection con = scFactory.createConnection();
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		SOAPPart soapPart = message.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPHeader header = envelope.getHeader();
		SOAPBody body = envelope.getBody();
		header.detachNode();

		Name bodyName = envelope.createName("getEstDte", "m", urlSolicitud);
		SOAPBodyElement gltp = body.addBodyElement(bodyName);

		Name toKname = envelope.createName("RutConsultante");
		SOAPElement toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(rutConsultante.substring(0, rutConsultante
				.length() - 2));

		toKname = envelope.createName("DvConsultante");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(rutConsultante.substring(
				rutConsultante.length() - 1, rutConsultante.length()));

		toKname = envelope.createName("RutCompania");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(rutEmisor.substring(0, rutEmisor.length() - 2));

		toKname = envelope.createName("DvCompania");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(rutEmisor.substring(rutEmisor.length() - 1,
				rutEmisor.length()));

		toKname = envelope.createName("RutReceptor");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(rutReceptor
				.substring(0, rutReceptor.length() - 2));

		toKname = envelope.createName("DvReceptor");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(rutReceptor.substring(rutReceptor.length() - 1,
				rutReceptor.length()));

		toKname = envelope.createName("TipoDte");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(Integer.toString(tipoDTE));

		toKname = envelope.createName("FolioDte");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(Long.toString(folioDTE));

		toKname = envelope.createName("FechaEmisionDte");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(fechaEmision);

		toKname = envelope.createName("MontoDte");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(Long.toString(montoTotal));

		toKname = envelope.createName("Token");
		toKsymbol = gltp.addChildElement(toKname);
		toKsymbol.addTextNode(token);

		message.getMimeHeaders().addHeader("SOAPAction", "");

		URL endpoint = new URL(urlSolicitud);

		SOAPMessage responseSII = con.call(message, endpoint);

		SOAPPart sp = responseSII.getSOAPPart();
		SOAPBody b = sp.getEnvelope().getBody();

		for (Iterator<SOAPBodyElement> res = b.getChildElements(sp
				.getEnvelope().createName("getEstDteResponse", "ns1",
						urlSolicitud)); res.hasNext();) {
			for (Iterator<SOAPBodyElement> ret = res.next().getChildElements(
					sp.getEnvelope().createName("getEstDteReturn", "ns1",
							urlSolicitud)); ret.hasNext();) {

				HashMap<String, String> namespaces = new HashMap<String, String>();
				namespaces.put("", "http://www.sii.cl/XMLSchema");
				XmlOptions opts = new XmlOptions();
				opts.setLoadSubstituteNamespaces(namespaces);

				return RESPUESTADocument.Factory.parse(ret.next().getValue(),
						opts);
			}
		}

		return null;

	}

	/**
	 * Envia el archivo XML indicado al ambiente de produccion del SII.
	 * 
	 * @param rutEnvia
	 * @param rutCompania
	 * @param archivoEnviarSII
	 * @param token
	 * @return
	 * @throws ClientProtocolException
	 * @throws ParseException
	 * @throws IOException
	 * @throws XmlException
	 */
	public RECEPCIONDTEDocument uploadEnvioProduccion(String rutEnvia,
			String rutCompania, File archivoEnviarSII, String token)
			throws ClientProtocolException, ParseException, IOException,
			XmlException {
		String urlEnvio = Utilities.netLabels
				.getString("URL_UPLOAD_PRODUCCION");
		String hostEnvio = Utilities.netLabels
				.getString("HOST_UPLOAD_PRODUCCION");
		return uploadEnvio(rutEnvia, rutCompania, archivoEnviarSII, token,
				urlEnvio, hostEnvio);
	}

	/**
	 * Envia el archivo XML indicado al ambiente de certificacion del SII.
	 * 
	 * @param rutEnvia
	 * @param rutCompania
	 * @param archivoEnviarSII
	 * @param token
	 * @return
	 * @throws ClientProtocolException
	 * @throws ParseException
	 * @throws IOException
	 * @throws XmlException
	 */
	public RECEPCIONDTEDocument uploadEnvioCertificacion(String rutEnvia,
			String rutCompania, File archivoEnviarSII, String token)
			throws ClientProtocolException, ParseException, IOException,
			XmlException {
		String urlEnvio = Utilities.netLabels
				.getString("URL_UPLOAD_CERTIFICACION");
		String hostEnvio = Utilities.netLabels
				.getString("HOST_UPLOAD_CERTIFICACION");
		return uploadEnvio(rutEnvia, rutCompania, archivoEnviarSII, token,
				urlEnvio, hostEnvio);
	}

	private RECEPCIONDTEDocument uploadEnvio(String rutEnvia,
			String rutCompania, File archivoEnviarSII, String token,
			String urlEnvio, String hostEnvio) throws ClientProtocolException,
			IOException, org.apache.http.ParseException, XmlException {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(urlEnvio);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("rutSender", new StringBody(rutEnvia.substring(0,
				rutEnvia.length() - 2)));
		reqEntity.addPart("dvSender", new StringBody(rutEnvia.substring(
				rutEnvia.length() - 1, rutEnvia.length())));
		reqEntity.addPart("rutCompany", new StringBody(rutCompania.substring(0,
				(rutCompania).length() - 2)));
		reqEntity.addPart("dvCompany", new StringBody(rutCompania.substring(
				rutCompania.length() - 1, rutCompania.length())));

		FileBody bin = new FileBody(archivoEnviarSII);
		reqEntity.addPart("archivo", bin);

		httppost.setEntity(reqEntity);

		BasicClientCookie cookie = new BasicClientCookie("TOKEN", token);
		cookie.setPath("/");
		cookie.setDomain(hostEnvio);
		cookie.setSecure(true);
		cookie.setVersion(1);

		CookieStore cookieStore = new BasicCookieStore();
		cookieStore.addCookie(cookie);

		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.RFC_2109);
		httppost.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);

		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		httppost.addHeader(new BasicHeader("User-Agent", Utilities.netLabels
				.getString("UPLOAD_SII_HEADER_VALUE")));

		HttpResponse response = httpclient.execute(httppost, localContext);

		HttpEntity resEntity = response.getEntity();

		RECEPCIONDTEDocument resp = null;

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		resp = RECEPCIONDTEDocument.Factory.parse(EntityUtils
				.toString(resEntity), opts);

		return resp;
	}

}
