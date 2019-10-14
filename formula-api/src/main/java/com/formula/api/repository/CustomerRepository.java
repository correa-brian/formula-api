package com.formula.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.formula.api.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
