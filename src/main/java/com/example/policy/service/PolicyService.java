package com.example.policy.service;

import com.example.policy.model.*;
import org.springframework.web.multipart.MultipartFile;


public interface PolicyService {
    Policy createPolicy(String policyName, String description, MultipartFile policyTemplateList, String version);
    Policy getPolicyById(Long policyId);
    Policy updatePolicy(Long policyId, String policyName, String description);
    void deletePolicy(Long policyId);
    Policy updatePolicyTemplate(Long policyId, MultipartFile policyTemplateList, String version);
    PolicyTemplate getPolicyTemplateById(Long templateId);
    PolicyReviewer updatePolicyReviewer(Long userId, boolean isAccepted, String rejectedReason);
    PolicyMembers addPolicyMember(Long policyId, Long userId, PolicyRole role);
}