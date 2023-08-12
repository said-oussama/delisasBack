package com.example.filedemo.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColisAssignAnomaliePayload {
 private List<String> barCodes;
 private String anomalieDescription;
}
