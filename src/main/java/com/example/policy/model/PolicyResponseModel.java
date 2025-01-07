package com.example.policy.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PolicyResponseModel {
    private long policyId ;

    private String policyName;
    private String description;

    @JsonIncludeProperties({"policyFilesId","policyVersion","status","effctiveStartDate","effectiveEndDate"})
    private List<PolicyFiles> policyFilesList;
}
