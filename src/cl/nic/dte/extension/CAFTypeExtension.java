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

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import cl.sii.siiDte.DTEDefType;

public interface CAFTypeExtension {

	public PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException;
	
	public DTEDefType.Documento.TED.DD.CAF getCAFforDocument();

	public DTEDefType.Exportaciones.TED.DD.CAF getCAFforExportacion();

	public DTEDefType.Liquidacion.TED.DD.CAF getCAFforLiquidacion();
	
	
}
