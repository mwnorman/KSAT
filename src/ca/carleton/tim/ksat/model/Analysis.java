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
package ca.carleton.tim.ksat.model;

import java.util.ArrayList;
import java.util.List;

import ca.carleton.tim.ksat.persist.AnalysisBuilder;

public class Analysis extends AnalysisBuilder {
	
	public int id;
	public String description;

	public List<Site> sites = new ArrayList<Site>();
	public List<KeywordExpression> expressions = new ArrayList<KeywordExpression>();
	public int expressionCount = 0;
	public List<AnalysisResult> results = new ArrayList<AnalysisResult>();
	
    public Analysis() {
        super();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<Site> getSites() {
        return sites;
    }
    public void setSites(List<Site> sites) {
        this.sites = sites;
    }
    public void addSite(Site site) {
        sites.add(site);
    }

    public List<KeywordExpression> getExpressions() {
        return expressions;
    }
    public void setExpressions(List<KeywordExpression> expressions) {
        this.expressions = expressions;
        this.expressionCount = expressions.size();
    }
    public void addKeywordExpression(KeywordExpression expression) {
        expressions.add(expression);
        expressionCount++;
    }
    public void removeKeywordExpression(KeywordExpression expression) {
        expressions.remove(expression);
        expressionCount--;
    }

    public int getExpressionCount() {
        return expressionCount;
    }
    public void setExpressionCount(int expressionCount) {
        this.expressionCount = expressionCount;
    }

    public List<AnalysisResult> getResults() {
        return results;
    }
    public void setResults(List<AnalysisResult> results) {
        this.results = results;
    }
    public void addAnalysisResult(AnalysisResult result) {
        results.add(result);
        result.setOwner(this);
    }

    @Override
    public String toString() {
        return "{" + id + "}Analysis(" + description + ")";
    }
	
}