package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.ProductMonthWiseDataDTO;
import com.wks.caseengine.rest.db1.entity.Product;
import com.wks.caseengine.rest.db1.entity.ProductMonthWiseData;
import com.wks.caseengine.service.product.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("product")
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
	public ResponseEntity<List<ProductMonthWiseDataDTO>> getProductListByTypeAndYear(@RequestParam String type, @RequestParam int year) {
		List<ProductMonthWiseDataDTO> productMonthWiseDataList = productService.getMonthWiseDataByTypeAndYear(type,year); 
	    return ResponseEntity.ok(productMonthWiseDataList);
	}


}
