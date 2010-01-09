package ca.carleton.tim.ksat.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Analysis {
	
	public Root root;
	
	public int id;
	public String description;

	public List<Site> sites = new ArrayList<Site>();
	public List<KeywordExpression> expressions = new ArrayList<KeywordExpression>(); 
	public Properties properties = new Properties();
	public List<AnalysisRun> runs = new ArrayList<AnalysisRun>();
	
}