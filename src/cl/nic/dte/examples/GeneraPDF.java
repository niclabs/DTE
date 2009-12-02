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

import jargs.gnu.CmdLineParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import cl.nic.dte.util.Utilities;

public class GeneraPDF {

	private static void printUsage() {
		System.err
				.println("Utilice: java cl.nic.dte.examples.GeneraPDF "
						+ "-p <plantilla.xsl> -o <resultado.pdf> <factura.xml>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option resultOpt = parser.addStringOption('o', "output");
		CmdLineParser.Option plantillaOpt = parser.addStringOption('p',
				"plantilla");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}

		String resultS = (String) parser.getOptionValue(resultOpt);
		String plantillaS = (String) parser.getOptionValue(plantillaOpt);

		if (resultS == null || plantillaS == null) {
			printUsage();
			System.exit(2);
		}

		String[] otherArgs = parser.getRemainingArgs();

		if (otherArgs.length != 1) {
			printUsage();
			System.exit(2);
		}

		
		Utilities.generatePDF(new FileInputStream(otherArgs[0]),
				new FileInputStream(plantillaS), new FileOutputStream(
						resultS));

	}

}
