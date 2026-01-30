package com.krouser.backend.rbac.repository;

import com.krouser.backend.rbac.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findByName(String name);

    @Query("SELECT COUNT(r) > 0 FROM Role r JOIN r.privileges p WHERE p.id = :privilegeId")
    boolean isPrivilegeAssignedToRoles(@Param("privilegeId") Long privilegeId);
}
