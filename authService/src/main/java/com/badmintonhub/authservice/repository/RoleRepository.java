package com.badmintonhub.authservice.repository;

import com.badmintonhub.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long>, JpaSpecificationExecutor<Role> {
    Optional<Role> findByName(String user);
}
