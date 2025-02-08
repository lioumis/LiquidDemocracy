package gr.upatras.ceid.ld.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class VotingCreationException extends Exception {

    private final Map<Integer, String> metadata;

    public VotingCreationException(String message, Map<Integer, String> metadata) {
        super(message);
        this.metadata = metadata;
    }
}
