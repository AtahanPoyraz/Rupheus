package ai.rupheus.application.common.validator;

import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.Validator;
import java.util.Set;

@Component
public class ObjectValidator {
    private final Validator validator;

    @Autowired
    public ObjectValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = this.validator.validate(object);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid object: " + violations.iterator().next().getMessage());
        }
    }
}
