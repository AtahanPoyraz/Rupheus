package ai.rupheus.application.dto.shared;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageableResponse<T> {
    T content;
    private PageableInfo pageable;

    @Setter
    @Getter
    public static class PageableInfo {
        private int page;
        private int size;
        private long totalItems;
        private long totalPages;
    }
}
