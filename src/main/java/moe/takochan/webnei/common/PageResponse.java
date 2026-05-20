package moe.takochan.webnei.common;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long total) {

    public static <T> PageResponse<T> of(List<T> items, PageRequest request, long total) {
        return new PageResponse<>(items, request.page(), request.size(), total);
    }
}
