package com.example.policy.repository;

import com.example.policy.model.PolicyApproverAndReviewer;
import com.example.policy.model.PolicyFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApproverAndReviewerRepository extends JpaRepository<PolicyApproverAndReviewer, Long> {
    List<PolicyApproverAndReviewer> findByUserIdAndPolicyFiles_Policy_PolicyId(Long userId, Long policyId);
    List<PolicyApproverAndReviewer> findByPolicyFiles(PolicyFiles policyFiles);
}