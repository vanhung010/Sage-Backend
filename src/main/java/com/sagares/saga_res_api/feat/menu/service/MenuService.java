package com.sagares.saga_res_api.feat.menu.service;


import com.sagares.saga_res_api.common.storage.CloudinaryImageService;
import com.sagares.saga_res_api.feat.menu.dto.CreateMenuRequest;
import com.sagares.saga_res_api.feat.menu.dto.CreateMenuResponse;
import com.sagares.saga_res_api.feat.menu.entity.Category;
import com.sagares.saga_res_api.feat.menu.entity.Menu;
import com.sagares.saga_res_api.feat.menu.exception.MenuInsertException;
import com.sagares.saga_res_api.feat.menu.exception.MenuNotFoundException;
import com.sagares.saga_res_api.feat.menu.repository.CategoryRepository;
import com.sagares.saga_res_api.feat.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryImageService cloudinaryImageService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CreateMenuResponse createWithImage(CreateMenuRequest request, MultipartFile image) {
        Set<Long> categoryIds = request.categoryIds() == null
                ? Set.of()
                : new HashSet<>(request.categoryIds());

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()
                || categories.stream().anyMatch(Category::isDeleted)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Có danh mục không tồn tại hoặc đã bị xóa");
        }

        var uploadedImage = cloudinaryImageService.uploadMenuImage(image);

        Menu menu = Menu.builder()
                .name(request.name().strip())
                .description(request.description())
                .price(request.price())
                .imageUrl(uploadedImage.url())
                .isAvailable(request.isAvailable())
                .categories(new HashSet<>(categories))
                .build();

        Menu saved;
        try {
            saved = menuRepository.saveAndFlush(menu);
        } catch (RuntimeException ex) {
            cloudinaryImageService.deleteQuietly(uploadedImage.publicId());
            throw ex;
        }

        return CreateMenuResponse.toDTO(saved);
    }



    public void save(CreateMenuResponse dto) throws MenuInsertException {
        Menu menu = Menu
                .builder()
                .name(dto.name())
                .description(dto.description())
                .build();
        menuRepository.save(menu);
    }

    public CreateMenuResponse getById(Long id) throws MenuNotFoundException {
        Menu entity = menuRepository.findById(id)
                .stream()
                .findFirst()
                .orElseThrow();
        return CreateMenuResponse
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public void update(CreateMenuResponse dto) throws MenuNotFoundException {

        Menu menu = Menu
                .builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .build();

        menuRepository.save(menu);
    }

    @Transactional
    public void deleteById(Long id) {
        Menu menu = menuRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new MenuNotFoundException("Không tìm thấy món có ID: " + id)
                );

        menu.setDeleted(true);

        // Entity đang được quản lý trong transaction.
        // Hibernate tự ghi UPDATE khi transaction hoàn tất.
    }

}
