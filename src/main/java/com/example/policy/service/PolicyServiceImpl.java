package com.example.policy.service;

import com.example.policy.model.*;
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
    private PolicyTemplateRepository policyTemplateRepository;
    private PolicyReviewerRepository policyReviewerRepository;
    private PolicyMembersRepository policyMembersRepository;
    private PolicyFilesRepository policyFilesRepository;
    private UserRepository userRepository;
    private PolicyApproverRepository policyApproverRepository;


    @Override
    @Transactional
    public Policy createPolicy(String policyName, String description, MultipartFile policyTemplateList, String version) {
        Policy policy = new Policy();
        policy.setPolicyName(policyName);
        policy.setDescription(description);
        policy.setStatus(false); // You can modify this based on your requirements

        // Create and save policy template
        List<PolicyTemplate> templates = new ArrayList<>();
        try {
            PolicyTemplate template = new PolicyTemplate();
            template.setFileName(policyTemplateList.getOriginalFilename());
            template.setFileType(policyTemplateList.getContentType());
            template.setFile(FileUtils.compressFile(policyTemplateList.getBytes()));
            template.setVersion(version);
            template.setPolicy(policy);
            templates.add(template);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + policyTemplateList.getOriginalFilename());
        }
        policy.setPolicyTemplateList(templates);

        // Create PolicyFiles
        PolicyFiles policyFiles = new PolicyFiles();
        policyFiles.setPolicyFileName(policyName + " File");
        policyFiles.setPolicyVersion(version);
        policyFiles.setCreatedDate(new Date());
        policyFiles.setPolicy(policy);

        List<PolicyFiles> policyFilesList = new ArrayList<>();
        policyFilesList.add(policyFiles);
        policy.setPolicyFilesList(policyFilesList);

        // Create PolicyMembers list
        List<PolicyMembers> policyMembersList = new ArrayList<>();
        policy.setPolicyMembersList(policyMembersList);

        // Save policy first to get the ID
        Policy savedPolicy = this.policyRepository.save(policy);

        return savedPolicy;
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

                PolicyReviewer reviewer = new PolicyReviewer();
                reviewer.setUserId(userId);
                reviewer.setAccepted(false); // default value
                reviewer.setPolicyFiles(latestPolicyFile);
                this.policyReviewerRepository.save(reviewer);
            }
        }

        if (role == PolicyRole.APPROVER) {
            // Get the latest policy file
            if (policy.getPolicyFilesList() != null && !policy.getPolicyFilesList().isEmpty()) {
                PolicyFiles latestPolicyFile = policy.getPolicyFilesList()
                        .get(policy.getPolicyFilesList().size() - 1);

                PolicyApprover approver = new PolicyApprover();
                approver.setUserId(userId);
                approver.setApproved(false); // default value
                approver.setPolicyFiles(latestPolicyFile);
                this.policyApproverRepository.save(approver);
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
    public Policy updatePolicy(Long policyId, String policyName, String description) {
        // Find existing policy or throw exception
        Policy existingPolicy = this.policyRepository.findById(policyId).orElse(null);

        // Update basic fields
        existingPolicy.setPolicyName(policyName);
        existingPolicy.setDescription(description);

        // Save and return the updated policy
        return this.policyRepository.save(existingPolicy);
    }


    @Override
    @Transactional
    public void deletePolicy(Long policyId) {
        Policy policy = this.policyRepository.findById(policyId).orElse(null);
        this.policyRepository.delete(policy);
    }

    @Override
    @Transactional
    public Policy updatePolicyTemplate(Long policyId, MultipartFile policyTemplateList, String version) {
        Policy existingPolicy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        try {
            PolicyTemplate template = new PolicyTemplate();
            template.setFileName(policyTemplateList.getOriginalFilename());
            template.setFileType(policyTemplateList.getContentType());
            template.setFile(FileUtils.compressFile(policyTemplateList.getBytes()));
            template.setVersion(version);
            template.setPolicy(existingPolicy);

            existingPolicy.getPolicyTemplateList().add(template);

            return this.policyRepository.save(existingPolicy);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + policyTemplateList.getOriginalFilename());
        }
    }

    @Override
    public PolicyTemplate getPolicyTemplateById(Long templateId) {
        return this.policyTemplateRepository.findById(templateId).orElse(null);
    }

    @Override
    @Transactional
    public PolicyReviewer updatePolicyReviewer(Long userId, boolean isAccepted, String rejectedReason) {
        List<PolicyReviewer> reviewers = this.policyReviewerRepository.findAllByUserId(userId);
        if (reviewers.isEmpty()) {
            throw new RuntimeException("No reviewers found for user ID: " + userId);
        }

        // Get the most recent reviewer (assuming it's the last one in the list)
        PolicyReviewer reviewer = reviewers.get(reviewers.size() - 1);

        reviewer.setAccepted(isAccepted);
        reviewer.setRejectedReason(rejectedReason);
        PolicyReviewer updatedReviewer = this.policyReviewerRepository.save(reviewer);

        // Update the policy files final acceptance status
        PolicyFiles policyFiles = reviewer.getPolicyFiles();
        List<PolicyMembers> reviewerMembers = this.policyMembersRepository
                .findByPolicyAndRole(policyFiles.getPolicy(), PolicyRole.REVIEWER);

        if (!reviewerMembers.isEmpty()) {
            long acceptedCount = this.policyReviewerRepository
                    .findByPolicyFiles(policyFiles)
                    .stream()
                    .filter(PolicyReviewer::isAccepted)
                    .count();

            double acceptancePercentage = (acceptedCount * 100.0) / reviewerMembers.size();
            policyFiles.setFinalAcceptance(acceptancePercentage > 50);
            this.policyFilesRepository.save(policyFiles);
        }

        return updatedReviewer;
    }

    @Override
    @Transactional
    public PolicyApprover updatePolicyApprover(Long userId, boolean isApproved, String rejectedReason) {
        // First, find all approvers for this user (similar to reviewer logic)
        List<PolicyApprover> approvers = this.policyApproverRepository.findAllByUserId(userId);
        if (approvers.isEmpty()) {
            throw new RuntimeException("No approvers found for user ID: " + userId);
        }

        // Get the most recent approver (assuming it's the last one in the list)
        PolicyApprover approver = approvers.get(approvers.size() - 1);

        // Update approver's decision
        approver.setApproved(isApproved);
        approver.setRejectedReason(rejectedReason);
        PolicyApprover updatedApprover = this.policyApproverRepository.save(approver);

        // Update the policy files final approval status
        PolicyFiles policyFiles = approver.getPolicyFiles();
        List<PolicyMembers> approverMembers = this.policyMembersRepository
                .findByPolicyAndRole(policyFiles.getPolicy(), PolicyRole.APPROVER);

        if (!approverMembers.isEmpty()) {
            // Check if any approver has rejected
            boolean hasRejection = this.policyApproverRepository
                    .findByPolicyFiles(policyFiles)
                    .stream()
                    .anyMatch(a -> !a.isApproved());

            // If any approver rejected, set finalApproval to false
            policyFiles.setFinalAcceptance(!hasRejection);
            this.policyFilesRepository.save(policyFiles);
        }

        return updatedApprover;
    }

}
