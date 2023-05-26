# ProcessInstanceApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**find3**](ProcessInstanceApi.md#find3) | **GET** /process-instance/ |  |
| [**getActivityInstances**](ProcessInstanceApi.md#getActivityInstances) | **GET** /process-instance/{id}/activity-instances |  |



## find3

> List&lt;ProcessInstance&gt; find3(businessKey)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        try {
            List<ProcessInstance> result = apiInstance.find3(businessKey);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#find3");
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
| **businessKey** | **String**|  | [optional] |

### Return type

[**List&lt;ProcessInstance&gt;**](ProcessInstance.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getActivityInstances

> List&lt;ActivityInstance&gt; getActivityInstances(id)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.ProcessInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        ProcessInstanceApi apiInstance = new ProcessInstanceApi(defaultClient);
        String id = "id_example"; // String | 
        try {
            List<ActivityInstance> result = apiInstance.getActivityInstances(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ProcessInstanceApi#getActivityInstances");
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
| **id** | **String**|  | |

### Return type

[**List&lt;ActivityInstance&gt;**](ActivityInstance.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

