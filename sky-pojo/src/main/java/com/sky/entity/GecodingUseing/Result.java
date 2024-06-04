package com.sky.entity.GecodingUseing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Location location;
    private int precise;
    private int confidence;
    private int comprehension;
    private String level;
}
