package com.tms.restapi.toolsmanagement.issuance.repository;

import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnRecord, Long> {
}
