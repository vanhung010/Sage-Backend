package com.sagares.saga_res_api.feat.menu.service;


import com.sagares.saga_res_api.common.exception.ResourceNotFoundException;
import com.sagares.saga_res_api.feat.menu.dto.CategoryDto;
import com.sagares.saga_res_api.feat.menu.entity.Category;
import com.sagares.saga_res_api.feat.menu.exception.CategoryInsertException;
import com.sagares.saga_res_api.feat.menu.exception.CategoryNotFoundException;
import com.sagares.saga_res_api.feat.menu.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;


    public void saveCategory(CategoryDto dto) throws CategoryInsertException {
        Category category = Category
                .builder()
                .name(dto.name())
                .description(dto.description())
                .build();
        categoryRepository.save(category);
    }


    public void saveCategories(List<CategoryDto> dtos) throws CategoryInsertException {
        var entities = dtos.stream().map(d -> Category
                .builder()
                .name(d.name())
                .description(d.description())
                .build())
                .toList();
        categoryRepository.saveAll(entities);
    }

    public CategoryDto getById(Long id) throws CategoryNotFoundException {

        Category entity = categoryRepository.findById(id).stream().findFirst().orElseThrow();

        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public void deleteById(Long id) throws ResourceNotFoundException {
        categoryRepository.deleteById(id);
    }
}
