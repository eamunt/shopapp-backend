package com.project.shopapp.services;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO categoryDTO);
    Category getCategoryById(Long categoryId);
    List<Category> getAllCategories();
    Category updateCategory(Long categoryId, CategoryDTO categoryDTO);
    void deleteCategory(Long categoryId);
}
