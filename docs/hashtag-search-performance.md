# 해시태그·기간 검색 성능 개선

## 1. 문제 정의

FOCAL은 사진 게시글에 최대 5개의 해시태그를 지정하고, 사용자가 특정 태그와 기간을 조합해 최신 게시글을 조회할 수 있는 서비스입니다.

초기 데이터에서는 별도의 검색용 복합 인덱스가 없어도 응답이 빨랐습니다. 그러나 게시글과 연결 데이터가 증가하면 다음 비용이 커질 것으로 예상했습니다.

- `post_hash_tags`에서 특정 태그와 연결된 게시글을 찾는 비용
- `posts`를 조회해 기간 조건을 검사하는 PK Lookup 비용
- 기간에 포함된 후보를 최신순으로 정렬하는 비용
- 최종 10건을 반환하기 전까지 읽어야 하는 행 수

태그마다 연결된 게시글 수가 다르기 때문에 데이터 규모뿐 아니라 **태그 선택도**와 **조회 기간**을 함께 변수로 두었습니다.

## 2. 대상 쿼리

### API 조건

| 항목 | 내용 |
| --- | --- |
| API | `GET /posts/search` |
| 조건 | `tag`, `startDate`, `endDate` |
| 정렬 | 생성일 내림차순 |
| 반환 수 | 최대 10건 |
| 인증 | JWT Access Token 필요 |

### 초기 QueryDSL

```java
return queryFactory
        .select(post)
        .from(postHashTag)
        .join(postHashTag.post, post)
        .join(post.user, user).fetchJoin()
        .join(postHashTag.hashtag, hashTag)
        .where(
                post.isDeleted.eq(false),
                hashTag.tagName.eq(command.tag()),
                post.createdAt.goe(start)
                        .and(post.createdAt.lt(end))
        )
        .orderBy(
                post.createdAt.desc(),
                post.id.desc()
        )
        .limit(10)
        .fetch();
```

### 초기 실행 SQL

```sql
SELECT p.*
FROM post_hash_tags pht
JOIN posts p ON pht.post_id = p.id
JOIN users u ON p.user_id = u.id
JOIN hash_tags ht ON pht.hashtag_id = ht.id
WHERE p.is_deleted = FALSE
  AND ht.tag_name = ?
  AND p.created_at >= ?
  AND p.created_at < ?
ORDER BY p.created_at DESC, p.id DESC
LIMIT 10;
```

`hash_tags.tag_name`에는 유니크 인덱스가 있으므로 태그 ID를 찾는 비용보다, 연결 행과 게시글 후보를 읽고 정렬하는 비용에 집중했습니다.

## 3. 실험 설계

### 데이터 분포

최근 1년 동안 게시글이 날짜별로 균등하게 생성됐다고 가정하고, 선택도가 다른 태그를 의도적으로 구성했습니다.

| 태그 유형 | 일별 포함 비율 | 특성 |
| --- | ---: | --- |
| 인기 태그 | 40% | 선택도가 낮고 후보가 많음 |
| 중간 태그 | 10% | 중간 수준의 후보 수 |
| 희소 태그 | 1% | 선택도가 높고 후보가 적음 |

각 태그에 대해 1일, 7일, 30일, 6개월 조건을 측정했습니다.

### 데이터 증가 단계

| 단계 | 일별 게시글 | 전체 게시글 | `post_hash_tags` 연결 행 |
| --- | ---: | ---: | ---: |
| 1차 | 100 | 36,500 | 109,500 |
| 2차 | 1,000 | 365,000 | 1,095,000 |
| 3차 | 10,000 | 3,650,000 | 편향 태그를 포함한 대용량 연결 데이터 |

초기 상태는 “인덱스가 전혀 없는 상태”가 아닙니다. FK와 `(post_id, hashtag_id)` 유니크 제약으로 MySQL이 생성한 인덱스는 존재했으며, **태그·기간·정렬을 함께 처리할 명시적 복합 인덱스가 없던 상태**입니다.

