# DataImportControllerApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**importData**](DataImportControllerApi.md#importData) | **POST** /import/ |  |



## importData

> importData(jsonObject)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.DataImportControllerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        DataImportControllerApi apiInstance = new DataImportControllerApi(defaultClient);
        JsonObject jsonObject = new JsonObject(); // JsonObject | 
        try {
            apiInstance.importData(jsonObject);
        } catch (ApiException e) {
            System.err.println("Exception when calling DataImportControllerApi#importData");
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
| **jsonObject** | [**JsonObject**](JsonObject.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

