package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.BoardDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.Board;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.service.BoardService;
import com.projectmanagement.userservice.service.WorkListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/boards")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BoardController {

    @Autowired
    private BoardService boardService;
    
    @Autowired
    private WorkListService workListService;
    
    @GetMapping("/worklist/{workListId}")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getBoardsByWorkList(@PathVariable Long workListId) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(workListId);
        
        if (!workListOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkList workList = workListOptional.get();
        List<Board> boards = boardService.getBoardsByWorkList(workList);
        List<BoardDto> boardDtos = boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageBoard(#id, principal)")
    public ResponseEntity<?> getBoardById(@PathVariable Long id) {
        Optional<Board> boardOptional = boardService.getBoardById(id);
        
        if (!boardOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Board board = boardOptional.get();
        return ResponseEntity.ok(convertToDto(board));
    }
    
    @PostMapping
    @PreAuthorize("@securityService.canManageWorkList(#boardDto.workListId, principal)")
    public ResponseEntity<?> createBoard(@Valid @RequestBody BoardDto boardDto) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(boardDto.getWorkListId());
        
        if (!workListOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("WorkList not found"));
        }
        
        WorkList workList = workListOptional.get();
        Board board = new Board();
        board.setName(boardDto.getName());
        board.setType(boardDto.getType());
        board.setWorkList(workList);
        
        Board savedBoard = boardService.createBoard(board);
        return ResponseEntity.ok(convertToDto(savedBoard));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canManageBoard(#id, principal)")
    public ResponseEntity<?> updateBoard(@PathVariable Long id, @Valid @RequestBody BoardDto boardDto) {
        Optional<Board> boardOptional = boardService.getBoardById(id);
        
        if (!boardOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Board board = boardOptional.get();
        board.setName(boardDto.getName());
        if (boardDto.getType() != null) {
            board.setType(boardDto.getType());
        }
        Board updatedBoard = boardService.updateBoard(board);
        return ResponseEntity.ok(convertToDto(updatedBoard));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canManageBoard(#id, principal)")
    public ResponseEntity<?> deleteBoard(@PathVariable Long id) {
        if (!boardService.getBoardById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        boardService.deleteBoard(id);
        return ResponseEntity.ok(new MessageResponse("Board deleted successfully"));
    }
    
    private BoardDto convertToDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setName(board.getName());
        dto.setType(board.getType());
        dto.setWorkListId(board.getWorkList().getId());
        return dto;
    }
}