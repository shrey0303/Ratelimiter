package com.example.grpc.client;

import com.example.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

public class UserServiceClient {
    private final UserServiceGrpc.UserServiceBlockingStub blockingStub;
    private final ManagedChannel channel;

    public UserServiceClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown();
    }

    public GetUserResponse getUser(String userId) {
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER), userId);
        UserServiceGrpc.UserServiceBlockingStub stubWithHeaders = MetadataUtils.attachHeaders(blockingStub, headers);

        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        return stubWithHeaders.getUser(request);
    }

    public UpdateUserResponse updateUser(String userId, String name, String email) {
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER), userId);
        UserServiceGrpc.UserServiceBlockingStub stubWithHeaders = MetadataUtils.attachHeaders(blockingStub, headers);

        UpdateUserRequest request = UpdateUserRequest.newBuilder()
                .setUserId(userId)
                .setName(name)
                .setEmail(email)
                .build();
        return stubWithHeaders.updateUser(request);
    }

    public static void main(String[] args) throws InterruptedException {
        UserServiceClient client = new UserServiceClient("localhost", 9090);
        try {
            // Test getUser
            GetUserResponse user = client.getUser("test-user");
            System.out.println("Got user: " + user.getName() + " (" + user.getEmail() + ")");

            // Test updateUser
            UpdateUserResponse updateResponse = client.updateUser("test-user", "Updated Name", "updated@example.com");
            System.out.println("Update response: " + updateResponse.getMessage());

            // Verify update
            user = client.getUser("test-user");
            System.out.println("Updated user: " + user.getName() + " (" + user.getEmail() + ")");
        } finally {
            client.shutdown();
        }
    }
} 