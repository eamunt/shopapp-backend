package com.project.shopapp.responses.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.responses.user.UserResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
public class CommentResponse {
    @JsonProperty("content")
    private String content;
    @JsonProperty("user")
    private UserResponse userResponse;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

}
