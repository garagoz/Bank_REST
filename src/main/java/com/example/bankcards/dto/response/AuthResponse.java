package com.example.bankcards.dto.response;


import com.example.bankcards.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserInfo user;

//    private AuthResponse(Builder builder) {
//        this.accessToken = builder.accessToken;
//        this.tokenType = builder.tokenType;
//        this.user = builder.user;
//    }

//    public static Builder builder() {
//        return new Builder();
//    }
//
//    public static class Builder {
//        private String accessToken;
//        private String tokenType = "Bearer";
//        private UserInfo user;
//
//        public Builder accessToken(String accessToken) {
//            this.accessToken = accessToken;
//            return this;
//        }
//
//        public Builder tokenType(String tokenType) {
//            this.tokenType = tokenType;
//            return this;
//        }
//
//        public Builder user(UserInfo user) {
//            this.user = user;
//            return this;
//        }
//
//        public AuthResponse build() {
//            return new AuthResponse(this);
//        }
//    }

    public static class UserInfo {

        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Set<Role> roles;

        private UserInfo(UserInfoBuilder builder) {
            this.id = builder.id;
            this.username = builder.username;
            this.email = builder.email;
            this.firstName = builder.firstName;
            this.lastName = builder.lastName;
            this.roles = builder.roles;
        }

        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        public static class UserInfoBuilder {
            private Long id;
            private String username;
            private String email;
            private String firstName;
            private String lastName;
            private Set<Role> roles;

            public UserInfoBuilder id(Long id) {
                this.id = id;
                return this;
            }

            public UserInfoBuilder username(String username) {
                this.username = username;
                return this;
            }

            public UserInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public UserInfoBuilder firstName(String firstName) {
                this.firstName = firstName;
                return this;
            }

            public UserInfoBuilder lastName(String lastName) {
                this.lastName = lastName;
                return this;
            }

            public UserInfoBuilder roles(Set<Role> roles) {
                this.roles = roles;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(this);
            }
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public Set<Role> getRoles() { return roles; }
    }

}
