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

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import cl.sii.siiDte.CAFType;
import cl.sii.siiDte.DTEDefType;
import cl.sii.siiDte.DTEDefType.Documento.TED.DD.CAF.DA;

public class CAFTypeExtensionHandler {

	/**
	 * Obtiene la llave p&uacute;blica entregada por SII en una
	 * autorizaci&oacute;n de folios.
	 * 
	 * @param caf
	 *            La autorizaci&oacute;n enviada por SII
	 * @return La llave publica contenida
	 * @throws NoSuchAlgorithmException
	 *             Si el algoritmo de la llave no es soportado (actualmente se
	 *             soporta RSA y DSA)
	 * @throws InvalidKeySpecException
	 *             Si la codificaci&oacute;n de la llave es incorrecta.
	 * 
	 * @see cl.sii.siiDte.AUTORIZACIONDocument
	 */
	public static PublicKey getPublicKey(CAFType caf)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		cl.sii.siiDte.CAFType.DA da = caf.getDA();
		if (da.isSetRSAPK()) {
			BigInteger modulus = new BigInteger(1, da.getRSAPK().getM());
			BigInteger exponent = new BigInteger(1, da.getRSAPK().getE());
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (kf.generatePublic(new RSAPublicKeySpec(modulus, exponent)));

		} else if (da.isSetDSAPK()) {
			BigInteger y = new BigInteger(1, da.getDSAPK().getY());
			BigInteger g = new BigInteger(1, da.getDSAPK().getG());
			BigInteger p = new BigInteger(1, da.getDSAPK().getP());
			BigInteger q = new BigInteger(1, da.getDSAPK().getQ());
			KeyFactory kf = KeyFactory.getInstance("DSA");
			return (kf.generatePublic(new DSAPublicKeySpec(y, p, q, g)));

		}

		return null;

	}

	public static DTEDefType.Documento.TED.DD.CAF getCAFforDocument(CAFType auth) {
		DTEDefType.Documento.TED.DD.CAF caf = DTEDefType.Documento.TED.DD.CAF.Factory
				.newInstance();
		DA da = caf.addNewDA();
		da.set(auth.getDA().copy());
		DTEDefType.Documento.TED.DD.CAF.FRMA fr = caf.addNewFRMA();
		fr.set(auth.getFRMA().copy());
		caf.setVersion(auth.getVersion());
		return caf;
	}

	public static DTEDefType.Exportaciones.TED.DD.CAF getCAFforExportacion(
			CAFType auth) {
		DTEDefType.Exportaciones.TED.DD.CAF caf = DTEDefType.Exportaciones.TED.DD.CAF.Factory
				.newInstance();
		DTEDefType.Exportaciones.TED.DD.CAF.DA da = caf.addNewDA();
		da.set(auth.getDA().copy());
		DTEDefType.Exportaciones.TED.DD.CAF.FRMA fr = caf.addNewFRMA();
		fr.set(auth.getFRMA().copy());
		caf.setVersion(auth.getVersion());
		return caf;
	}

	public static DTEDefType.Liquidacion.TED.DD.CAF getCAFforLiquidacion(
			CAFType auth) {
		DTEDefType.Liquidacion.TED.DD.CAF caf = DTEDefType.Liquidacion.TED.DD.CAF.Factory
				.newInstance();
		DTEDefType.Liquidacion.TED.DD.CAF.DA da = caf.addNewDA();
		da.set(auth.getDA().copy());
		DTEDefType.Liquidacion.TED.DD.CAF.FRMA fr = caf.addNewFRMA();
		fr.set(auth.getFRMA().copy());
		caf.setVersion(auth.getVersion());
		return caf;

	}
	
	

}
