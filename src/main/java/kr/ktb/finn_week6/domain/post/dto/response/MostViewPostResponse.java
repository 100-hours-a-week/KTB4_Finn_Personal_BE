package kr.ktb.finn_week6.domain.post.dto.response;

public record MostViewPostResponse(
        String title,
        String username
) {
    public static MostViewPostResponse createMostViewPostResponse(String title, String username){
        return new MostViewPostResponse(title, username);
    }
}
