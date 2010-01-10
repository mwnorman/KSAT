/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, Mike Norman
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
package ca.carleton.tim.ksat.plugin.prefs;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class KeywordAnalyzerPreferencePages implements IPreferencePage {

    public Point computeSize() {
        return null;
    }

    public boolean isValid() {
        return false;
    }

    public boolean okToLeave() {
        return false;
    }

    public boolean performCancel() {
        return false;
    }

    public boolean performOk() {
        return false;
    }

    public void setContainer(IPreferencePageContainer arg0) {
    }

    public void setSize(Point arg0) {
    }

    public void createControl(Composite arg0) {
    }

    public void dispose() {
    }

    public Control getControl() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getErrorMessage() {
        return null;
    }

    public Image getImage() {
        return null;
    }

    public String getMessage() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public void performHelp() {
    }

    public void setDescription(String arg0) {
    }

    public void setImageDescriptor(ImageDescriptor arg0) {
    }

    public void setTitle(String arg0) {
    }

    public void setVisible(boolean arg0) {
    }

}