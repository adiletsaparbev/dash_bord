package com.example.demo.controllers;

import com.example.demo.dto.request.UpdateNameRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Image;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =========================================================
    // GET /api/users/me
    // Получить информацию о себе
    // =========================================================
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                userService.getMyProfile(userDetails.getUsername())
        );
    }

    // =========================================================
    // POST /api/users/me/avatar
    // Загрузить или заменить аватар
    // =========================================================
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(
                userService.uploadAvatar(userDetails.getUsername(), file)
        );
    }

    // =========================================================
    // GET /api/users/me/avatar
    // Получить аватар в виде изображения
    // =========================================================
    @GetMapping("/me/avatar")
    public ResponseEntity<byte[]> getMyAvatar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Image avatar = userService.getAvatar(userDetails.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, avatar.getContentType())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + avatar.getOriginalFileName() + "\"")
                .body(avatar.getBytes());
    }

    // =========================================================
    // GET /api/users/{id}/avatar
    // Получить аватар другого пользователя по его email
    // (например, для отображения в карточках задач)
    // =========================================================
    @GetMapping("/{email}/avatar")
    public ResponseEntity<byte[]> getAvatarByEmail(
            @PathVariable String email) {

        Image avatar = userService.getAvatar(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, avatar.getContentType())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + avatar.getOriginalFileName() + "\"")
                .body(avatar.getBytes());
    }

    // =========================================================
    // PATCH /api/users/me/name
    // Изменить имя и/или фамилию
    // =========================================================
    @PatchMapping("/me/name")
    public ResponseEntity<UserResponse> updateName(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateNameRequest request) {

        return ResponseEntity.ok(
                userService.updateName(
                        userDetails.getUsername(),
                        request.name(),
                        request.surname()
                )
        );
    }
    // Добавьте в UserController.java

    @PatchMapping("/me/position")
    public ResponseEntity<UserResponse> updateMyPosition(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("positionId") Long positionId) {

        return ResponseEntity.ok(
                userService.updatePosition(userDetails.getUsername(), positionId)
        );
    }
}

