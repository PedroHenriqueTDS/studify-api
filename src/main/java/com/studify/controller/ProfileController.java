package com.studify.controller;

import com.studify.dto.profile.ProfileDTOs;
import com.studify.entity.User;
import com.studify.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil", description = "Gerenciamento do perfil do usuário autenticado")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Obter dados do perfil do usuário autenticado")
    public ResponseEntity<ProfileDTOs.ProfileResponse> getProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    @Operation(summary = "Atualizar perfil (partial update — apenas campos informados são alterados)")
    public ResponseEntity<ProfileDTOs.ProfileResponse> updateProfile(
            @Valid @RequestBody ProfileDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.updateProfile(request, user));
    }

    @PatchMapping("/password")
    @Operation(summary = "Alterar senha — exige a senha atual para confirmação")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ProfileDTOs.ChangePasswordRequest request,
            @AuthenticationPrincipal User user) {
        profileService.changePassword(request, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar foto de perfil (JPEG ou PNG, máx. 2MB)")
    public ResponseEntity<ProfileDTOs.ProfileResponse> uploadPicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.uploadProfilePicture(file, user));
    }
}
