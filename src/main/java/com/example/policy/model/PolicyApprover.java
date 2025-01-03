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
public class PolicyApprover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_approver_id")
    private long policyApproverId;

    private long userId;

    private String rejectedReason;

    private boolean finalApproval;
    private boolean isApproved;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "policy_files_id")
    @JsonIgnore
    private PolicyFiles policyFiles;

}
