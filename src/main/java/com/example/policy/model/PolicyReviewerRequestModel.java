package com.example.policy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PolicyReviewerRequestModel {
    private long policyId;
    private long policyFileId;
    private long userId;
    private boolean isAccepted;
    private boolean isApproved;
    private String rejectedReason;
}
