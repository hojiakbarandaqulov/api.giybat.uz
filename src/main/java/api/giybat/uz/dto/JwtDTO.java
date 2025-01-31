package api.giybat.uz.dto;

import api.giybat.uz.enums.ProfileRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtDTO {
    private String id;
    private String username;
    private ProfileRole role;

    public JwtDTO(String id, String userName, ProfileRole role) {
        this.id = id;
        this.username = userName;
        this.role = role;
    }

    public JwtDTO(String id) {
        this.id = id;
    }
}
