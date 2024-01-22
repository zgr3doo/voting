package com.example.voting_01.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VoterDTO {
    private Long id;
    private String name;
    private boolean voted;
}
