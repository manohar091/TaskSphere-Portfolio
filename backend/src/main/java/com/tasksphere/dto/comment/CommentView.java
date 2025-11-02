package com.tasksphere.dto.comment;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentView {
    private Long id;
    private String authorName;
    private String text;
    private Timestamp createdAt;
}