### 측정 환경과 해석 범위

| 항목 | 내용 |
| --- | --- |
| DB | MySQL 8.4 |
| API 실행 위치 | localhost |
| 집계 방식 | 반복 측정 평균 |
| 워밍업 횟수 | 기존 기록에서 확인되지 않음 |
| 본 측정 반복 횟수 | 기존 기록에서 확인되지 않음 |
| 장비 상세 사양 | 기존 기록에서 확인되지 않음 |

반복 횟수와 장비 사양이 기록되지 않은 한계가 있으므로 절대적인 벤치마크 수치보다는 같은 환경에서 실행 계획과 상대적인 변화가 어떻게 달라졌는지를 중심으로 해석했습니다.

## 4. 복합 인덱스 적용 전 결과

### 1차: 게시글 36,500건

| 기간 | 인기 태그 | 중간 태그 | 희소 태그 |
| --- | ---: | ---: | ---: |
| 1일 | 49.2ms | 23.4ms | 13.1ms |
| 7일 | 56.2ms | 22.4ms | 8.21ms |
| 30일 | 60.7ms | 27.3ms | 8.47ms |
| 6개월 | 79.2ms | 33.3ms | 11.1ms |

### 2차: 게시글 365,000건

| 기간 | 인기 태그 | 중간 태그 | 희소 태그 |
| --- | ---: | ---: | ---: |
| 1일 | 404ms | 110ms | 42.7ms |
| 7일 | 419ms | 109ms | 40.6ms |
| 30일 | 441ms | 116ms | 40.3ms |
| 6개월 | 512ms | 153ms | 50.7ms |

게시글이 10배 늘어도 태그별 실행 계획의 기본 구조는 유지됐지만 읽는 행 수가 증가하면서 실행 시간이 함께 증가했습니다.

### 태그 선택도에 따른 실행 계획

| 태그 | 옵티마이저의 시작 테이블 | 주요 병목 |
| --- | --- | --- |
| 인기 | `posts` | 전체 게시글 탐색, 날짜 필터링과 정렬 |
| 중간 | `post_hash_tags` | 태그 연결 행 조회 후 `posts` PK Lookup 반복 |
| 희소 | `post_hash_tags` | 같은 구조지만 반복 행이 적어 상대적으로 빠름 |

인기 태그의 1일 조회에서는 365,000개의 게시글을 읽고 1,000건만 날짜 조건으로 남겼습니다.

```text
-> Limit: 10 row(s) (actual time=404..404 rows=10)
   -> Sort: p.created_at DESC, p.id DESC (actual time=404..404)
      -> Filter: is_deleted=false and created_at range
         (actual time=25.3..404 rows=1000)
         -> Index range scan on posts using user_id FK index
            (actual time=0.339..389 rows=365000)
```

중간 태그는 `hashtag_id` 인덱스로 36,500개의 연결 행을 찾은 뒤 게시글 PK Lookup을 같은 횟수만큼 반복했습니다.

```text
-> Limit: 10 row(s) (actual time=110..110 rows=10)
   -> Sort: p.created_at DESC, p.id DESC
      -> Index lookup on post_hash_tags using hashtag_id
         (actual time=16.9..49.5 rows=36500)
      -> Single-row index lookup on posts using PRIMARY
         (loops=36500)
```

따라서 성능은 다음 두 값에 크게 좌우됐습니다.

1. 태그에 연결된 전체 게시글 수
2. 최초 접근 테이블에서 조건을 적용하기 위해 실제로 읽는 행 수

## 5. 구조와 인덱스 개선

### 판단

`posts.created_at` 인덱스는 게시글에서 시작하는 실행 계획의 날짜 탐색을 개선할 수 있지만, `post_hash_tags`에서 시작하면 게시글을 조회하기 전까지 생성일을 알 수 없습니다. 기간 밖 게시글도 일단 PK Lookup 해야 하는 문제가 남습니다.

