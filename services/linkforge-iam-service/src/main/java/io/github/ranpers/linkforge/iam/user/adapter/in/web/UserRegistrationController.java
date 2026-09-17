package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.application.port.in.RegisteredUser;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserRegistrationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接收公开用户注册请求。
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserRegistrationController {

    private final UserRegistrationUseCase userRegistration;

    public UserRegistrationController(UserRegistrationUseCase userRegistration) {
        this.userRegistration = userRegistration;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        RegisteredUser registered = userRegistration.register(request.toCommand());
        return RegisterResponse.from(registered);
    }
}
