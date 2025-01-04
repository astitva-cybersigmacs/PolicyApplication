package com.example.policy.repository;

import com.example.policy.model.PolicyFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyFilesRepository extends JpaRepository<PolicyFiles, Long> {
}
