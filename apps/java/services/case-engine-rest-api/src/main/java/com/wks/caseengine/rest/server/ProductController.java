package com.wks.caseengine.rest.server;

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
	public ResponseEntity<List<Object[]>> getProductListByTypeAndYear(@RequestParam String type, @RequestParam int year) {
		 List<Object[]> productMonthWiseDataList = productService.getMonthWiseDataByTypeAndYear(type,year);
	    return ResponseEntity.ok(productMonthWiseDataList);
	}
	
	@PostMapping(value = "/saveMonthWiseData")
	public ResponseEntity<ProductMonthWiseDataDTO> saveProductMonthWiseData(@RequestBody ProductMonthWiseDataDTO productMonthWiseDataDTO) {
	    ProductMonthWiseDataDTO savedData = productService.saveMonthWiseData(productMonthWiseDataDTO);
	    return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
	}


}
