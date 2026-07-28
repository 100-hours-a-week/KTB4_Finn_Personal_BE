package kr.ktb.finn_week6.domain.hashtag;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "HashTags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hashtag_tag_name",
                        columnNames = "tag_name"
                )
            }
        )
@Getter
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    public HashTag() {
    }

    public HashTag(String tagName) {
        this.tagName = tagName;
    }
}


