package com.example.filedemo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.example.filedemo.model.SocietePrincipal;
import com.example.filedemo.service.SocietePrincipalService;
import com.example.filedemo.utility.Utility;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class SocietePrincipalController {
    @Autowired
    SocietePrincipalService societePrincipalService;
    @Value("${images.direcotry.path}")
	private String imagesDirectory;
    
	@PostMapping(value = "/configureSocietePrincipal")
	public ResponseEntity configureSocietePrincipal(@RequestBody SocietePrincipal societePrincipal) {
		SocietePrincipal societePrincipalPostSave = null;
		try {
			societePrincipalPostSave=societePrincipalService.configureSocietePrincipal(societePrincipal);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(societePrincipalPostSave);
	}
	@GetMapping(value = "/initializeCompanyInfos")
	public ResponseEntity initializeCompanyInfos() {
		SocietePrincipal societePrincipal = null;
		try {
			societePrincipal=societePrincipalService.retrieveConfigSocietePrincipal();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(societePrincipal);
	}
	@PostMapping("/configureSocietePrincipalWithLogo")
	public ResponseEntity configureSocietePrincipalWithLogo(@RequestParam("logo") MultipartFile logoFile,
			@RequestParam("societePrincipal") String societePrincipal)
			throws JsonParseException, JsonMappingException, Exception {
		SocietePrincipal societePrincipalToBeUpdated =null;
		String logo =null;
		try {
			societePrincipalToBeUpdated = new ObjectMapper().readValue(societePrincipal, SocietePrincipal.class);
			boolean isExit = new File(Utility.directoryPath(imagesDirectory)).exists();
			if (!isExit) {
				new File(Utility.directoryPath(imagesDirectory)).mkdir();
			}
			logo = saveFile(logoFile);
			societePrincipalToBeUpdated.setLogo(logo);
			societePrincipalToBeUpdated= societePrincipalService.configureSocietePrincipal(societePrincipalToBeUpdated);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(societePrincipalToBeUpdated);
	}
	@GetMapping(path = "/logoSocietePrincipal")
	public ResponseEntity getLogo() throws Exception {
		SocietePrincipal societePrincipal = null;		
		try {
			societePrincipal = societePrincipalService.retrieveConfigSocietePrincipal();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
		if(societePrincipal==null || (societePrincipal!=null && societePrincipal.getLogo()==null)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(Files.readAllBytes(Paths.get(Utility.directoryPath(imagesDirectory)+
				File.separator +societePrincipal.getLogo())));
	}
	
	private String saveFile(MultipartFile file) throws IOException {
		String filename = file.getOriginalFilename();
		String newFileName = FilenameUtils.getBaseName(filename) +new Date().getTime()+ "." + FilenameUtils.getExtension(filename);
		File serverFile = new File(Utility.directoryPath(imagesDirectory)+ File.separator + newFileName);
		FileUtils.writeByteArrayToFile(serverFile, file.getBytes());
		return newFileName;
	}

}
