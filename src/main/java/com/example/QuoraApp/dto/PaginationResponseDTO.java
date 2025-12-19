package com.example.QuoraApp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaginationResponseDTO<T> {
    private List<T> data;
    private PaginationInfoDTO pagination;

}
