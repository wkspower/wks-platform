# Import Power Operational Hours - Implementation Summary

## Problem Identified
The POST endpoint `/assets/operational-hours/{financialYear}` was saving all operational hours to the `OperationalHours` table, regardless of asset type. Import power sources (identified by `assetType = "Rev Proc"`) should be saved to the `CPPImportPowerOperationalHours` table instead.

## Solution Implemented

### File Modified
`apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/service/PowerGenerationService.java`

### Method Updated
`setAssetOperationalHours(String financialYear, MasterAssetOperationalResponseDTO masterAssetOperationalResponseDTO)`

### Changes Made

#### 1. **Asset Type Detection**
Added conditional logic to identify import power sources:
```java
boolean isImportPowerSource = asset.getAssetType() != null && asset.getAssetType().equals("Rev Proc");
```

#### 2. **Conditional Routing**
Implemented two separate save paths:

**For PowerGenerationAssets (assetType != "Rev Proc"):**
- Saves to `OperationalHours` table via existing `repository.upsertOperationalHours()` method
- Updates remarks in `PowerGenerationAssets` table
- Uses existing financial month mapping

**For Import Power Sources (assetType == "Rev Proc"):**
- Saves to `CPPImportPowerOperationalHours` table
- Uses MERGE UPSERT statement for atomic insert/update
- Stores monthly operational hours (Apr-Mar) in direct columns
- Includes remarks field
- Uses `ImportPowerSource_FK_Id` (assetId) and `FinancialYear` for identification

#### 3. **SQL MERGE Statement**
Implemented atomic UPSERT operation for import power:
```sql
MERGE INTO CPPImportPowerOperationalHours AS target
USING (SELECT ? AS ImportPowerSource_FK_Id, ? AS FinancialYear) AS source
ON target.ImportPowerSource_FK_Id = source.ImportPowerSource_FK_Id
   AND target.FinancialYear = source.FinancialYear
WHEN MATCHED THEN UPDATE SET
  Apr = ?, May = ?, Jun = ?, Jul = ?, Aug = ?, Sep = ?, Oct = ?, Nov = ?, Dec = ?, Jan = ?, Feb = ?, Mar = ?, Remarks = ?
WHEN NOT MATCHED THEN INSERT
  (ImportPowerSource_FK_Id, FinancialYear, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Remarks)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```

### Data Flow

**Input (MasterAssetOperationalResponseDTO.powerResponse):**
- Array of AssetOperationalResponseDTO items
- Each item contains:
  - `assetId` (UUID) - Maps to ImportPowerSource_FK_Id for import sources
  - `assetType` ("Rev Proc" for imports, asset type name for others)
  - `assetName` (source name for imports)
  - Monthly operational hours (April through March)
  - `remarks` field

**Processing:**
1. Iterate through all power response items
2. Check assetType for each row
3. Build monthly data map from individual month fields
4. Route to appropriate table:
   - Import sources → CPPImportPowerOperationalHours
   - Assets → OperationalHours

**Output:**
- PowerGenerationAssets operational hours persisted to OperationalHours table
- Import power operational hours persisted to CPPImportPowerOperationalHours table
- Remarks updated for both asset types

### Database Tables Affected

#### OperationalHours (PowerGenerationAssets)
- `Asset_FK_Id` (UUID)
- `FinancialMonthId` (UUID)
- `OperationalHours` (double)
- Remarks updated via separate UPDATE statement

#### CPPImportPowerOperationalHours (Import Power Sources)
- `ImportPowerSource_FK_Id` (UUID) - From assetId
- `FinancialYear` (string)
- `Apr`, `May`, `Jun`, `Jul`, `Aug`, `Sep`, `Oct`, `Nov`, `Dec`, `Jan`, `Feb`, `Mar` (double values)
- `Remarks` (string)
- Updated via MERGE UPSERT

### Key Features

✅ **Asset Type Detection**: Uses `assetType == "Rev Proc"` as reliable detection mechanism  
✅ **Atomic Operations**: MERGE statement ensures single operation per import source record  
✅ **Backward Compatible**: Existing PowerGenerationAssets save logic unchanged  
✅ **Error Handling**: Preserves validation logic for month data  
✅ **No Check-Then-Act**: Uses MERGE instead of separate SELECT + INSERT/UPDATE  

### Testing Recommendations

1. **Mixed Asset Types**
   - Submit operational hours for both PowerGenerationAssets and import sources
   - Verify correct table saves for each type

2. **Import Power Updates**
   - Submit import power data
   - Update same import power record
   - Verify UPSERT correctly updates existing record

3. **Data Validation**
   - Confirm monthly hours sum correctly
   - Verify remarks persist for both asset types
   - Check financial year mapping

4. **GET Endpoint**
   - Confirm GET endpoint still returns both asset types correctly
   - Verify utility information still populated for import sources

### Related Components

**PowerGenerationController.java**
- POST endpoint at `/assets/operational-hours/{financialYear}`
- No changes needed - controller correctly passes MasterAssetOperationalResponseDTO to service

**PowerGenerationService.java - getAssetOperationalHours()**
- Already updated to fetch norm parameters for all asset types
- Identifies import sources by `assetType == "Rev Proc"`
- Returns mixed asset types in single response

**Database Schema**
- CPPImportPowerOperationalHours table structure assumed from implementation
- Requires columns: ImportPowerSource_FK_Id, FinancialYear, Apr-Mar, Remarks

### Status
✅ **IMPLEMENTATION COMPLETE**
- Code compiled without errors
- Ready for testing and deployment
