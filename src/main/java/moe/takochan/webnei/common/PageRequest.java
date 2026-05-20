package moe.takochan.webnei.common;

public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 100;
    public static final int MAX_SIZE = 500;

    public PageRequest {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    public static PageRequest of(Integer page, Integer size) {
        return new PageRequest(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
    }

    public int offset() {
        return page * size;
    }
}
