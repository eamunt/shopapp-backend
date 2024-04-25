package com.project.shopapp.controller;

import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryListReponse;
import com.project.shopapp.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/healthcheck")
@AllArgsConstructor
public class HealthCheckController {
    private final CategoryService categoryService;
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck(){
        try{
            PageRequest pageRequest = PageRequest.of(0, 10,
//                Sort.by("createdAt").descending());
                    Sort.by("id").ascending());
            Page<Category> categoryPage = categoryService.getAllCategories(pageRequest);
            // get total of pages
            int totalPages = categoryPage.getTotalPages();
            List<Category> categories = categoryPage.getContent();
            String computerName = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok().body("Computer Name: " + computerName);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("failed");
        }
    }
}
