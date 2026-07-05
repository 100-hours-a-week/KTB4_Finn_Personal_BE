package kr.ktb.finn_week6;

import kr.ktb.finn_week6.domain.user.dto.command.CreateUserCommand;
import kr.ktb.finn_week6.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.IntStream;

@Configuration
@RequiredArgsConstructor
public class InitEntityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner init(){
        return args -> initUser();
    }


    void initUser(){

        IntStream.range(0, 10).forEach(i -> {

            String email = "dummy" + i + "@gmail.com";
            String password = "1234";
            String nickname = "dummy" + i;
            CreateUserCommand createUserCommand = new CreateUserCommand(nickname, email, password, null);
            userService.register(createUserCommand);
        });
    }
}
