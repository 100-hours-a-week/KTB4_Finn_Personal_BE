package kr.ktb.finn_week6.domain.post.service;

import kr.ktb.finn_week6.domain.comment.Comment;
import kr.ktb.finn_week6.domain.comment.repository.CommentRepository;
import kr.ktb.finn_week6.domain.like.Like;
import kr.ktb.finn_week6.domain.like.repository.LikeRepository;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.dto.command.CreatePostCommand;
import kr.ktb.finn_week6.domain.post.dto.command.UpdatePostCommand;
import kr.ktb.finn_week6.domain.post.dto.response.*;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PermissionValidator permissionValidator;

    @Transactional
    public CreatePostResponse register(CreatePostCommand command){
        User user = userRepository.findById(command.userId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        Post post = new Post(user, command.title(), command.content(), command.contentImg());
        Post savedPost = postRepository.save(post);
        return CreatePostResponse.createPostResponse(savedPost);
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long postId, Long sessionUserId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );
        post.updateViewCount();

        if(post.isDeleted()){
            throw new IllegalResourceStateException(RequestMessage.RESOURCE_DELETED.getDescription());
        }

        boolean isPostAuthor = post.getUser().getId().equals(sessionUserId);
        Like like = likeRepository.findUndeletedByPostIdAndUserId(postId, sessionUserId).orElse(null);

        return PostDetailResponse.createPostDetailResponse(post,isPostAuthor, like);
    }

    public List<PostResponse> getPostListSortByCreatedAt(){
        List<Post> postList = postRepository.findPostsOrderByCreatedAtDesc();
        List<PostResponse> postResponses = new ArrayList<>();
        for(Post post : postList){
            postResponses.add(PostResponse.createPostResponse(post));
        }
        return postResponses;
    }

    public List<MostViewPostResponse> getPostListSortByViewCount(){
        return postRepository.findPostsOrderByViewCountDesc();
    }

    public List<PostResponse> getPostListSortByLikeCount(){
        List<Post> postList = postRepository.findPostsOrderByLikeCountDesc();
        List<PostResponse> postResponses = new ArrayList<>();
        for(Post post : postList){
            postResponses.add(PostResponse.createPostResponse(post));
        }
        return postResponses;
    }

    public List<PostResponse> getPostListByUserId(Long userId){
        List<Post> postList = postRepository.findPostsByUserId(userId);
        List<PostResponse> postResponses = new ArrayList<>();
        for(Post post : postList){
            postResponses.add(PostResponse.createPostResponse(post));
        }
        return postResponses;
    }



    @Transactional
    public UpdatePostResponse updatePost(UpdatePostCommand command){
        Post post = postRepository.findById(command.postId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );
        String targetImg = post.getContentImg();
        if(command.contentImg() != null){
            targetImg = command.contentImg();
        }
        permissionValidator.validatePermission(post.getUser().getId(), command.loginUserId());
        post.updatePost(command.title(),command.content(), targetImg);

        return  UpdatePostResponse.createResponse(post);
    }

    @Transactional
    public void deletePost(Long postId, Long sessionUserId){
        Post targetPost = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );
        permissionValidator.validatePermission(targetPost.getUser().getId(), sessionUserId);
        targetPost.setDeleted();

        List<Comment> comments = commentRepository.findByPostIdWithPost(postId);
        for (Comment comment : comments) {
            comment.setDeleted();
            comment.getPost().decreaseCommentCount();
        }

        List<Like> byPostId = likeRepository.findUndeletedByPostId(postId);
        for (Like like : byPostId) {
            like.setDeleted();
            like.getPost().decreaseLikeCount();
        }
    }

}
