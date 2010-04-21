package ca.carleton.tim.ksat.client;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class CreateNewDatabaseHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        MessageDialog.openInformation(Display.getDefault().getActiveShell(),
            "Cannot perform command", "Cannot (yet) Create New Database");
        return null;
    }

}
