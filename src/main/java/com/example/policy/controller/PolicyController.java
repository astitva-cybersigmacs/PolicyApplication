package com.example.policy.controller;

import com.example.policy.model.*;
import com.example.policy.repository.PolicyApproverRepository;
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
    private PolicyApproverRepository policyApproverRepository;

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestParam(name = "policyName") String policyName,
                                          @RequestParam(name = "description") String description,
                                          @RequestParam(name = "policyTemplate") MultipartFile policyTemplate,
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

    @PutMapping
    public ResponseEntity<?> updatePolicy(@RequestParam Long policyId,
                                          @RequestParam String policyName,
                                          @RequestParam String description) {
        // Validate input parameters
        if (policyName == null || policyName.trim().isEmpty()) {
            return ResponseModel.customValidations("policyName", "Policy name cannot be empty");
        }

        try {
            Policy updatedPolicy = this.policyService.updatePolicy(policyId, policyName, description);
            return ResponseModel.success("Policy updated successfully", updatedPolicy);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Policy not found")) {
                return ResponseModel.notFound(e.getMessage());
            }
            return ResponseModel.error("Failed to update policy: " + e.getMessage());
        }
    }


    @DeleteMapping
    public ResponseEntity<?> deletePolicy(@RequestParam Long policyId) {
        if (policyId == null) {
            return ResponseModel.customValidations("policyId", "Policy ID cannot be null");
        }
        try {
            this.policyService.deletePolicy(policyId);
            return ResponseModel.success("Policy deleted successfully");
        } catch (RuntimeException e) {
            // Check if the error is about the policy not being found
            if (e.getMessage().contains("Policy not found")) {
                return ResponseModel.notFound(e.getMessage());
            }
            return ResponseModel.error("Failed to delete policy: " + e.getMessage());
        }
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
            @RequestParam Long userId,
            @RequestParam boolean isAccepted,
            @RequestParam(required = false) String rejectedReason) {

        if (!isAccepted && (rejectedReason == null || rejectedReason.trim().isEmpty())) {
            return ResponseModel.customValidations("rejectedReason", "Reason is required when rejecting");
        }

        try {
            PolicyReviewer updatedReviewer = this.policyService.updatePolicyReviewer(
                    userId, isAccepted, rejectedReason);
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
            @RequestParam Long userId,
            @RequestParam boolean isApproved,
            @RequestParam(required = false) String rejectedReason) {

        if (!isApproved && (rejectedReason == null || rejectedReason.trim().isEmpty())) {
            return ResponseModel.customValidations("rejectedReason", "Reason is required when rejecting");
        }

        try {
            List<PolicyApprover> approvers = policyApproverRepository.findAllByUserId(userId);
            if (approvers.isEmpty()) {
                return ResponseModel.error("No approvers found for user ID: " + userId);
            }

            PolicyApprover approver = approvers.get(approvers.size() - 1);
            PolicyFiles policyFiles = approver.getPolicyFiles();

            // Check if finalAcceptance is true before proceeding
            if (!policyFiles.isFinalAcceptance()) {
                return ResponseModel.error("Cannot proceed with approval. Policy must be accepted by reviewers first.");
            }
            PolicyApprover updatedApprover = this.policyService.updatePolicyApprover(
                    userId, isApproved, rejectedReason);
            return ResponseModel.success("Policy approval updated successfully", updatedApprover);
        } catch (RuntimeException e) {
            return ResponseModel.error("Failed to update policy approval: " + e.getMessage());
        }
    }

}
