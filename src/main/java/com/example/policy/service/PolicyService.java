package com.example.policy.service;

import com.example.policy.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;


public interface PolicyService {
    Policy createPolicy(Policy policy);
    Policy getPolicyById(Long policyId);
    PolicyApproverAndReviewer updatePolicyReviewer(Long policyId, Long userId, boolean isAccepted, String rejectedReason, Long policyFileId);
    PolicyMembers addPolicyMember(Long policyId, Long userId, PolicyRole role);
    PolicyApproverAndReviewer updatePolicyApprover(Long policyId, Long policyFileId, Long userId, boolean isApproved, String rejectedReason);
    List<Policy> getAllPolicies();
    PolicyFiles updatePolicyFiles(Long policyId, Long policyFileId, MultipartFile file, String version, String status, Date effectiveEndDate);
    PolicyFiles addPolicyFile(Long policyId, MultipartFile file, String version, String status, Date createdDate, Date effectiveStartDate, Date effectiveEndDate);
    PolicyFiles getPolicyFilesById(Long policyFilesId);
}