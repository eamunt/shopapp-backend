package com.project.shopapp.services.product;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.order.OrderResponse;
import com.project.shopapp.responses.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ModelMapper modelMapper;
    private static String UPLOADS_FOLDER = "uploads";
    @Override
    @Transactional
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
    public Page<ProductResponse> getAllProducts(String keyword,
                                                Long category_id,
                                                PageRequest pageRequest) {
        // get elements theo page and limit and categoryId (if exists)
        Page<Product> productsPage;
        try {
            productsPage = productRepository.searchProducts(category_id, keyword, pageRequest);

            // sử dụng modelMapper
            TypeMap<Product, ProductResponse> typeMap = modelMapper.typeMap(Product.class, ProductResponse.class);

            typeMap.addMappings(mapper -> {
                // Add a custom converter for the createdDate field
                mapper.using(localDateTimeToDateConverter).map(Product::getCreatedAt, ProductResponse::setCreatedAt);
                mapper.using(localDateTimeToDateConverter).map(Product::getUpdatedAt, ProductResponse::setUpdatedAt);

            });
            return productsPage.map(f -> modelMapper.map(f, ProductResponse.class));
            // hoặc
            // return productsPage.map(ProductResponse::fromProduct);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    Converter<LocalDateTime, Date> localDateTimeToDateConverter = context -> {
        LocalDateTime source = context.getSource();
        return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
    };

    @Override
    @Transactional
    public Product updateProduct(Long idProduct, ProductDTO productDTO) throws Exception {
        Product exsistingProduct = getProductById(idProduct);
        if(exsistingProduct != null){

            Category exsistingCategory = categoryRepository.
                    findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new DataNotFoundException("Cannot find category with id " + productDTO.getCategoryId()));

            if(productDTO.getName() != null && !productDTO.getName().isEmpty()) {
                exsistingProduct.setName(productDTO.getName());
            }
            if(productDTO.getPrice() >= 0) {
                exsistingProduct.setPrice(productDTO.getPrice());
            }
            if(productDTO.getThumbnail() != null && !productDTO.getThumbnail().isEmpty()) {
                exsistingProduct.setThumbnail(productDTO.getThumbnail());
            }
            if(productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()) {
                exsistingProduct.setDescription(productDTO.getDescription());
            }
            if(productDTO.getCategoryId() != null ) {
                exsistingProduct.setCategoryId(exsistingCategory);
            }

            productRepository.save(exsistingProduct);
        }

        return exsistingProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public Boolean existsProductByName(String name){
        return productRepository.existsByName(name);
    }

    @Override
    @Transactional
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
        if(size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT ) {
            throw new InvalidParamException("Limit " +
                    ProductImage.MAXIMUM_IMAGES_PER_PRODUCT + " images for a product");
        }
        if(exsistingProduct.getThumbnail() == null){
            exsistingProduct.setThumbnail(newProductImage.getImageUrl());
        }
        productRepository.save(exsistingProduct);
        return productImageRepository.save(newProductImage);

    }

    @Override
    public void deleteFile(String fileName) throws IOException{
        // path to the folder containing file
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        // Full path to the file which we want to delete
        java.nio.file.Path filePath = uploadDir.resolve(fileName);

       // check existing file
       if(Files.exists(filePath)){
           // delete file
           Files.delete(filePath);
       }else {
           throw new FileNotFoundException("File not found: " + fileName);
       }
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Add UUID to before fileName to ensure uniqueness for fileName
        String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
        // path to folder which contains image.
        java.nio.file.Path uploadDir = Paths.get("uploads");
        // check and create folder if it no exists
        if (!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        // fully path to file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // copy file to destination directory
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    @Override
    public List<Product> findProductsByIds(List<Long> productIds) {
        return productRepository.findProductsByIds(productIds);
    }
}
