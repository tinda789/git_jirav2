package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Board;
import com.projectmanagement.userservice.entity.WorkList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByWorkList(WorkList workList);
}