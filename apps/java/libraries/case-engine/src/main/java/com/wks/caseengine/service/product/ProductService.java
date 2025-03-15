package com.wks.caseengine.service.product;

import java.util.List;


import com.wks.caseengine.rest.entity.Product;

public interface ProductService {
	public List<Product> getAllProducts();
    public  List<Object[]> getMonthWiseDataByTypeAndYear(String type, String currentYear);
    public List<Object[]> getAllProductsFromNormParameters(String normParameterTypeName);
    public List<Object[]> getMonthlyDataForYear(int year);
    

    
    //public ProductMonthWiseDataDTO saveMonthWiseData(ProductMonthWiseDataDTO productMonthWiseDataDTO);
}
