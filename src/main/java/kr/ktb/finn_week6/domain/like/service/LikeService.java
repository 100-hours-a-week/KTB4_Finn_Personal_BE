package kr.ktb.finn_week6.domain.like.service;

import kr.ktb.finn_week6.domain.like.Like;
import kr.ktb.finn_week6.domain.like.dto.command.LikePostCommand;
import kr.ktb.finn_week6.domain.like.dto.response.LikeResponse;
import kr.ktb.finn_week6.domain.like.repository.LikeRepository;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.repository.PostRepository;
import kr.ktb.finn_week6.domain.user.User;
import kr.ktb.finn_week6.domain.user.repository.UserRepository;
import kr.ktb.finn_week6.global.PermissionValidator;
import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.customException.IllegalResourceStateException;
import kr.ktb.finn_week6.global.dto.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PermissionValidator permissionValidator;

    @Transactional
    public LikeResponse likePost(LikePostCommand command){
        User user = userRepository.findById(command.userId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        Post post = postRepository.findById(command.postId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );
        Like like = likeRepository.findByPostIdAndUserId(command.postId(), command.userId()).orElse(null);

        if (like != null) {

            if (!like.isDeleted()) {
                throw new IllegalResourceStateException(RequestMessage.ALREADY_LIKED.getDescription());
            }
            like.recovered();
        } else {
            like = new Like(user, post);
            likeRepository.save(like);
        }
        post.increaseLikeCount();
        return new LikeResponse(like.getId(),true ,like.getPost().getLikeCount());
    }

    @Transactional
        public LikeResponse deleteLike(LikePostCommand command){
        Like like = likeRepository.findByPostIdAndUserId(command.postId(), command.userId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND.getDescription())
        );
        permissionValidator.validatePermission(like.getUser().getId(), command.userId());
        like.setDeleted();
        like.getPost().decreaseLikeCount();

        return new LikeResponse(like.getId(),false, like.getPost().getLikeCount());
    }

}
