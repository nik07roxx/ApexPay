package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Role;
import com.nik07roxx.apexPay.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String rolename);
    List<Role> findByUsers(User user);
}
