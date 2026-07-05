package kr.ktb.finn_week6.domain.comment.service;

import kr.ktb.finn_week6.domain.comment.Comment;
import kr.ktb.finn_week6.domain.comment.dto.command.CreateCommentCommand;
import kr.ktb.finn_week6.domain.comment.dto.command.DeleteCommentCommand;
import kr.ktb.finn_week6.domain.comment.dto.command.UpdateCommentCommand;
import kr.ktb.finn_week6.domain.comment.dto.response.CommentDetailResponse;
import kr.ktb.finn_week6.domain.comment.dto.response.CreateCommentResponse;
import kr.ktb.finn_week6.domain.comment.dto.response.UpdateCommentResponse;
import kr.ktb.finn_week6.domain.comment.repository.CommentRepository;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.repository.PostRepository;
import kr.ktb.finn_week6.domain.user.User;
import kr.ktb.finn_week6.domain.user.repository.UserRepository;
import kr.ktb.finn_week6.global.PermissionValidator;
import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.dto.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService{
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PermissionValidator permissionValidator;

    @Transactional
    public CreateCommentResponse register(CreateCommentCommand command){
        User user = userRepository.findById(command.userId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        Post post = postRepository.findById(command.postId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );

        if(post.isDeleted()){
            throw new NotFoundException(RequestMessage.NOT_FOUND.getDescription());
        }

        Comment comment = new Comment(user, post, command.content());
        Comment savedComment = commentRepository.save(comment);
        post.increaseCommentCount();

        return CreateCommentResponse.createCommentResponse(savedComment);
    }

    public List<CommentDetailResponse> getCommentsByPostId(Long postId, Long loginUserId){

        List<Comment> comments = commentRepository.findByPostIdWithUser(postId);

        return comments.stream()
                .map(comment -> {
                    User user = comment.getUser();
                    Long userId = comment.getUser().getId();

                    boolean commentOwner = userId.equals(loginUserId);

                    return new CommentDetailResponse(
                            comment.getId(),
                            comment.getUser().getId(),
                            user.getNickname(),
                            user.getProfileImg(),
                            comment.getCreatedAt(),
                            comment.getContent(),
                            commentOwner
                    );
                })
                .toList();

    }

    @Transactional
    public UpdateCommentResponse updateComment(UpdateCommentCommand command){

        Comment comment = commentRepository.findById(command.commentId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND.getDescription())
        );
        permissionValidator.validatePermission(comment.getUser().getId(), command.loginUserId());
        comment.updateComment(command.comment());
        return UpdateCommentResponse.createResponse(comment);
    }

    @Transactional
    public void deleteComment(DeleteCommentCommand command){
        Comment comment = commentRepository.findById(command.id()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND.getDescription())
        );
        permissionValidator.validatePermission(comment.getUser().getId(),command.sessionUserId());
        comment.setDeleted();
        comment.getPost().decreaseCommentCount();
    }
}
