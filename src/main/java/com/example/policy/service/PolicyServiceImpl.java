package com.example.policy.service;

import com.example.policy.model.*;
import com.example.policy.model.PolicyApproverAndReviewer;
import com.example.policy.repository.*;
import com.example.policy.utils.FileUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private PolicyRepository policyRepository;
    private PolicyMembersRepository policyMembersRepository;
    private PolicyFilesRepository policyFilesRepository;
    private UserRepository userRepository;
    private PolicyApproverAndReviewerRepository policyApproverAndReviewerRepository;


    @Override
    @Transactional
    public Policy createPolicy(Policy policy) {
        return this.policyRepository.save(policy);
    }

    @Transactional
    public PolicyMembers addPolicyMember(Long policyId, Long userId, PolicyRole role) {
        Policy policy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PolicyMembers policyMember = new PolicyMembers();
        policyMember.setPolicy(policy);
        policyMember.setUser(user);
        policyMember.setRole(role);

        // If the role is REVIEWER or APPROVER, create a PolicyApproverAndReviewer record
        if (role == PolicyRole.REVIEWER || role == PolicyRole.APPROVER) {
            PolicyApproverAndReviewer reviewer = new PolicyApproverAndReviewer();
            reviewer.setUserId(userId);
            reviewer.setRole(role);
            reviewer.setPolicy(policy);
            reviewer.setApproved(false); // default value
            this.policyApproverAndReviewerRepository.save(reviewer);
        }

        if (role == PolicyRole.CREATOR) {
            // Validate that there isn't already a creator for this policy
            List<PolicyMembers> existingCreators = policyMembersRepository.findByPolicyAndRole(policy, PolicyRole.CREATOR);
            if (!existingCreators.isEmpty()) {
                throw new RuntimeException("Policy already has a creator assigned");
            }
        }

        policy.getPolicyMembersList().add(policyMember);
        return this.policyMembersRepository.save(policyMember);
    }


    @Override
    public Policy getPolicyById(Long policyId) {
        return this.policyRepository.findById(policyId).orElse(null);
    }


    @Override
    @Transactional
    public PolicyApproverAndReviewer updatePolicyReviewer(Long policyId, Long userId, boolean isAccepted,
                                                          String rejectedReason, Long policyFileId) {

        Policy policy = this.policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy not found"));
        PolicyFiles policyFile = this.policyFilesRepository.findById(policyFileId).orElseThrow(() -> new RuntimeException("Policy file not found"));

        // Get current reviewer
        PolicyApproverAndReviewer reviewer = this.policyApproverAndReviewerRepository
                .findByUserIdAndPolicy_PolicyIdAndPolicyFiles_PolicyFilesId(userId, policyId, policyFileId)
                .stream()
                .filter(r -> r.getRole() == PolicyRole.REVIEWER)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Check if reviewer has already made a decision
        if (reviewer.isApproved() || reviewer.getRejectedReason() != null) {
            throw new RuntimeException("Reviewer has already made a decision for this policy file.");
        }

        // Update reviewer decision
        reviewer.setApproved(isAccepted);
        reviewer.setRejectedReason(rejectedReason);

        // Get all reviewers and their decisions
        List<PolicyApproverAndReviewer> allReviewers = this.policyApproverAndReviewerRepository.findByPolicyAndRoleAndPolicyFiles(policy, PolicyRole.REVIEWER, policyFile);

        boolean allReviewersResponded = allReviewers.stream()
                .allMatch(r -> r.isApproved() || r.getRejectedReason() != null);

        // Update policy file status if all reviewers have responded
        if (allReviewersResponded) {
            double approvalPercentage = (allReviewers.stream()
                    .filter(PolicyApproverAndReviewer::isApproved)
                    .count() * 100.0) / allReviewers.size();

            boolean shouldAccept = approvalPercentage > 50;
            policyFile.setFinalAcceptance(shouldAccept);
            policyFile.setStatus(shouldAccept ? "UNDER_APPROVAL" : "REJECTED_BY_REVIEWERS");
            this.policyFilesRepository.save(policyFile);
        }

        return this.policyApproverAndReviewerRepository.save(reviewer);
    }

    @Override
    @Transactional
    public PolicyApproverAndReviewer updatePolicyApprover(Long policyId, Long policyFileId, Long userId,
                                                          boolean isApproved, String rejectedReason) {
        // Validate policy and policy file existence
        Policy policy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));
        PolicyFiles policyFile = this.policyFilesRepository.findById(policyFileId)
                .orElseThrow(() -> new RuntimeException("Policy file not found with ID: " + policyFileId));

        // Retrieve approver
        PolicyApproverAndReviewer approver = this.policyApproverAndReviewerRepository
                .findByUserIdAndPolicy_PolicyIdAndPolicyFiles_PolicyFilesId(userId, policyId, policyFileId)
                .stream()
                .filter(a -> a.getRole() == PolicyRole.APPROVER)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (approver.isApproved() || approver.getRejectedReason() != null) {
            throw new RuntimeException("Approver has already made a decision for this policy file.");
        }

        approver.setApproved(isApproved);
        approver.setRejectedReason(rejectedReason);
        PolicyApproverAndReviewer updatedApprover = this.policyApproverAndReviewerRepository.save(approver);

        // Check final acceptance and update status if needed
        if (policy.getPolicyFilesList() != null && !policy.getPolicyFilesList().isEmpty() && policyFile.isFinalAcceptance()) {
            List<PolicyApproverAndReviewer> allApprovers = this.policyApproverAndReviewerRepository
                    .findByPolicyAndRoleAndPolicyFiles(policy, PolicyRole.APPROVER, policyFile);

            boolean hasRejection = allApprovers.stream().anyMatch(a -> !a.isApproved());
            boolean allResponded = allApprovers.stream().allMatch(a -> a.isApproved() || (a.getRejectedReason() != null && !a.getRejectedReason().trim().isEmpty()));

            if (allResponded) {
                policyFile.setFinalApproval(!hasRejection);
                policyFile.setStatus(hasRejection ? "REJECTED" : "APPROVED");
                this.policyFilesRepository.save(policyFile);
            }
        } else {
            System.out.println("Policy file does not have final acceptance from reviewers yet");
        }
        return updatedApprover;
    }

    @Override
    public List<Policy> getAllPolicies() {
        return this.policyRepository.findAll();
    }

    @Override
    @Transactional
    public PolicyFiles updatePolicyFiles(Long policyId, Long policyFileId, MultipartFile file,
                                         String version, String status, Date effectiveEndDate) {

        // First verify policy exists
        this.policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        // Find the specific policy file to update
        PolicyFiles existingPolicyFile = this.policyFilesRepository.findById(policyFileId).orElseThrow(() -> new RuntimeException("Policy file not found with id: " + policyFileId));

        // Verify the policy file belongs to the specified policy
        if (existingPolicyFile.getPolicy().getPolicyId() != policyId) {
            throw new RuntimeException("Policy file does not belong to the specified policy");
        }

        try {
            existingPolicyFile.setFileName(file.getOriginalFilename());
            existingPolicyFile.setFileType(file.getContentType());
            existingPolicyFile.setFile(FileUtils.compressFile(file.getBytes()));
            existingPolicyFile.setPolicyVersion(version);
            existingPolicyFile.setStatus(status);

            if (existingPolicyFile.getEffctiveStartDate() != null && effectiveEndDate.before(existingPolicyFile.getEffctiveStartDate())) {
                    throw new RuntimeException("Effective end date cannot be before effective start date");
            }
            existingPolicyFile.setEffectiveEndDate(effectiveEndDate);

            return this.policyFilesRepository.save(existingPolicyFile);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PolicyFiles addPolicyFile(Long policyId, MultipartFile file, String version,
                                     String status, Date createdDate, Date effectiveStartDate, Date effectiveEndDate) {
        // Get existing policy
        Policy policy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        // Check if the user is a CREATOR
        List<PolicyMembers> creators = this.policyMembersRepository.findByPolicyAndRole(policy, PolicyRole.CREATOR);
        if (creators.isEmpty()) {
            throw new RuntimeException("No CREATOR found for this policy");
        }

        try {
            PolicyFiles policyFile = new PolicyFiles();
            policyFile.setPolicy(policy);
            policyFile.setPolicyVersion(version);
            policyFile.setCreatedDate(createdDate);
            policyFile.setEffctiveStartDate(effectiveStartDate);
            policyFile.setEffectiveEndDate(effectiveEndDate);
            policyFile.setStatus(status);

            // Set file details
            policyFile.setFileName(file.getOriginalFilename());
            policyFile.setFileType(file.getContentType());
            policyFile.setFile(FileUtils.compressFile(file.getBytes()));

            // Set initial approval states
            policyFile.setFinalAcceptance(false);
            policyFile.setFinalApproval(false);

            policy.getPolicyFilesList().add(policyFile);

            // Save the policy file
            PolicyFiles savedPolicyFile = this.policyFilesRepository.save(policyFile);

            // Get all existing reviewers for the policy
            List<PolicyApproverAndReviewer> existingReviewers =
                    policyApproverAndReviewerRepository.findByPolicyAndRole(policy, PolicyRole.REVIEWER);

            // Get all existing approvers for the policy
            List<PolicyApproverAndReviewer> existingApprovers =
                    policyApproverAndReviewerRepository.findByPolicyAndRole(policy, PolicyRole.APPROVER);

            // Create new reviewer records for the new policy file only if they don't already exist
            for (PolicyApproverAndReviewer existing : existingReviewers) {
                // Check if the reviewer already exists for the new policy file
                boolean exists = policyApproverAndReviewerRepository.existsByPolicyAndPolicyFilesAndUserIdAndRole(
                        policy, savedPolicyFile, existing.getUserId(), PolicyRole.REVIEWER);

                if (!exists) {
                    PolicyApproverAndReviewer newRecord = new PolicyApproverAndReviewer();
                    newRecord.setUserId(existing.getUserId());
                    newRecord.setRole(PolicyRole.REVIEWER);
                    newRecord.setPolicy(policy);
                    newRecord.setPolicyFiles(savedPolicyFile);
                    newRecord.setApproved(false);
                    newRecord.setRejectedReason(null);
                    policyApproverAndReviewerRepository.save(newRecord);
                }
            }

            // Create new approver records for the new policy file only if they don't already exist
            for (PolicyApproverAndReviewer existing : existingApprovers) {
                // Check if the approver already exists for the new policy file
                boolean exists = policyApproverAndReviewerRepository.existsByPolicyAndPolicyFilesAndUserIdAndRole(
                        policy, savedPolicyFile, existing.getUserId(), PolicyRole.APPROVER);

                if (!exists) {
                    PolicyApproverAndReviewer newRecord = new PolicyApproverAndReviewer();
                    newRecord.setUserId(existing.getUserId());
                    newRecord.setRole(PolicyRole.APPROVER);
                    newRecord.setPolicy(policy);
                    newRecord.setPolicyFiles(savedPolicyFile);
                    newRecord.setApproved(false);
                    newRecord.setRejectedReason(null);
                    policyApproverAndReviewerRepository.save(newRecord);
                }
            }

            return savedPolicyFile;

        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + file.getOriginalFilename(), e);
        }
    }


    @Override
    public PolicyFiles getPolicyFilesById(Long policyFilesId) {
        return this.policyFilesRepository.findById(policyFilesId).orElse(null);
    }

    @Override
    public byte[] getPolicyFileContent(Long policyFilesId) {
        PolicyFiles policyFile = this.policyFilesRepository.findById(policyFilesId)
                .orElseThrow(() -> new RuntimeException("Policy file not found"));

        return FileUtils.decompressFile(policyFile.getFile());
    }
}
