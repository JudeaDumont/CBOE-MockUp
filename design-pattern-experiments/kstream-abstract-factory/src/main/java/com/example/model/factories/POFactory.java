package com.example.model.factories;

import com.example.model.buttons.Product;
import com.example.model.checkboxes.Order;

public interface POFactory {
    Product createProduct();
    Order createOrder();
}