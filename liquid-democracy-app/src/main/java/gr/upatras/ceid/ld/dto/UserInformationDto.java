package gr.upatras.ceid.ld.dto;

import gr.upatras.ceid.ld.enums.Role;

import java.util.Set;

public record UserInformationDto(
        String username,
        String name,
        String surname,
        String email,
        Set<Role> roles) {
}
