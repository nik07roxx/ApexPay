package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.User.UserCreationRequest;
import com.nik07roxx.apexPay.DTO.User.UserCreationResponse;
import com.nik07roxx.apexPay.DTO.User.UserLoginRequest;
import com.nik07roxx.apexPay.Entity.Role;
import com.nik07roxx.apexPay.Entity.User;
import com.nik07roxx.apexPay.Repository.RoleRepository;
import com.nik07roxx.apexPay.Repository.UserRepository;
import com.nik07roxx.apexPay.Service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserCreationResponse createUser(UserCreationRequest userCreationRequest) {
        // check if user with the email or username already exists
        if(userRepository.existsByUsername(userCreationRequest.username()))
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User with given username already exists"+userCreationRequest.username());
        }

        if(userRepository.existsByEmail(userCreationRequest.email()))
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User with given email already exists: "+userCreationRequest.email());
        }

        // transfer data to User Object
        User user = new User();
        user.setEmail(userCreationRequest.email());
        user.setUsername(userCreationRequest.username());
        // hash the password before setting it in user object
        user.setPassword(bCryptPasswordEncoder.encode(userCreationRequest.password()));

        // Set the roles in the User object
        Set<Role> roles = new HashSet<>();
        Role roleForUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No role found called USER."));
        roles.add(roleForUser);

        // Add code here to check input and see if flag for admin is marked, if yes then add admin role too
        if(userCreationRequest.isAdmin())
        {
            Role roleForAdmin = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No role found called ADMIN."));
            roles.add(roleForAdmin);
        }

        user.setRoles(roles);

        // save the User object
        User savedUser = userRepository.save(user);

        return new UserCreationResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "User successfully created!"
        );
    }

    @Override
    public String verifyUser(UserLoginRequest userLoginRequest) {
        // Create the token
        Authentication token = new UsernamePasswordAuthenticationToken(
                userLoginRequest.username(),
                userLoginRequest.password()
        );
        try
        {
            Authentication authenticate = authenticationManager.authenticate(token);
            // Set the user in SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authenticate);
            // Extract UserDetails from the auth object
            UserDetails userDetails = (UserDetails) authenticate.getPrincipal();

            return jwtService.generateToken(userLoginRequest.username(), userDetails);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
