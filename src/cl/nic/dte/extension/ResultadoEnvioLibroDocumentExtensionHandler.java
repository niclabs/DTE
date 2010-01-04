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

import noNamespace.ResultadoEnvioLibroDocument;

import org.apache.xmlbeans.XmlOptions;

import cl.nic.dte.VerifyResult;
import cl.nic.dte.util.XMLUtil;


public class ResultadoEnvioLibroDocumentExtensionHandler {

	public static VerifyResult verifyXML(ResultadoEnvioLibroDocument dte) {
		return XMLUtil.verifyXML(dte);

	}

	public static byte[] getBytes(ResultadoEnvioLibroDocument dte) throws IOException {

		XmlOptions opts = new XmlOptions();
		opts.setCharacterEncoding("ISO-8859-1");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dte.save(out, opts);
		return out.toByteArray();

	}
}
