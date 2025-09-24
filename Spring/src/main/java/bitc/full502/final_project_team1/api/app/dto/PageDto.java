package bitc.full502.final_project_team1.api.app.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageDto<T> {
    private List<T> content;
    private int number;
    private int size;
    private Long totalElements;
    private int totalPages;
    private boolean last;
}