package com.project.shopapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.product.ProductListResponse;
import com.project.shopapp.responses.product.ProductResponse;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.services.product.IProductService;
import com.project.shopapp.services.product.ProductRedisService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;
    private final LocalizationUtils localizationUtils;
    private final ProductRedisService productRedisService;

    // request with json without image
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //POST http://localhost:8088/v1/api/products
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
             BindingResult result
    ){

//        {
//            "name": "Ipad pro 2023",
//                "price": 8900000,
//                "thumbnail": "",
//                "description": "new seal",
//                "category_id": 1
//        }
        try{
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            // save product
            try {
                Product newProduct = productService.createProduct(productDTO);
                return ResponseEntity.ok(ResponseObject.builder()
                        .data(newProduct)
                        .message(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_PRODUCT_SUCCESSFULLY))
                        .status(HttpStatus.OK)
                        .build());
            }catch (Exception e){
                return ResponseEntity.ok(ResponseObject.builder()
                        .message(e.getMessage())
                        .status(HttpStatus.BAD_REQUEST)
                        .build());
            }
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // request with images
    @PostMapping(value = "uploadImage/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //POST http://localhost:8088/v1/api/products
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            Product existingProduct = productService.getProductById(productId);

            //check input
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if(Objects.equals(files.get(0).getOriginalFilename(), "")){
                return ResponseEntity.badRequest().body("Must select file");
            }

            if(files.size() >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.ERROR_MAX_5_IMAGES));
            }
            List<ProductImage> listProductImages = new ArrayList<>();

            for (MultipartFile file : files){
                if (file.getSize() == 0){
                    continue;
                }
                // check size of file, and format
                if (file.getSize() > 10 * 1024 * 1024){ // > 10MB
                    return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.FILE_LARGE));
                }

                // check format (is image or not ?)
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")){
                    return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.FILE_MUST_BE_IMAGE));
                }

                // Save file and update thumbnail trong DTO
                String filename = productService.storeFile(file);
                // save to product object trong DB: save to table => later
                ProductImage productImage = productService.createProductImage(
                        existingProduct,
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build()
                    );
                listProductImages.add(productImage);
            }
            return ResponseEntity.ok().body(listProductImages);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }



    @GetMapping("")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name="category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) throws JsonProcessingException {

        // create Pageable từ thông tin page và limit
        // sort: newest on top.
        PageRequest pageRequest = PageRequest.of(page, limit,
//                Sort.by("createdAt").descending());
                  Sort.by("id").ascending());
        logger.info(String.format("keyword = %s, category_id = %d, page = %d, limit = %d",
                keyword, categoryId, page, limit));


        // kiểm tra trong redis có tồn tại chưa
        ProductListResponse productListResponses = productRedisService.getAllProducts(keyword, categoryId, pageRequest);
        int totalPages = 0;

        if(productListResponses == null){
            Page<ProductResponse> productPage = productService.getAllProducts(keyword, categoryId, pageRequest);
            // get total of pages
            totalPages = productPage.getTotalPages();
            List<ProductResponse> productResponses = productPage.getContent();
            productListResponses = ProductListResponse
                    .builder()
                    .products(productResponses)
                    .totalPages(totalPages)
                    .build();
            productRedisService.saveAllProducts(
                    productListResponses,
                    keyword,
                    categoryId,
                    pageRequest
            );
        }
        return ResponseEntity.ok().body(productListResponses);


    }

    @GetMapping("/clear")
    public ResponseEntity<?> clearCache(){
        productRedisService.clear();
        return ResponseEntity.ok().body("clear successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long productId){
        try {
            Product existingProduct = productService.getProductById(productId);
            ProductResponse result = ProductResponse.fromProduct(existingProduct);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName){
        try {
            java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if(resource.exists()){
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }else{
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfound_image.jpeg").toUri()));
            }

        }catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateProductById(
            @PathVariable("id") Long productId,
            @RequestBody ProductDTO productDTO
    ){
        try {
            Product updatedProduct = productService.updateProduct(productId, productDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .data(updatedProduct)
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY) + " with id: " + productId )
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteProduct(@PathVariable Long id){
//        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_PRODUCT_SUCCESSFULLY) + " with id: " + id)
                    .status(HttpStatus.OK)
                    .build());
        }catch (Exception e){
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam("ids") String ids){
        // ex: 1,3,4,5
        try{
            // spit strings to a int array
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .toList();
            List<Product> products = productService.findProductsByIds(productIds);
            return ResponseEntity.ok(products);

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


//    @PostMapping("/generateFakeProducts")
    private ResponseEntity<String> generateFakeProducts(){
        Faker faker = new Faker();
        for(int i = 0; i < 8_000; i++){
            String productName = faker.commerce().productName();
            if(productService.existsProductByName(productName)){
                continue;
            }
            ProductDTO productDTO = ProductDTO
                    .builder()
                    .name(productName)
                    .price(faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long)faker.number().numberBetween(2,8))
                    .build();

            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake products created successfully");
    }
}
