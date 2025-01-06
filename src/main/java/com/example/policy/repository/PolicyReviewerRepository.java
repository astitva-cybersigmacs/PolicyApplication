package com.example.policy.repository;

import com.example.policy.model.PolicyFiles;
import com.example.policy.model.PolicyReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyReviewerRepository extends JpaRepository<PolicyReviewer, Long> {
    List<PolicyReviewer> findByPolicyFiles(PolicyFiles policyFiles);

    @Query("SELECT pr FROM PolicyReviewer pr WHERE pr.userId = :userId AND pr.policyFiles.policy.policyId = :policyId")
    List<PolicyReviewer> findByUserIdAndPolicyId(@Param("userId") Long userId, @Param("policyId") Long policyId);
}

