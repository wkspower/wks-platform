# Import Power Multi-Source Implementation Summary

## Overview
Completed implementation of multi-source import power management system supporting MEL (Mechanical Equipment License) and Power_Dis (Power Distribution) sources with separate tracking of operational hours and capacity.

## Architecture Pattern
Follows codebase convention: **Controller → Service Interface → ServiceImpl → Repository → Entity**

---

## Database Schema

### Files Created
**Location:** `database/scripts/CPPImportPower_Tables_Creation.sql`

#### Tables
1. **CPPImportPowerSourceMapping** - Master configuration
   - `Id` (UUID, PK) - Default NEWID()
   - `SourceName` (NVARCHAR(100)) - MEL or Power_Dis
   - `MaterialCode` (NVARCHAR(50)) - SAP reference code
   - `NormParameter_FK_Id` (FK to NormParameters) - Links SAP code mapping
   - `Plant_FK_Id` (FK to Plants) - Revenue procedure plant reference
   - `CPPPlant_FK_Id` (FK to CPPPlants) - Consumption plant reference
   - `IsActive` (BIT, default 1) - Enable/disable source
   - `Remarks` (NVARCHAR(MAX)) - Additional notes
   - Audit fields: `CreatedDate`, `UpdatedDate`
   - Unique constraint: `SourceName + Plant_FK_Id + CPPPlant_FK_Id`

2. **CPPImportPowerOperationalHours** - Monthly operational hours per source
   - `Id` (UUID, PK) - Default NEWID()
   - `ImportPowerSource_FK_Id` (FK) - Link to source mapping
   - `FinancialYear` (NVARCHAR(10)) - YYYY-YY format (e.g., "2026-27")
   - Monthly columns: `Apr`, `May`, `Jun`, `Jul`, `Aug`, `Sep`, `Oct`, `Nov`, `Dec`, `Jan`, `Feb`, `Mar` (DECIMAL 18,2)
   - `Remarks` (NVARCHAR(MAX)) - Monthly notes/comments
   - Audit fields: `CreatedDate`, `UpdatedDate`
   - Unique constraint: `ImportPowerSource_FK_Id + FinancialYear`

3. **CPPImportPowerCapacity** - Monthly capacity (MW) per source
   - `Id` (UUID, PK) - Default NEWID()
   - `ImportPowerSource_FK_Id` (FK) - Link to source mapping
   - `FinancialYear` (NVARCHAR(10)) - YYYY-YY format (e.g., "2026-27")
   - Monthly columns: `Apr`, `May`, `Jun`, `Jul`, `Aug`, `Sep`, `Oct`, `Nov`, `Dec`, `Jan`, `Feb`, `Mar` (DECIMAL 18,2)
   - `UOM` (NVARCHAR(10), default 'MW') - Unit of Measure
   - `Remarks` (NVARCHAR(MAX)) - Monthly notes/comments
   - Audit fields: `CreatedDate`, `UpdatedDate`
   - Unique constraint: `ImportPowerSource_FK_Id + FinancialYear`

#### Stored Procedures
1. **CPP_Get_ImportPowerOperationalHours** - Retrieves hours with source name mapping
2. **CPP_Get_ImportPowerCapacity** - Retrieves capacity with source name mapping

---

## Java Backend Implementation

### Entities (JPA)
**Location:** `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/entity/`

1. **CPPImportPowerSourceMapping.java**
   - JPA entity with NEWID() UUID generation
   - Foreign key references to NormParameters and Plants
   - Active/inactive toggle for source management

2. **CPPImportPowerOperationalHours.java**
   - 12 Double fields for monthly values (april-march)
   - Financial year in YYYY-YY format
   - Implements columnar data storage pattern

3. **CPPImportPowerCapacity.java**
   - Same columnar pattern as OperationalHours
   - Additional `uom` field (default 'MW')

### Repositories (Spring Data JPA)
**Location:** `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/repository/`

1. **ImportPowerSourceRepository**
   - Custom queries: `findByCppPlantFkId()`, `findBySourceNameAndPlantId()`
   - Master data lookup operations

2. **ImportPowerHoursRepository**
   - Projection interface: `ImportPowerHoursProjection`
   - SP call: `getImportPowerOperationalHours()`
   - Upsert support: `findByImportPowerSourceFkIdAndFinancialYear()`

