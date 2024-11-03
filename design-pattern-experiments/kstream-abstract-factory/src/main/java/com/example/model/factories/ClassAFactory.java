package com.example.model.factories;

import com.example.model.buttons.Product;
import com.example.model.buttons.ClassAProduct;
import com.example.model.checkboxes.Order;
import com.example.model.checkboxes.ClassAOrder;

public class ClassAFactory implements POFactory {

    @Override
    public Product createProduct() {
        return new ClassAProduct();
    }

    @Override
    public Order createOrder() {
        return new ClassAOrder();
    }
}