package com.example.grpc.service;

import com.example.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserServiceImpl userService;

    @Mock
    private StreamObserver<GetUserResponse> getUserResponseObserver;

    @Mock
    private StreamObserver<UpdateUserResponse> updateUserResponseObserver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl();
    }

    @Test
    void getUser_ShouldReturnDefaultUser_WhenUserDoesNotExist() {
        // Given
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId("non-existent")
                .build();

        // When
        userService.getUser(request, getUserResponseObserver);

        // Then
        ArgumentCaptor<GetUserResponse> responseCaptor = ArgumentCaptor.forClass(GetUserResponse.class);
        verify(getUserResponseObserver).onNext(responseCaptor.capture());
        verify(getUserResponseObserver).onCompleted();
        verify(getUserResponseObserver, never()).onError(any());

        GetUserResponse response = responseCaptor.getValue();
        assertEquals("non-existent", response.getUserId());
        assertEquals("Default User", response.getName());
        assertEquals("default@example.com", response.getEmail());
    }

    @Test
    void updateUser_ShouldStoreAndReturnSuccess() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.newBuilder()
                .setUserId("test-user")
                .setName("Test User")
                .setEmail("test@example.com")
                .build();

        // When
        userService.updateUser(request, updateUserResponseObserver);

        // Then
        ArgumentCaptor<UpdateUserResponse> responseCaptor = ArgumentCaptor.forClass(UpdateUserResponse.class);
        verify(updateUserResponseObserver).onNext(responseCaptor.capture());
        verify(updateUserResponseObserver).onCompleted();
        verify(updateUserResponseObserver, never()).onError(any());

        UpdateUserResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals("User updated successfully", response.getMessage());

        // Verify user was stored
        GetUserRequest getUserRequest = GetUserRequest.newBuilder()
                .setUserId("test-user")
                .build();
        userService.getUser(getUserRequest, getUserResponseObserver);

        ArgumentCaptor<GetUserResponse> getUserResponseCaptor = ArgumentCaptor.forClass(GetUserResponse.class);
        verify(getUserResponseObserver).onNext(getUserResponseCaptor.capture());
        GetUserResponse getUserResponse = getUserResponseCaptor.getValue();
        assertEquals("test-user", getUserResponse.getUserId());
        assertEquals("Test User", getUserResponse.getName());
        assertEquals("test@example.com", getUserResponse.getEmail());
    }
} 