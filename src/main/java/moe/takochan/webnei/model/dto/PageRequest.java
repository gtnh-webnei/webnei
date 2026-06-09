package moe.takochan.webnei.model.dto;

public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 100;
    public static final int DEFAULT_MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
    }

    public static PageRequest of(Integer page, Integer size) {
        return of(page, size, DEFAULT_SIZE, DEFAULT_MAX_SIZE);
    }

    public static PageRequest of(Integer page, Integer size, int defaultSize, int maxSize) {
        int checkedPage = page == null || page < 0 ? 0 : page;
        int checkedSize = size == null || size <= 0 ? defaultSize : Math.min(size, maxSize);
        return new PageRequest(checkedPage, checkedSize);
    }

    public int offset() {
        return page * size;
    }
}
