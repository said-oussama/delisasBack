package com.example.filedemo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.filedemo.controller.ColisController;
import com.example.filedemo.model.Colis;
import com.example.filedemo.model.ColisEtat;
import com.example.filedemo.model.Hub;
import com.example.filedemo.model.PDFGenerator3;
import com.example.filedemo.model.Personnel;
import com.example.filedemo.model.Runsheet;
import com.example.filedemo.model.RunsheetEtat;
import com.example.filedemo.model.User;
import com.example.filedemo.payload.RunsheetPayload;
import com.example.filedemo.payload.RunsheetResponsePayload;
import com.example.filedemo.repository.ColisRepository;
import com.example.filedemo.repository.RunsheetRepository;
import com.example.filedemo.utility.Utility;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

@Service
public class RunsheetServiceImpl implements RunsheetService {
	@Value("${barCodeDirectory.path}")
	private String barCodeDirectory;
	@Value("${pdf.direcotry.path}")
	private String pdfDirectory;
	@Autowired
	RunsheetRepository runsheetRepository;

	@Autowired
	private ColisService colisService;

	@Autowired
	private ColisRepository colisRepository;

	@Autowired
	private PersonnelService personnelService;
	@Autowired
	Iuserservice userService;
	@Autowired
	SocietePrincipalService societePrincipalService;
	@Override
	public List<Runsheet> retrieveAllRunsheets() {
		return (List<Runsheet>) runsheetRepository.findAll();
	}

	@Transactional
	@Override
	public Runsheet addRunsheet(RunsheetPayload runsheetPayload) {
		Personnel livreur = personnelService.retrievePersonnel(runsheetPayload.getLivreurId());
		User creator = userService.loadUserById(runsheetPayload.getCreatorId());
		Runsheet runsheet = new Runsheet();
		runsheet.setEtat(RunsheetEtat.nonCloture);
		runsheet.setLivreur(livreur);
		runsheet.setCreator(creator);
		List<Colis> colis = colisService.findColisByBarCodesList(runsheetPayload.getColisBarCodes());
		runsheet.setTotalPrix((float) colis.stream().mapToDouble(c -> c.getCod()).sum());
		runsheet = runsheetRepository.save(runsheet);
		runsheet.setColis(updateEtatColis(colis, runsheet));
		runsheet.setBarCode(generateRunsheetBarCode(runsheet));
		return runsheetRepository.save(runsheet);
	}

	@Override
	public void deleteRunsheet(Long id) {
		List<Colis> listColis = colisService.findColisByRunsheet_code(id);
		listColis.forEach(c -> {
			c.setRunsheet(null);
			c.setEtat(ColisEtat.enStock);
			colisRepository.save(c);
		});
		runsheetRepository.deleteById(id);
	}

	@Override
	public Runsheet updateRunsheet(RunsheetPayload runsheetPayload) {
		Runsheet runsheet = runsheetRepository.findById(runsheetPayload.getId()).get();
		if (runsheetPayload.getLivreurId() != null) {
			Personnel livreur = personnelService.retrievePersonnel(runsheetPayload.getLivreurId());
			runsheet.setLivreur(livreur);
		}
		float total = colisService.totalCodPerRunsheet(runsheetPayload.getId());
		if (total > 0) {
			runsheet.setTotalPrix(total);
		} else {
			runsheet.setTotalPrix(0);
		}
		runsheet.setBarCode(generateRunsheetBarCode(runsheet));
		return runsheetRepository.save(runsheet);
	}

	@Override
	public Runsheet findByBarCode(String barCode) {
		return runsheetRepository.findByBarCode(barCode).orElse(null);
	}

	@Override
	public Runsheet findById(Long codeRunsheet) {
		return runsheetRepository.findById(codeRunsheet).orElse(null);
	}

