package com.example.policy.repository;

import com.example.policy.model.PolicyFiles;
import com.example.policy.model.PolicyReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyReviewerRepository extends JpaRepository<PolicyReviewer, Long> {
    List<PolicyReviewer> findByPolicyFiles(PolicyFiles policyFiles);
    List<PolicyReviewer> findAllByUserId(Long userId);
}

