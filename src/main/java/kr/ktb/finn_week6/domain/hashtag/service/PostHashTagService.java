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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostHashTagService {
    private final HashTagRepository hashTagRepository;
    private final PostHashtagRepository postHashtagRepository;

    @Transactional
    public List<String> updatePostTags(Post post, List<String> requestedTags) {
        List<String> resultTags = HashtagNormalizer.normalize(requestedTags);

        List<PostHashTag> currentPostTags //기존 해시태그
                = postHashtagRepository.findTagsByPostId(post.getId());

        List<PostHashTag> postTagsToDelete = //기존 해시태그에는 있지만 수정된 해시태그에는 없는 것들
                currentPostTags.stream()
                .filter(postHashTag -> !requestedTags.contains(postHashTag.getHashtag().getTagName())).toList();

        postHashtagRepository.deleteAll(postTagsToDelete);//수정된 해시태그에 없는 것들 PostHashTag에서 삭제

        Set<String> currentTagsSet = currentPostTags.stream()//기존 해시태그 이름 추출
                .map(postHashTag -> postHashTag.getHashtag().getTagName()).collect(Collectors.toSet());

        requestedTags.stream()
                .filter(tagName -> !currentTagsSet.contains(tagName))//기존 해시태그 중 없는 것들에 필터링
                .forEach(tagName -> {
                    HashTag hashTag = hashTagRepository.findByTagName(tagName).orElseGet(() -> hashTagRepository.save(new HashTag(tagName)));//HashTag에 존재하는지 확인하고 없으면 새로 생성

                    PostHashTag postHashTag = new PostHashTag(post, hashTag);//PostHasTag 관계 추가
                    postHashtagRepository.save(postHashTag);
                });
        return resultTags;
    }
}
