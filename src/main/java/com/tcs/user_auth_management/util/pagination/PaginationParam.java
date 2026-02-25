package com.tcs.user_auth_management.util.pagination;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

@Getter
@Setter
@Schema(description = "Pagination parameters for API requests")
public class PaginationParam {

    @Schema(description = "Page number (starts from 1)", example = "1", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Page size (max 500)", example = "10", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Field to sort by", example = "id", allowableValues = {"id", "name", "email", "createdAt"})
    private String field;

    @Schema(description = "Sort direction", example = "ASC", allowableValues = {"ASC", "DESC"})
    private String direction;

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public void setSize(int size) {
        this.size = Math.max(1, Math.min(size, 500));
    }

    @JsonIgnore
    @Schema(hidden = true)
    public Pageable toPageable() {
        if (Objects.isNull(this.field) || Objects.isNull(this.direction)) {
            return PageRequest.of(this.page - 1, this.size);
        }
        return PageRequest.of(
                this.page - 1, this.size, Sort.by(Sort.Direction.fromString(this.direction), this.field));
    }
}