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

//EclipseLink imports
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;
import org.eclipse.persistence.tools.schemaframework.ForeignKeyConstraint;
import org.eclipse.persistence.tools.schemaframework.TableCreator;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

public class AnalysisProjectTablesCreator extends TableCreator {

    public AnalysisProjectTablesCreator() {
        setName("AnalysisProject");

        addTableDefinition(buildKSAT_ANALYSIS_TABLE());
        addTableDefinition(buildKSAT_KEYWORD_TABLE());
        addTableDefinition(buildKSAT_SITE_TABLE());
        addTableDefinition(buildKSAT_RESULT_TABLE());
        addTableDefinition(buildKSAT_SEQUENCE_TABLE());
    }

    protected TableDefinition buildKSAT_ANALYSIS_TABLE() {
        TableDefinition table = new TableDefinition();
        table.setName("KSAT_ANALYSIS_TABLE");
        
        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("integer");
        fieldID.setSize(0);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        FieldDefinition fieldDESCRIPT = new FieldDefinition();
        fieldDESCRIPT.setName("DESCRIPT");
        fieldDESCRIPT.setTypeName("varchar");
        fieldDESCRIPT.setSize(40);
        fieldDESCRIPT.setSubSize(0);
        fieldDESCRIPT.setIsPrimaryKey(false);
        fieldDESCRIPT.setIsIdentity(false);
        fieldDESCRIPT.setUnique(false);
        fieldDESCRIPT.setShouldAllowNull(true);
        table.addField(fieldDESCRIPT);

        FieldDefinition fieldKWRDCOUNT = new FieldDefinition();
        fieldKWRDCOUNT.setName("KWRDCOUNT");
        fieldKWRDCOUNT.setTypeName("integer");
        fieldKWRDCOUNT.setSize(0);
        fieldKWRDCOUNT.setSubSize(0);
        fieldKWRDCOUNT.setIsPrimaryKey(false);
        fieldKWRDCOUNT.setIsIdentity(false);
        fieldKWRDCOUNT.setUnique(false);
        fieldKWRDCOUNT.setShouldAllowNull(true);
        table.addField(fieldKWRDCOUNT);

        return table;
    }

    protected TableDefinition buildKSAT_KEYWORD_TABLE() {
        TableDefinition table = new TableDefinition();
        table.setName("KSAT_KEYWORD_TABLE");
        
        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("integer");
        fieldID.setSize(0);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        FieldDefinition fieldEXPRESSION = new FieldDefinition();
        fieldEXPRESSION.setName("EXPRESSION");
        fieldEXPRESSION.setTypeName("varchar");
        fieldEXPRESSION.setSize(2000);
        fieldEXPRESSION.setSubSize(0);
        fieldEXPRESSION.setIsPrimaryKey(false);
        fieldEXPRESSION.setIsIdentity(false);
        fieldEXPRESSION.setUnique(false);
        fieldEXPRESSION.setShouldAllowNull(false);
        table.addField(fieldEXPRESSION);
        
        FieldDefinition fieldANALYSIS_ID = new FieldDefinition();
        fieldANALYSIS_ID.setName("ANALYSIS_ID");
        fieldANALYSIS_ID.setTypeName("integer");
        fieldANALYSIS_ID.setSize(0);
        fieldANALYSIS_ID.setSubSize(0);
        fieldEXPRESSION.setIsPrimaryKey(false);
        fieldEXPRESSION.setIsIdentity(false);
        fieldEXPRESSION.setUnique(false);
        fieldEXPRESSION.setShouldAllowNull(false);
        table.addField(fieldANALYSIS_ID);

        ForeignKeyConstraint foreignKeyKEYWORD_ANALYSIS_REF = new ForeignKeyConstraint();
        foreignKeyKEYWORD_ANALYSIS_REF.setName("KEYWORD_ANALYSIS_REF");
        foreignKeyKEYWORD_ANALYSIS_REF.setTargetTable("KSAT_ANALYSIS_TABLE");
        foreignKeyKEYWORD_ANALYSIS_REF.addSourceField("ANALYSIS_ID");
        foreignKeyKEYWORD_ANALYSIS_REF.addTargetField("ID");
        table.addForeignKeyConstraint(foreignKeyKEYWORD_ANALYSIS_REF);
        
        return table;
    }
    
