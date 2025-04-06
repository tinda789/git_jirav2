package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Attachment;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentService {
    
    private final AttachmentRepository attachmentRepository;
    
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }
    
    public List<Attachment> getAllAttachments() {
        return attachmentRepository.findAll();
    }
    
    public Optional<Attachment> getAttachmentById(Long id) {
        return attachmentRepository.findById(id);
    }
    
    public List<Attachment> getAttachmentsByIssue(Issue issue) {
        return attachmentRepository.findByIssue(issue);
    }
    
    public Attachment saveAttachment(MultipartFile file, Issue issue, User uploader) throws IOException {
        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Tạo tên file duy nhất
        String originalFilename = file.getOriginalFilename();
        @SuppressWarnings("null")
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Lưu file vào thư mục
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        
        // Tạo entity Attachment
        Attachment attachment = new Attachment();
        attachment.setFileName(originalFilename);
        attachment.setFileUrl("/uploads/" + uniqueFilename);
        attachment.setFileSize(file.getSize());
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setIssue(issue);
        attachment.setUploader(uploader);
        
        return attachmentRepository.save(attachment);
    }
    
    public void deleteAttachment(Long id) throws IOException {
        Optional<Attachment> attachmentOpt = attachmentRepository.findById(id);
        if (attachmentOpt.isPresent()) {
            Attachment attachment = attachmentOpt.get();
            
            // Xóa file từ hệ thống
            String fileUrl = attachment.getFileUrl();
            if (fileUrl.startsWith("/uploads/")) {
                String filename = fileUrl.substring("/uploads/".length());
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
            }
            
            // Xóa record từ database
            attachmentRepository.deleteById(id);
        }
    }
    
    public boolean canDeleteAttachment(User user, Attachment attachment) {
        // Người tải lên có thể xóa
        if (attachment.getUploader().getId().equals(user.getId())) {
            return true;
        }
        
        // Worklist lead có thể xóa
        Issue issue = attachment.getIssue();
        WorkList workList = issue.getWorkList();
        if (workList.getLead() != null && workList.getLead().getId().equals(user.getId())) {
            return true;
        }
        
        // Admin luôn có thể xóa
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }
}