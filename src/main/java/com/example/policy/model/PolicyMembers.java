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
@Table(name = "policy_members")
public class PolicyMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_member_id")
    private long policyMemberId ;

    @Enumerated(EnumType.STRING)
    private PolicyRole role;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "policy_id")
    @JsonIgnore
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
