package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.UserUpdateDto;

public interface UserService {
    void updateUser(Long userId, UserUpdateDto dto);
    void deleteUser(Long userId);
}

