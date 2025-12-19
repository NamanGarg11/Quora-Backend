package com.example.QuoraApp.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaginationInfoDTO {
    private long totalRecords;
    private int currentPage;
    private int totalPages;
    private Integer nextPage;
    private Integer prevPage;
}
