

# CaseInstance


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**getId** | **String** |  |  [optional] |
|**businessKey** | **String** |  |  [optional] |
|**caseDefinitionId** | **String** |  |  [optional] |
|**stage** | **String** |  |  [optional] |
|**caseOwner** | **String** |  |  [optional] |
|**caseOwnerName** | **String** |  |  [optional] |
|**comments** | [**List&lt;Comment&gt;**](Comment.md) |  |  [optional] |
|**documents** | [**List&lt;CaseDocument&gt;**](CaseDocument.md) |  |  [optional] |
|**attributes** | [**List&lt;CaseAttribute&gt;**](CaseAttribute.md) |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**id** | **String** |  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| WIP_CASE_STATUS | &quot;WIP_CASE_STATUS&quot; |
| CLOSED_CASE_STATUS | &quot;CLOSED_CASE_STATUS&quot; |
| ARCHIVED_CASE_STATUS | &quot;ARCHIVED_CASE_STATUS&quot; |



