# HealtcheckApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**check**](HealtcheckApi.md#check) | **GET** /healthCheck/ |  |



## check

> String check()



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.HealtcheckApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        HealtcheckApi apiInstance = new HealtcheckApi(defaultClient);
        try {
            String result = apiInstance.check();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling HealtcheckApi#check");
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

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

