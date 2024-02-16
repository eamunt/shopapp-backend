package com.project.shopapp.controller;

import com.project.shopapp.dtos.CategoryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
//@Validated
public class CategoryController {
    // display all categories
    @GetMapping("")
    public ResponseEntity<String> getAllCategories(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        return ResponseEntity.ok(String.format("Hello %d : %d", page, limit));
    }
    @PostMapping("")
    // data transfer object
    public ResponseEntity<?> insertCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result){
        if(result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        return ResponseEntity.ok("This is insert " + categoryDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id){
        return ResponseEntity.ok("This is update " + id);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id){
        return ResponseEntity.ok("This is delete " + id);
    }
}
