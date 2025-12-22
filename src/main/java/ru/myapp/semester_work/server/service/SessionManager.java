package ru.myapp.semester_work.server.service;

import ru.myapp.semester_work.models.PlayerType;
import ru.myapp.semester_work.models.SessionState;
import ru.myapp.semester_work.models.TeamType;

import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final ConcurrentHashMap<String, SessionState> sessions;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public Optional<SessionState> getSessionStateByRoomName(String roomName) {
        return Optional.ofNullable(sessions.get(roomName));
    }

    public SessionState addAndGetSessionState(String roomName) {
        SessionState sessionState = new SessionState();
        sessions.put(roomName, sessionState);
        return sessionState;
    }

    public void updateClientType(String roomName, SocketChannel clientChannel, TeamType teamType, PlayerType playerType,
                                 TeamType oldTeamType, PlayerType oldPlayerType) {
        SessionState sessionState = sessions.get(roomName);
        sessionState.clearClientType(oldTeamType, oldPlayerType);
        sessionState.setClientType(clientChannel, teamType, playerType);
    }
}