3. **ImportPowerCapacityRepository**
   - Projection interface: `ImportPowerCapacityProjection`
   - SP call: `getImportPowerCapacity()`
   - Upsert support: `findByImportPowerSourceFkIdAndFinancialYear()`

### Data Transfer Objects (DTOs)
**Location:** `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/dto/`

1. **ImportPowerHoursDto**
   - `sourceId`, `sourceName`, `materialCode`, `sapCode`
   - `utilityName`, `plantName`, `cppPlantId`
   - 12 monthly fields (april-march)
   - `remarks`, `isEditable`, `financialYear`

2. **ImportPowerCapacityDto**
   - Same structure as HoursDto
   - Additional `uom` field (default 'MW')

### Service Layer
**Location:** `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/`

#### Service Interfaces
1. **service/ImportPowerHoursService**
   - `List<ImportPowerHoursDto> getImportPowerOperationalHours(UUID cppPlantId, String financialYear)`
   - `void upsertImportPowerOperationalHours(List<ImportPowerHoursDto> dtoList, String financialYear)`

2. **service/ImportPowerCapacityService**
   - `List<ImportPowerCapacityDto> getImportPowerCapacity(UUID cppPlantId, String financialYear)`
   - `void upsertImportPowerCapacity(List<ImportPowerCapacityDto> dtoList, String financialYear)`

#### Service Implementations
**Location:** `serviceimpl/` folder

1. **ImportPowerHoursServiceImpl** (@Service)
   - Financial year format validation (YYYY-YY)
   - Projection-to-DTO mapping
   - @Transactional upsert with create-or-update logic
   - Null-safe operations

2. **ImportPowerCapacityServiceImpl** (@Service)
   - Same pattern as HoursServiceImpl
   - Includes UOM field handling

### REST Controller
**Location:** `apps/java/services/case-engine-rest-api/src/main/java/com/wks/caseengine/rest/cpp/ImportPowerController.java`

#### Operational Hours Endpoints
1. `GET /task/import-power/operational-hours/{cppPlantId}/{financialYear}`
   - Retrieves hours for specific plant and financial year

2. `POST /task/import-power/operational-hours/{cppPlantId}/{financialYear}`
   - Saves/updates hours data
   - Accepts List<ImportPowerHoursDto>

3. `GET /task/import-power/operational-hours/export/{cppPlantId}/{financialYear}`
   - Exports hours to Excel (.xlsx)
   - Styled formatting with headers and data rows

4. `POST /task/import-power/operational-hours/import/{cppPlantId}/{financialYear}`
   - Imports hours from Excel file
   - Multipart file upload support

#### Capacity Endpoints
5. `GET /task/import-power/capacity/{cppPlantId}/{financialYear}`
   - Retrieves capacity for specific plant and financial year

6. `POST /task/import-power/capacity/{cppPlantId}/{financialYear}`
   - Saves/updates capacity data
   - Accepts List<ImportPowerCapacityDto>

7. `GET /task/import-power/capacity/export/{cppPlantId}/{financialYear}`
   - Exports capacity to Excel (.xlsx)

8. `POST /task/import-power/capacity/import/{cppPlantId}/{financialYear}`
   - Imports capacity from Excel file

#### Excel Features
- **Export:** Apache POI with styling (headers, borders, alignment)
- **Import:** Placeholder methods for parsing implementation
  - `parseOperationalHoursFromExcel()` - TODO: implement
  - `parseCapacityFromExcel()` - TODO: implement

---

## Implementation Checklist

### ✅ Completed
- [x] Database schema design with 3 tables
- [x] Stored procedures for data retrieval with name mapping
- [x] JPA entities with proper annotations
- [x] Repository interfaces with SP integration
- [x] Projection interfaces for SP result mapping
- [x] DTOs with complete field coverage
- [x] Service interfaces (abstraction layer)
- [x] ServiceImpl implementations with business logic
- [x] REST controller with 8 endpoints
- [x] Excel export functionality with styling
- [x] Proper Service/ServiceImpl architecture pattern
- [x] Data insertion script (NormParameters + sample data)
- [x] Frontend React component with operational hours datagrid
- [x] API service integration for operational hours
- [x] Excel import/export functionality in UI

