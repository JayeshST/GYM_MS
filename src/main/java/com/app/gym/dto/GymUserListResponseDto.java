package com.app.gym.dto;

import java.util.List;

public class GymUserListResponseDto {

    private final List<GymUserListItemDto> users;
    private final int page;
    private final int size;
    private final int numberOfElements;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public GymUserListResponseDto(
            List<GymUserListItemDto> users,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {

        this.users = users;
        this.page = page;
        this.size = size;
        this.numberOfElements = users.size();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public List<GymUserListItemDto> getUsers() {
        return users;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }
}