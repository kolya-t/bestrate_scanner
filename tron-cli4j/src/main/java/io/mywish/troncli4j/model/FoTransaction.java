package io.mywish.troncli4j.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoTransaction {
    @Getter
    @JsonProperty("id")
    private final String id;
    @Getter
    @JsonProperty("fee")
    private final Long fee;
    @Getter
    @JsonProperty("blockNumber")
    private final String blockNumber;
    @Getter
    @JsonProperty("blockTimeStamp")
    private final String blockTimeStamp;
}
