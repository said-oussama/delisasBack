package com.example.filedemo.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.filedemo.model.Colis;
import com.example.filedemo.model.ColisEtat;
import com.example.filedemo.model.Console;
import com.example.filedemo.model.ConsoleEtat;
import com.example.filedemo.model.ConsolePDFGenerator;
import com.example.filedemo.model.Hub;
import com.example.filedemo.model.PDFGenerator3;
import com.example.filedemo.model.Personnel;
import com.example.filedemo.model.Runsheet;
import com.example.filedemo.model.User;
import com.example.filedemo.payload.ConsolePayload;
import com.example.filedemo.payload.ConsoleStatisticsResponse;
import com.example.filedemo.payload.DebriefStatisticsResponse;
import com.example.filedemo.repository.ColisRepository;
import com.example.filedemo.repository.ConsoleRepository;
import com.example.filedemo.utility.Utility;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

@Service
public class ConsoleService {
	@Value("${barCodeDirectory.path}")
	private String barCodeDirectory;
	@Value("${pdf.direcotry.path}")
	private String pdfDirectory;
	@Autowired
	ConsoleRepository consoleRepository;
	@Autowired
	ColisService colisService;
	@Autowired
	FournisseurService fournisseurService;
	@Autowired
	ColisRepository colisRepository;
	@Autowired
	PersonnelService personnelService;
	@Autowired
	Iuserservice userService;
	@Autowired
	HubService hubService;
	@Autowired
	SocietePrincipalService societePrincipalService;
	public List<Console> retrieveAllConsoles() {
		return (List<Console>) consoleRepository.findAll();
	}

	public Console addConsole(ConsolePayload consolePayload) {
		Console console = new Console();
		User creator = userService.loadUserById(consolePayload.getIdCreator());
		Personnel livreur = personnelService.retrievePersonnel(consolePayload.getIdLivreur());
		console.setEtat(ConsoleEtat.enAttente);
		console.setIdHubArrivee(consolePayload.getIdHubArrivee());
		console.setIdHubDepart(consolePayload.getIdHubDepart());
		console.setTitreHubArrivee(hubService.retrieveHub(consolePayload.getIdHubArrivee()).getTitre());
		console.setTitreHubDepart(hubService.retrieveHub(consolePayload.getIdHubDepart()).getTitre());
		console.setCreator(creator);
		console.setLivreur(livreur);
		console = consoleRepository.save(console);
		generateColisBarCode(console.createBarCode());
		console.setColis(processColisConsole(consolePayload.getColisBarCode(), console));
		return consoleRepository.save(console);
	}

	public void deleteConsole(Long id) {
		Console console = consoleRepository.findById(id).get();
		console.getColis().stream().forEach(c -> {
			c.setConsole(null);
	        c.setEtat(ColisEtat.enAttenteDEnlevement);
			colisRepository.save(c);
		});
		consoleRepository.deleteById(id);
	}

	public Console updateConsole(Console p) {
		return (consoleRepository.save(p));
	}

	public Console retrieveConsole(String id) {
		return (consoleRepository.findById(Long.parseLong(id)).orElse(null));
	}

	public Hub getArriveeByBarCode(String barcode) {
		if (colisService.findColisByBarCode(barcode).getHub() == null) {
			return null;
		} else {
			return colisService.findColisByBarCode(barcode).getHub();
		}
	}

	private List<Colis> processColisConsole(List<String> colisBarCode, Console console) {
		List<Colis> coliss = new ArrayList<>();
		colisBarCode.stream().forEach(c -> {
			Colis colis = colisRepository.findColisByBarCode(c);
			colis.setConsole(console);
			colis.setEtat(ColisEtat.enCoursDeTransfert);
			coliss.add(colisRepository.save(colis));
		});
		return coliss;
	}

