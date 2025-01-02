package com.example.policy.model;

import com.example.policy.utils.Tracker;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "policy_template")
public class PolicyTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_template_id")
    private long policyTemplateId;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] file;

    private String fileName;

    private String fileType;

    private String version;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "policy_id")
    @JsonIgnore
    private Policy policy;
}
