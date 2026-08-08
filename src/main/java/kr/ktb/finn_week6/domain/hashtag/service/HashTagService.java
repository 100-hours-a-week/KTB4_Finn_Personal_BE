package kr.ktb.finn_week6.domain.hashtag.service;

import jakarta.transaction.Transactional;
import kr.ktb.finn_week6.domain.hashtag.HashTag;
import kr.ktb.finn_week6.domain.hashtag.PostHashTag;
import kr.ktb.finn_week6.domain.hashtag.repository.HashTagRepository;
import kr.ktb.finn_week6.domain.hashtag.repository.PostHashtagRepository;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.global.util.HashtagNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashTagService {
    private final HashTagRepository hashTagRepository;
    private final PostHashtagRepository postHashtagRepository;

    @Transactional
    public void registerPostTags(Post post, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        List<String> normalizedTags = HashtagNormalizer.normalize(tags);

        for (String tagName : normalizedTags) {
            HashTag hashTag = hashTagRepository.findByTagName(tagName)
                    .orElseGet(() ->
                            hashTagRepository.save(new HashTag(tagName))
                    );
            postHashtagRepository.save(new PostHashTag(post, hashTag));
        }
    }

    public List<HashTag> findTagsByPostId(Long postId) {
        List<PostHashTag> postHashTags = postHashtagRepository.findTagsByPostId(postId);
        List<HashTag> hashTags = new ArrayList<>();

        for (PostHashTag postHashTag : postHashTags) {
            hashTags.add(postHashTag.getHashtag());
        }
        return hashTags;
    }

    public Map<Long, List<String>> findTagNamesByPostIds(Set<Long> postIds) {
        return postHashtagRepository.findAllByPostIds(postIds)
                .stream()
                .collect(Collectors.groupingBy(
                        postHashTag -> postHashTag.getPost().getId(),
                        Collectors.mapping(
                                postHashTag -> postHashTag.getHashtag().getTagName(),
                                Collectors.toList()
                        )
                ));
    }
}
