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
package ca.carleton.tim.ksat.persist;

//javase imports
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Vector;
import static java.util.logging.Level.SEVERE;

//EclipseLink imports
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

//KSAT imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;

public class NewAnalysisOperationModel extends AnalysisOperationModel {

    protected String sitesFilename;
    protected String expressionsFilename;
    protected boolean createTables = false;
    protected String analysisDescription;
    
    public NewAnalysisOperationModel() {
        super();
    }

    public String getSitesFilename() {
        return sitesFilename;
    }
    public void setSitesFilename(String sitesFilename) {
        this.sitesFilename = sitesFilename;
    }

    public String getExpressionsFilename() {
        return expressionsFilename;
    }
    public void setExpressionsFilename(String expressionsFilename) {
        this.expressionsFilename = expressionsFilename;
    }

    public boolean createTables() {
        return createTables;
    }
    public void setCreateTables(boolean createTables) {
        this.createTables = createTables;
    }

    public String getAnalysisDescription() {
        return analysisDescription;
    }
    public void setAnalysisDescription(String analysisDescription) {
        this.analysisDescription = analysisDescription;
    }

    
    @SuppressWarnings("unchecked")
    public void build(AnalysisBuilder builder, UnitOfWork uow) {
        SchemaManager schemaManager = new SchemaManager(builder.getSession());
        if (createTables) {
            try {
                schemaManager.replaceDefaultTables(true, true);
            }
            catch (Exception e) {
                builder.logMessage(SEVERE, "failure re-creating Analysis tables", e);
            }
        }
        Vector<Site> allSites = uow.readAllObjects(Site.class);
        Vector<KeywordExpression> allExpressions = uow.readAllObjects(KeywordExpression.class);
        Analysis newAnalysis = (Analysis)uow.registerNewObject(new Analysis());
        uow.assignSequenceNumber(newAnalysis);
        newAnalysis.setDescription(analysisDescription);
        try {
            FileReader sitesReader = new FileReader(sitesFilename);
            FileReader expressionsReader = new FileReader(expressionsFilename);
            build(builder, uow, allSites, allExpressions, newAnalysis, sitesReader, expressionsReader);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void build(AnalysisBuilder builder, UnitOfWork uow, Vector<Site> allSites,
        Vector<KeywordExpression> allExpressions, Analysis newAnalysis, Reader sitesReader,
        Reader expressionsReader) {
        try {
            BufferedReader input = new BufferedReader(sitesReader);
            String line = null;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line != "" && !line.startsWith("#")) {
                    // already in database ?
                    Site siteFromDb = scanForExistingSite(line, allSites);
                    if (siteFromDb == null) {
                        // already in memory ?
                        Site siteFromMem = scanForExistingSite(line, newAnalysis.getSites());
                        if (siteFromMem == null) {
                            // create new site
                            Site newSite = (Site)uow.registerNewObject(new Site(line, ""));
                            uow.assignSequenceNumber(newSite);
                            newAnalysis.addSite(newSite);
                        }
                    }
                    else {
                        // already in memory ?
                        Site siteFromMem = scanForExistingSite(line, newAnalysis.getSites());
                        if (siteFromMem == null) {
                            // only add if it is not already in the list
                            newAnalysis.addSite(siteFromDb);
                        }
                    }
                }
            }
            input.close();
        }
        catch (Exception e) {
            builder.logMessage(SEVERE, "failure reading sites file", e);
        }
        try {
            BufferedReader input = new BufferedReader(expressionsReader);
            String line = null;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line != "" && !line.startsWith("#")) {
                    // already in database ?
                    KeywordExpression expFromDb = scanForExistingExpression(line, allExpressions);
                    if (expFromDb == null) {
                        // already in memory ?
                        KeywordExpression expFromMem = scanForExistingExpression(line,
                            newAnalysis.getExpressions());
                        if (expFromMem == null) {
                            // create new expression
                            KeywordExpression newExp = 
                                (KeywordExpression)uow.registerNewObject(new KeywordExpression());
                            newExp.setExpression(line);
                            uow.assignSequenceNumber(newExp);
                            newAnalysis.addKeywordExpression(newExp);
                        }
                    }
                    else {
                        // already in memory ?
                        KeywordExpression expFromMem = scanForExistingExpression(line,
                            newAnalysis.getExpressions());
                        if (expFromMem == null) {
                            // only add if it is not already in the list
                            newAnalysis.addKeywordExpression(expFromDb);
                        }
                    }
                }
            }
            input.close();
        }
        catch (Exception e) {
            builder.logMessage(SEVERE, "failure reading keyword expressions file", e);
        }
    }

    protected Site scanForExistingSite(String line, List<Site> allSites) {
        Site foundSite = null;
        for (Site site : allSites) {
            if (site.getUrl().equals(line)) {
                foundSite = site;
                break;
            }
        }
        return foundSite;
    }

    protected KeywordExpression scanForExistingExpression(String line, List<KeywordExpression> allKExpressions) {
        KeywordExpression foundExpression = null;
        for (KeywordExpression expression : allKExpressions) {
            if (expression.getExpression().equals(line)) {
                foundExpression = expression;
                break;
            }
        }
        return foundExpression;
    }

}