기간 필터와 정렬을 연결 테이블에서 끝내기 위해 게시글 생성 시각을 `PostHashTag`에 함께 저장했습니다. 게시글의 생성 시각은 변경되지 않으므로 동기화 비용이 제한적이라고 판단했습니다.

```java
@Table(
        name = "post_hash_tags",
        indexes = @Index(
                name = "idx_pht_hashtag_created_post",
                columnList = "hashtag_id, created_at, post_id"
        ),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_hashtag",
                columnNames = {"post_id", "hashtag_id"}
        )
)
public class PostHashTag {
    // ...

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PostHashTag(Post post, HashTag hashtag) {
        this.post = post;
        this.hashtag = hashtag;
        this.createdAt = post.getCreatedAt();
    }
}
```

### 컬럼 순서

복합 인덱스는 `(hashtag_id, created_at, post_id)` 순서로 구성했습니다.

1. `hashtag_id`: 동등 조건으로 검색 범위를 먼저 제한
2. `created_at`: 기간 범위 조건과 최신순 탐색 처리
3. `post_id`: 인덱스에서 게시글 조인 키 확보

최종 목표는 인덱스에서 태그와 날짜를 만족하는 최신 연결 행 10개를 먼저 결정하고, 그 10개에 대해서만 게시글과 작성자를 조회하는 것입니다.

### 변경 QueryDSL

```java
return queryFactory
        .select(post)
        .from(postHashTag)
        .join(postHashTag.post, post)
        .join(post.user, user).fetchJoin()
        .join(postHashTag.hashtag, hashTag)
        .leftJoin(post.place, place).fetchJoin()
        .where(
                post.isDeleted.eq(false),
                hashTag.tagName.eq(command.tag()),
                postHashTag.createdAt.goe(start)
                        .and(postHashTag.createdAt.lt(end))
        )
        .orderBy(postHashTag.createdAt.desc())
        .limit(10)
        .fetch();
```

## 6. ORM 정렬로 발생한 추가 병목

처음에는 생성 시간이 같을 때 결과 순서를 고정하기 위해 다음 정렬을 사용했습니다.

```java
.orderBy(
        postHashTag.createdAt.desc(),
        post.id.desc()
)
```

그러나 `PostHashTag.post`는 JPA 연관관계입니다. QueryDSL의 `post.id`는 `pht.post_id`가 아니라 조인된 `posts.id`로 번역됐습니다.

```sql
ORDER BY pht.created_at DESC, p.id DESC
LIMIT 10;
```

`(hashtag_id, created_at, post_id)` 인덱스는 필터에는 사용됐지만 `p.id`를 포함한 전체 정렬을 처리할 수 없었습니다. 인기 태그 6개월 조건에서는 날짜 범위의 후보 724,000건을 게시글·사용자와 조인한 뒤 추가 정렬했습니다.

```text
-> Limit: 10 row(s) (actual time=2857..2857 rows=10)
   -> Sort: pht.created_at DESC, p.id DESC
      (actual time=2857..2857 rows=10)
      -> Stream results
         (actual time=3.02..2704 rows=724000)
         -> Covering index range scan on post_hash_tags
            using idx_pht_hashtag_created_post
            (actual time=2.94..329 rows=724000)
         -> Single-row index lookup on posts using PRIMARY
            (loops=724000)
         -> Single-row index lookup on users using PRIMARY
            (loops=724000)
```

DB에서 직접 작성한 `ORDER BY pht.created_at DESC, pht.post_id DESC` 쿼리는 인덱스로 처리됐지만 ORM 생성 SQL은 그렇지 않았습니다. 인덱스 선언만 확인하지 않고 실제 생성 SQL과 실행 계획까지 검증해야 한다는 점을 확인했습니다.

## 7. 최종 해결과 트레이드오프

현재 서비스는 최대 10건을 조회하며, 동일 생성 시각의 게시글 순서보다 검색 응답 시간을 우선했습니다. 이에 따라 `post.id` 보조 정렬을 제거하고 `pht.created_at DESC`만 사용했습니다.

