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
package ca.carleton.tim.ksat.client.views;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

//KSAT domain imports
import ca.carleton.tim.ksat.client.AnalysisDatabase;
import ca.carleton.tim.ksat.client.KSATApplication;
import ca.carleton.tim.ksat.client.KSATRoot;
import ca.carleton.tim.ksat.client.ResultAdapter;

public class AnalysesLabelProvider extends LabelProvider {

    @Override
    public String getText(Object obj) {
        String text = "";
        if (obj != null) {
            text = obj.toString();
        }
        return text;
    }

    @Override
    public Image getImage(Object element) {
        String imageKey = "analyses";
        if (element instanceof ResultAdapter) {
            imageKey = "report";
        }
        else if (element == KSATRoot.defaultInstance()) {
            imageKey = "root";
        }
        else if (element instanceof AnalysisDatabase) {
            AnalysisDatabase database = (AnalysisDatabase)element;
            if (database.isConnected()) {
                imageKey = "connecteddb";
            }
            else {
                imageKey = "disconnecteddb";
            }
        }
        return KSATApplication.IMAGE_REGISTRY.get(imageKey);
    }

}