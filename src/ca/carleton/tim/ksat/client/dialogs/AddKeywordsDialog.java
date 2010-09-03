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

//javase imports
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;

//Graphics (SWT/JFaces) imports
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

//KSAT domain imports
import ca.carleton.tim.ksat.client.KSATRoot;
import ca.carleton.tim.ksat.client.TableAndViewer;
import ca.carleton.tim.ksat.client.handlers.AddKeywordsHandler;
import ca.carleton.tim.ksat.client.views.KeywordsView;
import ca.carleton.tim.ksat.model.KeywordExpression;

public class AddKeywordsDialog extends Dialog {
	
	public static final String AVAILABLE_LABEL = "Available Keyword Expressions:";
	public static final String IMPORT_FROM_FILE_LABEL = "Import from file:";
	public static final String ENTER_NEW_LABEL = "New Keyword Expression:";
	public static final String ENTER_NEW = "enter new keyword expression";

    protected CheckboxTableViewer tableViewer;
    protected Table table;
    protected List<KeywordExpression> allKeywordsFromDB;
    protected HashSet<KeywordExpression> additionalKeywords;
    protected AddKeywordsHandler addKeywordsHandler;
    private DatabaseSession session;
    
    public AddKeywordsDialog(Shell parent) {
        super(parent);
    }
    
    public AddKeywordsDialog(Shell activeShell, AddKeywordsHandler addKeywordsHandler,
        List<KeywordExpression> allKeywordsFromDB, HashSet<KeywordExpression> additionalKeywords) {
        this(activeShell);
        this.addKeywordsHandler = addKeywordsHandler;
        this.allKeywordsFromDB = allKeywordsFromDB;
        this.additionalKeywords = additionalKeywords;
        init();
    }

    protected void init() {
        session = KSATRoot.defaultInstance().getCurrentSession();
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.RESIZE | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite outerContainer = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        outerContainer.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.grabExcessHorizontalSpace = true;
        outerContainer.setLayoutData(data);
        new Label(outerContainer, SWT.NONE).setText(AVAILABLE_LABEL);
        TableAndViewer tableAndViewer = KeywordsView.buildTable(outerContainer, null, true, session);
        table = tableAndViewer.table;
        tableViewer = (CheckboxTableViewer) tableAndViewer.tableViewer;
        for (KeywordExpression ke : additionalKeywords) {
            tableViewer.add(ke);
            tableViewer.setChecked(ke, true);
            table.setTopIndex(table.getItemCount());
        }
        Composite buttonComposite = new Composite(outerContainer, SWT.NONE);
        FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        fillLayout.spacing = 10;
        buttonComposite.setLayout(fillLayout);
        Button addButton = new Button(buttonComposite, SWT.PUSH);
        addButton.setText(IMPORT_FROM_FILE_LABEL);
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(),
                    SWT.OPEN);
                String sitesFileName = fileDialog.open();
                try {
                    FileReader sitesReader = new FileReader(sitesFileName);
                    BufferedReader input = new BufferedReader(sitesReader);
                    String line = null;
                    while ((line = input.readLine()) != null) {
                        line = line.trim();
                        if (line != "" && !line.startsWith("#")) {
                            // already in database ?
                            KeywordExpression keFromDb = scanForExistingExpression(line, allKeywordsFromDB);
                            if (keFromDb == null) {
                                KeywordExpression newExpression = new KeywordExpression();
                                // don't URL encode - accept as is
                                newExpression.setExpression(line);
                                tableViewer.add(newExpression);
                                tableViewer.setChecked(newExpression, true);
                            }
                        }
                    }
                    table.setTopIndex(table.getItemCount());
                    input.close();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        new Label(outerContainer, SWT.NONE).setText(ENTER_NEW_LABEL);
        final Text text = new Text(outerContainer, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText(ENTER_NEW);
        text.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                String newExpText = text.getText();
                if (!ENTER_NEW.equals(newExpText)) {
                    try {
						KeywordExpression newExpression = new KeywordExpression();
                        // have to encode line
                        String encLine = URLEncoder.encode(newExpText, "UTF-8");
						newExpression.setExpression(encLine);
						tableViewer.add(newExpression);
						tableViewer.setChecked(newExpression, true);
						table.setTopIndex(table.getItemCount());
					}
                    catch (Exception e1) {
						e1.printStackTrace();
					}
                }
            }
        });
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    e.detail = SWT.TRAVERSE_NONE;
                }
            }
        });
        outerContainer.pack();
        return outerContainer;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) { //Ok
            Object[] checkedElements = tableViewer.getCheckedElements();
            for (Object checkedElement : checkedElements) {
                addKeywordsHandler.addSelectedExpression((KeywordExpression)checkedElement);
            }
        }
        super.buttonPressed(buttonId);
    }

    protected KeywordExpression scanForExistingExpression(String line, 
    	List<KeywordExpression> allExpressions) {
    	
    	KeywordExpression foundExpression = null;
    	String dLine;
		try {
			dLine = URLDecoder.decode(line, "UTF-8");
		}catch (UnsupportedEncodingException e) {
			dLine = line;		
		}
        for (KeywordExpression expression : allExpressions) {
        	String expr = expression.getExpression();
        	if (expr.equals(line) || expr.equals(dLine) ) {
                foundExpression = expression;
                break;
            }
        }
        return foundExpression;
    }
  
}