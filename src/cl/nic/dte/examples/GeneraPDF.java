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
import java.io.FileOutputStream;

import cl.nic.dte.util.Utilities;

public class GeneraPDF {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		if (args.length != 3) {
			System.err
					.println("Utilice: java cl.nic.dte.examples.GeneraPDF " 
							+ "<factura.xml> <formato.xsl> <resultado.pdf>");
			System.exit(-1);
		}
		
		Utilities.generatePDF(new FileInputStream(args[0]),
				new FileInputStream(args[1]), new FileOutputStream(
						args[2]));

	}

}
