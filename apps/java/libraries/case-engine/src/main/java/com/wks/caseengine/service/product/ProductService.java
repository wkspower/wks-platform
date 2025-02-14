package com.wks.caseengine.service.product;

import java.util.List;

import com.wks.caseengine.dto.product.ProductMonthWiseDataDTO;
import com.wks.caseengine.rest.db1.entity.Product;

public interface ProductService {
	List<Product> getAllProducts();
    public List<ProductMonthWiseDataDTO> getMonthWiseDataByTypeAndYear(String type, int currentYear);
}
