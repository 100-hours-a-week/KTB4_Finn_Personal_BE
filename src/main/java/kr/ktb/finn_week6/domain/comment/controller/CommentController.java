package kr.ktb.finn_week6.domain.comment.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.ktb.finn_week6.domain.comment.dto.command.DeleteCommentCommand;
import kr.ktb.finn_week6.domain.comment.dto.command.UpdateCommentCommand;
import kr.ktb.finn_week6.domain.comment.dto.request.UpdateCommentRequest;
import kr.ktb.finn_week6.domain.comment.dto.response.UpdateCommentResponse;
import kr.ktb.finn_week6.domain.comment.service.CommentService;
import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.SessionManager;
import kr.ktb.finn_week6.global.dto.ApiResponse;
import kr.ktb.finn_week6.security.auth.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final SessionManager sessionManager;
    private final LoginUserProvider loginUserProvider;

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UpdateCommentResponse> updateComment(@PathVariable Long commentId, @Valid @RequestBody UpdateCommentRequest request){
        Long loginUserId = loginUserProvider.getLoginUserId();
        UpdateCommentCommand commentCommand = request.createCommentCommand(loginUserId, commentId, request.comment());
        UpdateCommentResponse updateCommentResponse = commentService.updateComment(commentCommand);

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), updateCommentResponse);
    }
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId){
        Long loginUserId = loginUserProvider.getLoginUserId();
        DeleteCommentCommand deleteCommentCommand = DeleteCommentCommand.createDeleteCommentCommand(commentId, loginUserId);
        commentService.deleteComment(deleteCommentCommand);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), null);
    }
}
