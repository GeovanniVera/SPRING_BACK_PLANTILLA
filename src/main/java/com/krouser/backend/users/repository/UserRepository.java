package com.krouser.backend.users.repository;

import com.krouser.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdPublic(UUID idPublic);

    boolean existsByIdPublic(UUID idPublic);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByTag(String tag);
}
