package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.BoardColumnDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.Board;
import com.projectmanagement.userservice.entity.BoardColumn;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.service.BoardColumnService;
import com.projectmanagement.userservice.service.BoardService;
import com.projectmanagement.userservice.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/board-columns")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BoardColumnController {

    @Autowired
    private BoardColumnService boardColumnService;
    
    @Autowired
    private BoardService boardService;
    
    @Autowired
    private IssueService issueService;
    
    @GetMapping("/board/{boardId}")
    @PreAuthorize("@securityService.canManageBoard(#boardId, principal)")
    public ResponseEntity<?> getColumnsByBoard(@PathVariable Long boardId) {
        return boardService.getBoardById(boardId)
                .map(board -> {
                    List<BoardColumn> columns = boardColumnService.getBoardColumnsByBoard(board);
                    List<BoardColumnDto> columnDtos = columns.stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(columnDtos);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("@securityService.canManageBoard(#columnDto.boardId, principal)")
    public ResponseEntity<?> createColumn(@Valid @RequestBody BoardColumnDto columnDto) {
        Optional<Board> boardOptional = boardService.getBoardById(columnDto.getBoardId());
        
        if (!boardOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Board not found"));
        }
        
        Board board = boardOptional.get();
        BoardColumn column = new BoardColumn();
        column.setName(columnDto.getName());
        column.setPosition(columnDto.getPosition());
        column.setBoard(board);
        column.setIssues(new ArrayList<>());
        
        // Thêm issues vào column nếu có
        if (columnDto.getIssueIds() != null && !columnDto.getIssueIds().isEmpty()) {
            List<Issue> issues = columnDto.getIssueIds().stream()
                    .map(issueId -> issueService.getIssueById(issueId).orElse(null))
                    .filter(issue -> issue != null)
                    .collect(Collectors.toList());
            column.setIssues(issues);
        }
        
        BoardColumn savedColumn = boardColumnService.createBoardColumn(column);
        return ResponseEntity.ok(convertToDto(savedColumn));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canManageBoard(#columnDto.boardId, principal)")
    public ResponseEntity<?> updateColumn(@PathVariable Long id, @Valid @RequestBody BoardColumnDto columnDto) {
        return boardColumnService.getBoardColumnById(id)
                .map(column -> {
                    column.setName(columnDto.getName());
                    if (columnDto.getPosition() != null) {
                        column.setPosition(columnDto.getPosition());
                    }
                    
                    // Cập nhật issues nếu có
                    if (columnDto.getIssueIds() != null) {
                        List<Issue> issues = columnDto.getIssueIds().stream()
                                .map(issueId -> issueService.getIssueById(issueId).orElse(null))
                                .filter(issue -> issue != null)
                                .collect(Collectors.toList());
                        column.setIssues(issues);
                    }
                    
                    BoardColumn updatedColumn = boardColumnService.updateBoardColumn(column);
                    return ResponseEntity.ok(convertToDto(updatedColumn));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canManageBoard(#boardId, principal)")
    public ResponseEntity<?> deleteColumn(@PathVariable Long id, @RequestParam Long boardId) {
        if (!boardColumnService.getBoardColumnById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        boardColumnService.deleteBoardColumn(id);
        return ResponseEntity.ok(new MessageResponse("Column deleted successfully"));
    }
    
    @PostMapping("/reorder")
    @PreAuthorize("@securityService.canManageBoard(#boardId, principal)")
    public ResponseEntity<?> reorderColumns(@RequestParam Long boardId, @RequestBody List<Long> columnIds) {
        return boardService.getBoardById(boardId)
                .map(board -> {
                    boardColumnService.reorderColumns(board, columnIds);
                    return ResponseEntity.ok(new MessageResponse("Columns reordered successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{columnId}/issues/{issueId}")
    @PreAuthorize("@securityService.canManageBoard(#boardId, principal)")
    public ResponseEntity<?> addIssueToColumn(@PathVariable Long columnId, @PathVariable Long issueId, @RequestParam Long boardId) {
        return boardColumnService.getBoardColumnById(columnId)
                .map(column -> {
                    return issueService.getIssueById(issueId)
                            .map(issue -> {
                                // Kiểm tra issue thuộc cùng worklist với board
                                if (!issue.getWorkList().getId().equals(column.getBoard().getWorkList().getId())) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("Issue must belong to the same worklist as the board"));
                                }
                                
                                // Thêm issue vào column
                                List<Issue> issues = column.getIssues();
                                if (!issues.contains(issue)) {
                                    issues.add(issue);
                                    boardColumnService.updateBoardColumn(column);
                                }
                                
                                return ResponseEntity.ok(new MessageResponse("Issue added to column successfully"));
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{columnId}/issues/{issueId}")
    @PreAuthorize("@securityService.canManageBoard(#boardId, principal)")
    public ResponseEntity<?> removeIssueFromColumn(@PathVariable Long columnId, @PathVariable Long issueId, @RequestParam Long boardId) {
        return boardColumnService.getBoardColumnById(columnId)
                .map(column -> {
                    List<Issue> issues = column.getIssues();
                    boolean removed = issues.removeIf(issue -> issue.getId().equals(issueId));
                    
                    if (removed) {
                        boardColumnService.updateBoardColumn(column);
                        return ResponseEntity.ok(new MessageResponse("Issue removed from column successfully"));
                    } else {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Issue not found in this column"));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private BoardColumnDto convertToDto(BoardColumn column) {
        BoardColumnDto dto = new BoardColumnDto();
        dto.setId(column.getId());
        dto.setName(column.getName());
        dto.setPosition(column.getPosition());
        dto.setBoardId(column.getBoard().getId());
        
        if (column.getIssues() != null) {
            List<Long> issueIds = column.getIssues().stream()
                    .map(Issue::getId)
                    .collect(Collectors.toList());
            dto.setIssueIds(issueIds);
        }
        
        return dto;
    }
}