# CPPNorms Implementation Summary

## Overview
This document provides a complete summary of the CPPNorms feature implementation, which maintains norm values for utilities against plants on a month-by-month basis with bidirectional sync to NormsMonthDetail table.

---

## Database Components

### 1. CPPNorms Table
**File**: `CPPNorms_Table_Creation.sql`

**Structure**:
- Primary Key: `Id` (UNIQUEIDENTIFIER)
- Foreign Keys: 
  - `NormsHeader_FK_Id` → NormsHeader(Id)
  - `NormType_FK_Id` → NormTypes(Id) [MANDATORY]
- Financial Year: `FinancialYear` (NVARCHAR(20))
- Monthly Norms: 12 columns (Apr-Mar) - `Apr_Norms` through `Mar_Norms` (DECIMAL(18,6))
- `Remarks` (NVARCHAR(1000)) - Single remarks field for entire row
- Audit fields: CreatedBy, CreatedDate, ModifiedBy, ModifiedDate
- Unique Constraint: (NormsHeader_FK_Id, FinancialYear)

### 2. Stored Procedures

#### a. CPP_GetCPPNorms
**Purpose**: Fetch CPPNorms data for a CPP plant and financial year

**Parameters**:
- `@CPPPlantId` (UNIQUEIDENTIFIER)
- `@FinancialYear` (NVARCHAR(20))

**Returns**: Complete norm data with plant, utility, material details and all 12 months of norms

#### b. CPP_UpdateCPPNorms
**Purpose**: Insert/Update CPPNorms and sync to NormsMonthDetail

**Parameters**:
- `@Id` (UNIQUEIDENTIFIER) - CPPNorms Id (nullable for insert)
- `@NormsHeaderFkId` (UNIQUEIDENTIFIER)
- `@FinancialYear` (NVARCHAR(20))
- `@NormTypeFkId` (UNIQUEIDENTIFIER) - MANDATORY
- `@Apr_Norms` through `@Mar_Norms` (DECIMAL(18,6))
- `@Remarks` (NVARCHAR(1000))
- `@ModifiedBy` (NVARCHAR(100))

**Logic**:
1. Check if record exists by NormsHeader_FK_Id + FinancialYear
2. Insert or Update CPPNorms table
3. Parse financial year to determine start/end years
4. Sync all 12 months to NormsMonthDetail table (updates Norms field only)
5. Transaction-based for data integrity

#### c. CPP_UpdateNormsFromPythonModel
**Purpose**: Update single month norm from Python model and sync back to CPPNorms

**Parameters**:
- `@NormsHeaderFkId` (UNIQUEIDENTIFIER)
- `@FinancialYearMonthFkId` (UNIQUEIDENTIFIER)
- `@Norms` (DECIMAL(18,6))
- `@ModifiedBy` (NVARCHAR(100)) - Default: 'PythonModel'

**Logic**:
1. Update NormsMonthDetail.Norms for the specific month
2. Determine financial year from FinancialYearMonth
3. Find corresponding CPPNorms record
4. Update the specific month column in CPPNorms (e.g., Apr_Norms for Month=4)
5. Transaction-based for data integrity

### 3. Migration Script
**File**: `CPPNorms_Migration_Script.sql`

**Purpose**: One-time migration of existing data from NormsMonthDetail to CPPNorms

**Logic**:
1. Creates/finds default NormType ('Fixed')
2. Groups NormsMonthDetail by NormsHeader_FK_Id and FinancialYear
3. Pivots monthly norms into 12 columns
4. Inserts into CPPNorms with default NormType
5. Skips records that already exist

---

## Java Components

### 1. Entity
**File**: `CPPNorms.java`
**Location**: `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/entity/`

**Fields**:
- All database columns mapped with JPA annotations
- Uses `@Table(name = "CPPNorms", schema = "dbo", catalog = "RIL.AOP")`

### 2. Repository
**File**: `CPPNormsRepository.java`
**Location**: `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/repository/`

**Methods**:
- `findByNormsHeaderFkIdAndFinancialYear()` - Find specific record
- `findByFinancialYear()` - Find all records for a financial year
- `updateCPPNorms()` - Stored procedure call (not used in current implementation)

### 3. DTOs

#### a. CPPNormsRequestDTO
**File**: `CPPNormsRequestDTO.java`
**Location**: `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/dto/norm/`

**Purpose**: Request payload for create/update operations

**Key Fields**:
- `cppNormsId` - For updates
- `normsHeaderFkId` - REQUIRED
- `normTypeFkId` - REQUIRED
- 12 monthly norms fields (aprNorms through marNorms)
- `remarks`
- Plant/utility metadata fields

#### b. CPPNormsResponseDTO
**File**: `CPPNormsResponseDTO.java`

**Purpose**: Response payload for GET operations

**Key Fields**:
- All request fields plus:
- `normTypeName` - Name from NormTypes table
- `modifiedBy`, `modifiedDate` - Audit info

### 4. Service Layer

#### Interface: CPPNormsService.java
**Location**: `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/service/`

