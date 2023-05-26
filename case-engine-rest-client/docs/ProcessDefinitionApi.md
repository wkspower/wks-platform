# ProcessDefinitionApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**find4**](ProcessDefinitionApi.md#find4) | **GET** /process-definition/ |  |
| [**get2**](ProcessDefinitionApi.md#get2) | **GET** /process-definition/{processDefinitionId}/xml |  |
| [**get3**](ProcessDefinitionApi.md#get3) | **GET** /process-definition/{bpmEngineId}/{processDefinitionId}/xml |  |



## find4

> List&lt;ProcessDefinition&gt; find4()



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        try {
            List<ProcessDefinition> result = apiInstance.find4();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#find4");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**List&lt;ProcessDefinition&gt;**](ProcessDefinition.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## get2

> String get2(processDefinitionId)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            String result = apiInstance.get2(processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#get2");
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
| **processDefinitionId** | **String**|  | |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/xml


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## get3

> String get3(bpmEngineId, processDefinitionId)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.ProcessDefinitionApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        ProcessDefinitionApi apiInstance = new ProcessDefinitionApi(defaultClient);
        String bpmEngineId = "bpmEngineId_example"; // String | 
        String processDefinitionId = "processDefinitionId_example"; // String | 
        try {
            String result = apiInstance.get3(bpmEngineId, processDefinitionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessDefinitionApi#get3");
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
| **bpmEngineId** | **String**|  | |
| **processDefinitionId** | **String**|  | |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/xml


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

