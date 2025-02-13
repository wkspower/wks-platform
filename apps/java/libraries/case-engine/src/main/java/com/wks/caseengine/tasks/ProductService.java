package com.wks.caseengine.tasks;

import java.util.List;

import com.wks.caseengine.rest.db1.entity.Product;


public interface ProductService {
    List<Product> getAllProducts();
}