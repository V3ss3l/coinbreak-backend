package com.example.coinbreakbackend.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Currency implements Serializable {
    private String name;
    private String symbol;
}
