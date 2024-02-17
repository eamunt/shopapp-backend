package com.project.shopapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.shopapp.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
