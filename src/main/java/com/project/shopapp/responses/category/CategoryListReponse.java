package com.project.shopapp.responses.category;

import com.project.shopapp.models.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CategoryListReponse {
    private List<Category> categories;
    private int totalPages;
}
