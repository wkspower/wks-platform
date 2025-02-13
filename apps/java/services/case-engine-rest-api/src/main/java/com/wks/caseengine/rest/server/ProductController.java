package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.rest.db1.entity.Product;
import com.wks.caseengine.tasks.ProductService;

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
    
    
	@GetMapping(value = "")
	public ResponseEntity<List<Product>> getProducts() {
	    List<Product> products = productService.getAllProducts(); 
	    return ResponseEntity.ok(products);
	}
}