	public Console removeColisFromConsole(String barCode, Long idConsole) {
           Colis colis = colisRepository.findColisByBarCode(barCode);
           colis.setConsole(null);
           colis.setEtat(ColisEtat.enAttenteDEnlevement);
           colisRepository.save(colis);
           return consoleRepository.findById(idConsole).get();
	}
	private void generateColisBarCode(String consoleBarCode) {
		Utility.createDirectoryIfNotExist(Utility.directoryPath(barCodeDirectory));
		String path = Utility.directoryPath(barCodeDirectory) + File.separator + consoleBarCode + ".jpg";
		try {
			Code128Writer writer = new Code128Writer();
			BitMatrix matrix = writer.encode(consoleBarCode, BarcodeFormat.CODE_128, 400, 90);
			MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(path));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public Console findConsoleByBarCode(String barCode) {
		return consoleRepository.findByBarCode(barCode).orElse(null);
	}
	
	public Console approveConsole(String barCode, long idValidator) {
		Console console = consoleRepository.findByBarCode(barCode).orElse(null);
		if(console==null) return null;
		User validator = userService.loadUserById(idValidator);
		console.setValidator(validator);
		console.setEtat(ConsoleEtat.approuve);
		console.getColis().stream().forEach(c -> {
			c.setEtat(ColisEtat.enStock);
			c=colisRepository.save(c);
		});
		return consoleRepository.save(console);
	}
	public List<Console> findConsoleByIdValidor(long idValidator){
		return consoleRepository.findByValidator(idValidator);
	}
	public List<Console> findConsoleByIdCreator(long idCreator){
		return consoleRepository.findByCreator(idCreator);
	}
	public List<Console> findConsolesEntrant(long idPersonnel){
		Personnel personnel=personnelService.retrievePersonnel(idPersonnel);
		if (personnel==null) return new ArrayList<>();
		Hub hubPersonnel = personnel.getHub();
		return hubPersonnel!=null?consoleRepository.findByIdHubArrivee(hubPersonnel.getId_hub()): new ArrayList<>();
	}
	public List<Console> findConsolesSortant(long idPersonnel){
		Personnel personnel=personnelService.retrievePersonnel(idPersonnel);
		if (personnel==null) return new ArrayList<>();
		Hub hubPersonnel = personnel.getHub();
		return hubPersonnel!=null?consoleRepository.findByIdHubDepart(hubPersonnel.getId_hub()): new ArrayList<>();
	}
	
	public List<Console> findConsolesEntrantByIdHub(Long idHub) {
		return consoleRepository.findByIdHubArrivee(idHub);
	}

	public List<Console> findConsolesSortantbyIdHub(Long idHub) {
		return consoleRepository.findByIdHubDepart(idHub);
	}

	public ByteArrayInputStream generatePDF(String consoleBarCode) {
		Console console = findConsoleByBarCode(consoleBarCode);
		ByteArrayInputStream bis=null;
		try {
			if(console.getPdfFileName()!=null) {
				bis= new ByteArrayInputStream(Files.readAllBytes(Paths.get(Utility.directoryPath(this.pdfDirectory)+File.separator+console.getPdfFileName())));
			}
			else {
			bis = ConsolePDFGenerator.generateConsolePDF(console, Utility.directoryPath(this.barCodeDirectory),societePrincipalService.retrieveConfigSocietePrincipal(),
					colisService.getImagesDirectory());
			String pdfFileName="consolePDF"+console.getIdConsole()+".pdf";
			console.setPdfFileName(pdfFileName);;
			Utility.createDirectoryIfNotExist(Utility.directoryPath(this.pdfDirectory));
			File file = new File(Utility.directoryPath(this.pdfDirectory)+File.separator+pdfFileName);
		    FileUtils.copyInputStreamToFile(bis, file);
		    consoleRepository.save(console);
		    bis= new ByteArrayInputStream(Files.readAllBytes(Paths.get(Utility.directoryPath(this.pdfDirectory)+File.separator+console.getPdfFileName())));
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return bis;
	}

	public ConsoleStatisticsResponse getConsoleStatistics() {
		return ConsoleStatisticsResponse.builder()
				.nbrConsoleEntrantHubs(consoleRepository.getNbrConsoleEntrantHubs())
				.nbrConsoleSortantHubs(consoleRepository.getNbrConsoleSortantHubs())
				.totalConsole(consoleRepository.getTotalConsole()).build();
	}
}
