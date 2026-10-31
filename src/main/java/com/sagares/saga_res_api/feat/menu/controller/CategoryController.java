package com.sagares.saga_res_api.feat.menu.controller;


import com.sagares.saga_res_api.feat.menu.dto.CategoryDto;
import com.sagares.saga_res_api.feat.menu.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        CategoryDto dto = categoryService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryDto dto) {
        categoryService.saveCategory(dto);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody CategoryDto dto) {
        categoryService.saveCategory(dto);
        return ResponseEntity.noContent().build();
    }

}
