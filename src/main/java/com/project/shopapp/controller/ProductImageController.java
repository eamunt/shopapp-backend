package com.project.shopapp.controller;

import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.services.ProductService;
import com.project.shopapp.services.product.image.IProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/products/product_images")
@RequiredArgsConstructor
public class ProductImageController {
    private final IProductImageService productImageService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> delete(
            @PathVariable Long id
    ) throws  Exception {
        ProductImage productImage = productImageService.deleteProductImage(id);
        if(productImage != null){
            productService.deleteFile(productImage.getImageUrl());
            // kiểm tra nếu là ảnh cuối cùng thì xóa luôn thumbnail
            Product existingProduct = productImage.getProductId();
            if(productImageRepository.findByProductId(existingProduct).isEmpty()){
                existingProduct.setThumbnail(null);
                productRepository.save(existingProduct);
            }
            else if(productImageRepository.findByProductId(existingProduct).size() == 1){
                    String newThumbnail = productImageRepository.findByProductId(existingProduct).get(0).getImageUrl();
                    existingProduct.setThumbnail(newThumbnail);
                    productRepository.save(existingProduct);
            }
            else{
                if(Objects.equals(existingProduct.getThumbnail(), productImage.getImageUrl())){
                    String newThumbnail = productImageRepository.findByProductId(existingProduct).get(0).getImageUrl();
                    existingProduct.setThumbnail(newThumbnail);
                    productRepository.save(existingProduct);
                }
            }
            }

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Delete product image successfully")
                        .data(productImage)
                        .status(HttpStatus.OK)
                        .build()
        );
    }
}
