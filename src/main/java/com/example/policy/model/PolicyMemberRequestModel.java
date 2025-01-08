package com.example.policy.model;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PolicyMemberRequestModel {
    private long policyId;
    private long userId;
    private PolicyRole role;
}
