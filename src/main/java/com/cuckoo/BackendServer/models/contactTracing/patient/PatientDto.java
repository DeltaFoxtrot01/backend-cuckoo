package com.cuckoo.BackendServer.models.contactTracing.patient;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class PatientDto implements Serializable {

    @Getter
    @Setter
    private String encodedSeed;

    @Getter
    @Setter
    private Long epoch;

    @Getter
    @Setter
    private Long randomNumber;

    @Getter
    @Setter
    private Long infectedEpoch;

    public PatientDto() {}

    public PatientDto(String encodedSeed, Long epoch, Long randomNumber, Long infectedEpoch) {
        this.encodedSeed = encodedSeed;
        this.epoch = epoch;
        this.randomNumber = randomNumber;
        this.infectedEpoch = infectedEpoch;
    }
}
