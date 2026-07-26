package kr.ktb.finn_week6.domain.hashtag;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "HashTags")
@Getter
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String tagName;
}
