package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.application.port.in.RegisteredUser;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserRegistrationUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 驱动适配器:只依赖入港接口,不感知应用服务实现
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRegistrationUseCase userRegistrationUseCase;

    public UserController(UserRegistrationUseCase userRegistrationUseCase) {
        this.userRegistrationUseCase = userRegistrationUseCase;
    }

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisteredUser registered = userRegistrationUseCase.register(request.toCommand());
        return Result.ok(RegisterResponse.from(registered));
    }
}
