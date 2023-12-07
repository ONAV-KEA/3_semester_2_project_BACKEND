package dk.kea.onav2ndproject_rest.api;

import dk.kea.onav2ndproject_rest.JwtTokenManager;
import dk.kea.onav2ndproject_rest.dto.EventDTO;
import dk.kea.onav2ndproject_rest.dto.UserDTO;
import dk.kea.onav2ndproject_rest.entity.JwtRequestModel;
import dk.kea.onav2ndproject_rest.entity.JwtResponseModel;
import dk.kea.onav2ndproject_rest.entity.User;
import dk.kea.onav2ndproject_rest.repository.UserRepository;
import dk.kea.onav2ndproject_rest.service.IUserService;
import dk.kea.onav2ndproject_rest.service.JwtUserDetailsService;
import dk.kea.onav2ndproject_rest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenManager jwtTokenManager;
    @Autowired
    private IUserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<JwtResponseModel> signup(@RequestBody JwtRequestModel request){
        System.out.println("signup: username:" + request.getUsername() + " password: " + request.getPassword() );
        User user = new User(request.getUsername(),request.getPassword());
        if(userService.findByName(user.getUsername()).size()==0) {
            if (userService.save(user) != null) {
                return ResponseEntity.ok(new JwtResponseModel("created user: " + user.getUsername() + " pw: " + user.getPassword()));
            } else {
                return ResponseEntity.ok(new JwtResponseModel("error creating user: " + user.getUsername()));
            }
        }else {
            return ResponseEntity.ok(new JwtResponseModel("error: user exists: " + user.getUsername()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseModel> createToken(@RequestBody JwtRequestModel request) throws Exception {
        // HttpServletRequest servletRequest is available from Spring, if needed.
        System.out.println(" JwtController createToken Call: 4" + request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(),
                            request.getPassword())
            );
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            return ResponseEntity.ok(new JwtResponseModel("bad credentials"));
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwtToken = jwtTokenManager.generateJwtToken(userDetails);
        return ResponseEntity.ok(new JwtResponseModel(jwtToken));
    }


    @PostMapping("/getSecret")
    public ResponseEntity<Map> getSecret() {
        System.out.println("getSecret is called");
        Map<String,String > map = new HashMap<>();
        map.put("message","this is secret from server");
        return ResponseEntity.ok(map);
    }

    @GetMapping("/getAllUsers")
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @Secured("MANAGER")
    @PostMapping("/createUser")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Secured("MANAGER")
    @DeleteMapping("/deleteUser")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestBody User user) {
        System.out.println("deleteUser is called with user: " + user.getUsername());
        List<User> users = userService.findByName(user.getUsername());

        if (users.isEmpty()) {
            Map<String, String> map = new HashMap<>();
            map.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }

        User userToDelete = users.get(0);
        userService.delete(userToDelete);

        Map<String, String> map = new HashMap<>();
        map.put("message", "User deleted: " + user.getUsername());
        return ResponseEntity.ok(map);
    }

    @GetMapping()
    public ResponseEntity<UserDTO> getUserByToken(@RequestParam String token){
        System.out.println("getUserByToken is called with token: " + token);
        UserDTO user = userService.findByToken(token);
        return ResponseEntity.ok(user);
    }
}
