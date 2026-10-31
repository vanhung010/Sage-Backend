package com.sagares.saga_res_api.feat.menu.service;


import com.sagares.saga_res_api.feat.menu.dto.MenuDto;
import com.sagares.saga_res_api.feat.menu.entity.Menu;
import com.sagares.saga_res_api.feat.menu.exception.MenuInsertException;
import com.sagares.saga_res_api.feat.menu.exception.MenuNotFoundException;
import com.sagares.saga_res_api.feat.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public void save(MenuDto dto) throws MenuInsertException {
        Menu menu = Menu
                .builder()
                .name(dto.name())
                .description(dto.description())
                .build();
        menuRepository.save(menu);
    }

    public MenuDto getById(Long id) throws MenuNotFoundException {
        Menu entity = menuRepository.findById(id)
                .stream()
                .findFirst()
                .orElseThrow();
        return MenuDto
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public void update(MenuDto dto) throws MenuNotFoundException {

        Menu menu = Menu
                .builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .build();

        menuRepository.save(menu);
    }

    public void deleteById(Long id) throws MenuNotFoundException {
        menuRepository.deleteById(id);
    }

}
