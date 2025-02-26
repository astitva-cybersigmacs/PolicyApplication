package com.example.policy.controller;

import com.example.policy.model.*;
import com.example.policy.service.PolicyService;
import com.example.policy.utils.FileFormats;
import com.example.policy.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("policy")
@CrossOrigin
public class PolicyController {

    private PolicyService policyService;


    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody Policy policy) {
        try {
            this.policyService.createPolicy(policy);
            return ResponseModel.success("Policy created successfully");
        } catch (Exception e) {
            return ResponseModel.error("Failed to create policy: " + e.getMessage());
        }
    }

    @PostMapping("/files")
    public ResponseEntity<?> addPolicyFile(
            @RequestParam("policyId") Long policyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("version") String version,
            @RequestParam("status") String status,
            @RequestParam("createdDate") @DateTimeFormat(pattern = "dd MMM yyyy") Date createdDate,
            @RequestParam("effectiveStartDate") @DateTimeFormat(pattern = "dd MMM yyyy") Date effectiveStartDate,
            @RequestParam("effectiveEndDate") @DateTimeFormat(pattern = "dd MMM yyyy") Date effectiveEndDate) {

        if (!"CREATED".equals(status)) {
            return ResponseEntity.badRequest().body("Invalid status. Only 'CREATED' is allowed.");
        }

        if (file == null) {
            return ResponseModel.customValidations("file", "File is required");
        }

        // Validate dates
        if (effectiveStartDate != null && effectiveEndDate != null && effectiveStartDate.after(effectiveEndDate)) {
            return ResponseModel.customValidations("dates", "Effective start date must be before effective end date");
        }

        // Validate file format
        if (!FileFormats.proposalFileFormat().contains(file.getContentType())) {
            return ResponseModel.customValidations("fileFormat",
                    "Unsupported file format: " + file.getContentType());
        }

        try {
            this.policyService.addPolicyFile(policyId, file, version, status,
                    createdDate, effectiveStartDate, effectiveEndDate);
            return ResponseModel.success("Policy file added successfully");
        } catch (Exception e) {
            return ResponseModel.error("Failed to add policy file: " + e.getMessage());
        }
    }

    @PostMapping("/members")
    public ResponseEntity<?> addPolicyMember(@RequestBody PolicyMemberRequestModel memberRequest) {
        try {
            this.policyService.addPolicyMember(
                    memberRequest.getPolicyId(),
                    memberRequest.getUserId(),
                    memberRequest.getRole());
            return ResponseModel.success("Policy member added successfully");
        } catch (Exception e) {
            return ResponseModel.error("Failed to add policy member: " + e.getMessage());
        }
    }

    @PutMapping("/reviewer-decision")
    public ResponseEntity<?> updatePolicyReviewer(@RequestBody PolicyReviewerRequestModel reviewerRequest) {
        if (!reviewerRequest.isAccepted() && (reviewerRequest.getRejectedReason() == null || reviewerRequest.getRejectedReason().trim().isEmpty())) {
            return ResponseModel.customValidations("rejectedReason", "Reason is required when rejecting");
        }

        try {
           this.policyService.updatePolicyReviewer(
                    reviewerRequest.getPolicyId(),
                    reviewerRequest.getUserId(),
                    reviewerRequest.isAccepted(),
                    reviewerRequest.getRejectedReason(),
                    reviewerRequest.getPolicyFileId());
            return ResponseModel.update("Policy review updated successfully");
        } catch (RuntimeException e) {
            return ResponseModel.error("Failed to update policy review: " + e.getMessage());
        }
    }

    @PutMapping("/approver-decision")
    public ResponseEntity<?> updatePolicyApprover(@RequestBody PolicyReviewerRequestModel approverRequest) {
        try {

            Policy policy = this.policyService.getPolicyById(approverRequest.getPolicyId());
            if (policy == null) {
                return ResponseModel.error("Policy not found with ID: " + approverRequest.getPolicyId());
            }

           this.policyService.updatePolicyApprover(
                    approverRequest.getPolicyId(),
                    approverRequest.getPolicyFileId(),
                    approverRequest.getUserId(),
                    approverRequest.isApproved(),
                    approverRequest.getRejectedReason());
            return ResponseModel.update("Policy approval updated successfully");
        } catch (RuntimeException e) {
            return ResponseModel.error("Failed to update policy approval: " + e.getMessage());
        }
    }

    @PutMapping("/files")
    public ResponseEntity<?> updatePolicyFiles(
            @RequestParam Long policyId,
            @RequestParam Long policyFileId,
            @RequestParam MultipartFile file,
            @RequestParam String version,
            @RequestParam String status,
            @RequestParam @DateTimeFormat(pattern = "dd MMM yyyy") Date effectiveEndDate) {

        // Validate file format if file is being updated
        if (file != null && !FileFormats.proposalFileFormat().contains(file.getContentType())) {
            return ResponseModel.customValidations("fileFormat",
                    "Unsupported file format: " + file.getContentType());
        }
        try {
            PolicyFiles updatedPolicyFiles = this.policyService.updatePolicyFiles(
                    policyId, policyFileId, file, version, status, effectiveEndDate);
            return ResponseModel.success("Policy files updated successfully", updatedPolicyFiles);
        } catch (RuntimeException e) {
            return ResponseModel.error("Failed to update policy files: " + e.getMessage());
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

    @GetMapping("/{policyFilesId}")
    public ResponseEntity<?> getPolicyById(@PathVariable Long policyFilesId) {
        PolicyFiles policy = this.policyService.getPolicyFilesById(policyFilesId);
        if (policy == null) {
            return ResponseModel.notFound("Policy not found");
        }
        return ResponseModel.success("Policy retrieved successfully", policy);
    }

    @GetMapping("/response/{policyId}")
    public ResponseEntity<?> getPolicyResponseModelById(@PathVariable Long policyId) {
        try {
            // Retrieve the policy from the service
            Policy policy = this.policyService.getPolicyById(policyId);
            if (policy == null) {
                return ResponseModel.notFound("Policy not found");
            }

            // Map Policy to PolicyResponseModel
            PolicyResponseModel responseModel = new PolicyResponseModel(
                    policy.getPolicyId(),
                    policy.getPolicyName(),
                    policy.getDescription(),
                    policy.getPolicyFilesList()
            );

            return ResponseModel.success("Policy response model retrieved successfully", responseModel);
        } catch (Exception e) {
            return ResponseModel.error("Failed to retrieve policy response model: " + e.getMessage());
        }
    }

    @GetMapping("/download/{policyFilesId}")
    public ResponseEntity<?> downloadPolicyFile(@PathVariable Long policyFilesId) {
        try {
            PolicyFiles policyFile = this.policyService.getPolicyFilesById(policyFilesId);
            if (policyFile == null) {
                return ResponseModel.notFound("Policy file not found");
            }

           this.policyService.getPolicyFileContent(policyFilesId);

            return ResponseModel.sendMediaWithDecompress(
                    policyFile.getFile(),
                    policyFile.getFileType()
            );

        } catch (Exception e) {
            return ResponseModel.error("Failed to download policy file: " + e.getMessage());
        }
    }
}
