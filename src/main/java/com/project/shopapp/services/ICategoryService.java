package com.project.shopapp.services;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


public interface ICategoryService {
    Category createCategory(CategoryDTO categoryDTO);
    Category getCategoryById(Long categoryId);
    Page<Category> getAllCategories(PageRequest pageRequest);
    Category updateCategory(Long categoryId, CategoryDTO categoryDTO);
    void deleteCategory(Long categoryId);
}
