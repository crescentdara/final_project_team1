package bitc.full502.final_project_team1.api.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class PageResponseDto<T> {

    private List<T> content;
    private Long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
