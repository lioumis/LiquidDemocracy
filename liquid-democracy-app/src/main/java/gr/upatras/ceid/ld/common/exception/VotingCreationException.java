package gr.upatras.ceid.ld.common.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Getter
public class VotingCreationException extends Exception {

    private final Map<Integer, String> metadata;

    public VotingCreationException(String message, Map<Integer, String> metadata) {
        super(message);
        this.metadata = metadata;
        log.warn(message);
    }
}
