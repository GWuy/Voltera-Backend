package com.g_wuy.swp391.voltera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g_wuy.swp391.voltera.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer id);

    Optional<User> findUserById(Integer id);

    boolean existsByEmail(String email);

    Optional<User> findUserByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean isEmailExist(@Param("email") String email);

    @Query("SELECT a.user FROM Account a WHERE a.username = :username")
    User findUserByUsername(@Param("username") String username);

    @Query("SELECT a.user.id FROM Account a WHERE a.username = :username")
    Integer findUserIdByUsername(@Param("username") String username);
}
