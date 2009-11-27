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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import org.apache.commons.ssl.PEMItem;
import org.apache.commons.ssl.PEMUtil;
import org.apache.commons.ssl.PKCS8Key;

import cl.nic.dte.util.Utilities;
import cl.sii.siiDte.AutorizacionType;

public class AutorizacionTypeExtensionHandler {

	@SuppressWarnings("unchecked")
	public static PrivateKey getPrivateKey(AutorizacionType auth,
			char[] password) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeySpecException, InvalidAlgorithmParameterException,
			IOException {

		List<PEMItem> items = PEMUtil.decode(auth.getRSASK().getBytes());

		for (PEMItem item : items) {
			if ("RSA PRIVATE KEY".equals(item.pemType)) {
				try {
					PKCS8Key pkcs8 = new PKCS8Key(item.getDerBytes(), password);

					return Utilities.readPrivateKey(pkcs8.getDecryptedBytes(),
							"RSA", password);
				} catch (GeneralSecurityException e) {
					throw new InvalidKeySpecException(e);
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static PublicKey getPublicKey(AutorizacionType auth)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		List<PEMItem> items = PEMUtil.decode(auth.getRSAPUBK().getBytes());

		for (PEMItem item : items) {
			if ("PUBLIC KEY".equals(item.pemType)) {
				X509EncodedKeySpec enc;
				try {
					enc = new X509EncodedKeySpec(item.getDerBytes());
					KeyFactory rsaKeyFac;
					rsaKeyFac = KeyFactory.getInstance("RSA");
					return (PublicKey) rsaKeyFac.generatePublic((enc));
				} catch (GeneralSecurityException e) {
					throw new InvalidKeySpecException(e);
				}
			}
		}
		return null;
	}
}
