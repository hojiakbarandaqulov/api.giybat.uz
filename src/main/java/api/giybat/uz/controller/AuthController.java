package api.giybat.uz.controller;

import api.giybat.uz.dto.ApiResponse;
import api.giybat.uz.dto.LoginDTO;
import api.giybat.uz.dto.profile.ProfileDTO;
import api.giybat.uz.dto.auth.RegistrationDTO;
import api.giybat.uz.dto.auth.ResetPasswordConfirmDTO;
import api.giybat.uz.dto.auth.ResetPasswordDTO;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<String>> registration(@Valid @RequestBody RegistrationDTO dto,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        ApiResponse<String> ok = authService.registration(dto, language);
        return ResponseEntity.ok(ok);
    }

    @GetMapping("/registration/verification/{token}")
    public ResponseEntity<ApiResponse<String>> registrationVerification(@PathVariable("token") String token,
                                                                        @RequestHeader(value = "Accept-Language",defaultValue = "UZ") AppLanguage lang) {
        ApiResponse<String> ok = authService.regVerification(token, lang);
        return ResponseEntity.ok(ok);
    }

    @PostMapping("/registration/login")
    public ResponseEntity<ApiResponse<ProfileDTO>> login(@Valid @RequestBody LoginDTO dto,
                                                         @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        ApiResponse<ProfileDTO> ok = authService.login(dto, language);
        return ResponseEntity.ok(ok);
    }

    @PostMapping("/registration/reset")
    public ResponseEntity<ApiResponse<String>> resent(@Valid @RequestBody ResetPasswordDTO dto,
                                                      @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        ApiResponse<String> ok = authService.resetPassword(dto, language);
        return ResponseEntity.ok(ok);
    }

    @PostMapping("/registration/reset-password/confirm")
    public ResponseEntity<ApiResponse<String>> resentPassword(@Valid @RequestBody ResetPasswordConfirmDTO dto,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage language) {
        ApiResponse<String> ok = authService.resetPasswordConfirm(dto, language);
        return ResponseEntity.ok(ok);
    }
}




