package com.example.policy.controller;

import com.example.policy.model.Policy;
import com.example.policy.service.PolicyService;
import com.example.policy.utils.FileFormats;
import com.example.policy.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestParam String policyName,
                                          @RequestParam String description,
                                          @RequestParam MultipartFile policyTemplateList,
                                          @RequestParam String version) {
        if (policyTemplateList == null) {
            return ResponseModel.customValidations("policyTemplateList", "policyTemplateList is not present");
        }

        // Validate file format
        if (!FileFormats.proposalFileFormat().contains(policyTemplateList.getContentType())) {
            return ResponseModel.customValidations("fileFormat",
                    "Unsupported file format: " + policyTemplateList.getContentType());
        }
        try {
            Policy createdPolicy = this.policyService.createPolicy(policyName, description, policyTemplateList, version); // Pass version to service
            return ResponseModel.success("Policy created successfully", createdPolicy);
        } catch (Exception e) {
            return ResponseModel.error("Failed to create policy: " + e.getMessage());
        }
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<?> getPolicyById(@PathVariable Long policyId) {
        Policy policy = this.policyService.getPolicyById(policyId);
        if (policy == null) {
            return ResponseModel.notFound("Policy with ID " + policyId + " not found");
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

        if (description == null || description.trim().isEmpty()) {
            return ResponseModel.customValidations("description", "Description cannot be empty");
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

}
