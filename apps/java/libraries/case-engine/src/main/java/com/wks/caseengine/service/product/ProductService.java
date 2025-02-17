package com.wks.caseengine.service.product;

import java.util.List;

import com.wks.caseengine.dto.product.ProductMonthWiseDataDTO;
import com.wks.caseengine.rest.entity.Product;

public interface ProductService {
	public List<Product> getAllProducts();
    public  List<Object[]> getMonthWiseDataByTypeAndYear(String type, int currentYear);
    public ProductMonthWiseDataDTO saveMonthWiseData(ProductMonthWiseDataDTO productMonthWiseDataDTO);
}
