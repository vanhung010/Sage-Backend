package com.sagares.saga_res_api.feat.menu.controller;


import com.sagares.saga_res_api.feat.menu.dto.CategoryDto;
import com.sagares.saga_res_api.feat.menu.dto.MenuDto;
import com.sagares.saga_res_api.feat.menu.service.CategoryService;
import com.sagares.saga_res_api.feat.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;


    @GetMapping("/{id}")
    public ResponseEntity<MenuDto> getById(@PathVariable Long id) {
        MenuDto dto = menuService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping()
    public ResponseEntity<?> create(@Valid @RequestBody MenuDto dto) {
        menuService.save(dto);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody MenuDto dto) {
        menuService.save(dto);
        return ResponseEntity.noContent().build();
    }
}
