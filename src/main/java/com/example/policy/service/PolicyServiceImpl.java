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
    private PolicyTemplateRepository policyTemplateRepository;
    private PolicyMembersRepository policyMembersRepository;
    private PolicyFilesRepository policyFilesRepository;
    private UserRepository userRepository;
    private PolicyApproverAndReviewerRepository policyApproverAndReviewerRepository;


    @Override
    @Transactional
    public Policy createPolicy(String policyName, String description, MultipartFile policyTemplateList, String version) {
        Policy policy = new Policy();
        policy.setPolicyName(policyName);
        policy.setDescription(description);

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
    public PolicyApproverAndReviewer updatePolicyReviewer(Long policyId, Long userId, boolean isAccepted, String rejectedReason) {
        // First validate if policy exists
        this.policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

        // Find reviewer for this user and policy
        List<PolicyApproverAndReviewer> reviewers = this.policyApproverAndReviewerRepository
                .findByUserIdAndPolicyFiles_Policy_PolicyId(userId, policyId);

        // Get the most recent reviewer
        PolicyApproverAndReviewer reviewer = reviewers.get(reviewers.size() - 1);
        reviewer.setApproved(isAccepted);
        reviewer.setRejectedReason(rejectedReason);
        PolicyApproverAndReviewer updatedReviewer = this.policyApproverAndReviewerRepository.save(reviewer);

        // Update the policy files final acceptance status
        PolicyFiles policyFiles = reviewer.getPolicyFiles();
        List<PolicyMembers> reviewerMembers = this.policyMembersRepository
                .findByPolicyAndRole(policyFiles.getPolicy(), PolicyRole.REVIEWER);

        if (!reviewerMembers.isEmpty()) {
            long acceptedCount = this.policyApproverAndReviewerRepository
                    .findByPolicyFiles(policyFiles)
                    .stream()
                    .filter(PolicyApproverAndReviewer::isApproved)
                    .count();

            double acceptancePercentage = (acceptedCount * 100.0) / reviewerMembers.size();
            policyFiles.setFinalAcceptance(acceptancePercentage > 50);
            this.policyFilesRepository.save(policyFiles);
        }

        return updatedReviewer;
    }

    @Override
    @Transactional
    public PolicyApproverAndReviewer updatePolicyApprover(Long policyId, Long userId, boolean isApproved, String rejectedReason) {
        // First validate if policy exists
        this.policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));


        // Find approver for this user and policy using the corrected method name
        List<PolicyApproverAndReviewer> approvers = this.policyApproverAndReviewerRepository.findByUserIdAndPolicyFiles_Policy_PolicyId(userId, policyId);

        // Get the most recent approver
        PolicyApproverAndReviewer approver = approvers.get(approvers.size() - 1);
        approver.setApproved(isApproved);
        approver.setRejectedReason(rejectedReason);
        PolicyApproverAndReviewer updatedApprover = this.policyApproverAndReviewerRepository.save(approver);

        // Update the policy files final approval status
        PolicyFiles policyFiles = approver.getPolicyFiles();

        // First check if finalAcceptance is true
        if (policyFiles.isFinalAcceptance()) {
            List<PolicyMembers> approverMembers = this.policyMembersRepository
                    .findByPolicyAndRole(policyFiles.getPolicy(), PolicyRole.APPROVER);

            if (!approverMembers.isEmpty()) {
                // Check if any approver has rejected
                boolean hasRejection = this.policyApproverAndReviewerRepository
                        .findByPolicyFiles(policyFiles)
                        .stream()
                        .anyMatch(a -> !a.isApproved());

                // If no approver rejected and finalAcceptance is true, set finalApproval to true
                policyFiles.setFinalApproval(!hasRejection);
                this.policyFilesRepository.save(policyFiles);
            }
        }
        return updatedApprover;
    }
    @Override
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Override
    @Transactional
    public PolicyFiles updatePolicyFiles(Long policyId, String policyFileName, MultipartFile file, String version) {
        Policy policy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        try {
            PolicyFiles policyFiles = new PolicyFiles();
            policyFiles.setPolicyFileName(policyFileName);
            policyFiles.setPolicyVersion(version);
            policyFiles.setCreatedDate(new Date());
            policyFiles.setPolicy(policy);

            // Set file details
            policyFiles.setFileName(file.getOriginalFilename());
            policyFiles.setFileType(file.getContentType());
            policyFiles.setFile(FileUtils.compressFile(file.getBytes()));

            // Set initial approval states
            policyFiles.setFinalAcceptance(false);
            policyFiles.setFinalApproval(false);

            // Add to policy's files list
            policy.getPolicyFilesList().add(policyFiles);

            // Save and return
            return this.policyFilesRepository.save(policyFiles);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + file.getOriginalFilename());
        }
    }
}
