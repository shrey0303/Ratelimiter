package com.example.grpc.service;

import com.example.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
@Service
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        String userId = request.getUserId();
        User user = userStore.getOrDefault(userId, createDefaultUser(userId));

        GetUserResponse response = GetUserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        String userId = request.getUserId();
        User user = new User(userId, request.getName(), request.getEmail());
        userStore.put(userId, user);

        UpdateUserResponse response = UpdateUserResponse.newBuilder()
                .setSuccess(true)
                .setMessage("User updated successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private User createDefaultUser(String userId) {
        return new User(userId, "Default User", "default@example.com");
    }

    private static class User {
        private final String userId;
        private final String name;
        private final String email;

        public User(String userId, String name, String email) {
            this.userId = userId;
            this.name = name;
            this.email = email;
        }

        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
} 