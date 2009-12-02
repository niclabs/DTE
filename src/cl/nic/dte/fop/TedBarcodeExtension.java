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


package cl.nic.dte.fop;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.krysalis.barcode4j.xalan.BarcodeExt;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cl.nic.dte.util.XMLUtil;
import cl.sii.siiDte.TEDType;


public class TedBarcodeExtension extends BarcodeExt {

	public DocumentFragment generate(NodeList nl, NodeList tedxml) throws SAXException {
		// TODO Auto-generated method stub
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("", "http://www.sii.cl/SiiDte");
		XmlOptions opts = new XmlOptions();
		opts.setLoadSubstituteNamespaces(namespaces);
		
		TEDType ted;
		
		try {
			ted = TEDType.Factory.parse(tedxml.item(0),opts);
			String msg = null;
			try {
				msg = new String(XMLUtil.getCleaned(ted),"ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return generate(nl, msg);

		
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SAXException(e);
		}
	}

}
