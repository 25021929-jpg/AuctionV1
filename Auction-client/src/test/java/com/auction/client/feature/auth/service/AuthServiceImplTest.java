package com.auction.client.feature.auth.service;

import com.auction.shared.protocol.ActionConstants;
import com.auction.client.core.error.ApiException;
import com.auction.client.testsupport.FakeServerCommunicator;
import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.UserInfo;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.shared.dto.auth.request.RegisterRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceImplTest {

    @Test
    void loginSendsAuthLoginAndReturnsAuthResponse() throws IOException {
        FakeServerCommunicator communicator = new FakeServerCommunicator();
        UserInfo user = new UserInfo(1L, "Nguyen Van A", "vana", "a@example.com", "0900000000", "2000-01-01", "BIDDER");
        communicator.setNextResponse(Response.success("OK", new AuthResponse(user)));

        AuthServiceImpl service = new AuthServiceImpl(communicator);
        AuthResponse result = service.login(new LoginRequest("vana", "secret"));

        assertSame(user, result.getUser());
        assertEquals(ActionConstants.AUTH_LOGIN, communicator.lastCall().action());
        assertInstanceOf(LoginRequest.class, communicator.lastCall().body());
        assertEquals(AuthResponse.class, communicator.lastCall().responseType());
    }

    @Test
    void loginThrowsWhenServerReturnsSuccessWithoutUser() {
        FakeServerCommunicator communicator = new FakeServerCommunicator();
        communicator.setNextResponse(Response.success("OK", new AuthResponse(null)));

        AuthServiceImpl service = new AuthServiceImpl(communicator);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("vana", "secret")));
        assertEquals(ActionConstants.AUTH_LOGIN, exception.getAction());
    }

    @Test
    void registerSendsAuthRegister() throws IOException {
        FakeServerCommunicator communicator = new FakeServerCommunicator();
        communicator.setNextResponse(Response.success("Created", null));

        RegisterRequest request = new RegisterRequest(
                "Nguyen Van A", "vana", "a@example.com", "0900000000",
                "secret123", "secret123", java.time.LocalDate.of(2000, 1, 1)
        );

        AuthServiceImpl service = new AuthServiceImpl(communicator);
        service.register(request);

        assertEquals(ActionConstants.AUTH_REGISTER, communicator.lastCall().action());
        assertSame(request, communicator.lastCall().body());
        assertEquals(Void.class, communicator.lastCall().responseType());
    }
}
