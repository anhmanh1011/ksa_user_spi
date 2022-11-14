package com.ksa.model;

import lombok.Data;

@Data
public class UserDto {
    String id;
    String customerCode;
    String email;
    String phone;
    String fullName;
    String isReset;
}
