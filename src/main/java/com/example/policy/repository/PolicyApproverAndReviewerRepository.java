package com.example.policy.repository;

import com.example.policy.model.Policy;
import com.example.policy.model.PolicyApproverAndReviewer;
import com.example.policy.model.PolicyFiles;
import com.example.policy.model.PolicyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApproverAndReviewerRepository extends JpaRepository<PolicyApproverAndReviewer, Long> {
    List<PolicyApproverAndReviewer> findByPolicyAndRole(Policy policy, PolicyRole role);

    List<PolicyApproverAndReviewer> findByUserIdAndPolicy_PolicyIdAndPolicyFiles_PolicyFilesId(Long userId, Long policyId, Long policyFileId);

    List<PolicyApproverAndReviewer> findByPolicyAndRoleAndPolicyFiles(Policy policy, PolicyRole role, PolicyFiles policyFiles);

    boolean existsByPolicyAndPolicyFilesAndUserIdAndRole(Policy policy, PolicyFiles policyFiles, Long userId, PolicyRole role);


}