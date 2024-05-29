package com.marin.quotesdashboardbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostDTO {

    private Long id;
    private QuoteDTO quote;

    private List<CommentDTO> comments;

    private String postPhotoUrl;

    private UserDTO createdBy;

    private String text;

    private boolean isPublic;

    private LocalDateTime addedAt;

    private LocalDateTime updatedAt;
}
