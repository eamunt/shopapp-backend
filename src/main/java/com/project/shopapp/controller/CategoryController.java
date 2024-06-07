package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.components.converters.CategoryMessageConverter;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryListReponse;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.services.category.CategoryService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
//@Validated
public class CategoryController {
    private final CategoryService categoryService;
    private final LocalizationUtils localizationUtils;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    // data transfer object
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result){
        if(result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.CREATE_CATEGORY_FAILED));
        }
        Category category = categoryService.createCategory(categoryDTO);
        if(category == null){
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Duplicate category")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
        this.kafkaTemplate.send("insert-a-category", category);
        this.kafkaTemplate.setMessageConverter(new CategoryMessageConverter());
        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.CREATE_CATEGORY_SUCCESSFULLY))
                .status(HttpStatus.OK)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getCategoryById(
            @PathVariable("id") Long categoryId
    ) {
        Category existingCategory = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(existingCategory)
                .message("Get category information successfully")
                .status(HttpStatus.OK)
                .build());
    }

    // display all categories
    @GetMapping("")
    public ResponseEntity<CategoryListReponse> getAllCategories(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        PageRequest pageRequest = PageRequest.of(page, limit,
//                Sort.by("createdAt").descending());
                Sort.by("id").ascending());
        Page<Category> categoryPage = categoryService.getAllCategories(pageRequest);
        // get total of pages
        int totalPages = categoryPage.getTotalPages();
        List<Category> categories = categoryPage.getContent();
        this.kafkaTemplate.send("get-all-categories", categories);
        return ResponseEntity.ok(CategoryListReponse.builder()
                .categories(categories)
                .totalPage(totalPages)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryDTO categoryDTO){

        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                .status(HttpStatus.OK)
                .build());
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id){
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY, String.valueOf(id)))
                    .status(HttpStatus.OK)
                    .build());
        }catch (Exception e){
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }
}
