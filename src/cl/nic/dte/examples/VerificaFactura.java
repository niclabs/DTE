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

import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.xmlbeans.XmlOptions;

import cl.nic.dte.VerifyResult;
import cl.sii.siiDte.DTEDocument;

public class VerificaFactura {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err
					.println("Utilice: java cl.nic.dte.examples.VerificaFactura " 
							+ "<factura.xml>");
			System.exit(-1);
		}
		
		// Meto el namespace en caso que no exista
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);

		DTEDocument doc = DTEDocument.Factory.parse(new FileInputStream(
				args[0]), opts);

		VerifyResult resl = doc.getDTE().verifyTimbre();
		if (!resl.isOk()) {
			System.out.println("Documento: Timbre Incorrecto: "
					+ resl.getMessage());
		} else {
			System.out.println("Documento: Timbre OK");
		}

		resl = doc.getDTE().verifyXML();
		if (!resl.isOk()) {
			System.out.println("Documento: Estructura XML Incorrecta: "
					+ resl.getMessage());
		} else {
			System.out.println("Documento: Estructura XML OK");
		}

		resl = doc.getDTE().verifySignature();
		if (!resl.isOk()) {
			System.out.println("Documento: Firma XML Incorrecta: "
					+ resl.getMessage());
		} else {
			System.out.println("Documento: Firma XML OK");
		}

	}

}
