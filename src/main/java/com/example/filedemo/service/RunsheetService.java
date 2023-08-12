package com.example.filedemo.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.example.filedemo.model.Runsheet;
import com.example.filedemo.payload.RunsheetPayload;
import com.example.filedemo.payload.RunsheetResponsePayload;

public interface RunsheetService {
	List<Runsheet> retrieveAllRunsheets();
	Runsheet addRunsheet(RunsheetPayload runsheetPayload);
	void deleteRunsheet(Long id);
	Runsheet updateRunsheet(RunsheetPayload runsheetPayload);
	Runsheet findByBarCode(String barCode);
	Runsheet findById(Long codeRunsheet);
	Runsheet addColisToRunsheet(List<String> barCodeList ,  Long idRunsheet );
	String generateRunsheetBarCode (Runsheet runsheet);
	Runsheet encloseRunsheet(Long id);
	List<RunsheetResponsePayload> retrieveAllRunsheetsNCltrByLivreurId(Long id);
	String getBarCodeColisDirectory();
	ByteArrayInputStream generatePDF(Long id);
}
