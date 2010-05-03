package ca.carleton.tim.ksat.client;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

public class CreateNewAnalysisDialog extends InputDialog {

	public static final String ENTER_NEW_LABEL = "New Analysis:";
	public static final String ENTER_NEW = "new Analysis name";
	
	protected CreateNewAnalysisHandler newAnalysisHandler;
	
	public CreateNewAnalysisDialog(Shell activeShell, String dialogTitle,
            String dialogMessage, String initialValue, CreateNewAnalysisHandler newAnalysisHandler) {
		super(activeShell, dialogTitle, dialogMessage, initialValue, null);
		this.newAnalysisHandler = newAnalysisHandler;
	}

	
}