package com.example.filedemo.payload;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RunsheetResponsePayload {
	private Long id;
	private String barCode;
	private LocalDateTime creationDate;
}
