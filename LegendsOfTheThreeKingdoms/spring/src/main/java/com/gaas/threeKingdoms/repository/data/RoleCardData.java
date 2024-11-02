package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCardData {

    private String roleName; // Assuming Role enum has a name or identifier that can be stored as a String

    // Convert to domain object
    public RoleCard toDomain() {
        Role role = Role.valueOf(this.roleName); // Convert string back to Role enum
        return new RoleCard(role);
    }

    // Convert from domain object
    public static RoleCardData fromDomain(RoleCard roleCard) {
        return RoleCardData.builder()
                .roleName(roleCard.getRole().name()) // Convert Role enum to its name
                .build();
    }
}
