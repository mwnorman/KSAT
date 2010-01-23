package ca.carleton.tim.ksat.persist;

import java.util.List;

import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;

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
    
    public int getAnalysisId() {
        return reportingAnalysis.getId();
    }
    
    public String getAnalysisDescription() {
        return reportingAnalysis.getDescription();
    }
    
    public List<Site> getAnalysisSites() {
        return reportingAnalysis.getSites();
    }

    public List<KeywordExpression> getAnalysisKeywords() {
        return reportingAnalysis.getExpressions();
    }
    
}