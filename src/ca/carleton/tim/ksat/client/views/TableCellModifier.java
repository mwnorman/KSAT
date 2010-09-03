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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.swt.widgets.TableItem;

import ca.carleton.tim.ksat.model.Site;

public class TableCellModifier implements ICellModifier {
    
    protected List<String> headings = null;
    protected final TableViewer tableViewer;
    protected final DatabaseSession currentSession;
    
    public TableCellModifier(String[] columnHeadings, TableViewer tableViewer,
        DatabaseSession currentSession) {
        this.tableViewer = tableViewer;
        this.currentSession = currentSession;
        headings = Arrays.asList(columnHeadings);
    }
    public boolean canModify(Object element, String property) {
          return true;
    }
    public Object getValue(Object element, String property) {
          Object result = null;
          Site site = (Site)element;
          int columnIndex = headings.indexOf(property);
          switch (columnIndex) {
          case 0:
                result = site.getUrl();
                break;
          case 1:
                result = site.getDescription();
                break;
          }
          return result;
    }
    public void modify(Object element, String property, Object value) {
          int columnIndex = headings.indexOf(property);
          TableItem tableItem = (TableItem)element;
          Site site = (Site)tableItem.getData();
          if (columnIndex == 1) {
                String v = (String)value;
                if (currentSession != null) {
                    UnitOfWork uow = currentSession.acquireUnitOfWork();
                    Site siteClone = (Site)uow.registerObject(site);
                    siteClone.setDescription(v);
                    uow.commit();
                }
          }
          tableViewer.update(site, null);
    }
}