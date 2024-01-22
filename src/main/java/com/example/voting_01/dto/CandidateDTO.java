package com.example.voting_01.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CandidateDTO {
    private Long id;
    private String name;
    private Long votes;
}
