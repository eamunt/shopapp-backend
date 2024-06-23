package com.project.shopapp.responses.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductListResponse {
    private List<ProductResponse> products;
    private int totalPages;
}
