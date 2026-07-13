package kr.ktb.finn_week6.domain.post.controller;

import jakarta.validation.Valid;
import kr.ktb.finn_week6.domain.comment.dto.command.CreateCommentCommand;
import kr.ktb.finn_week6.domain.comment.dto.request.CreateCommentRequest;
import kr.ktb.finn_week6.domain.comment.dto.response.CommentDetailResponse;
import kr.ktb.finn_week6.domain.comment.dto.response.CommentListResponse;
import kr.ktb.finn_week6.domain.comment.dto.response.CreateCommentResponse;
import kr.ktb.finn_week6.domain.comment.service.CommentService;
import kr.ktb.finn_week6.domain.like.Like;
import kr.ktb.finn_week6.domain.like.dto.command.LikePostCommand;
import kr.ktb.finn_week6.domain.like.dto.response.LikeResponse;
import kr.ktb.finn_week6.domain.like.service.LikeService;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.dto.command.CreatePostCommand;
import kr.ktb.finn_week6.domain.post.dto.command.UpdatePostCommand;
import kr.ktb.finn_week6.domain.post.dto.enums.PostFilter;
import kr.ktb.finn_week6.domain.post.dto.request.CreatePostRequest;
import kr.ktb.finn_week6.domain.post.dto.request.UpdatePostRequest;
import kr.ktb.finn_week6.domain.post.dto.response.*;
import kr.ktb.finn_week6.domain.post.service.PostService;
import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.dto.ApiResponse;
import kr.ktb.finn_week6.security.auth.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreatePostResponse> registerPost(@Valid @RequestBody CreatePostRequest request, @AuthenticationPrincipal Long userId){
        CreatePostCommand postCommand = request.createPostCommand(userId);
        CreatePostResponse response = postService.register(postCommand);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), response);
    }

    @GetMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId, @AuthenticationPrincipal Long userId){
        PostDetailResponse postDetail = postService.getPostDetail(postId, userId);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), postDetail);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostHomeResponse> getPostList(@RequestParam(defaultValue = "RECENT") PostFilter filter, @AuthenticationPrincipal Long userId){
        System.out.println("filter : " + filter + " userId : " + userId);

        List<PostResponse> postList = null;
        if(filter == PostFilter.RECENT){
            postList = postService.getPostListSortByCreatedAt();
        }else if(filter == PostFilter.POPULAR){
            postList = postService.getPostListSortByLikeCount();
        }else if (filter == PostFilter.MINE){
            postList = postService.getPostListByUserId(userId);
        }
        List<MostViewPostResponse> mostViewPostResponses = postService.getPostListSortByViewCount();
        PostHomeResponse postListResponse = PostHomeResponse.createPostListResponse(postList, mostViewPostResponses);

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), postListResponse);
    }

//    @GetMapping("/sort/like")
//    @ResponseStatus(HttpStatus.OK)
//    public ApiResponse<PostListResponse> getPostListByLikeCount(){
//        List<PostResponse> postListSortByLikeCount = postService.getPostListSortByLikeCount();
//        PostListResponse postListResponse = new PostListResponse(postListSortByLikeCount);
//        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), postListResponse);
//    }
//
//    @GetMapping("/sort/mine")
//    @ResponseStatus(HttpStatus.OK)
//    public ApiResponse<PostListResponse> getPostListOfMine(@AuthenticationPrincipal Long userId){
//        List<PostResponse> postListSortByLikeCount = postService.getPostListByUserId(userId);
//        PostListResponse postListResponse = new PostListResponse(postListSortByLikeCount);
//        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), postListResponse);
//    }


    @PatchMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UpdatePostResponse> updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostRequest request, @AuthenticationPrincipal Long userId){
        UpdatePostCommand command = request.toCommand(userId, postId);
        UpdatePostResponse updatePostResponse = postService.updatePost(command);

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), updatePostResponse);
    }


    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateCommentResponse> registerComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request, @AuthenticationPrincipal Long userId){
        CreateCommentCommand commentCommand = request.createCommentCommand(postId, userId, request);
        CreateCommentResponse register = commentService.register(commentCommand);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), register);
    }

    @GetMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CommentListResponse> getCommentsByPostId(@PathVariable Long postId, @AuthenticationPrincipal Long userId){
        List<CommentDetailResponse> comments = commentService.getCommentsByPostId(postId, userId);
        CommentListResponse commentListResponse = CommentListResponse.createCommentListResponse(comments);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), commentListResponse);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId, @AuthenticationPrincipal Long userId){
        postService.deletePost(postId, userId);
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<LikeResponse> likePost(@PathVariable Long postId, @AuthenticationPrincipal Long userId){
        LikePostCommand likePostCommand = LikePostCommand.createLikePostCommand(userId, postId);
        LikeResponse likeResponseDto = likeService.likePost(likePostCommand);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), likeResponseDto);
    }

    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LikeResponse> unLikePost(@PathVariable Long postId, @AuthenticationPrincipal Long userId){
        LikePostCommand command = LikePostCommand.createLikePostCommand(userId, postId);
        LikeResponse likeResponse = likeService.deleteLike(command);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), likeResponse);
    }

}
