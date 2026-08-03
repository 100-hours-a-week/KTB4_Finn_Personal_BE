package kr.ktb.finn_week6.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String email;
    private String password;
    private String profileImg;
    private LocalDateTime createdAt;
    private boolean isDeleted;
    private LocalDateTime deletedAt;

    protected User() { //User 생성 아무렇게나 되지 않도록 protected
    }

    public User(String nickname, String email, String password, String profileImg) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.profileImg = profileImg;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    public void updateUser(String nickname, String profileImg){
        this.nickname = nickname;
        if(profileImg != null){
            this.profileImg = profileImg;
        }
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void setDeleted(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

}
