package com.user.identity.dto.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileUpdateRequestTest {

    @Test
    @DisplayName("Should allow updating each profile field individually")
    void test_01(){
        UserProfileUpdateRequest displayNameUpdate =
                new UserProfileUpdateRequest("Johnny", null,null);

        UserProfileUpdateRequest firstNameUpdate =
                new UserProfileUpdateRequest(null, "John",null);

        UserProfileUpdateRequest lastNameUpdate =
                new UserProfileUpdateRequest(null, null,"Doe");

        assertThat(displayNameUpdate.displayName()).isEqualTo("Johnny");
        assertThat(displayNameUpdate.firstName()).isNull();
        assertThat(displayNameUpdate.lastName()).isNull();

        assertThat(firstNameUpdate.displayName()).isNull();
        assertThat(firstNameUpdate.firstName()).isEqualTo("John");
        assertThat(firstNameUpdate.lastName()).isNull();

        assertThat(lastNameUpdate.displayName()).isNull();
        assertThat(lastNameUpdate.firstName()).isNull();
        assertThat(lastNameUpdate.lastName()).isEqualTo("Doe");
    }
    @Test
    @DisplayName("Should allow updating multiple profile field simultaneously")
    void test_02() {
        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Johnny", "John", null);

        assertThat(request.displayName()).isEqualTo("Johnny");
        assertThat(request.firstName()).isEqualTo("John");
        assertThat(request.lastName()).isNull();
    }
    @Test
    @DisplayName("Should allow updating all profile field simultaneously")
    void test_03() {
        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Johnny", "John", "Doe");

        assertThat(request.displayName()).isEqualTo("Johnny");
        assertThat(request.firstName()).isEqualTo("John");
        assertThat(request.lastName()).isEqualTo("Doe");
    }
    @Test
    @DisplayName("Should allow constructing request with all fields null")
    void test_04() {
        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(null, null, null);

        assertThat(request.displayName()).isNull();
        assertThat(request.firstName()).isNull();
        assertThat(request.lastName()).isNull();
    }
}
