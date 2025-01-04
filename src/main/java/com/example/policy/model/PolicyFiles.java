package com.example.policy.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "policy_files")
public class PolicyFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_files_id")
    private long policyFilesId ;

    private String policyFileName;
    private String policyVersion;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] file;

    private String fileName;

    private String fileType;

    private boolean finalAcceptance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    @Temporal(TemporalType.TIMESTAMP)
    private Date effctiveStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveEndDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "policy_id")
    @JsonIgnore
    private Policy policy;

    @OneToMany(mappedBy = "policyFiles", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolicyReviewer> policyReviewerList ;

    @OneToMany(mappedBy = "policyFiles", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolicyApprover> policyApproverList;
}
