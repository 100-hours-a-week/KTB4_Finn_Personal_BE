package kr.ktb.finn_week6.domain.like.controller;


import jakarta.servlet.http.HttpSession;
import kr.ktb.finn_week6.domain.like.dto.response.LikeResponse;
import kr.ktb.finn_week6.domain.like.service.LikeService;
import kr.ktb.finn_week6.domain.post.service.PostService;
import kr.ktb.finn_week6.global.SessionManager;
import kr.ktb.finn_week6.security.auth.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    private final PostService postService;
    private final LoginUserProvider loginUserProvider;


//    @DeleteMapping("/{likeId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public LikeResponse unLikePost(@PathVariable Long likeId){
//        Long loginUserId = loginUserProvider.getLoginUserId();
//        likeService.deleteLike(likeId, loginUserId);
//
//        return new LikeResponse(likeId,);
//    }

}
