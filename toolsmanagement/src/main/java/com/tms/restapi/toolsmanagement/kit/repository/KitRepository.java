package com.tms.restapi.toolsmanagement.kit.repository;

import com.tms.restapi.toolsmanagement.kit.model.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitRepository extends JpaRepository<Kit, Long> {
}