# RecordControllerApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**delete**](RecordControllerApi.md#delete) | **DELETE** /record/{recordTypeId}/{id} |  |
| [**find1**](RecordControllerApi.md#find1) | **GET** /record/{recordTypeId} |  |
| [**get**](RecordControllerApi.md#get) | **GET** /record/{recordTypeId}/{id} |  |
| [**save**](RecordControllerApi.md#save) | **POST** /record/{recordTypeId} |  |
| [**update**](RecordControllerApi.md#update) | **PATCH** /record/{recordTypeId}/{id} |  |



## delete

> delete(recordTypeId, id)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.RecordControllerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        RecordControllerApi apiInstance = new RecordControllerApi(defaultClient);
        String recordTypeId = "recordTypeId_example"; // String | 
        String id = "id_example"; // String | 
        try {
            apiInstance.delete(recordTypeId, id);
        } catch (ApiException e) {
            System.err.println("Exception when calling RecordControllerApi#delete");
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
| **recordTypeId** | **String**|  | |
| **id** | **String**|  | |

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


## find1

> List&lt;JsonObject&gt; find1(recordTypeId)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.RecordControllerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        RecordControllerApi apiInstance = new RecordControllerApi(defaultClient);
        String recordTypeId = "recordTypeId_example"; // String | 
        try {
            List<JsonObject> result = apiInstance.find1(recordTypeId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RecordControllerApi#find1");
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
| **recordTypeId** | **String**|  | |

### Return type

[**List&lt;JsonObject&gt;**](JsonObject.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## get

> JsonObject get(recordTypeId, id)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.RecordControllerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        RecordControllerApi apiInstance = new RecordControllerApi(defaultClient);
        String recordTypeId = "recordTypeId_example"; // String | 
        String id = "id_example"; // String | 
        try {
            JsonObject result = apiInstance.get(recordTypeId, id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RecordControllerApi#get");
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
| **recordTypeId** | **String**|  | |
| **id** | **String**|  | |

### Return type

[**JsonObject**](JsonObject.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## save

> save(recordTypeId, jsonObject)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.RecordControllerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        RecordControllerApi apiInstance = new RecordControllerApi(defaultClient);
        String recordTypeId = "recordTypeId_example"; // String | 
        JsonObject jsonObject = new JsonObject(); // JsonObject | 
        try {
            apiInstance.save(recordTypeId, jsonObject);
        } catch (ApiException e) {
            System.err.println("Exception when calling RecordControllerApi#save");
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
| **recordTypeId** | **String**|  | |
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


## update

> update(recordTypeId, id, jsonObject)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.RecordControllerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        RecordControllerApi apiInstance = new RecordControllerApi(defaultClient);
        String recordTypeId = "recordTypeId_example"; // String | 
        String id = "id_example"; // String | 
        JsonObject jsonObject = new JsonObject(); // JsonObject | 
        try {
            apiInstance.update(recordTypeId, id, jsonObject);
        } catch (ApiException e) {
            System.err.println("Exception when calling RecordControllerApi#update");
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
| **recordTypeId** | **String**|  | |
| **id** | **String**|  | |
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

