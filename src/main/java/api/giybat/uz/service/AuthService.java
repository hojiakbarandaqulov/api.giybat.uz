package api.giybat.uz.service;

import api.giybat.uz.dto.*;
import api.giybat.uz.dto.auth.RegistrationDTO;
import api.giybat.uz.dto.auth.ResetPasswordConfirmDTO;
import api.giybat.uz.dto.auth.ResetPasswordDTO;
import api.giybat.uz.dto.profile.ProfileDTO;
import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.repository.ProfileRoleRepository;
import api.giybat.uz.service.sms.EmailSendingService;
import api.giybat.uz.service.sms.SmsService;
import api.giybat.uz.util.EmailUtil;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.PhoneUtil;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class AuthService {
    private final ProfileRepository profileRepository;
    private final EmailSendingService emailSendingService;
    private final ProfileService profileService;
    private final ProfileRoleRepository profileRoleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ProfileRoleService profileRoleService;
    private final ResourceBundleService messagesService;
    private final SmsService smsService;
    private final AttachService attachService;


    public ApiResponse<String> registration(RegistrationDTO dto, AppLanguage language) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isPresent()) {
            ProfileEntity profileEntity = optional.get();
            if (profileEntity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRoleService.deleteRoles(profileEntity.getId());
                profileRepository.delete(profileEntity);
            } else {
                throw new AppBadException(messagesService.getMessage("email.phone.exists", language));
            }
        }
        ProfileEntity entity = new ProfileEntity();
        entity.setName(dto.getName());
        entity.setUsername(dto.getUsername());
        entity.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        entity.setStatus(GeneralStatus.IN_REGISTRATION);
        entity.setVisible(true);
        entity.setCreatedDate(LocalDateTime.now());
        profileRepository.save(entity);

        profileRoleService.create(entity.getId(), ProfileRole.ROLE_USER);

        emailSendingService.sendRegistrationEmail(dto.getUsername(), entity.getId(), language);
        return new ApiResponse<>(messagesService.getMessage("registration.successful", language));
    }

    public ApiResponse<String> regVerification(String token, AppLanguage language) {
        try {
            Integer profileId = JwtUtil.decodeVerRegToken(token);
            ProfileEntity profile = profileService.getById(profileId);
            if (profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRepository.changeStatus(profileId, GeneralStatus.ACTIVE);
                return new ApiResponse<>(messagesService.getMessage("verification.successful", language));
            }
        } catch (JwtException e) {
            throw new RuntimeException(e);
        }
        throw new AppBadException(messagesService.getMessage("verification.wrong", language));
    }

    public ApiResponse<ProfileDTO> login(LoginDTO loginDTO, AppLanguage language) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(loginDTO.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(messagesService.getMessage("username.password.wrong", language));
        }
        ProfileEntity profile = optional.get();
        if (!bCryptPasswordEncoder.matches(loginDTO.getPassword(), profile.getPassword())) {
            throw new AppBadException(messagesService.getMessage("wrong.password", language));
        }
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(messagesService.getMessage("wrong.status", language));
        }
        ProfileDTO response = new ProfileDTO();
        response.setName(profile.getName());
        response.setUsername(profile.getUsername());
        response.setRole(profileRoleRepository.getAllRolesListByProfileId(profile.getId()));
        response.setJwt(JwtUtil.encode(profile.getUsername(), profile.getId(), response.getRole()));
        response.setName(profile.getName());
        response.setPhoto(attachService.attachDTO(profile.getPhotoId()));
        return new ApiResponse<>(response);
    }

    public ApiResponse<String> resetPassword(ResetPasswordDTO dto, AppLanguage language) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(messagesService.getMessage("username.password.wrong", language));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(messagesService.getMessage("wrong.status", language));
        }
        emailSendingService.sentResetPasswordEmail(dto.getUsername(), language);
        return new ApiResponse<>(messagesService.getMessage("reset.password.response", language));
    }

    public ApiResponse<String> resetPasswordConfirm(ResetPasswordConfirmDTO dto, AppLanguage language) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(messagesService.getMessage("verification.wrong", language));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(messagesService.getMessage("wrong.status", language));
        }
        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsService.sendSms(dto.getUsername());
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            emailSendingService.sentResetPasswordEmail(dto.getUsername(), language);
        }
        profileRepository.updatePassword(profile.getId(), bCryptPasswordEncoder.encode(dto.getPassword()));
        return new ApiResponse<>(messagesService.getMessage("reset.password.success", language));
    }
}