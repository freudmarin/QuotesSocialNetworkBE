package com.marin.socialnetwork.dtos.api;

import lombok.Getter;

@Getter
public class TagDTO {
    private String name;
    private String slug;
    private int quoteCount;
    private String dateAdded;
    private String dateModified;
}
