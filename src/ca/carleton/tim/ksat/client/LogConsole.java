package ca.carleton.tim.ksat.client;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class LogConsole extends MessageConsole {

    protected MessageConsoleStream messageStream;
    
    public LogConsole(String name, ImageDescriptor imageDescriptor) {
        super(name, imageDescriptor);
        messageStream = newMessageStream();
    }

    public MessageConsoleStream getMessageStream() {
        return messageStream;
    }
    
}