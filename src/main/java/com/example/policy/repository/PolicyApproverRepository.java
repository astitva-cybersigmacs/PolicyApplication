package com.example.policy.repository;

import com.example.policy.model.PolicyApprover;
import com.example.policy.model.PolicyFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApproverRepository extends JpaRepository<PolicyApprover, Long> {
    List<PolicyApprover> findByPolicyFiles(PolicyFiles policyFiles);
    List<PolicyApprover> findAllByUserId(Long userId);
}