    protected TableDefinition buildKSAT_SITE_TABLE() {
        TableDefinition table = new TableDefinition();
        table.setName("KSAT_SITE_TABLE");
        
        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("integer");
        fieldID.setSize(0);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        FieldDefinition fieldDESCRIPT = new FieldDefinition();
        fieldDESCRIPT.setName("DESCRIPT");
        fieldDESCRIPT.setTypeName("varchar");
        fieldDESCRIPT.setSize(40);
        fieldDESCRIPT.setSubSize(0);
        fieldDESCRIPT.setIsPrimaryKey(false);
        fieldDESCRIPT.setIsIdentity(false);
        fieldDESCRIPT.setUnique(false);
        fieldDESCRIPT.setShouldAllowNull(true);
        table.addField(fieldDESCRIPT);
        
        FieldDefinition fieldURL = new FieldDefinition();
        fieldURL.setName("URL");
        fieldURL.setTypeName("varchar");
        fieldURL.setSize(2000);
        fieldURL.setSubSize(0);
        fieldURL.setIsPrimaryKey(false);
        fieldURL.setIsIdentity(false);
        fieldURL.setUnique(false);
        fieldURL.setShouldAllowNull(false);
        table.addField(fieldURL);

        FieldDefinition fieldANALYSIS_ID = new FieldDefinition();
        fieldANALYSIS_ID.setName("ANALYSIS_ID");
        fieldANALYSIS_ID.setTypeName("integer");
        fieldANALYSIS_ID.setSize(0);
        fieldANALYSIS_ID.setSubSize(0);
        fieldANALYSIS_ID.setIsPrimaryKey(false);
        fieldANALYSIS_ID.setIsIdentity(false);
        fieldANALYSIS_ID.setUnique(false);
        fieldANALYSIS_ID.setShouldAllowNull(false);
        table.addField(fieldANALYSIS_ID);
        
        ForeignKeyConstraint foreignKeySITE_ANALYSIS_REF = new ForeignKeyConstraint();
        foreignKeySITE_ANALYSIS_REF.setName("SITE_ANALYSIS_REF");
        foreignKeySITE_ANALYSIS_REF.setTargetTable("KSAT_ANALYSIS_TABLE");
        foreignKeySITE_ANALYSIS_REF.addSourceField("ANALYSIS_ID");
        foreignKeySITE_ANALYSIS_REF.addTargetField("ID");
        table.addForeignKeyConstraint(foreignKeySITE_ANALYSIS_REF);

        return table;
    }

    protected TableDefinition buildKSAT_RESULT_TABLE() {
        TableDefinition table = new TableDefinition();
        table.setName("KSAT_RESULT_TABLE");
        
        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("integer");
        fieldID.setSize(0);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);
        
        FieldDefinition fieldRUN_DATE = new FieldDefinition();
        fieldRUN_DATE.setName("RUN_DATE");
        fieldRUN_DATE.setTypeName("datetime");
        fieldRUN_DATE.setSize(0);
        fieldRUN_DATE.setSubSize(0);
        fieldRUN_DATE.setIsPrimaryKey(false);
        fieldRUN_DATE.setIsIdentity(false);
        fieldRUN_DATE.setUnique(false);
        fieldRUN_DATE.setShouldAllowNull(false);
        table.addField(fieldRUN_DATE);

        FieldDefinition fieldRAW_RESULT = new FieldDefinition();
        fieldRAW_RESULT.setName("RAW_RESULT");
        fieldRAW_RESULT.setTypeName("blob");
        fieldRAW_RESULT.setSize(0);
        fieldRAW_RESULT.setSubSize(0);
        fieldRAW_RESULT.setIsPrimaryKey(false);
        fieldRAW_RESULT.setIsIdentity(false);
        fieldRAW_RESULT.setUnique(false);
        fieldRAW_RESULT.setShouldAllowNull(true);
        table.addField(fieldRAW_RESULT);
        
        FieldDefinition fieldANALYSIS_ID = new FieldDefinition();
        fieldANALYSIS_ID.setName("ANALYSIS_ID");
        fieldANALYSIS_ID.setTypeName("integer");
        fieldANALYSIS_ID.setSize(0);
        fieldANALYSIS_ID.setSubSize(0);
        fieldANALYSIS_ID.setIsPrimaryKey(false);
        fieldANALYSIS_ID.setIsIdentity(false);
        fieldANALYSIS_ID.setUnique(false);
        fieldANALYSIS_ID.setShouldAllowNull(false);
        table.addField(fieldANALYSIS_ID);

        ForeignKeyConstraint foreignKeyRESULTS_ANALYSIS_REF = new ForeignKeyConstraint();
        foreignKeyRESULTS_ANALYSIS_REF.setName("RESULTS_ANALYSIS_REF");
        foreignKeyRESULTS_ANALYSIS_REF.setTargetTable("KSAT_ANALYSIS_TABLE");
        foreignKeyRESULTS_ANALYSIS_REF.addSourceField("ANALYSIS_ID");
        foreignKeyRESULTS_ANALYSIS_REF.addTargetField("ID");
        table.addForeignKeyConstraint(foreignKeyRESULTS_ANALYSIS_REF);
        
        return table;
    }

    protected TableDefinition buildKSAT_SEQUENCE_TABLE() {
        TableDefinition table = new TableDefinition();
        table.setName("KSAT_SEQUENCE_TABLE");
        
        FieldDefinition fieldSEQ_NAME = new FieldDefinition();
        fieldSEQ_NAME.setName("SEQ_NAME");
        fieldSEQ_NAME.setTypeName("varchar");
        fieldSEQ_NAME.setSize(40);
        fieldSEQ_NAME.setSubSize(0);
        fieldSEQ_NAME.setIsPrimaryKey(false);
        fieldSEQ_NAME.setIsIdentity(false);
        fieldSEQ_NAME.setUnique(false);
        fieldSEQ_NAME.setShouldAllowNull(false);
        table.addField(fieldSEQ_NAME);
        
        FieldDefinition fieldSEQ_COUNT = new FieldDefinition();
        fieldSEQ_COUNT.setName("SEQ_COUNT");
        fieldSEQ_COUNT.setTypeName("bigint");
        fieldSEQ_COUNT.setSize(0);
        fieldSEQ_COUNT.setSubSize(0);
        fieldSEQ_COUNT.setIsPrimaryKey(false);
        fieldSEQ_COUNT.setIsIdentity(false);
        fieldSEQ_COUNT.setUnique(false);
        fieldSEQ_COUNT.setShouldAllowNull(false);
        table.addField(fieldSEQ_COUNT);
        
        return table;
    }
}
