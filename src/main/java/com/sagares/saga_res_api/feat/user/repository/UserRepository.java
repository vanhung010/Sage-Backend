package com.sagares.saga_res_api.feat.user.repository;

import com.sagares.saga_res_api.feat.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.WebFilter;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmailAndDeletedFalse(String staffEmail);
}
