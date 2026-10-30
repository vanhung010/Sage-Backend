package com.sagares.saga_res_api.feat.menu.repository;

import com.sagares.saga_res_api.feat.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
