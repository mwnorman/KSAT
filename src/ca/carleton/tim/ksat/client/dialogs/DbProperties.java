/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2010, Mike Norman
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
 * USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package ca.carleton.tim.ksat.client.dialogs;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class DbProperties {

	protected Text databaseNameText;
	protected Text userNameText;
	protected Text urlText;
	protected Text driverText;
	protected Text platformText;
	protected Text passwordText;
	protected Combo logLevelCombo;
	
    public DbProperties(Text databaseNameText, Text userNameText, Text urlText, Text driverText,
    	Text platformText, Text passwordText, Combo logLevelCombo) {
		this.databaseNameText = databaseNameText;
		this.userNameText = userNameText;
		this.urlText = urlText;
		this.driverText = driverText;
		this.platformText = platformText;
		this.passwordText = passwordText;
		this.logLevelCombo = logLevelCombo;
    }
}