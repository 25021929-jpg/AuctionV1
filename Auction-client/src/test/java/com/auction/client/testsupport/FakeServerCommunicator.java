package com.auction.client.testsupport;

import com.auction.client.network.ServerCommunicator;
import com.auction.shared.dto.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Test double cho service tests: không cần server/socket thật. */
public class FakeServerCommunicator implements ServerCommunicator {

  public record Call(String action, Object body, Class<?> responseType) {}

  private boolean connected = true;
  private Response<?> nextResponse = Response.success("OK", null);
  private IOException nextException;
  private final List<Call> calls = new ArrayList<>();

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public void setNextResponse(Response<?> nextResponse) {
    this.nextResponse = nextResponse;
  }

  public void setNextException(IOException nextException) {
    this.nextException = nextException;
  }

  public List<Call> calls() {
    return calls;
  }

  public Call lastCall() {
    return calls.isEmpty() ? null : calls.get(calls.size() - 1);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Response<T> send(String action, Object body, Class<T> responseType)
      throws IOException {
    calls.add(new Call(action, body, responseType));
    if (nextException != null) {
      throw nextException;
    }
    return (Response<T>) nextResponse;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }
}