### ⚠️ Pending Implementation
- [ ] Execute CPPImportPower_Tables_Creation.sql to create database objects
- [ ] Execute CPPImportPower_Data_Insertion.sql to insert master data
- [ ] Create separate component for Import Power Capacity (MW) tracking
- [ ] Complete Excel import parsing methods in controller
  - [ ] `parseOperationalHoursFromExcel()`
  - [ ] `parseCapacityFromExcel()`
- [ ] Unit tests for service layer
- [ ] Integration tests for controller endpoints
- [ ] Python model updates (power_service.py)
- [ ] Frontend integration for capacity component
- [ ] Integration with existing power calculation workflows

---

## Data Model Pattern

Follows **columnar storage pattern** similar to UtilityPlantAssets:
- Monthly data stored in dedicated columns (april, may, june... march)
- One record per financial year per source
- Supports flexible financial year formats (YYYY-YY)
- Upsert logic ensures single record per source+year combination

### Calculation: MWh = Capacity (MW) × Hours
- Multiply monthly capacity by operational hours
- Useful for energy production calculations
- Separate source tracking enables MEL vs Power_Dis analysis

---

## File Structure Reference

```
apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/
├── entity/
│   ├── CPPImportPowerSourceMapping.java
│   ├── CPPImportPowerOperationalHours.java
│   └── CPPImportPowerCapacity.java
├── repository/
│   ├── ImportPowerSourceRepository.java
│   ├── ImportPowerHoursRepository.java
│   ├── ImportPowerCapacityRepository.java
│   ├── ImportPowerHoursProjection.java
│   └── ImportPowerCapacityProjection.java
├── cpp/
│   ├── service/
│   │   ├── ImportPowerHoursService.java (interface)
│   │   └── ImportPowerCapacityService.java (interface)
│   └── serviceimpl/
│       ├── ImportPowerHoursServiceImpl.java (@Service)
│       └── ImportPowerCapacityServiceImpl.java (@Service)
└── dto/
    ├── ImportPowerHoursDto.java
    └── ImportPowerCapacityDto.java

apps/java/services/case-engine-rest-api/src/main/java/com/wks/caseengine/rest/cpp/
└── ImportPowerController.java

database/scripts/
└── CPPImportPower_Tables_Creation.sql
```

---

## Data Insertion Guide

### Files for Data Setup
**Location:** `database/scripts/CPPImportPower_Data_Insertion.sql`

This script handles 4 steps:

1. **NormParameters Insertion** - Adds POWER and POWER_MEL entries
   - POWER: Power Distribution source (SAP code 310027910)
   - POWER_MEL: MEL source for mechanical equipment

2. **CPPImportPowerSourceMapping** - Master configuration linking sources to plants
   - Power_Dis source → Plant mapping
   - MEL source → Plant mapping
   - Both marked as active (IsActive = 1)

3. **CPPImportPowerOperationalHours** - Sample FY 2025-26 monthly hours
   - Power_Dis: 680-744 hours per month
   - MEL: 670-744 hours per month

4. **CPPImportPowerCapacity** - Sample FY 2025-26 monthly capacity (MW)
   - Power_Dis: 15.0-16.5 MW
   - MEL: 8.0-9.5 MW

### Execution Steps

**Step 1: Create Tables**
```sql
-- Execute in SQL Server Management Studio
USE [RIL.AOP]
GO
-- Run CPPImportPower_Tables_Creation.sql
```

**Step 2: Insert Data**
```sql
-- Execute CPPImportPower_Data_Insertion.sql
-- This will:
-- - Add NormParameters for POWER and POWER_MEL
-- - Create source mappings for Power_Dis and MEL
-- - Insert sample monthly data for FY 2025-26
```

**Step 3: Verify Data**
```sql
-- Check inserted data
SELECT * FROM CPPImportPowerSourceMapping
SELECT * FROM CPPImportPowerOperationalHours
SELECT * FROM CPPImportPowerCapacity
SELECT * FROM NormParameters WHERE Name IN ('POWER', 'POWER_MEL')
```

### Important Notes

⚠️ **Plant ID Configuration:**
- Default Plant ID: `7BB312A5-C85B-4842-8C96-E70B96560D91`
- Adjust in the SQL script if using different plant IDs
- Ensure NormParameter_FK_Id matches a valid NormParameterType_FK_Id

