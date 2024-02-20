package com.project.shopapp.controller;

import com.project.shopapp.dtos.ProductDTO;
import jakarta.validation.Valid;
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
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
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

            List<MultipartFile> files = productDTO.getFiles();
            //check input
            files = files == null ? new ArrayList<MultipartFile>() : files;
            for (MultipartFile file : files){
                if (file != null) {
                    if (file.getSize() == 0){
                        continue;
                    }
                    // check size of file, and format
                    if (file.getSize() > 10 * 1024 * 1024){ // > 10MB
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body("File is too large! Maximum size is 10 MB.");
                    }

                    //check format (is image or not ?)
                    String contentType = file.getContentType();
                    if(contentType == null || !contentType.startsWith("image/")){
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body("File must be an image");
                    }

                    // Save file and update thumbnail trong DTO
                    String filename = storeFile(file);
                    // save to product object trong DB: save to table => later
                }
            }


            return ResponseEntity.ok("Product created successfully");
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
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
    public ResponseEntity<String> getProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        return ResponseEntity.ok("getProducts here");
    }
    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable("id") String productId){
        return ResponseEntity.ok("Product with ID: " + productId);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id){
//        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
        return ResponseEntity.ok(String.format("Product %s deleted successfully", id));
    }
}