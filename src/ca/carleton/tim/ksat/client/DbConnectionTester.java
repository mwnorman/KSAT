package ca.carleton.tim.ksat.client;

import org.eclipse.core.expressions.PropertyTester;

public class DbConnectionTester extends PropertyTester {

	public DbConnectionTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		AnalysisDatabase database = (AnalysisDatabase)receiver;
		if ("isConnected".equals(property)) {
			return database.isConnected();
		}
		return false;
	}

}
