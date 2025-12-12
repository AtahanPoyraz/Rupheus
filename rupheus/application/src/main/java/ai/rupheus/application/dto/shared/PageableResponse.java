package ai.rupheus.application.dto.shared;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageableResponse<T> {
    private T content;
    private PageableInfo pageable;

    @Setter
    @Getter
    public static class PageableInfo {
        private int page;
        private int size;
        private long totalItems;
        private long totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private boolean isFirst;
        private boolean isLast;
        private SortInfo sort;
    }

    @Setter
    @Getter
    public static class SortInfo {
        private boolean sorted;
        private boolean unsorted;
        private boolean empty;
    }
}
