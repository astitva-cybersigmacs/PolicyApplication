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
import java.util.ArrayList;
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

        // If the role is REVIEWER, create a PolicyReviewer record
        if (role == PolicyRole.REVIEWER) {
            // Get the latest policy file
            if (policy.getPolicyFilesList() != null && !policy.getPolicyFilesList().isEmpty()) {
                PolicyFiles latestPolicyFile = policy.getPolicyFilesList()
                        .get(policy.getPolicyFilesList().size() - 1);

                PolicyApproverAndReviewer reviewer = new PolicyApproverAndReviewer();
                reviewer.setUserId(userId);
                reviewer.setRole(PolicyRole.REVIEWER);
                reviewer.setPolicyFiles(latestPolicyFile);
                this.policyApproverAndReviewerRepository.save(reviewer);
            }
        }

        if (role == PolicyRole.APPROVER) {
            // Get the latest policy file
            if (policy.getPolicyFilesList() != null && !policy.getPolicyFilesList().isEmpty()) {
                PolicyFiles latestPolicyFile = policy.getPolicyFilesList()
                        .get(policy.getPolicyFilesList().size() - 1);

                PolicyApproverAndReviewer approver = new PolicyApproverAndReviewer();
                approver.setUserId(userId);
                approver.setRole(PolicyRole.APPROVER);
                approver.setApproved(false); // default value
                approver.setPolicyFiles(latestPolicyFile);
                this.policyApproverAndReviewerRepository.save(approver);
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
    public PolicyApproverAndReviewer updatePolicyReviewer(Long policyId, Long userId, boolean isAccepted, String rejectedReason) {
        // First validate if policy exists
        this.policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

        // Find reviewer for this user and policy
        List<PolicyApproverAndReviewer> reviewers = this.policyApproverAndReviewerRepository.findByUserIdAndPolicyFiles_Policy_PolicyId(userId, policyId);

        // Get the most recent reviewer
        PolicyApproverAndReviewer reviewer = reviewers.get(reviewers.size() - 1);
        reviewer.setApproved(isAccepted);
        reviewer.setRejectedReason(rejectedReason);
        PolicyApproverAndReviewer updatedReviewer = this.policyApproverAndReviewerRepository.save(reviewer);

        // Update the policy files final acceptance status
        PolicyFiles policyFiles = reviewer.getPolicyFiles();

        List<PolicyApproverAndReviewer> allReviewers = this.policyApproverAndReviewerRepository
                .findByPolicyFiles(policyFiles)
                .stream()
                .filter(r -> r.getRole() == PolicyRole.REVIEWER)
                .toList();

        if (!allReviewers.isEmpty()) {
            long acceptedCount = allReviewers.stream()
                    .filter(PolicyApproverAndReviewer::isApproved)
                    .count();
            double acceptancePercentage = (acceptedCount * 100.0) / allReviewers.size();
            boolean shouldAccept = acceptancePercentage > 50;
            policyFiles.setFinalAcceptance(shouldAccept);
            this.policyFilesRepository.save(policyFiles);
        }

        return updatedReviewer;
    }

    @Override
    @Transactional
    public PolicyApproverAndReviewer updatePolicyApprover(Long policyId, Long userId, boolean isApproved, String rejectedReason) {
        // First validate if policy exists
        this.policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

        // Find approvers for this user and policy
        List<PolicyApproverAndReviewer> approvers = this.policyApproverAndReviewerRepository
                .findByUserIdAndPolicyFiles_Policy_PolicyId(userId, policyId);

        // Get the most recent approver
        PolicyApproverAndReviewer approver = approvers.get(approvers.size() - 1);
        approver.setApproved(isApproved);
        approver.setRejectedReason(rejectedReason);
        PolicyApproverAndReviewer updatedApprover = this.policyApproverAndReviewerRepository.save(approver);

        // Update the policy files final approval status
        PolicyFiles policyFiles = approver.getPolicyFiles();

        // Only proceed if the policy has final acceptance from reviewers
        if (policyFiles.isFinalAcceptance()) {
            // Get all approvers for this policy file
            List<PolicyApproverAndReviewer> allApprovers = this.policyApproverAndReviewerRepository
                    .findByPolicyFiles(policyFiles)
                    .stream()
                    .filter(a -> a.getRole() == PolicyRole.APPROVER)
                    .toList();

            // Check if any approver has rejected
            boolean hasRejection = allApprovers.stream()
                    .anyMatch(a -> !a.isApproved());

            // Check if all approvers have made a decision
            // Changed this part to handle null rejectedReason
            boolean allApproversResponded = allApprovers.stream()
                    .allMatch(a -> {
                        if (a.isApproved()) {
                            return true; // If approved, no need to check reason
                        } else {
                            // If not approved, check if rejected reason exists and is not empty
                            String reason = a.getRejectedReason();
                            return reason != null && !reason.trim().isEmpty();
                        }
                    });

            if (allApproversResponded) {
                if (hasRejection) {
                    // If any approver rejected, mark as rejected
                    policyFiles.setFinalApproval(false);
                    policyFiles.setStatus("REJECTED");
                } else {
                    // If no rejections and all approved, mark as approved
                    policyFiles.setFinalApproval(true);
                    policyFiles.setStatus("APPROVED");
                }
                this.policyFilesRepository.save(policyFiles);
            }
        } else {
            System.out.println("Policy does not have final acceptance from reviewers yet");
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

            // Add to policy's files list
            if (policy.getPolicyFilesList() == null) {
                policy.setPolicyFilesList(new ArrayList<>());
            }
            policy.getPolicyFilesList().add(policyFile);

            // Save the policy file
            return this.policyFilesRepository.save(policyFile);

        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public PolicyFiles getPolicyFilesById(Long policyFilesId) {
        return this.policyFilesRepository.findById(policyFilesId).orElse(null);
    }
}
