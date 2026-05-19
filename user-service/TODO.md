# TODO: Implement User Roles (Admin, User)

## Steps to Complete

- [ ] Create Role enum with ADMIN and USER values
- [ ] Add role field to User.java entity
- [ ] Update CreateUserRequest.java to include role field (optional, default USER)
- [ ] Update UserResponse.java to include role field
- [ ] Update UserDetailsServiceImpl.java to include GrantedAuthority based on role
- [ ] Update JwtUtil.java to include roles in JWT claims
- [ ] Update SecurityConfig.java to add role-based access control
- [ ] Update UserService.java to set role during user creation
- [ ] Update AuthService.java if needed for role handling in login
- [ ] Test the implementation
