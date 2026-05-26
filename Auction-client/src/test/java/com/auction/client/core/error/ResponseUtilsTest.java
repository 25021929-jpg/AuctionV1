package com.auction.client.core.error;

import com.auction.shared.dto.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseUtilsTest {

    @Test
    void unwrap_shouldReturnData_whenSuccess() throws Exception {
        Response<String> res = new Response<>();
        res.setSuccess(true);
        res.setData("OK");

        String data = ResponseUtils.unwrap("TEST_ACTION", res);
        assertEquals("OK", data);
    }

    @Test
    void unwrap_shouldThrowApiException_whenResponseNull() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ResponseUtils.unwrap("TEST_ACTION", null));
        assertTrue(ex.getMessage().contains("TEST_ACTION"));
    }

    @Test
    void unwrap_shouldThrowApiException_whenFailureAndMessagePresent() {
        Response<Void> res = new Response<>();
        res.setSuccess(false);
        res.setMessage("Bad request");

        ApiException ex = assertThrows(ApiException.class,
                () -> ResponseUtils.unwrap("TEST_ACTION", res));
        assertTrue(ex.getMessage().contains("Bad request"));
    }

    @Test
    void unwrap_shouldThrowApiException_whenFailureAndMessageBlank() {
        Response<Void> res = new Response<>();
        res.setSuccess(false);
        res.setMessage("  ");

        ApiException ex = assertThrows(ApiException.class,
                () -> ResponseUtils.unwrap("TEST_ACTION", res));
        assertTrue(ex.getMessage().contains("TEST_ACTION"));
    }

    @Test
    void unwrap_shouldThrowApiException_whenFailureAndMessageNull() {
        Response<Void> res = new Response<>();
        res.setSuccess(false);
        res.setMessage(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> ResponseUtils.unwrap("TEST_ACTION", res));
        assertTrue(ex.getMessage().contains("TEST_ACTION"));
    }

    @Test
    void unwrap_shouldReturnNull_whenSuccessAndDataNull() throws Exception {
        Response<String> res = new Response<>();
        res.setSuccess(true);
        res.setData(null);

        assertNull(ResponseUtils.unwrap("TEST_ACTION", res));
    }
}
