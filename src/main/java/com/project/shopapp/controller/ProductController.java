package com.project.shopapp.controller;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;


    // request with json without image
    @PostMapping("")
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
            Product newProduct = productService.createProduct(productDTO);

            return ResponseEntity.ok(newProduct);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // request with images
    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //POST http://localhost:8088/v1/api/products
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

            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest().body("You can upload maximum "
                        + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT+ " images");
            }
            List<ProductImage> listProductImages = new ArrayList<>();

            for (MultipartFile file : files){
                if (file.getSize() == 0){
                    continue;
                }
                // check size of file, and format
                if (file.getSize() > 10 * 1024 * 1024){ // > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large! Maximum size is 10 MB.");
                }

                // check format (is image or not ?)
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body("File must be an image");
                }

                // Save file and update thumbnail trong DTO
                String filename = storeFile(file);
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

    private String storeFile(MultipartFile file) throws IOException {
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

    @GetMapping("")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        // create Pageable từ thông tin page và limit
        // sort: newest on top.
        PageRequest pageRequest = PageRequest.of(page, limit,
                Sort.by("createdAt").descending());

        Page<ProductResponse> productPage = productService.getAllProducts(pageRequest);
        // get total of pages
        int totalPages = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();

        return ResponseEntity.ok(ProductListResponse
                .builder()
                        .products(products)
                        .totalPages(totalPages)
                .build());
    }
    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable("id") String productId){
        return ResponseEntity.ok("Product with ID: " + productId);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id){
//        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
        productService.deleteProduct(id);
        return ResponseEntity.ok(String.format("Product %s deleted successfully", id));
    }
}
