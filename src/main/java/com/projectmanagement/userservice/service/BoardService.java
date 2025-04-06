package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Board;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {
    
    private final BoardRepository boardRepository;
    
    @Autowired
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }
    
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }
    
    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }
    
    public List<Board> getBoardsByWorkList(WorkList workList) {
        return boardRepository.findByWorkList(workList);
    }
    
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }
    
    public Board updateBoard(Board board) {
        return boardRepository.save(board);
    }
    
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }
}