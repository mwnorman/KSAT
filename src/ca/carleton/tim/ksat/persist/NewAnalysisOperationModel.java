/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, Mike Norman
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
import java.io.FileReader;
import static java.util.logging.Level.SEVERE;

//EclipseLink imports
import org.eclipse.persistence.sessions.UnitOfWork;

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

    public void build(AnalysisBuilder builder, UnitOfWork uow) {
        if (createTables) {
            AnalysisProjectTablesCreator aptc = new AnalysisProjectTablesCreator();
            try {
                aptc.dropTables(builder.getSession());
            }
            catch (Exception e) {
                // ignore
            }
            try {
                aptc.createTables(builder.getSession());
            }
            catch (Exception e) {
                // e.printStackTrace();
                builder.logMessage(SEVERE, "failure creating Analysis tables", e);
            }
        }
        Analysis newAnalysis = (Analysis)uow.registerNewObject(new Analysis());
        newAnalysis.setDescription(analysisDescription);
        try {
            BufferedReader input = new BufferedReader(new FileReader(sitesFilename));
            String line = null;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line != "" && !line.startsWith("#")) {
                    Site site = (Site)uow.registerNewObject(new Site(line, ""));
                    newAnalysis.addSite(site);
                }
            }
            input.close();
        }
        catch (Exception e) {
            builder.logMessage(SEVERE, "failure reading sites file", e);
        }
        try {
            BufferedReader input = new BufferedReader(new FileReader(expressionsFilename));
            String line = null;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line != "" && !line.startsWith("#")) {
                    KeywordExpression exp = (KeywordExpression)uow.registerNewObject(
                        new KeywordExpression());
                    exp.setExpression(line);
                    newAnalysis.addKeywordExpression(exp);
                }
            }
            input.close();
        }
        catch (Exception e) {
            builder.logMessage(SEVERE, "failure reading keyword expressions file", e);
        }
        uow.assignSequenceNumbers();
    }

}
