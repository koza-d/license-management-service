package koza.licensemanagementservice.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private Pagination pagination;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .items(page.getContent())
                .pagination(Pagination.from(page))
                .build();
    }

    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .items(page.map(mapper).getContent())
                .pagination(Pagination.from(page))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private long totalItems;
        private int totalPages;
        private boolean hasNext;

        public static Pagination from(Page<?> page) {
            return Pagination.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalItems(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .hasNext(page.hasNext())
                    .build();
        }
    }
}
