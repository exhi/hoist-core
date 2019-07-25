package io.xh.hoist.websocket

import groovy.transform.CompileStatic
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import static io.xh.hoist.util.Utils.getWebSocketService

@CompileStatic
class HoistWebSocketHandler extends TextWebSocketHandler {

    @Override
    void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketService.registerSession(session)
    }

    @Override
    void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        webSocketService.onMessage(session, message)
    }

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        webSocketService.unregisterSession(session, closeStatus)
    }

}
