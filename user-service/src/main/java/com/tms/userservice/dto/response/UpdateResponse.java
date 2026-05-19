package com.tms.userservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateResponse{
    private String username;
    private String phoneNumber;
    private String jobTitle;
}
