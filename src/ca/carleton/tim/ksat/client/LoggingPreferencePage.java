package ca.carleton.tim.ksat.client;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class LoggingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String LOGGING_PREFKEY = "enableConsoleLog";
    
    public LoggingPreferencePage() {
        super(GRID);
        setPreferenceStore(PlatformUI.getPreferenceStore());
    }
    
    public void createFieldEditors() {
        addField(new  
            BooleanFieldEditor(LOGGING_PREFKEY, "enable console log", getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }
    
}