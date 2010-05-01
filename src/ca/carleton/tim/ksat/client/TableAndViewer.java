package ca.carleton.tim.ksat.client;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

public class TableAndViewer {

    public Table table;
    public TableViewer tableViewer;
    public TableAndViewer(Table table, TableViewer tableViewer) {
        this.table = table;
        this.tableViewer = tableViewer;
    }
}
