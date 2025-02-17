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
import org.springframework.http.HttpStatus;
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
	public ResponseEntity<List<ProductMonthWiseDataDTO>> getProductListByTypeAndYear(@RequestParam String type, @RequestParam int year) {
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
	
	/*@PostMapping(value = "/saveMonthWiseData")
	public ResponseEntity<ProductMonthWiseDataDTO> saveProductMonthWiseData(@RequestBody ProductMonthWiseDataDTO productMonthWiseDataDTO) {
	    ProductMonthWiseDataDTO savedData = productService.saveMonthWiseData(productMonthWiseDataDTO);
	    return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
	}*/


}
