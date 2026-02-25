package com.tcs.user_auth_management.util.pagination;

import java.util.Collections;
import java.util.List;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PaginationEntityResponse<T> {
  private List<T> contents;
  private int page;
  private int pageSize;
  private int totalPages;
  private long total;
  private boolean hasNext;

  public PaginationEntityResponse(Page<T> page) {
    this(
        ObjectUtils.getIfNull(page.getContent(), Collections.emptyList()),
        page.getNumber() + 1,
        page.getSize(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.hasNext());
  }

  public PaginationEntityResponse(
      List<T> contents, int page, int pageSize, int totalPages, long total, boolean hasNext) {
    this.contents = ObjectUtils.getIfNull(contents, Collections.emptyList());
    this.page = page;
    this.totalPages = totalPages;
    this.pageSize = pageSize;
    this.total = total;
    this.hasNext = hasNext;
  }
}
