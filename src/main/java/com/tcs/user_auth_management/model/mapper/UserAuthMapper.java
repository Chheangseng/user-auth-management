package com.tcs.user_auth_management.model.mapper;

import com.tcs.user_auth_management.model.dto.user.DtoUserRegister;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {PasswordEncoder.class})
public interface UserAuthMapper {
  @Mapping(target = "password", expression = "java(passwordEncoder.encode(register.password()))")
  @Mapping(source = "register.email", target = "recoveryEmail")
  @Mapping(source = "register.fullName", target = "displayName")
  UserAuth toEntity(DtoUserRegister register, PasswordEncoder passwordEncoder);
}