**Methods**:
- `getCPPNorms(UUID cppPlantId, String financialYear)` - Fetch norms
- `saveOrUpdateCPPNorms(List<CPPNormsRequestDTO> dtoList, String financialYear, String modifiedBy)` - Bulk save/update

#### Implementation: CPPNormsServiceImpl.java
**Location**: `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cpp/serviceimpl/`

**Key Features**:
- Uses EntityManager for stored procedure calls
- Bulk processing with individual error handling
- Returns AOPMessageVM with status codes (200, 207 for partial success, 400, 500)
- Comprehensive logging

### 5. Controller
**File**: `CPPNormsController.java`
**Location**: `apps/java/services/case-engine-rest-api/src/main/java/com/wks/caseengine/rest/cpp/`

**Endpoints**:

#### GET `/task/cpp-norms`
**Parameters**:
- `cppPlantId` (UUID) - REQUIRED
- `financialYear` (String) - REQUIRED (format: "2024-25")

**Response**: AOPMessageVM with List<CPPNormsResponseDTO>

#### POST `/task/cpp-norms/{financialYear}`
**Path Variable**:
- `financialYear` (String) - Financial year

**Query Parameter**:
- `modifiedBy` (String) - Optional, default: "SYSTEM"

**Body**: List<CPPNormsRequestDTO>

**Response**: AOPMessageVM with success/error details

---

## Data Flow

### 1. UI → CPPNorms → NormsMonthDetail (User Updates)
```
User edits norms in UI
    ↓
POST /task/cpp-norms/{financialYear}
    ↓
CPPNormsService.saveOrUpdateCPPNorms()
    ↓
Calls SP: CPP_UpdateCPPNorms
    ↓
Updates CPPNorms table (12 month columns)
    ↓
Syncs to NormsMonthDetail (12 separate records)
```

### 2. Python Model → NormsMonthDetail → CPPNorms (Model Updates)
```
Python model calculates norm for specific month
    ↓
Calls SP: CPP_UpdateNormsFromPythonModel
    ↓
Updates NormsMonthDetail.Norms (single month)
    ↓
Syncs back to CPPNorms (specific month column)
```

### 3. Fetch Flow
```
User requests norms data
    ↓
GET /task/cpp-norms?cppPlantId=xxx&financialYear=2024-25
    ↓
CPPNormsService.getCPPNorms()
    ↓
Calls SP: CPP_GetCPPNorms
    ↓
Returns data with plant, utility, and 12 months of norms
```

---

## Key Design Decisions

1. **Bidirectional Sync**: Both CPPNorms and NormsMonthDetail can be updated, changes sync automatically
2. **No Triggers**: Sync logic handled in stored procedures only (controlled access)
3. **Single Remarks**: One remarks field per utility per year (not per month)
4. **Mandatory NormType**: Every norm must have a type (Historical/Python Model/etc.)
5. **Financial Year Format**: "2024-25" or "24-25" (auto-converted to 4-digit years)
6. **Bulk Operations**: UI sends list of records for efficient processing
7. **Partial Success Handling**: Returns 207 status if some records fail
8. **Transaction Safety**: All updates wrapped in transactions

---

## Deployment Steps

### 1. Database Setup
```sql
-- Step 1: Create CPPNorms table
-- Run: CPPNorms_Table_Creation.sql

-- Step 2: Migrate existing data
-- Run: CPPNorms_Migration_Script.sql

-- Step 3: Create stored procedures
-- Run: CPP_GetCPPNorms_SP.sql
-- Run: CPP_UpdateCPPNorms_SP.sql
-- Run: CPP_UpdateNormsFromPythonModel_SP.sql
```

### 2. Java Deployment
- Build and deploy the updated case-engine library
- Deploy the updated case-engine-rest-api service
- Restart application servers

### 3. Verification
```bash
# Test GET endpoint
curl -X GET "http://localhost:8080/task/cpp-norms?cppPlantId={uuid}&financialYear=2024-25"

# Test POST endpoint
curl -X POST "http://localhost:8080/task/cpp-norms/2024-25?modifiedBy=TestUser" \
  -H "Content-Type: application/json" \
  -d '[{"normsHeaderFkId":"xxx","normTypeFkId":"yyy","aprNorms":1.5,...}]'
```

---

## Future Enhancements

1. **Validation Rules**: Add business rule validations (e.g., norms cannot be negative)
2. **Audit History**: Track all changes to norms with history table
3. **Bulk Import/Export**: Excel import/export for CPPNorms
4. **NormType Management**: UI for managing NormTypes
5. **Approval Workflow**: Add approval process for norm changes
6. **Python Integration**: Direct Python API for model updates

---

## Notes

- **No changes required** to existing `CPP_NMD_GetNormBasedUtilityBudget` SP (continues to fetch from NormsMonthDetail)
- NormTypes table already exists and is in use
- CPPNorms is the **master source of truth** for norms going forward
- Existing NormsMonthDetail.Remarks field can be removed from UI (norms not editable from that screen)

---

## Contact & Support

For issues or questions regarding this implementation, contact the development team.

**Implementation Date**: January 23, 2026
**Version**: 1.0
