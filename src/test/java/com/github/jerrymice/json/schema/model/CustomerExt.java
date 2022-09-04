package com.github.jerrymice.json.schema.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerExt {
    private String name;
    private Integer age;
    private Boolean sex;
    private Integer work;
    private String  city;
    private Integer marriage;
    private Mate mate;
}
