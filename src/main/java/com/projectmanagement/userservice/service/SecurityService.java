package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.*;
import com.projectmanagement.userservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkListRepository workListRepository;
    private final IssueRepository issueRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    
    @Autowired
    public SecurityService(
            WorkspaceRepository workspaceRepository,
            WorkListRepository workListRepository,
            IssueRepository issueRepository,
            BoardRepository boardRepository,
            BoardColumnRepository boardColumnRepository,
            CommentRepository commentRepository,
            AttachmentRepository attachmentRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workListRepository = workListRepository;
        this.issueRepository = issueRepository;
        this.boardRepository = boardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
    }
    
    // Workspace security
    public boolean isWorkspaceOwner(Long workspaceId, User user) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        return workspaceOpt.map(workspace -> 
                workspace.getOwner().getId().equals(user.getId())).orElse(false);
    }
    
    public boolean canManageWorkspace(Long workspaceId, User user) {
        // Admin hoặc workspace owner có thể quản lý
        return hasAdminRole(user) || isWorkspaceOwner(workspaceId, user);
    }
    
    // WorkList security
    public boolean isWorkListLead(Long workListId, User user) {
        Optional<WorkList> workListOpt = workListRepository.findById(workListId);
        return workListOpt.map(workList ->
                workList.getLead() != null && workList.getLead().getId().equals(user.getId())).orElse(false);
    }
    
    public boolean canManageWorkList(Long workListId, User user) {
        Optional<WorkList> workListOpt = workListRepository.findById(workListId);
        if (workListOpt.isEmpty()) {
            return false;
        }
        
        WorkList workList = workListOpt.get();
        
        // Admin, workspace owner hoặc worklist lead có thể quản lý
        return hasAdminRole(user) || 
               isWorkspaceOwner(workList.getWorkspace().getId(), user) ||
               isWorkListLead(workListId, user);
    }
    
    // Issue security
    public boolean canModifyIssue(Long issueId, User user) {
        Optional<Issue> issueOpt = issueRepository.findById(issueId);
        if (issueOpt.isEmpty()) {
            return false;
        }
        
        Issue issue = issueOpt.get();
        
        // Admin, workspace owner, worklist lead, reporter hoặc assignee có thể sửa
        return hasAdminRole(user) ||
               isWorkspaceOwner(issue.getWorkList().getWorkspace().getId(), user) ||
               isWorkListLead(issue.getWorkList().getId(), user) ||
               (issue.getReporter() != null && issue.getReporter().getId().equals(user.getId())) ||
               (issue.getAssignee() != null && issue.getAssignee().getId().equals(user.getId()));
    }
    
    // Board security
    public boolean canManageBoard(Long boardId, User user) {
        Optional<Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            return false;
        }
        
        Board board = boardOpt.get();
        
        // Admin, workspace owner hoặc worklist lead có thể quản lý
        return hasAdminRole(user) ||
               isWorkspaceOwner(board.getWorkList().getWorkspace().getId(), user) ||
               isWorkListLead(board.getWorkList().getId(), user);
    }
    
    // Comment security
    public boolean isCommentAuthor(Long commentId, User user) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        return commentOpt.map(comment -> 
                comment.getAuthor().getId().equals(user.getId())).orElse(false);
    }
    
    // Attachment security
    public boolean canManageAttachment(Long attachmentId, User user) {
        Optional<Attachment> attachmentOpt = attachmentRepository.findById(attachmentId);
        if (attachmentOpt.isEmpty()) {
            return false;
        }
        
        Attachment attachment = attachmentOpt.get();
        
        // Admin, workspace owner, worklist lead hoặc uploader có thể quản lý
        return hasAdminRole(user) ||
               isWorkspaceOwner(attachment.getIssue().getWorkList().getWorkspace().getId(), user) ||
               isWorkListLead(attachment.getIssue().getWorkList().getId(), user) ||
               attachment.getUploader().getId().equals(user.getId());
    }
    
    // Helper methods
    public boolean isCurrentUser(Long userId, User user) {
        return user.getId().equals(userId);
    }
    
    public boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }
}