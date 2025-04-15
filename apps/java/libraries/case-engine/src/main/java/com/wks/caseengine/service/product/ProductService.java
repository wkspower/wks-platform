package com.wks.caseengine.service.product;

import java.util.List;
import java.util.UUID;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ProductService {
    public AOPMessageVM getAllProducts();

    public List<Object[]> getMonthWiseDataByTypeAndYear(String type, String currentYear);

    public List<Object[]> getAllProductsFromNormParameters(String normParameterTypeName, UUID plantId);

    public List<Object[]> getMonthlyDataForYear(int year);

    // public ProductMonthWiseDataDTO saveMonthWiseData(ProductMonthWiseDataDTO
    // productMonthWiseDataDTO);
}
