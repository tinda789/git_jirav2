package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Board;
import com.projectmanagement.userservice.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByBoard(Board board);
    List<BoardColumn> findByBoardOrderByPosition(Board board);
}