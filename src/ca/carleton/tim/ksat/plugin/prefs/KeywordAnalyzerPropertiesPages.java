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

import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class KeywordAnalyzerPropertiesPages implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench arg0) {
    }

    @Override
    public Point computeSize() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean okToLeave() {
        return false;
    }

    @Override
    public boolean performCancel() {
        return false;
    }

    @Override
    public boolean performOk() {
        return false;
    }

    @Override
    public void setContainer(IPreferencePageContainer arg0) {
    }

    @Override
    public void setSize(Point arg0) {
    }

    @Override
    public void createControl(Composite arg0) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Control getControl() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void performHelp() {
    }

    @Override
    public void setDescription(String arg0) {
    }

    @Override
    public void setImageDescriptor(ImageDescriptor arg0) {
    }

    @Override
    public void setTitle(String arg0) {
    }

    @Override
    public void setVisible(boolean arg0) {
    }
}