	@Override
	public Runsheet addColisToRunsheet(List<String> barCodeList, Long idRunsheet) {
		Runsheet runsheet = findById(idRunsheet);
		float totalRunsheet = 0;
		for (String barCode : barCodeList) {
			Colis colis = colisService.findColisByBarCode(barCode);
			totalRunsheet += colis.getCod();
			colis.setRunsheet(runsheet);
			colis.setEtat(ColisEtat.enCoursDeLivraison);
			colisRepository.save(colis);
		}
		runsheet.setTotalPrix(totalRunsheet + runsheet.getTotalPrix());
		return runsheetRepository.save(runsheet);
	}

	@Override
	public String generateRunsheetBarCode(Runsheet runsheet) {
		String text = runsheet.createRunsheetBarCode(extractRunsheetHub(runsheet).getId_hub());
		Utility.createDirectoryIfNotExist(Utility.directoryPath(barCodeDirectory));
		String path = Utility.directoryPath(barCodeDirectory) + File.separator + text + ".jpg";
		try {
			Code128Writer writer = new Code128Writer();
			BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, 400, 90);
			MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(path));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return text;
	}

	private List<Colis> updateEtatColis(List<Colis> colis, Runsheet runsheet) {
		colis.stream().forEach(c -> {
			if (!c.getEtat().equals(ColisEtat.planificationRetour) &&
					!c.getEtat().equals(ColisEtat.planificationRetourEchange)) {
				c.setEtat(ColisEtat.enCoursDeLivraison);
			}
			c.setRunsheet(runsheet);
			c = colisRepository.save(c);
		});
		return colis;
	}

	@Override
	public Runsheet encloseRunsheet(Long id) {
		Runsheet runsheet = findById(id);
		runsheet.setEtat(RunsheetEtat.cloture);
		/*
		 * runsheet.getColis().stream().forEach(c->{ c.setRunsheet(runsheet);
		 * c.setEtat(ColisEtat.livre); c = colisRepository.save(c); });
		 */
		return runsheetRepository.save(runsheet);
	}

	@Override
	public List<RunsheetResponsePayload> retrieveAllRunsheetsNCltrByLivreurId(Long id) {
		List<RunsheetResponsePayload> responsePayloads = new ArrayList<>();
		runsheetRepository.retrieveAllRunsheetsNCltrByLivreurId(id).stream().forEach(r -> {
			RunsheetResponsePayload payload = new RunsheetResponsePayload();
			payload.setId(r.getId());
			payload.setBarCode(r.getBarCode());
			payload.setCreationDate(r.getCreationDate());
			responsePayloads.add(payload);
		});
		return responsePayloads;
	}

	private Hub extractRunsheetHub(Runsheet runsheet) {
		for (Colis c : runsheet.getColis()) {
			if (c.getHub() != null) {
				return c.getHub();
			}
		}
		return null;
	}

	@Override
	public String getBarCodeColisDirectory() {
		return Utility.directoryPath(this.barCodeDirectory);
	}

	@Override
	public ByteArrayInputStream generatePDF(Long id) {
		Runsheet runsheet = findById(id);
		ByteArrayInputStream bis=null;
		try {
			if(runsheet.getPdfFileName()!=null) {
				bis= new ByteArrayInputStream(Files.readAllBytes(Paths.get(Utility.directoryPath(this.pdfDirectory)+File.separator+runsheet.getPdfFileName())));
			}
			else {
			bis = PDFGenerator3.runsheetPDFReport(runsheet, Utility.directoryPath(this.barCodeDirectory),societePrincipalService.retrieveConfigSocietePrincipal(), colisService.getImagesDirectory() );
			String pdfFileName="runsheetPDF"+runsheet.getId()+".pdf";
			runsheet.setPdfFileName(pdfFileName);;
			Utility.createDirectoryIfNotExist(Utility.directoryPath(this.pdfDirectory));
			File file = new File(Utility.directoryPath(this.pdfDirectory)+File.separator+pdfFileName);
		    FileUtils.copyInputStreamToFile(bis, file);
		    runsheetRepository.save(runsheet);
		    bis= new ByteArrayInputStream(Files.readAllBytes(Paths.get(Utility.directoryPath(this.pdfDirectory)+File.separator+runsheet.getPdfFileName())));
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return bis;
	}

}
