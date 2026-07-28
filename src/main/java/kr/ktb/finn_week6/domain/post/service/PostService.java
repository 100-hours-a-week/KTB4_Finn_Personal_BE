package kr.ktb.finn_week6.domain.post.service;

import kr.ktb.finn_week6.domain.comment.Comment;
import kr.ktb.finn_week6.domain.comment.repository.CommentRepository;
import kr.ktb.finn_week6.domain.hashtag.HashTag;
import kr.ktb.finn_week6.domain.hashtag.service.HashTagService;
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
import org.jspecify.annotations.NonNull;
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
    private final HashTagService hashTagService;

    @Transactional
    public CreatePostResponse register(CreatePostCommand command){
        User user = userRepository.findById(command.userId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        Post post = postRepository.save(
                new Post(user, command.title(), command.content(), command.contentImg())
        );

        hashTagService.registerPostTags(post, command.tags());

        return CreatePostResponse.createPostResponse(post);
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
        boolean isLiked = like != null;

        List<HashTag> hashTags = hashTagService.findTagsByPostId(postId);
        List<String> tagNames = new ArrayList<>();
        for (HashTag hashTag : hashTags) {
            tagNames.add(hashTag.getTagName());
            System.out.println(hashTag.getTagName());
        }
        return PostDetailResponse.createPostDetailResponse(post,isPostAuthor, isLiked, tagNames);
    }

    public List<PostResponse> getPostListSortByCreatedAt(Long userId){
        List<Post> postList = postRepository.findPostsOrderByCreatedAtDesc();
        return getPostResponses(userId, postList);
    }

    public List<MostViewPostResponse> getPostListSortByViewCount(){
        return postRepository.findPostsOrderByViewCountDesc();
    }

    public List<PostResponse> getPostListSortByLikeCount(Long userId){
        List<Post> postList = postRepository.findPostsOrderByLikeCountDesc();
        return getPostResponses(userId, postList);
    }

    public List<PostResponse> getPostListByUserId(Long userId){
        List<Post> postList = postRepository.findPostsByUserId(userId);
        return getPostResponses(userId, postList);
    }

    @NonNull
    private List<PostResponse> getPostResponses(Long userId, List<Post> postList) {
        return postList.stream().map(
                post -> {
                    boolean isLiked = likeRepository.findUndeletedByPostIdAndUserId(post.getId(), userId).isPresent();
                    List<HashTag> hashTags = hashTagService.findTagsByPostId(post.getId());
                    List<String> tagNames = new ArrayList<>();
                    for (HashTag hashTag : hashTags) {
                        tagNames.add(hashTag.getTagName());
                        System.out.println(hashTag.getTagName());
                    }
                    return PostResponse.createPostResponse(post, isLiked,tagNames);
                }
        ).toList();
    }


    @Transactional
    public UpdatePostResponse updatePost(UpdatePostCommand command){
        Post post = postRepository.findById(command.postId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );

        permissionValidator.validatePermission(post.getUser().getId(), command.userId());
        post.updatePost(command.title(),command.content(), command.contentImg());

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
