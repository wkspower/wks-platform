# CaseInstanceApi

All URIs are relative to *http://127.0.0.1:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**delete3**](CaseInstanceApi.md#delete3) | **DELETE** /case/{businessKey} |  |
| [**deleteComment**](CaseInstanceApi.md#deleteComment) | **DELETE** /case/{businessKey}/comment/{commentId} |  |
| [**find6**](CaseInstanceApi.md#find6) | **GET** /case/ |  |
| [**get5**](CaseInstanceApi.md#get5) | **GET** /case/{businessKey} |  |
| [**save4**](CaseInstanceApi.md#save4) | **POST** /case/ |  |
| [**saveComment**](CaseInstanceApi.md#saveComment) | **POST** /case/{businessKey}/comment |  |
| [**saveDocument**](CaseInstanceApi.md#saveDocument) | **POST** /case/{businessKey}/document |  |
| [**udpateComment**](CaseInstanceApi.md#udpateComment) | **PATCH** /case/{businessKey}/comment/{commentId} |  |
| [**update3**](CaseInstanceApi.md#update3) | **PATCH** /case/{businessKey} |  |



## delete3

> delete3(businessKey)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        try {
            apiInstance.delete3(businessKey);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#delete3");
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
| **businessKey** | **String**|  | |

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


## deleteComment

> deleteComment(businessKey, commentId)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        String commentId = "commentId_example"; // String | 
        try {
            apiInstance.deleteComment(businessKey, commentId);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#deleteComment");
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
| **businessKey** | **String**|  | |
| **commentId** | **String**|  | |

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


## find6

> Object find6(status, caseDefinitionId, before, after, sort, limit)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String status = "status_example"; // String | 
        String caseDefinitionId = "caseDefinitionId_example"; // String | 
        String before = "before_example"; // String | 
        String after = "after_example"; // String | 
        String sort = "sort_example"; // String | 
        String limit = "limit_example"; // String | 
        try {
            Object result = apiInstance.find6(status, caseDefinitionId, before, after, sort, limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#find6");
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
| **status** | **String**|  | [optional] |
| **caseDefinitionId** | **String**|  | [optional] |
| **before** | **String**|  | [optional] |
| **after** | **String**|  | [optional] |
| **sort** | **String**|  | [optional] |
| **limit** | **String**|  | [optional] |

### Return type

**Object**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## get5

> CaseInstance get5(businessKey)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        try {
            CaseInstance result = apiInstance.get5(businessKey);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#get5");
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
| **businessKey** | **String**|  | |

### Return type

[**CaseInstance**](CaseInstance.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## save4

> CaseInstance save4(caseInstance)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        CaseInstance caseInstance = new CaseInstance(); // CaseInstance | 
        try {
            CaseInstance result = apiInstance.save4(caseInstance);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#save4");
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
| **caseInstance** | [**CaseInstance**](CaseInstance.md)|  | |

### Return type

[**CaseInstance**](CaseInstance.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## saveComment

> saveComment(businessKey, comment)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        Comment comment = new Comment(); // Comment | 
        try {
            apiInstance.saveComment(businessKey, comment);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#saveComment");
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
| **businessKey** | **String**|  | |
| **comment** | [**Comment**](Comment.md)|  | |

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


## saveDocument

> saveDocument(businessKey, caseDocument)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        CaseDocument caseDocument = new CaseDocument(); // CaseDocument | 
        try {
            apiInstance.saveDocument(businessKey, caseDocument);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#saveDocument");
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
| **businessKey** | **String**|  | |
| **caseDocument** | [**CaseDocument**](CaseDocument.md)|  | |

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


## udpateComment

> udpateComment(businessKey, commentId, comment)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        String commentId = "commentId_example"; // String | 
        Comment comment = new Comment(); // Comment | 
        try {
            apiInstance.udpateComment(businessKey, commentId, comment);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#udpateComment");
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
| **businessKey** | **String**|  | |
| **commentId** | **String**|  | |
| **comment** | [**Comment**](Comment.md)|  | |

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


## update3

> update3(businessKey, caseInstance)



### Example

```java
// Import classes:
import com.wks.caseengine.client.invoker.ApiClient;
import com.wks.caseengine.client.invoker.ApiException;
import com.wks.caseengine.client.invoker.Configuration;
import com.wks.caseengine.client.invoker.models.*;
import com.wks.caseengine.client.api.CaseInstanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://127.0.0.1:8081");

        CaseInstanceApi apiInstance = new CaseInstanceApi(defaultClient);
        String businessKey = "businessKey_example"; // String | 
        CaseInstance caseInstance = new CaseInstance(); // CaseInstance | 
        try {
            apiInstance.update3(businessKey, caseInstance);
        } catch (ApiException e) {
            System.err.println("Exception when calling CaseInstanceApi#update3");
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
| **businessKey** | **String**|  | |
| **caseInstance** | [**CaseInstance**](CaseInstance.md)|  | |

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

