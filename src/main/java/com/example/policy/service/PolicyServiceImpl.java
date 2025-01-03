package com.example.policy.service;

import com.example.policy.model.Policy;
import com.example.policy.model.PolicyTemplate;
import com.example.policy.repository.PolicyRepository;
import com.example.policy.utils.FileUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private PolicyRepository policyRepository;


    @Override
    public Policy createPolicy(String policyName, String description, MultipartFile policyTemplateList, String version) { // Added version parameter
        Policy policy = new Policy();
        policy.setPolicyName(policyName);
        policy.setDescription(description);
        List<PolicyTemplate> templates = new ArrayList<>();

        try {
            // Handling single file upload
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
        return this.policyRepository.save(policy);
    }


    @Override
    public Policy getPolicyById(Long policyId) {
        return this.policyRepository.findById(policyId).orElse(null);
    }

    @Override
    @Transactional
    public Policy updatePolicy(Long policyId, String policyName, String description) {
        // Find existing policy or throw exception
        Policy existingPolicy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        // Update basic fields
        existingPolicy.setPolicyName(policyName);
        existingPolicy.setDescription(description);

        // Save and return the updated policy
        return this.policyRepository.save(existingPolicy);
    }


    @Override
    @Transactional
    public void deletePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        policyRepository.delete(policy);
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

            // Clear and add new template while maintaining the policy reference
            existingPolicy.getPolicyTemplateList().clear();
            existingPolicy.getPolicyTemplateList().add(template);

            return this.policyRepository.save(existingPolicy);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + policyTemplateList.getOriginalFilename());
        }
    }

    @Override
    public byte[] downloadPolicyFile(Long policyId) {
        Policy policy = this.policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        if (policy.getPolicyTemplateList().isEmpty()) {
            throw new RuntimeException("No template found for policy id: " + policyId);
        }

        PolicyTemplate template = policy.getPolicyTemplateList().get(0);
        return template.getFile();
    }

}
