package com.example.filedemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.filedemo.model.SocietePrincipal;
import com.example.filedemo.repository.SocietePrincipalRepository;

@Service
public class SocietePrincipalServiceImpl implements SocietePrincipalService {
    @Autowired
    SocietePrincipalRepository societePrincipalRepository;
    
	@Override
	public SocietePrincipal configureSocietePrincipal(SocietePrincipal societePrincipal) {
		SocietePrincipal existingSP=null;
		if(retrieveConfigSocietePrincipal()!=null) {
			existingSP= retrieveConfigSocietePrincipal();
			existingSP.setAdresse(societePrincipal.getAdresse());
			existingSP.setEmail(societePrincipal.getEmail());
			existingSP.setLogo(societePrincipal.getLogo());
			existingSP.setMatriculeFiscale(societePrincipal.getMatriculeFiscale());
			existingSP.setNomComplet(societePrincipal.getNomComplet());
			existingSP.setSigle(societePrincipal.getSigle());
			existingSP.setTelephone(societePrincipal.getTelephone());
		}
		return societePrincipalRepository.save(existingSP!=null?existingSP:societePrincipal);
	}

	@Override
	public SocietePrincipal retrieveConfigSocietePrincipal() {
		return societePrincipalRepository.findFirstByOrderByIdAsc().orElse(null);
	}

}
