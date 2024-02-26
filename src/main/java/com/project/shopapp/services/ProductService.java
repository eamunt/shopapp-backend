package com.project.shopapp.services;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    @Override
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category exsistingCategory = categoryRepository.
                findById(productDTO.getCategoryId())
                .orElseThrow(() ->
                        new DataNotFoundException("Cannot find category with id " + productDTO.getCategoryId()));

        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .description(productDTO.getDescription())
                .categoryId(exsistingCategory)
                .build();

        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(Long productId) throws Exception {
        return productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id " + productId));
    }

    @Override
    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
        // get elements theo page and limit
        return productRepository.findAll(pageRequest).map(product -> {
            ProductResponse productResponse = ProductResponse.builder()
                    .name(product.getName())
                    .price(product.getPrice())
                    .thumbnail(product.getThumbnail())
                    .description(product.getDescription())
                    .categoryId(product.getCategoryId().getId())
                    .build();
            productResponse.setCreatedAt(product.getCreatedAt());
            productResponse.setUpdatedAt(product.getUpdatedAt());
            return productResponse;
            }
        );
    }

    @Override
    public Product updateProduct(Long idProduct, ProductDTO productDTO) throws Exception {
        Product exsistingProduct = getProductById(idProduct);
        if(exsistingProduct != null){

            Category exsistingCaterory = categoryRepository.
                    findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new DataNotFoundException("Cannot find category with id " + productDTO.getCategoryId()));

            exsistingProduct.setName(productDTO.getName());
            exsistingProduct.setPrice(productDTO.getPrice());
            exsistingProduct.setThumbnail(productDTO.getThumbnail());
            exsistingProduct.setDescription(productDTO.getDescription());
            exsistingProduct.setCategoryId(exsistingCaterory);

            return productRepository.save(exsistingProduct);
        }

        return null;
    }

    @Override
    public void deleteProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public Boolean existsProduct(String name){
        return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(
            Product productId,
            ProductImageDTO productImageDTO) throws Exception{

        Product exsistingProduct = productRepository.
                findById(productId.getId())
                .orElseThrow(() ->
                        new DataNotFoundException("Cannot find category with id " + productImageDTO.getProductId()));

        ProductImage newProductImage = ProductImage.builder()
                .productId(exsistingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();

        // limit 5 images for a product
        int size = productImageRepository.findByProductId(productId).size();
        if(size > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT ) {
            throw new InvalidParamException("Limit" +
                    ProductImage.MAXIMUM_IMAGES_PER_PRODUCT + " images for a product");
        }
        return productImageRepository.save(newProductImage);

    }
}
