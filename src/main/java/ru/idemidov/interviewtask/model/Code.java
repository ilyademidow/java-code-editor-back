package ru.idemidov.interviewtask.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Code implements Serializable {
    private String username;
    private String code;
    private String apiKey;
}
