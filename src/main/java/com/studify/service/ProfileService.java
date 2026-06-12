package com.studify.service;

import com.studify.dto.profile.ProfileDTOs;
import com.studify.entity.User;
import com.studify.exception.BusinessException;
import com.studify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public ProfileDTOs.ProfileResponse getProfile(User user) {
        return toResponse(user);
    }

    @Transactional
    public ProfileDTOs.ProfileResponse updateProfile(ProfileDTOs.UpdateRequest request, User user) {
        if (request.name() != null)        user.setName(request.name());
        if (request.course() != null)      user.setCourse(request.course());
        if (request.institution() != null) user.setInstitution(request.institution());
        if (request.semester() != null)    user.setSemester(request.semester());
        if (request.timezone() != null)    user.setTimezone(request.timezone());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ProfileDTOs.ChangePasswordRequest request, User user) {
        // Verificar senha atual
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta.");
        }

        // Não permitir reutilizar a mesma senha
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("A nova senha não pode ser igual à senha atual.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public ProfileDTOs.ProfileResponse uploadProfilePicture(MultipartFile file, User user) {
        String url = fileStorageService.storeProfilePicture(file);
        user.setProfilePicture(url);
        return toResponse(userRepository.save(user));
    }

    private ProfileDTOs.ProfileResponse toResponse(User user) {
        return new ProfileDTOs.ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getCourse(),
                user.getInstitution(),
                user.getSemester(),
                user.getTimezone(),
                user.getCreatedAt()
        );
    }
}
