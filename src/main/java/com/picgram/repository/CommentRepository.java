package com.picgram.repository;

import com.picgram.model.Comment;
import com.picgram.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPost(Post post, Pageable pageable);
}
