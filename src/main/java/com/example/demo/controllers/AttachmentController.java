package com.example.demo.controllers;

import com.example.demo.entity.Attachment;
import com.example.demo.enums.EntityType;
import com.example.demo.services.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    // POST /api/attachments/upload?entityType=TASK&entityId=1
    @PostMapping("/upload")
    public ResponseEntity<Attachment> upload(
            @RequestParam MultipartFile file,
            @RequestParam EntityType entityType,
            @RequestParam Long entityId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        return ResponseEntity.ok(
                attachmentService.upload(file, entityType, entityId, userDetails.getUsername()));
    }

    // GET /api/attachments?entityType=TASK&entityId=1
    @GetMapping
    public ResponseEntity<List<Attachment>> list(
            @RequestParam EntityType entityType,
            @RequestParam Long entityId) {
        return ResponseEntity.ok(attachmentService.getAttachments(entityType, entityId));
    }

    // GET /api/attachments/{id}/download
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        Resource resource = attachmentService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // DELETE /api/attachments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
