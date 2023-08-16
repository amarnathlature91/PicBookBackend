package com.picgram.repository;

import com.picgram.model.Post;
import com.picgram.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findPostsByCreator(User author, Pageable pageable);
    List<Post> findPostsByCreatorIn(Collection<User> creator ,Pageable pageable);

    void deleteAllPostsByCreator(User u);

}
