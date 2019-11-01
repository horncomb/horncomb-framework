package cn.horncomb.framework.web.rest;

import lombok.Data;

import java.util.Collection;

@Data
public class PageEntity<T> {
    private Collection<T> content;
    private Pagination pagination;

    @Data
    private class Pagination {
        private long total;
        private int page;
        private int size;
    }
}
