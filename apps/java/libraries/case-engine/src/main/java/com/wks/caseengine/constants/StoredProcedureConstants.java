package com.wks.caseengine.constants;

public class StoredProcedureConstants {

    
    public static final String MEG_LOAD_MC_VALUES = """
        EXEC MEG_LoadMCValues
        @plantId = :plantId,
        @siteId = :siteId,
        @verticalId = :verticalId,
        @finYear = :finYear
    """;
}
