package com.sagares.saga_res_api.feat.menu.controller;


import com.sagares.saga_res_api.feat.menu.dto.CreateMenuRequest;
import com.sagares.saga_res_api.feat.menu.dto.CreateMenuResponse;
import com.sagares.saga_res_api.feat.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;


@RequiredArgsConstructor
@RestController
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;


    @GetMapping("/{id}")
    public ResponseEntity<CreateMenuResponse> getById(@PathVariable Long id) {
        CreateMenuResponse dto = menuService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateMenuResponse> create(
            @Valid @ModelAttribute CreateMenuRequest request,
            @RequestParam("image") MultipartFile image
    ) {
        CreateMenuResponse created = menuService.createWithImage(request, image);
        return ResponseEntity
                .created(URI.create("/menus/" + created.id()))
                .body(created);
    }


    @PostMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody CreateMenuResponse dto) {
        menuService.save(dto);
        return ResponseEntity.noContent().build();
    }
}
