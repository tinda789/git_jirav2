package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "boards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private BoardType type;
    
    @ManyToOne
@JoinColumn(name = "worklist_id", nullable = false)
private WorkList workList;
    
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<BoardColumn> columns;
}