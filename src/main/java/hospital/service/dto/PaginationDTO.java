package hospital.service.dto;

import java.io.Serializable;

public class PaginationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int page;
    private int limit;
    private long total;
    private long totalPages;

    public PaginationDTO() {}

    public PaginationDTO(int page, int limit, long total, long totalPages) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }
}
