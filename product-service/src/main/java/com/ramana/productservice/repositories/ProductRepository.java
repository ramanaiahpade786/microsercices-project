package com.ramana.productservice.repositories;

import com.ramana.productservice.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
