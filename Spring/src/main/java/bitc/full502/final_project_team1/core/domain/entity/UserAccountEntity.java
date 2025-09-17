package bitc.full502.final_project_team1.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_account", schema = "java502_team1_final_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;                     // PK (INT AI)

    @Column(name = "username", length = 60, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name", length = 100, nullable = false)
    private String name;                        //  사람 이름 컬럼

    public enum Role { ADMIN, EDITOR, VIEWER }

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;                          // DB에 'EDITOR' 같은 문자열로 저장됨

    @Column(name = "status", nullable = false)
    private Integer status;                     // Active = 1 , Inactive = 2

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;            // DATETIME 매핑

}