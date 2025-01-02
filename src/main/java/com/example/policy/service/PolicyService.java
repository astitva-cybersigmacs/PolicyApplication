package com.example.policy.service;

import com.example.policy.model.Policy;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PolicyService {
    Policy createPolicy(String policyName, String description, MultipartFile policyTemplateList, String version);
    Policy getPolicyById(Long policyId);
    Policy updatePolicy(Long policyId, String policyName, String description);
    void deletePolicy(Long policyId);
}