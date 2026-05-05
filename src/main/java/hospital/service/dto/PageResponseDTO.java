package hospital.service.dto;

import java.io.Serializable;
import java.util.List;

public class PageResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> data;
    private PaginationDTO pagination;

    public PageResponseDTO() {}

    public PageResponseDTO(List<T> data, PaginationDTO pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }
}
