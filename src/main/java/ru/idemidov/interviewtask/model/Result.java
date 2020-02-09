package ru.idemidov.interviewtask.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    private String success;
    private String error;
}
