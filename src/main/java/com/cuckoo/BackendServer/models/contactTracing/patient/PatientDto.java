package com.cuckoo.BackendServer.models.contactTracing.patient;

import lombok.Getter;
import lombok.Setter;

public class PatientDto {

    @Getter
    @Setter
    private Long seed;

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

    public PatientDto(Long seed, Long epoch, Long randomNumber, Long infectedEpoch) {
        this.seed = seed;
        this.epoch = epoch;
        this.randomNumber = randomNumber;
        this.infectedEpoch = infectedEpoch;
    }
}
