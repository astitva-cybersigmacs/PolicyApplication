package com.example.policy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class PolicyApproverAndReviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_approver_and_reviewer_id")
    private long policyApproverAndReviewerId;

    private long userId;

    @Enumerated(EnumType.STRING)
    private PolicyRole role;

    private boolean isApproved;
    private String rejectedReason;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "policy_id")
    @JsonIgnore
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "policy_files_id")
    @JsonIgnore
    private PolicyFiles policyFiles;

}
