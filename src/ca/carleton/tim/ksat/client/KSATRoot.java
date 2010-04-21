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
package ca.carleton.tim.ksat.client;

//javase imports
import java.util.ArrayList;
import java.util.List;

public class KSATRoot {
    
    // avoid double-locked init of singleton
    static class KSATRootHelper {
        static KSATRoot singleton = new KSATRoot();
    }
    public static KSATRoot defaultInstance() {
        return KSATRootHelper.singleton;
    }

    protected Object parent;
    protected List<AnalysisDatabase> databases = new ArrayList<AnalysisDatabase>();
    protected LogConsole logConsole;
    
    public KSATRoot() {
        super();
    }
    
    public Object getParent() {
        return parent;
    }
    public void setParent(Object parent) {
        this.parent = parent;
    }

    public LogConsole getLogConsole() {
        return logConsole;
    }
    public void setLogConsole(LogConsole logConsole) {
        this.logConsole = logConsole;
    }

    public List<AnalysisDatabase> getDatabases() {
        // TODO figure out persistent properties
        if (databases.isEmpty()) {
            AnalysisDatabase database1 = new AnalysisDatabase();
            addDatabase(database1);
            database1.buildDatabaseSession();
        }
        return databases;
    }
    public void addDatabase(AnalysisDatabase database) {
        if (database != null) {
            databases.add(database);
            database.setParent(this);
        }
    }
    public void removeDatabase(AnalysisDatabase database) {
        databases.remove(database);
        database.setParent(null);
    }

    @Override
    public String toString() {
        return "KSAT";
    }
    
}