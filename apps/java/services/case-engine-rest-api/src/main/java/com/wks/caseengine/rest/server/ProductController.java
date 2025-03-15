package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;

import com.wks.caseengine.dto.product.ProductDTO;
import com.wks.caseengine.dto.product.ProductYearlyDataDTO;
import com.wks.caseengine.dto.product.ProductMonthWiseDataDTO;
import com.wks.caseengine.rest.entity.Product;
import com.wks.caseengine.service.product.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
@Tag(name = "Product", description = "test Product")
public class ProductController {
	private ProductService productService;
	
	public String getMonthName(int monthNumber) {
	    return switch (monthNumber) {
	        case 1 -> "January";
	        case 2 -> "February";
	        case 3 -> "March";
	        case 4 -> "April";
	        case 5 -> "May";
	        case 6 -> "June";
	        case 7 -> "July";
	        case 8 -> "August";
	        case 9 -> "September";
	        case 10 -> "October";
	        case 11 -> "November";
	        case 12 -> "December";
	        default -> "Invalid Month";
	    };
	}
	  // Constructor-based Dependency Injection
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
      
	@GetMapping(value = "/productList")
	public ResponseEntity<List<Product>> getProducts() {
	    List<Product> products = productService.getAllProducts(); 
	    return ResponseEntity.ok(products);
	}
	
	@GetMapping(value = "/getMonthWiseData")
	public ResponseEntity<List<ProductMonthWiseDataDTO>> getProductListByTypeAndYear(@RequestParam String type, @RequestParam String year) {
		 List<Object[]> productMonthWiseDataList = productService.getMonthWiseDataByTypeAndYear(type,year);
		 List<ProductMonthWiseDataDTO> productMonthWiseDataDTOList = new ArrayList<>();
			for(Object obj : productMonthWiseDataList) {
			    Object[] data = (Object[]) obj;
			    ProductMonthWiseDataDTO productMonthWiseDataDTO = new ProductMonthWiseDataDTO();
			    
			    int monthNumber = ((Number) data[0]).intValue();
			    productMonthWiseDataDTO.setMonth(getMonthName(monthNumber));
			    
			    productMonthWiseDataDTO.setPlantId(((Number) data[1]).longValue());
			    productMonthWiseDataDTO.setMonthValue(((Number) data[2]).longValue());
			    
			    productMonthWiseDataDTOList.add(productMonthWiseDataDTO);
			}

	    return ResponseEntity.ok(productMonthWiseDataDTOList);
	}


	@GetMapping(value = "/getAllProducts")
	public ResponseEntity<List<ProductDTO>> getAllProducts(@RequestParam  String normParameterTypeName) {
		List<Object[]> productList = productService.getAllProductsFromNormParameters(normParameterTypeName);
		List<ProductDTO> productDTOList = new ArrayList<>();
	
		for (Object[] obj : productList) {
			ProductDTO productDTO = new ProductDTO();
	
			// Convert UUID to String (Avoid using Long)
			productDTO.setId(obj[0] != null ? obj[0].toString() : null);
	
			productDTO.setName((String) obj[1]);
			productDTO.setDisplayName((String) obj[2]);
			productDTOList.add(productDTO);
		}
	
		return ResponseEntity.ok(productDTOList);
	}
	
	
	/*@PostMapping(value = "/saveMonthWiseData")
	public ResponseEntity<ProductMonthWiseDataDTO> saveProductMonthWiseData(@RequestBody ProductMonthWiseDataDTO productMonthWiseDataDTO) {
	    ProductMonthWiseDataDTO savedData = productService.saveMonthWiseData(productMonthWiseDataDTO);
	    return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
	}*/


	@GetMapping("/yearly-data")
	public ResponseEntity<List<ProductYearlyDataDTO>> getProductYearlyData(@RequestParam int year) {
		List<Object[]> products = productService.getAllProductsFromNormParameters(null);
		List<Object[]> monthlyData = productService.getMonthlyDataForYear(year);
		Map<String, ProductYearlyDataDTO> productDataMap = new HashMap<>();
	
		// Store product details
		for (Object[] obj : products) {
			String id = obj[0].toString();  // Convert to String to handle both UUID and Long
			String name = (String) obj[1];
			productDataMap.put(id, new ProductYearlyDataDTO(id, name));
		}
	
		// Store monthly data
		for (Object[] obj : monthlyData) {
			String productId = obj[0].toString(); // Convert to String to handle UUID and Long
			String month = getFormattedMonth((String) obj[1], year);
			Long monthValue = Long.parseLong(obj[2].toString()); // Ensure proper conversion
			String remark = (String) obj[3];
	
			if (productDataMap.containsKey(productId)) {
				ProductYearlyDataDTO product = productDataMap.get(productId);
				product.addMonthData(month, monthValue);
				product.setRemark(remark);
			}
		}
	
		List<ProductYearlyDataDTO> responseList = new ArrayList<>(productDataMap.values());
		return ResponseEntity.ok(responseList);
	}
	
	


	private String getFormattedMonth(String month, int year) {
        return switch (month.toLowerCase()) {
            case "january" -> "Jan" + (year % 100);
            case "february" -> "Feb" + (year % 100);
            case "march" -> "Mar" + (year % 100);
            case "april" -> "Apr" + (year % 100);
            case "may" -> "May" + (year % 100);
            case "june" -> "Jun" + (year % 100);
            case "july" -> "Jul" + (year % 100);
            case "august" -> "Aug" + (year % 100);
            case "september" -> "Sep" + (year % 100);
            case "october" -> "Oct" + (year % 100);
            case "november" -> "Nov" + (year % 100);
            case "december" -> "Dec" + (year % 100);
            default -> month;
        };
    }


}
