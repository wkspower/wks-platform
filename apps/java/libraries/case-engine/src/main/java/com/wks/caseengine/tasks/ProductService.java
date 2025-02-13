package com.wks.caseengine.tasks;

import java.util.List;

import com.wks.bpm.engine.model.spi.Product;

public interface ProductService {
    List<Product> getAllProducts();
}