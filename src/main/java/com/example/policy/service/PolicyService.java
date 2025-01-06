package com.example.policy.service;

import com.example.policy.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface PolicyService {
    Policy createPolicy(String policyName, String description, MultipartFile policyTemplateList, String version);
    Policy getPolicyById(Long policyId);
    Policy updatePolicyTemplate(Long policyId, MultipartFile policyTemplateList, String version);
    PolicyTemplate getPolicyTemplateById(Long templateId);
    PolicyApproverAndReviewer updatePolicyReviewer(Long policyId, Long userId, boolean isAccepted, String rejectedReason);
    PolicyMembers addPolicyMember(Long policyId, Long userId, PolicyRole role);
    PolicyApproverAndReviewer updatePolicyApprover(Long policyId, Long userId, boolean isApproved, String rejectedReason);
    List<Policy> getAllPolicies();
    PolicyFiles updatePolicyFiles(Long policyId, String policyFileName, MultipartFile file, String version);
}