package com.sky.dto;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePassordDTO {
   private Long userId;
   private String oldPassword;
   private String newPassword;
}
