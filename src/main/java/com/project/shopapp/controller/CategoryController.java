package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryListReponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.CategoryService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("")
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
        categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.CREATE_CATEGORY_SUCCESSFULLY));
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
        return ResponseEntity.ok(CategoryListReponse.builder()
                .categories(categories)
                .totalPage(totalPages)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryDTO categoryDTO){

        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id){
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY, String.valueOf(id)));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
