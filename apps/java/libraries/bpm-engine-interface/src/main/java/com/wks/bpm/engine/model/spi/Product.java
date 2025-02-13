package com.wks.bpm.engine.model.spi;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Product {
	
    public Product(int productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

	protected int productId;
	protected String productName;

}
