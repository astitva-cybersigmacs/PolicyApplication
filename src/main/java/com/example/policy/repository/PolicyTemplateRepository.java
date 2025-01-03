package com.example.policy.repository;

import com.example.policy.model.PolicyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyTemplateRepository extends JpaRepository<PolicyTemplate, Long> {
}

