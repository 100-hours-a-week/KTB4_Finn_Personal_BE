package kr.ktb.finn_week6.domain.post;

import jakarta.persistence.*;
import kr.ktb.finn_week6.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private  String content;
    private  String contentImg;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private LocalDateTime deletedAt;

    protected Post() {
    }

    public Post(User user, String title, String content, String contentImg) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.contentImg = contentImg;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public void updatePost(String title, String content, String contentImg){
        this.title = title;
        this.content = content;
        this.contentImg = contentImg;
        this.updatedAt = LocalDateTime.now();
    }
    public void increaseCommentCount(){
        this.commentCount += 1;
    }
    public void decreaseCommentCount(){
        this.commentCount -= 1;
    }
    public void increaseLikeCount(){
        this.likeCount += 1;
    }
    public void decreaseLikeCount(){
        this.likeCount -= 1;
    }
    public void updateViewCount(){
        this.viewCount += 1;
    }

    public void setDeleted(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
