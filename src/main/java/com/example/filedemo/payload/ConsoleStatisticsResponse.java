package com.example.filedemo.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsoleStatisticsResponse {
	private int nbrConsoleSortantHubs;
	private int nbrConsoleEntrantHubs;
	private int totalConsole;
}
