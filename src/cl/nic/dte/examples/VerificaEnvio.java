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
import java.security.cert.X509Certificate;

import cl.nic.dte.VerifyResult;
import cl.nic.dte.util.XMLUtil;
import cl.sii.siiDte.DTEDefType;
import cl.sii.siiDte.EnvioDTEDocument;

public class VerificaEnvio {

	private static void printUsage() {
		System.err.println("Utilice: java cl.nic.dte.examples.VerificaEnvio "
				+ "<envio.xml>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			printUsage();
			System.exit(2);
		}

		EnvioDTEDocument doc = EnvioDTEDocument.Factory
				.parse(new FileInputStream(args[0]));

		VerifyResult resl = doc.verifyXML();
		if (!resl.isOk()) {
			System.out.println("Envio: Estructura XML Incorrecta: "
					+ resl.getMessage());
		} else {
			System.out.println("Envio: Estructura XML OK");
		}

		resl = doc.verifySignature();
		if (!resl.isOk()) {
			System.out.println("Envio: Firma XML Incorrecta: "
					+ resl.getMessage());
		} else {
			System.out.println("Envio: Firma XML OK");
		}

		X509Certificate x509 = XMLUtil.getCertificate(doc.getEnvioDTE()
				.getSignature());
		System.out.println("Firmado por: "
				+ x509.getSubjectX500Principal().getName());

		for (DTEDefType dte : doc.getEnvioDTE().getSetDTE().getDTEArray()) {
			resl = dte.verifySignature();

			System.out.println("DTE ID " + dte.getDocumento().getID()
					+ " Firmado por: "
					+ x509.getSubjectX500Principal().getName());
			if (!resl.isOk()) {
				System.out.println("Envio, DTE ID "
						+ dte.getDocumento().getID()
						+ " : Firma XML Incorrecta: " + resl.getMessage());
			} else {
				System.out.println("Envio, DTE ID "
						+ dte.getDocumento().getID() + " : Firma XML OK");
			}
		}
	}

}
