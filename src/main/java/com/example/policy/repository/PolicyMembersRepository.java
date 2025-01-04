package com.example.policy.repository;

import com.example.policy.model.Policy;
import com.example.policy.model.PolicyMembers;
import com.example.policy.model.PolicyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyMembersRepository extends JpaRepository<PolicyMembers, Long> {
    List<PolicyMembers> findByPolicyAndRole(Policy policy, PolicyRole role);
}