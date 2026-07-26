package kr.ktb.finn_week6;

import kr.ktb.finn_week6.domain.post.dto.command.CreatePostCommand;
import kr.ktb.finn_week6.domain.post.service.PostService;
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
    private final PostService postService;
//    @Bean
//    ApplicationRunner init(){
//        return args -> initUser();
//    }
//
//
//    void initUser(){
//
//        IntStream.range(0, 10).forEach(i -> {
//
//            String email = "dummy" + i + "@gmail.com";
//            String password = "1234";
//            String nickname = "dummy" + i;
//            CreateUserCommand createUserCommand = new CreateUserCommand(nickname, email, password, null);
//            userService.register(createUserCommand);
//        });
//
//        IntStream.range(0, 10000).forEach(i -> {
//           CreatePostCommand createPostCommand = new CreatePostCommand(1L, "title" + i, "content" + i, "http://localhost:8080/images/posts/bcd32f61-58d7-441a-9e3a-830f9e891090.jpg");
//           postService.register(createPostCommand);
//        });
//    }
}
