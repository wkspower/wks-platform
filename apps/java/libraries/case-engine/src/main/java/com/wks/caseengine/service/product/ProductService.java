package com.wks.caseengine.service.product;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.rest.entity.Product;

public interface ProductService {
	public List<Product> getAllProducts();
    public  List<Object[]> getMonthWiseDataByTypeAndYear(String type, String currentYear);
    public List<Object[]> getAllProductsFromNormParameters(String normParameterTypeName,UUID plantId);
    public List<Object[]> getMonthlyDataForYear(int year);
    

    
    //public ProductMonthWiseDataDTO saveMonthWiseData(ProductMonthWiseDataDTO productMonthWiseDataDTO);
}