```sql
SELECT p.*
FROM post_hash_tags pht
JOIN posts p ON pht.post_id = p.id
JOIN users u ON p.user_id = u.id
JOIN hash_tags ht ON pht.hashtag_id = ht.id
WHERE p.is_deleted = FALSE
  AND ht.tag_name = ?
  AND pht.created_at >= ?
  AND pht.created_at < ?
ORDER BY pht.created_at DESC
LIMIT 10;
```

개선된 실행 흐름은 다음과 같습니다.

```text
post_hash_tags 복합 인덱스 역방향 탐색
→ 태그와 날짜 범위 조건 적용
→ 최신 연결 행 10개 선택
→ 선택한 post_id로 posts PK Lookup 10회
→ users PK Lookup 10회
→ 결과 반환
```

대표 실행 계획에서도 인덱스가 10건을 찾은 시점에 탐색을 멈춥니다.

```text
-> Limit: 10 row(s) (actual time=1.18..1.24 rows=10)
   -> Covering index range scan on post_hash_tags
      using idx_pht_hashtag_created_post (reverse)
      (actual time=1.10..1.11 rows=10)
   -> Single-row index lookup on posts using PRIMARY (loops=10)
   -> Single-row covering index lookup on users using PRIMARY (loops=10)
```

이 선택으로 동일한 `created_at`을 가진 게시글의 상대적인 순서는 보장하지 않습니다. 안정적인 정렬이 필요해지면 `post_id`를 읽기 전용 스칼라 필드로 추가 매핑하고 `pht.post_id DESC`가 생성되도록 변경하는 방안을 검토할 수 있습니다.

## 8. 최종 결과

### 365만 게시글 환경의 DB 쿼리 평균

| 기간 | 인기 태그 | 중간 태그 | 희소 태그 |
| --- | ---: | ---: | ---: |
| 1일 | 5.94ms | 2.52ms | 3.76ms |
| 7일 | 3.56ms | 1.59ms | 0.209ms |
| 30일 | 5.96ms | 2.76ms | 2.10ms |
| 6개월 | 1.24ms | 3.80ms | 2.92ms |

### 365만 게시글 환경의 API 평균

| 기간 | 인기 태그 | 중간 태그 | 희소 태그 |
| --- | ---: | ---: | ---: |
| 1일 | 49ms | 45ms | 64ms |
| 7일 | 44ms | 35ms | 55ms |
| 30일 | 46ms | 60ms | 36ms |
| 6개월 | 54ms | 61ms | 34ms |

### 동일 환경의 ORM 정렬 문제 해결 전후

| 태그 | 개선 전 | 개선 후 | 감소율 |
| --- | ---: | ---: | ---: |
| 인기 | 3.15s | 54ms | 약 98.3% |
| 중간 | 1.51s | 61ms | 약 96.0% |
| 희소 | 798ms | 34ms | 약 95.7% |

서로 다른 데이터 규모인 1차, 2차, 3차 결과는 데이터 증가에 따른 추세를 파악하는 데만 사용했습니다. 위 개선율은 동일한 365만 게시글 환경의 API 반복 측정 평균끼리 비교한 값입니다.

## 9. 배운 점과 다음 단계

- 태그 선택도에 따라 옵티마이저가 시작 테이블을 다르게 선택하므로 평균적인 데이터만으로 실행 계획을 판단하면 안 됩니다.
- `LIMIT 10`이 있어도 인덱스가 필터와 정렬을 함께 처리하지 못하면 후보 전체를 조인·정렬할 수 있습니다.
- JPA 연관 필드 표현과 실제 FK 컬럼 표현은 생성 SQL에서 다른 정렬을 만들 수 있습니다.
- 반정규화는 쓰기 시 동기화 책임을 만들지만, 변경되지 않는 생성 시각을 복제해 읽기 비용을 크게 줄일 수 있었습니다.
- 후속 측정에서는 장비 사양, 워밍업 횟수, 반복 횟수와 평균·중앙값·상위 백분위 값을 함께 기록해 재현성을 높일 예정입니다.

