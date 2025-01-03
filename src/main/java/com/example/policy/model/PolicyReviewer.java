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
@Table(name = "policy_reviewer")
public class PolicyReviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_reviewer_id")
    private long policyReviewerId ;

    private long userId;

    private String rejectedReason;

    private boolean isAccepted;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "policy_files_id")
    @JsonIgnore
    private PolicyFiles policyFiles;


}
