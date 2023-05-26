# CaseDefinitionApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**delete4**](CaseDefinitionApi.md#delete4) | **DELETE** /case-definition/{caseDefId} |  |
| [**find7**](CaseDefinitionApi.md#find7) | **GET** /case-definition/ |  |
| [**get6**](CaseDefinitionApi.md#get6) | **GET** /case-definition/{caseDefId} |  |
| [**save5**](CaseDefinitionApi.md#save5) | **POST** /case-definition/ |  |
| [**update4**](CaseDefinitionApi.md#update4) | **PATCH** /case-definition/{caseDefId} |  |



## delete4

> delete4(caseDefId)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseDefinitionApi apiInstance = new CaseDefinitionApi(defaultClient);
        String caseDefId = "caseDefId_example"; // String | 
        try {
            apiInstance.delete4(caseDefId);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseDefinitionApi#delete4");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **caseDefId** | **String**|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## find7

> List&lt;CaseDefinition&gt; find7(deployed)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseDefinitionApi apiInstance = new CaseDefinitionApi(defaultClient);
        Boolean deployed = true; // Boolean | 
        try {
            List<CaseDefinition> result = apiInstance.find7(deployed);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseDefinitionApi#find7");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **deployed** | **Boolean**|  | [optional] |

### Return type

[**List&lt;CaseDefinition&gt;**](CaseDefinition.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## get6

> CaseDefinition get6(caseDefId)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseDefinitionApi apiInstance = new CaseDefinitionApi(defaultClient);
        String caseDefId = "caseDefId_example"; // String | 
        try {
            CaseDefinition result = apiInstance.get6(caseDefId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseDefinitionApi#get6");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **caseDefId** | **String**|  | |

### Return type

[**CaseDefinition**](CaseDefinition.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## save5

> CaseDefinition save5(caseDefinition)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseDefinitionApi apiInstance = new CaseDefinitionApi(defaultClient);
        CaseDefinition caseDefinition = new CaseDefinition(); // CaseDefinition | 
        try {
            CaseDefinition result = apiInstance.save5(caseDefinition);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseDefinitionApi#save5");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **caseDefinition** | [**CaseDefinition**](CaseDefinition.md)|  | |

### Return type

[**CaseDefinition**](CaseDefinition.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## update4

> CaseDefinition update4(caseDefId, caseDefinition)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseDefinitionApi apiInstance = new CaseDefinitionApi(defaultClient);
        String caseDefId = "caseDefId_example"; // String | 
        CaseDefinition caseDefinition = new CaseDefinition(); // CaseDefinition | 
        try {
            CaseDefinition result = apiInstance.update4(caseDefId, caseDefinition);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseDefinitionApi#update4");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **caseDefId** | **String**|  | |
| **caseDefinition** | [**CaseDefinition**](CaseDefinition.md)|  | |

### Return type

[**CaseDefinition**](CaseDefinition.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

