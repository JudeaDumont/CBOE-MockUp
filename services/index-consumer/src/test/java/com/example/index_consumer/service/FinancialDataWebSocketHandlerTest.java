package com.example.index_consumer.service;

import com.example.index_consumer.websocket.FinancialDataWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class FinancialDataWebSocketHandlerTest {

    private FinancialDataWebSocketHandler webSocketHandler;
    private WebSocketSession mockSession1;
    private WebSocketSession mockSession2;
    private List<WebSocketSession> sessions;

    @BeforeEach
    public void setup() {
        webSocketHandler = new FinancialDataWebSocketHandler();
        mockSession1 = mock(WebSocketSession.class);
        mockSession2 = mock(WebSocketSession.class);
        sessions = new ArrayList<>();
        sessions.add(mockSession1);
        sessions.add(mockSession2);

        // Ensure the sessions are considered open
        when(mockSession1.isOpen()).thenReturn(true);
        when(mockSession2.isOpen()).thenReturn(true);
    }

    @Test
    public void testAfterConnectionEstablished() throws IOException {
        webSocketHandler.afterConnectionEstablished(mockSession1);
        webSocketHandler.afterConnectionEstablished(mockSession2);

        // Simulate sending a message
        String testMessage = "Test Message";
        webSocketHandler.sendMessageToAll(testMessage);

        // Verify that the message is sent to both sessions
        verify(mockSession1, times(1)).sendMessage(new TextMessage(testMessage));
        verify(mockSession2, times(1)).sendMessage(new TextMessage(testMessage));
    }

    @Test
    public void testAfterConnectionClosed() throws Exception {
        when(mockSession1.isOpen()).thenReturn(true);
        when(mockSession2.isOpen()).thenReturn(true);

        webSocketHandler.afterConnectionEstablished(mockSession1);
        webSocketHandler.afterConnectionEstablished(mockSession2);

        webSocketHandler.afterConnectionClosed(mockSession1, null);

        String testMessage = "Test Message";
        webSocketHandler.sendMessageToAll(testMessage);

        // Verify that the message is only sent to the open session
        verify(mockSession1, times(0)).sendMessage(new TextMessage(testMessage));
        verify(mockSession2, times(1)).sendMessage(new TextMessage(testMessage));
    }
}
