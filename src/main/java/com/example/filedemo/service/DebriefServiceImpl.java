package com.example.filedemo.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.filedemo.model.Colis;
import com.example.filedemo.model.ColisEtat;
import com.example.filedemo.model.Debrief;
import com.example.filedemo.model.DebriefEtat;
import com.example.filedemo.model.Personnel;
import com.example.filedemo.model.User;
import com.example.filedemo.payload.DebriefPayload;
import com.example.filedemo.payload.DebriefStatisticsResponse;
import com.example.filedemo.repository.ColisRepository;
import com.example.filedemo.repository.DebriefRepository;

@Service
public class DebriefServiceImpl implements DebriefService {
	@Autowired
	DebriefRepository debriefRepository;
	@Autowired
	ColisService colisService;
	@Autowired
	ColisRepository colisRepository;
	@Autowired
	AnomalieService anomalieService;
	@Autowired
	PersonnelService personnelService;
	@Autowired
	RunsheetService runsheetService;
	@Autowired
	Iuserservice userService;

	@Override
	public List<Debrief> getAllDebrief() {
		return (List<Debrief>) debriefRepository.findAll();
	}

	@Override
	public List<Debrief> getAllDebriefByIdValidator(Long idValidator) {
		return debriefRepository.getDebriefByIdValidator(idValidator);
	}

	@Override
	public List<Debrief> getAllDebriefByIdLivreur(Long idLivreur) {
		return debriefRepository.getDebriefByIdLivreur(idLivreur);
	}

	@Transactional
	@Override
	public Debrief createDebrief(DebriefPayload debriefPayload) {
		Debrief debrief = new Debrief();
		debrief.setEtat(debriefPayload.isToBeEnclosed() ? DebriefEtat.cloture : DebriefEtat.nonCloture);
		Personnel livreur = personnelService.retrievePersonnel(debriefPayload.getIdLivreur());
		User validator = userService.loadUserById(debriefPayload.getIdValidator());
		debrief.setLivreur(livreur);
		debrief.setValidator(validator);
		debrief.setCreationDate(new Date());
		debrief = debriefRepository.save(debrief);
		List<Colis> livreurColis = colisService.findColisByIdLivreur(debriefPayload.getIdLivreur(),
				debriefPayload.getIdRunsheet());
		debrief.setTotalCODColisLivre((float) livreurColis.stream().filter(c -> c.getEtat().equals(ColisEtat.livre))
				.mapToDouble(c -> c.getCod()).sum());
		debrief.setColis(assignDebriefToColis(livreurColis, debrief));
		if (debriefPayload.isToBeEnclosed()) {
			unDebriefColis(debrief.getColis());
			encloseRunsheets(debriefPayload.getIdRunsheet());
		}
		return debriefRepository.save(debrief);
	}

	@Override
	public Boolean checkDebriefEligibility(Long idLivreur) {
		Debrief debrief = debriefRepository.checkDebriefEligibility(idLivreur, new Date()).orElse(null);
		return debrief == null ? Boolean.TRUE : Boolean.FALSE;
	}

	private List<Colis> assignDebriefToColis(List<Colis> colis, Debrief debrief) {
		colis.stream().forEach(c -> {
			c.setDebrief(debrief);
			colisRepository.save(c);
		});
		return colis;
	}

	private void encloseRunsheets(Long idRunsheet) {
		/*
		 * livreurDebriefColis.stream().forEach(c -> { if (c.getRunsheet() != null) {
		 * runsheetService.encloseRunsheet(c.getRunsheet().getId()); } });
		 */
		if (idRunsheet != null)
			runsheetService.encloseRunsheet(idRunsheet);
	}

	@Override
	public Debrief updateDebrief(DebriefPayload debriefPayload) {
		Debrief debrief = debriefRepository.findById(debriefPayload.getId()).get();
		debrief.setEtat(debriefPayload.isToBeEnclosed() ? DebriefEtat.cloture : DebriefEtat.nonCloture);
		if (debriefPayload.getIdValidator() != null) {
			User validator = userService.loadUserById(debriefPayload.getIdValidator());
			debrief.setValidator(validator);
		}
		debrief.setTotalCODColisLivre((float) debrief.getColis().stream()
				.filter(c -> c.getEtat().equals(ColisEtat.livre)).mapToDouble(c -> c.getCod()).sum());
		if (debriefPayload.isToBeEnclosed()) {
			unDebriefColis(debrief.getColis());
			encloseRunsheets(debriefPayload.getIdRunsheet());
		}
		return debriefRepository.save(debrief);
	}

	@Override
	public Debrief findDebriefById(Long id) {
		return debriefRepository.findById(id).orElse(null);
	}

	private void unDebriefColis(List<Colis> colis) {
		colis.stream().forEach(c -> {
			c.setDebriefed(false);
			colisRepository.save(c);
		});
	}

	@Override
	public DebriefStatisticsResponse getDebriefStatistics() {
		return DebriefStatisticsResponse.builder().nbrDebriefCloture(debriefRepository.getNbrDebriefCloture())
				.nbrDebriefNonCloture(debriefRepository.getNbrDebriefNonCloture())
				.nbrColisLivre(debriefRepository.getNbrColisDebriefLivre())
				.totalColisDebrief(debriefRepository.getTotalColisDebrief()).build();
	}

}
