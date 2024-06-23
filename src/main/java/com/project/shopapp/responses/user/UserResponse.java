package com.project.shopapp.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Role;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String address;
    private boolean active;
    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
    @JsonProperty("facebook_account_id")
    private int facebookAccountId;
    @JsonProperty("google_account_id")
    private int googleAccountId;
    @JsonProperty("role_id")
    private Role roleId;
}
