package ai.rupheus.application.dto.admin;

import ai.rupheus.application.model.user.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "First name cannot be empty")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    private String lastName;

    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{6,20}$",
            message = "Password must contain at least one letter, one number, and one special character"
    )
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotNull(message = "Is enable cannot be null")
    private Boolean isEnabled;

    @NotNull(message = "Is account non expired cannot be null")
    private Boolean isAccountNonExpired;

    @NotNull(message = "Is account non locked cannot be null")
    private Boolean isAccountNonLocked;

    @NotNull(message = "Is credentials non expired cannot be null")
    private Boolean isCredentialsNonExpired;

    @NotNull(message = "Roles cannot be null")
    private EnumSet<UserRole> roles;
}