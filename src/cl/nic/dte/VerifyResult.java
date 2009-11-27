/*
Copyright 2008 NIC Chile.

Castellano
			
DTE OpenLibs es software libre: usted puede redistribuirlo y/o modificarlo 
conforme a los términos de la Licencia Pública General Reducida de GNU publicada 
por la Fundación para el Software Libre, ya sea la versión 3 de esta Licencia
o (a su elección) cualquier versión posterior.

DTE OpenLibs se distribuye con el deseo de que le resulte útil, pero SIN 
GARANTÍAS DE NINGÚN TIPO; ni siquiera con las garantías implícitas de
COMERCIABILIDAD o APTITUD PARA UN PROPÓSITO DETERMINADO. Para más información,
consulte la Licencia Pública General Reducida de GNU.

Junto con DTE OpenLibs , se debería incluir una copia de la Licencia Pública 
General Reducida de GNU. De no ser así, refiérase a <http://www.gnu.org/licenses/>

			
English
This file is part of DTE OpenLibs.

DTE OpenLibs is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DTE OpenLibs is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU General Public License
along with DTE OpenLibs.  If not, see <http://www.gnu.org/licenses/>
					
*/

package cl.nic.dte;

/**
 * Esta clase encapsula los resultados de m&eacute;todos de verificaci&oacute;n
 * de otras clases
 * 
 * @author Tom&aacute;s Barros &lt;tbarros@nic.cl&gt;
 * 
 */
public class VerifyResult {

	/**
	 * Indica cuando la verificaci&oacute;n de la estructura XML fue correcta
	 */
	public static final short XML_STRUCTURE_OK = 0x01;

	/**
	 * Indica cuando la verificaci&oacute;n de la estructura XML fue err&oacute;nea
	 */
	public static final short XML_STRUCTURE_WRONG = 0x02;

	/**
	 * Indica cuando la verificaci&oacute;n de la firma XML fue correcta
	 */
	public static final short XML_SIGNATURE_OK = 0x03;

	/**
	 * Indica cuando la verificaci&oacute;n de la firma XML fue err&oacute;nea
	 */
	public static final short XML_SIGNATURE_WRONG = 0x04;

	public static final short TED_CONTENTS_WRONG = 0x05;

	public static final short TED_WRONG_CAF = 0x06;

	public static final short TED_BAD_SIGNATURE = 0x07;

	public static final short TED_OK = 0x08;

	private short code;

	private boolean ok;

	private String message;

	/**
	 * Entrega el mensaje con informaci&oacute;n de la verificaci&oacute;n.
	 * 
	 * @return El mensaje
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Asigna el mensaje con informaci&oacute;n de la verificaci&oacute;n.
	 * 
	 * @param message
	 *            el mensaje a asignar
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Entrega el c&oacute;digo con el estado de la verificaci&oacute;n.
	 * 
	 * @return el c&oacute;digo
	 */
	public short getCode() {
		return code;
	}

	public void setCode(short code) {
		this.code = code;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean status) {
		this.ok = status;
	}

	/**
	 * Constructor
	 * 
	 * @param code C&oacute;digo indicando estado.
	 * @param status <code>true</code> cuando verifica correctamente o <code>flaso</code> en caso contrario.
	 * @param message Mensaje con informaci&oacute;n de la verificaci&oacute;n.
	 */
	public VerifyResult(short code, boolean status, String message) {
		super();
		this.code = code;
		this.ok = status;
		this.message = message;
	}

}
