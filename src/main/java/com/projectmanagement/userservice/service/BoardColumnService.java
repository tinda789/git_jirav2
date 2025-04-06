package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Board;
import com.projectmanagement.userservice.entity.BoardColumn;
import com.projectmanagement.userservice.repository.BoardColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardColumnService {
    
    private final BoardColumnRepository boardColumnRepository;
    
    @Autowired
    public BoardColumnService(BoardColumnRepository boardColumnRepository) {
        this.boardColumnRepository = boardColumnRepository;
    }
    
    public List<BoardColumn> getAllBoardColumns() {
        return boardColumnRepository.findAll();
    }
    
    public Optional<BoardColumn> getBoardColumnById(Long id) {
        return boardColumnRepository.findById(id);
    }
    
    public List<BoardColumn> getBoardColumnsByBoard(Board board) {
        return boardColumnRepository.findByBoardOrderByPosition(board);
    }
    
    public BoardColumn createBoardColumn(BoardColumn boardColumn) {
        // Nếu không có vị trí, đặt vào cuối
        if (boardColumn.getPosition() == null) {
            List<BoardColumn> columns = boardColumnRepository.findByBoardOrderByPosition(boardColumn.getBoard());
            boardColumn.setPosition(columns.size() + 1);
        }
        return boardColumnRepository.save(boardColumn);
    }
    
    public BoardColumn updateBoardColumn(BoardColumn boardColumn) {
        return boardColumnRepository.save(boardColumn);
    }
    
    public void deleteBoardColumn(Long id) {
        boardColumnRepository.deleteById(id);
    }
    
    public void reorderColumns(Board board, List<Long> columnIdsInOrder) {
        List<BoardColumn> columns = boardColumnRepository.findByBoardOrderByPosition(board);
        
        for (int i = 0; i < columnIdsInOrder.size(); i++) {
            for (BoardColumn column : columns) {
                if (column.getId().equals(columnIdsInOrder.get(i))) {
                    column.setPosition(i + 1);
                    boardColumnRepository.save(column);
                    break;
                }
            }
        }
    }
}