package com.project.shopapp.services.product.image;

import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService{
    private final ProductImageRepository productImageRepository;
    @Override
    @Transactional
    public ProductImage deleteProductImage(Long id) throws Exception {
        Optional<ProductImage> productImage = productImageRepository.findById(id);
        // check existing
        if(productImage.isEmpty()){
            throw new DataNotFoundException(
                    String.format("Cannot find product image with id: %ld", id)
            );
        }

        productImageRepository.deleteById(id);

        return productImage.get();
    }
}
