package com.example.policy.controller;

import com.example.policy.model.*;
import com.example.policy.repository.PolicyMembersRepository;
import com.example.policy.service.PolicyService;
import com.example.policy.utils.FileFormats;
import com.example.policy.utils.FileUtils;
import com.example.policy.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("policy")
@CrossOrigin
public class PolicyController {

    private PolicyService policyService;
    private PolicyMembersRepository policyMembersRepository;

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestParam(name = "policyName") String policyName,
                                          @RequestParam(name = "description") String description,
                                          @RequestParam(name = "policyTemplate", required = false) MultipartFile policyTemplate,
                                          @RequestParam(name = "version") String version) {
        if (policyTemplate == null) {
            return ResponseModel.customValidations("policyTemplateList", "policyTemplateList is not present");
        }

        // Validate file format
        if (!FileFormats.proposalFileFormat().contains(policyTemplate.getContentType())) {
            return ResponseModel.customValidations("fileFormat",
                    "Unsupported file format: " + policyTemplate.getContentType());
        }
        try {
            this.policyService.createPolicy(policyName, description, policyTemplate, version); // Pass version to service
            return ResponseModel.success("Policy created successfully");
        } catch (Exception e) {
            return ResponseModel.error("Failed to create policy: " + e.getMessage());
        }
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<?> getPolicyById(@PathVariable Long policyId) {
        Policy policy = this.policyService.getPolicyById(policyId);
        if (policy == null) {
            return ResponseModel.notFound("Policy not found");
        }
        return ResponseModel.success("Policy retrieved successfully", policy);
    }


    @PutMapping("/template")
    public ResponseEntity<?> updatePolicyTemplate(@RequestParam Long policyId,
                                                  @RequestParam MultipartFile policyTemplate,
                                                  @RequestParam String version) {
        if (policyTemplate == null) {
            return ResponseModel.customValidations("policyTemplateList", "policyTemplateList is not present");
        }

        if (!FileFormats.proposalFileFormat().contains(policyTemplate.getContentType())) {
            return ResponseModel.customValidations("fileFormat",
                    "Unsupported file format: " + policyTemplate.getContentType());
        }

        try {
             this.policyService.updatePolicyTemplate(policyId, policyTemplate, version);
             return ResponseModel.success("Policy template updated successfully");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Policy not found")) {
                return ResponseModel.notFound(e.getMessage());
            }
            return ResponseModel.error("Failed to update policy template: " + e.getMessage());
        }
    }

    @GetMapping("/download/template/{templateId}")
    public ResponseEntity<?> downloadPolicyFile(@PathVariable Long templateId) {
        try {
            PolicyTemplate template = policyService.getPolicyTemplateById(templateId);
            if (template == null) {
                return ResponseModel.notFound("Policy template not found with ID: " + templateId);
            }

            byte[] decompressedFile = FileUtils.decompressFile(template.getFile());
            return ResponseModel.mediaFile(
                    template.getFileType(),
                    decompressedFile
            );
        } catch (Exception e) {
            return ResponseModel.error("Error downloading file: " + e.getMessage());
        }
    }

    @PutMapping("/reviewer-decision")
    public ResponseEntity<?> updatePolicyReviewer(
            @RequestParam Long policyId,
            @RequestParam Long userId,
            @RequestParam boolean isAccepted,
            @RequestParam(required = false) String rejectedReason) {

        if (!isAccepted && (rejectedReason == null || rejectedReason.trim().isEmpty())) {
            return ResponseModel.customValidations("rejectedReason", "Reason is required when rejecting");
        }

        try {
            // First check if policy exists
            Policy policy = this.policyService.getPolicyById(policyId);
            if (policy == null) {
                return ResponseModel.error("Policy not found with ID: " + policyId);
            }

            // Check if user is a reviewer for this policy
            List<PolicyMembers> policyMembers = this.policyMembersRepository.findByPolicyAndRole(policy, PolicyRole.REVIEWER);
            boolean isReviewer = policyMembers.stream()
                    .anyMatch(member -> member.getUser().getUserId()==(userId));

            if (!isReviewer) {
                return ResponseModel.error("User is not authorized as a reviewer for this policy");
            }

            PolicyApproverAndReviewer updatedReviewer = this.policyService.updatePolicyReviewer(
                    policyId, userId, isAccepted, rejectedReason);
            return ResponseModel.success("Policy review updated successfully", updatedReviewer);
        } catch (RuntimeException e) {
            return ResponseModel.error("Failed to update policy review: " + e.getMessage());
        }
    }

    @PostMapping("/members")
    public ResponseEntity<?> addPolicyMember(
            @RequestParam Long policyId,
            @RequestParam Long userId,
            @RequestParam PolicyRole role) {
        try {
            this.policyService.addPolicyMember(policyId, userId, role);
            return ResponseModel.success("Policy member added successfully");
        } catch (Exception e) {
            return ResponseModel.error("Failed to add policy member: " + e.getMessage());
        }
    }

    @PutMapping("/approver-decision")
    public ResponseEntity<?> updatePolicyApprover(
            @RequestParam Long policyId,
            @RequestParam Long userId,
            @RequestParam boolean isApproved,
            @RequestParam(required = false) String rejectedReason) {

        if (!isApproved && (rejectedReason == null || rejectedReason.trim().isEmpty())) {
            return ResponseModel.customValidations("rejectedReason", "Reason is required when rejecting");
        }

        try {
            // First check if policy exists
            Policy policy = policyService.getPolicyById(policyId);
            if (policy == null) {
                return ResponseModel.error("Policy not found with ID: " + policyId);
            }

            // Check if user is an approver for this policy
            List<PolicyMembers> policyMembers = policyMembersRepository.findByPolicyAndRole(policy, PolicyRole.APPROVER);
            boolean isApprover = policyMembers.stream()
                    .anyMatch(member -> member.getUser().getUserId() == (userId));

            if (!isApprover) {
                return ResponseModel.error("User is not authorized as an approver for this policy");
            }

            PolicyApproverAndReviewer updatedApprover = this.policyService.updatePolicyApprover(
                    policyId, userId, isApproved, rejectedReason);
            return ResponseModel.success("Policy approval updated successfully", updatedApprover);
        } catch (RuntimeException e) {
            return ResponseModel.error("Failed to update policy approval: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPolicies() {
        try {
            List<Policy> policies = this.policyService.getAllPolicies();
            if (policies.isEmpty()) {
                return ResponseModel.success("No policies found", policies);
            }
            return ResponseModel.success("Policies retrieved successfully", policies);
        } catch (Exception e) {
            return ResponseModel.error("Failed to retrieve policies: " + e.getMessage());
        }
    }

    @PutMapping("/files")
    public ResponseEntity<?> updatePolicyFiles(
            @RequestParam Long policyId,
            @RequestParam String policyFileName,
            @RequestParam MultipartFile file,
            @RequestParam String version) {

        if (file == null) {
            return ResponseModel.customValidations("file", "File is required");
        }

        // Validate file format
        if (!FileFormats.proposalFileFormat().contains(file.getContentType())) {
            return ResponseModel.customValidations("fileFormat",
                    "Unsupported file format: " + file.getContentType());
        }

        try {
            PolicyFiles updatedPolicyFiles = this.policyService.updatePolicyFiles(
                    policyId, policyFileName, file, version);
            return ResponseModel.success("Policy files updated successfully", updatedPolicyFiles);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Policy not found")) {
                return ResponseModel.notFound(e.getMessage());
            }
            return ResponseModel.error("Failed to update policy files: " + e.getMessage());
        }
    }

}
