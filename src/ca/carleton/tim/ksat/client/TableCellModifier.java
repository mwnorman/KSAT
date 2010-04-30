package ca.carleton.tim.ksat.client;

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