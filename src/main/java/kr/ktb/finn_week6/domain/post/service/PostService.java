package kr.ktb.finn_week6.domain.post.service;

import kr.ktb.finn_week6.domain.comment.Comment;
import kr.ktb.finn_week6.domain.comment.repository.CommentRepository;
import kr.ktb.finn_week6.domain.hashtag.HashTag;
import kr.ktb.finn_week6.domain.hashtag.service.HashTagService;
import kr.ktb.finn_week6.domain.hashtag.service.PostHashTagService;
import kr.ktb.finn_week6.domain.like.Like;
import kr.ktb.finn_week6.domain.like.repository.LikeRepository;
import kr.ktb.finn_week6.domain.place.Place;
import kr.ktb.finn_week6.domain.place.dto.PostPlaceInfo;
import kr.ktb.finn_week6.domain.place.repository.PlaceRepository;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.dto.command.CreatePostCommand;
import kr.ktb.finn_week6.domain.post.dto.command.PostSearchCommand;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PlaceRepository placeRepository;

    private final PermissionValidator permissionValidator;
    private final HashTagService hashTagService;
    private final PostHashTagService postHashTagService;

    @Transactional
    public CreatePostResponse register(CreatePostCommand command){

        Place place = null;

        User user = userRepository.findById(command.userId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );

        if (command.location() != null) {

            place = placeRepository
                    .findByProviderPlaceId(
                            command.location().providerPlaceId()
                    )
                    .orElseGet(() -> placeRepository.save(
                            new Place(
                                    command.location().providerPlaceId(),
                                    command.location().placeName(),
                                    command.location().roadAddressName(),
                                    command.location().latitude(),
                                    command.location().longitude()
                            )
                    ));
        }

        Post post = postRepository.save(
                new Post(user, command.title(), command.content(), command.contentImg(), place)
        );

        hashTagService.registerPostTags(post, command.tags());

        return CreatePostResponse.createPostResponse(post);
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long postId, Long sessionUserId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );

        if(post.isDeleted()){
            throw new IllegalResourceStateException(RequestMessage.RESOURCE_DELETED.getDescription());
        }

        post.updateViewCount();

        PostPlaceInfo postLocationResponse = null;
        Place place = post.getPlace();

        if (place != null) {
            postLocationResponse =
                    PostPlaceInfo.createPostLocationResponse(place);
        }

        boolean isPostAuthor = post.getUser().getId().equals(sessionUserId);
        boolean isLiked = likeRepository.findUndeletedByPostIdAndUserId(postId, sessionUserId).isPresent();

        List<HashTag> hashTags = hashTagService.findTagsByPostId(postId);
        List<String> tagNames = new ArrayList<>();
        for (HashTag hashTag : hashTags) {
            tagNames.add(hashTag.getTagName());
        }
        return PostDetailResponse.createPostDetailResponse(post,isPostAuthor, isLiked, tagNames, postLocationResponse);
    }

    public List<PostResponse> getPostListSortByCreatedAt(Long userId){
        List<Post> postList = postRepository.findPostsOrderByCreatedAtDesc();
        return getPostResponses(userId, postList);
    }

    public List<PostResponse> getPostListBySearchTag(PostSearchCommand command){
        List<Post> postsBySearchTag = postRepository.findPostsBySearchTag(command);
        return getPostResponses(command.userId(), postsBySearchTag);
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


    @Transactional
    public UpdatePostResponse updatePost(UpdatePostCommand command){
        Post post = postRepository.findById(command.postId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_POST.getDescription())
        );
        permissionValidator.validatePermission(post.getUser().getId(), command.userId());

        Place place = getPlaceOrCreatePlace(command);

        PostPlaceInfo postLocationResponse = null;

        if (place != null) {
            postLocationResponse =
                    PostPlaceInfo.createPostLocationResponse(place);
        }
        post.updatePost(command.title(),command.content(), command.contentImg(), place);

        List<String> updatedTags = postHashTagService.updatePostTags(post, command.tags());
        return  UpdatePostResponse.createResponse(post, updatedTags, postLocationResponse);
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



    @NonNull
    private List<PostResponse> getPostResponses(Long userId, List<Post> postList) {
        Set<Long> postIds  = new HashSet<>();
        for (Post post : postList) {
            postIds.add(post.getId());
        }
        Set<Long> likedPostIds = likeRepository.findUndeletedByPostIdsAndUserId(postIds, userId)
                .stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());

        Map<Long, List<String>> tagsByPostId =
                hashTagService.findTagNamesByPostIds(postIds);

        return postList.stream()
                .map(post -> PostResponse.createPostResponse(
                        post,
                        likedPostIds.contains(post.getId()),
                        tagsByPostId.getOrDefault(post.getId(), List.of()),
                        post.getPlace() != null ? PostPlaceInfo.createPostLocationResponse(post.getPlace()) : null
                ))
                .toList();
    }

    private Place getPlaceOrCreatePlace(UpdatePostCommand command) {

        if(command.location() == null){
            return null;
        }

        return placeRepository.findByProviderPlaceId(command.location().providerPlaceId()).orElseGet(() -> placeRepository.save(new Place(
                command.location().providerPlaceId(),
                command.location().placeName(),
                command.location().roadAddressName(),
                command.location().latitude(),
                command.location().longitude())
        ));
    }

}
