package com.example.model.factories;

import com.example.model.buttons.Product;
import com.example.model.buttons.ClassBProduct;
import com.example.model.checkboxes.Order;
import com.example.model.checkboxes.ClassBOrder;

public class ClassBFactory implements POFactory {

    @Override
    public Product createProduct() {
        return new ClassBProduct();
    }

    @Override
    public Order createOrder() {
        return new ClassBOrder();
    }
}