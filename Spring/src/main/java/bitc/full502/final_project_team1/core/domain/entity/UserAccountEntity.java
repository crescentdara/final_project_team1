package bitc.full502.final_project_team1.core.domain.entity;

import bitc.full502.final_project_team1.core.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
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
    private Long userId;                     // PK (INT AI)

    @Column(name = "username", length = 60, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name", length = 100, nullable = false)
    private String name;                        //  사람 이름 컬럼

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;                          // DB에 'EDITOR' 같은 문자열로 저장됨

    @Column(name = "status", nullable = false)
    private Integer status;                     // Active = 1 , Inactive = 2

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;            // DATETIME 매핑

    @Column(name = "emp_no", length = 50, nullable = false, unique = true)
    private String empNo;

    /** 선호 지역 컬럼 추가 */
    @Column(name = "preferred_region", length = 200)
    private String preferredRegion;

    /** 파이어베이스 FCM 토큰 **/
    @Column(name = "fcm_token")
    private String fcmToken;
}