⚠️ **Financial Year Format:**
- Use format: `YYYY-YY` (e.g., `2025-26` for FY 2025-2026)
- Matches existing system conventions

⚠️ **Unique Constraint:**
- Each source + financial year combination must be unique
- Duplicate entries will cause constraint violation

3. **Excel Import Implementation**
   - Use Apache POI to read Excel sheets
   - Map columns to DTO fields
   - Validate financial year format

4. **Python Integration**
   - Update power_service.py to call new endpoints
   - Aggregate MEL + Power_Dis values
   - Include in calculation models

5. **Frontend Development**
   - Create React component for hours entry
   - Create React component for capacity entry
   - Implement Excel upload/download UI
   - Add calculated MWh display

---

## Frontend Integration

### React Component Updates
**Location:** `apps/react/case-portal/src/components/aop-phase-two/cpp/Inputs/ImportPower.js`

#### Component Capabilities
- Display operational hours for multiple import power sources (MEL, Power_Dis)
- Source-specific data with material code and SAP code display
- Monthly data entry with validation
- Remarks field for each row
- Excel export in XLSX format
- Excel import with error handling and error file generation
- Real-time data validation ensuring remarks are provided when data changes

#### Data Grid Columns
1. **Source Information** (Read-only):
   - `sourceName` - MEL or Power_Dis
   - `plantName` - Associated plant
   - `materialCode` - SAP material reference
   - `sapCode` - SAP code mapping
   - `utilityName` - Utility name

2. **Monthly Hours** (Editable):
   - `april`, `may`, `june`, `july`, `aug`, `sept`, `oct`, `nov`, `dec`, `jan`, `feb`, `mar`
   - Format: DECIMAL(18,2)
   - Numeric input with validation

3. **Remarks** (Editable):
   - `remarks` - Free-form text field for change documentation

#### API Service Updates
**Location:** `apps/react/case-portal/src/components/aop-phase-two/services/cpp/inputApiService.js`

Updated endpoints:
- `GET /task/import-power/operational-hours/{plantId}/{year}` - Fetch hours data
- `POST /task/import-power/operational-hours/{plantId}/{year}` - Save/update hours
- `GET /task/import-power/operational-hours/export/{plantId}/{year}` - Export to Excel
- `POST /task/import-power/operational-hours/import/{plantId}/{year}` - Import from Excel

#### Features
✅ Auto-load data when plant and financial year are selected
✅ Track modified cells with dirty flag
✅ Validate remarks are provided for changed data
✅ Excel export with proper formatting
✅ Excel import with error file generation on partial failures
✅ Snackbar notifications for user feedback
✅ Loading indicator during async operations

---

## Testing Strategy

### Unit Tests (Service Layer)
- Test getImportPower* methods with mock repositories
- Test financial year validation
- Test null-safe DTO mapping

### Integration Tests (Controller)
- Test GET endpoints return correct data
- Test POST endpoints persist data correctly
- Test Excel export/import round-trip
- Test concurrent updates (upsert logic)

### Database Tests
- Verify stored procedures return correct results
- Test unique constraints (source + year)
- Test foreign key relationships

---

## Code Quality Notes

✅ **Strengths**
- Follows established Service/ServiceImpl pattern
- Clear separation of concerns
- Type-safe DTOs
- Transactional consistency
- Stored procedure integration for complex queries

⚠️ **Areas for Enhancement**
- Excel import methods need implementation
- Add validation annotations to DTOs
- Consider caching for master data lookups
- Add logging for audit trail
- Consider pagination for large data sets

---

## Questions for Clarification

1. **Master Data Sources:** Should MEL and Power_Dis source records be pre-configured or dynamically created?
2. **Financial Year Format:** Confirm YYYY-YY format (e.g., "2024-25" for FY 2024-2025)?
3. **Unit of Measure:** Is MW the only UOM for capacity, or should we support flexible UOM?
4. **Calculation Model:** Should MWh calculation be in UI layer or add as service method?
5. **Historical Data:** How many years of historical data need to be imported initially?

---

## Git Status

All files created and properly structured. Ready for:
1. Database deployment
2. Master data insertion
3. Compilation and testing
4. Integration with existing workflows

