package ca.carleton.tim.ksat.persist;

import ca.carleton.tim.ksat.model.Analysis;

public class AnalysisReport {

    private Analysis reportingAnalysis;

    public AnalysisReport() {
    }

    public Analysis getReportingAnalysis() {
        return reportingAnalysis;
    }
    public void setReportingAnalysis(Analysis reportingAnalysis) {
        this.reportingAnalysis = reportingAnalysis;
    }
    
}