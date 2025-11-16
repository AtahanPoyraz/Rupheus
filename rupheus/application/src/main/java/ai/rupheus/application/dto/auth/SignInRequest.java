package ai.rupheus.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "email flag cannot be empty")
    private String email;

    @NotBlank(message = "password flag cannot be empty")
    private String password;
}