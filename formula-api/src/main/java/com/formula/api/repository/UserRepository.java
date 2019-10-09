package com.formula.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.formula.api.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

}
