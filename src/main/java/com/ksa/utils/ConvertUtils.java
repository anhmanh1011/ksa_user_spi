package com.ksa.utils;

import com.ksa.entity.KsaCustomerEntity;
import com.ksa.model.UserDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConvertUtils {
    public static UserDto convertKsaCustomerEntityToUserDto(KsaCustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setCustomerCode(entity.getCustomerCode());
        userDto.setEmail(entity.getEmail());
        userDto.setFullName(entity.getName());
        userDto.setPhone(entity.getMobile());
        userDto.setId(entity.getId());
        return userDto;
    }
}
