package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.ApprovalRequestDTO;
import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;

public interface ApprovalService {
    ApprovalEntity approve(ApprovalRequestDTO dto);
    ApprovalEntity reject(ApprovalRequestDTO dto);
}
