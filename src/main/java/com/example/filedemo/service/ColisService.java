package com.example.filedemo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.filedemo.model.Anomalie;
import com.example.filedemo.model.AnomalieType;
import com.example.filedemo.model.Colis;
import com.example.filedemo.model.ColisEtat;
import com.example.filedemo.model.Console;
import com.example.filedemo.model.Dispatch;
import com.example.filedemo.model.Fournisseur;
import com.example.filedemo.model.Hub;
import com.example.filedemo.payload.ColisAssignAnomaliePayload;
import com.example.filedemo.payload.ColisBSTreatment;
import com.example.filedemo.payload.ColisDebriefBSResponse;
import com.example.filedemo.payload.ColisForceModificationsPayload;
import com.example.filedemo.payload.ColisHubsResponsePayload;
import com.example.filedemo.repository.ColisRepository;
import com.example.filedemo.repository.ColisRepository.HistoStateOnly;
import com.example.filedemo.repository.FournisseurRepository;
import com.example.filedemo.repository.HubRepository;
import com.example.filedemo.utility.Utility;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class ColisService {
	@Value("${barCodeDirectory.path}")
	private String barCodeDirectory;
    @Value("${qrCodeDirectory.path}")
	private String qrCodeDirectory;
    @Value("${images.direcotry.path}")
	private String imagesDirectory;

	private List<Colis> colisList = new ArrayList<>();


	@Autowired
	private FournisseurService fournisseurService;

	@Autowired
	private FournisseurRepository fournisseurRepository;

	@Autowired
	private HubService hubService;
	@Autowired
	private HubRepository hubRepository;
	@Autowired
	private final ColisRepository colisRepository;
	@Autowired
	private AnomalieService anomalieService;

	@Autowired
	public ColisService(ColisRepository colisRepository) {
		this.colisRepository = colisRepository;
	}

	// save Colis
	@Transactional
	public Colis saveColis(Colis colis) {
		colis.setEtat(ColisEtat.cree);
		if(colis.getNb_p()==null || colis.getNb_p()==0) {
			colis.setNb_p(1);
		}
		if(colis.getLargeur()==null || colis.getLargeur()==0) {
			colis.setLargeur(1);
		}
		if(colis.getLongeur()==null || colis.getLongeur()==0) {
			colis.setLongeur(1);
		}
		if(colis.getHauteur()==null || colis.getHauteur()==0) {
			colis.setHauteur(1);
		}
		Hub hub = getRelatedHub(colis.getGouvernorat());
		if (hub != null) {
			colis.setHub(hub);
		}
		if(colis.getFournisseur()!=null && new Long(colis.getFournisseur().getIduser())!=null) {
			colis.setFournisseur(fournisseurService.findById(colis.getFournisseur().getIduser()).get());
		}
		if (colis.getService().equals(com.example.filedemo.model.ColisService.echange)
				&& colis.getBarCodeAncienColis() != null) {
			if (getAncienColis(colis.getBarCodeAncienColis()) != null) {
				Colis ancienColis = getAncienColis(colis.getBarCodeAncienColis());
				colis.setAncienColisId(ancienColis.getReference());
				ancienColis.setEtat(ColisEtat.retourEchange);
				colisRepository.save(ancienColis);
			}
			else {
				throw new RuntimeException("No Colis found for barCodeAncienColis");
			}
		}
		Date dateLivraison = null;
		if (colis.getDateLivraison() == null) {
			try {
				dateLivraison = new SimpleDateFormat("yyyy-MM-dd")
						.parse(colis.getDate_creation().plusDays(2).toLocalDate().toString());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		} else {
			dateLivraison = colis.getDateLivraison();
		}
		dateLivraison.setHours(12);
		colis.setDateLivraison(dateLivraison);
		Colis colisResult = colisRepository.save(colis);
		colisResult.setBar_code(colisResult.createDataToBarQrCodes());
		colisRepository.save(colisResult);
		generateColisBarCodeAndQrCode(colisResult.getReference());
		return colisResult;
	}

	// update Colis
	public Colis updateColis(Colis colis) {
		colis.setFournisseur(fournisseurRepository.findById(colis.fournisseur.getIduser()).orElse(new Fournisseur()));
		if (colis.getHub() != null && colis.getHub().getId_hub() != null) {
			colis.setHub(hubRepository.findById(colis.getHub().getId_hub()).orElse(null));
		}
		colis.setBar_code(colis.createDataToBarQrCodes());
		colis = colisRepository.save(colis);
		generateColisBarCodeAndQrCode(colis.getReference());
		return colis;
	}

	// lister les colis
	public List<Colis> findAllColisByFournisseur(Long id) {
		return colisRepository.findByFournisseur_id(id);
	}

	// liste des audits

	public List<HistoStateOnly> getColisAud(String barcode) {
		Colis colis = findColisByBarCode(barcode);
		return colisRepository.getColisAud(colis.getReference());

	}

	// supprimer un colis
	public void deleteColis(Long reference) {
		colisRepository.deleteById(reference);
	}

	// findColisCreé
	public List<Colis> findColisByEtat(ColisEtat etat) {
		List<Colis> list = colisRepository.findColisByEtat(etat);
		return list;
	}

	public List<Colis> findAllColis() {
		return colisRepository.findAll();
	}

	public List<Colis> findByObjectList(List<Long> inputList) {
		List<Colis> List1 = colisRepository.findByObjectList(inputList);
		return List1;
	}

	public Colis findColisByBarCode(String bar_code) {
		Colis List1 = colisRepository.findColisByBarCode(bar_code);
		return List1;
	}

	// findColis by id
	public Optional<Colis> findById(Long referene)

	{
		return colisRepository.findById(referene);
	}

	public List<Colis> findColisByRunsheet_code(Long runsheet_code)

	{
		return colisRepository.findColisByRunsheet_code(runsheet_code);
	}

	// findColis by id and by etat
	public List<Colis> findByFournisseurAndEtat(ColisEtat etat, Long referene)

	{
		return colisRepository.findByFournisseurAndEtat(etat, referene);
	}

	// findColis by id and by service
	public List<Colis> findByFournisseurAndService(com.example.filedemo.model.ColisService service, Long referene)

	{
		return colisRepository.findByFournisseurAndService(service, referene);
	}

	// findColis by service
	public List<Colis> findByService(com.example.filedemo.model.ColisService service)

	{
		return colisRepository.findAllColisByService(service);
	}

	public String generateColisBarCodeAndQrCode(Long reference) {
		Colis colisBarCode = findById(reference).orElse(new Colis());
		String text = colisBarCode.createDataToBarQrCodes();
		Utility.createDirectoryIfNotExist(Utility.directoryPath(barCodeDirectory));
		Utility.createDirectoryIfNotExist(Utility.directoryPath(qrCodeDirectory));
		String pathBarCode = Utility.directoryPath(barCodeDirectory) + File.separator + text + ".jpg";
		String pathQRCode = Utility.directoryPath(qrCodeDirectory) + File.separator + text + ".jpg";
		try {
			//BarCode generation
			Code128Writer writer = new Code128Writer();
			BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, 400, 90);
			MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(pathBarCode));
			//QrCode generation
			BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 400, 400);
			FileOutputStream fileOutputStream = new FileOutputStream(new File(pathQRCode));
	        MatrixToImageWriter.writeToStream(bitMatrix, "jpg", fileOutputStream);
	        fileOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return text;
	}

	public int countByEtat(Long id, ColisEtat etat) {

		return colisRepository.countByEtat(id, etat);

	}

	public void removeColisFromRunsheet(Long reference) {

		Colis colis = findById(reference).get();
		colis.setRunsheet(null);
		updateColis(colis);
	}

	public Float totalCodPerRunsheet(Long id) {

		return colisRepository.totalCodPerRunsheet(id);
	}

	public String getBarCodeColisDirectory() {
		return Utility.directoryPath(this.barCodeDirectory);
	}
	public String getImagesDirectory() {
		return Utility.directoryPath(this.imagesDirectory);
	}

	private Hub getRelatedHub(String colisGovernorat) {
		if (colisGovernorat != null) {
			return hubService.retrieveAllHubs().stream().filter(
					h -> h.getGouvernorat().equals(colisGovernorat) || h.getGouvernorat_lie().contains(colisGovernorat))
					.findFirst().orElse(null);
		}
		return null;
	}

	private Colis getAncienColis(String barCode) {
		return colisRepository.findColisByBarCode(barCode);
	}

	public List<Colis> assignColisToDispatch(Dispatch dispatch, List<Long> colisReferences) {
		List<Colis> colis = findByObjectList(colisReferences);
		colis.stream().forEach(c -> {
			c.setDispatch(dispatch);
			c.setEtat(ColisEtat.enAttenteDEnlevement);
			colisRepository.save(c);
		});
		return colis;
	}

	public Colis assignColisToHub(Long refColis, Long idHub) {
		Colis colis = colisRepository.findById(refColis).get();
		Hub hub = hubRepository.findById(idHub).get();
		colis.setHub(hub);
		colis.setBar_code(colis.createDataToBarQrCodes());
		colis = colisRepository.save(colis);
		generateColisBarCodeAndQrCode(colis.getReference());
		return colis;
	}
	@Transactional
	public void assignColisToHub(List<String> colisBarcodes, Long idHub) {
		List<Colis> colis = colisRepository.findColisByBarCodesList(colisBarcodes);
		Hub hub = hubRepository.findById(idHub).get();
		colis.stream().forEach(c->{
			c.setHub(hub);
			c.setBar_code(c.createDataToBarQrCodes());
			c.setGouvernorat(hub.getGouvernorat());
			generateColisBarCodeAndQrCode(c.getReference());
		});
		hub.setColis(colis);
		hubRepository.save(hub);
	}

	public List<Colis> assignColisToConsole(Console console, List<Long> colisReferences) {
		List<Colis> colis = findByObjectList(colisReferences);
		colis.stream().forEach(c -> {
			c.setConsole(console);
			colisRepository.save(c);
		});
		return colis;
	}

	public ColisHubsResponsePayload findColisHubDepartHubArrivee(String colisBarcode) {
		ColisHubsResponsePayload colisHubsResponsePayload = new ColisHubsResponsePayload();
		Colis colis = findColisByBarCode(colisBarcode);
		if (colis == null)
			return ColisHubsResponsePayload.builder().error("404").build();
		if (colis != null && !colis.getEtat().equals(ColisEtat.enAttenteDEnlevement))
			return ColisHubsResponsePayload.builder().error("406").build();
		Long idHubArrivee = 0l;
		if (colis.getHub() != null) {
			idHubArrivee = colis.getHub().getId_hub();
			colisHubsResponsePayload.setHubArriveeId(idHubArrivee);
			colisHubsResponsePayload.setHubArriveeGovernorat(colis.getHub().getGouvernorat());
			colisHubsResponsePayload.setHubArriveeTitre(colis.getHub().getTitre());
		}
		Hub hubService = fournisseurService.getRelatedHub(colis.getFournisseur().getGouvernorat_societe());
		Long idHubDepart = 0l;
		if (hubService != null) {
			idHubDepart = hubService.getId_hub();
			colisHubsResponsePayload.setHubDepartId(idHubDepart);
			colisHubsResponsePayload.setHubDepartGovernorat(hubService.getGouvernorat());
			colisHubsResponsePayload.setHubDepartTitre(hubService.getTitre());
			colisHubsResponsePayload.setColis(colis);
		}
		if (idHubArrivee == 0 || idHubDepart == 0)
			return colisHubsResponsePayload;
		return colisHubsResponsePayload;
	}
	public List<Colis> findColisByBarCodesList(List<String> barCodesList) {
		return colisRepository.findColisByBarCodesList(barCodesList);
	}
	public List<Colis> findColisByIdLivreur(Long idLivreur, Long idSelectedRunsheet){
		List<Colis> colis = new ArrayList<>();
		colis.addAll(colisRepository.findColisDispatchByIdLivreur(idLivreur));
		List<Colis> colisRunsheet= new ArrayList<>();
		if (idSelectedRunsheet!=null && idSelectedRunsheet!=0) {
			colisRunsheet=colisRepository.findColisSelectedRunsheet(idSelectedRunsheet);
			colis.addAll(colisRunsheet);
		}
		colisRunsheet.stream().forEach(c->{
			if(c.getService().equals(com.example.filedemo.model.ColisService.echange)&&
					c.getBarCodeAncienColis()!=null) {
				colis.add(findColisByBarCode(c.getBarCodeAncienColis()));
			}
		});
		return colis;
	}
	public Colis findColisEchange(String barCode) {
		return colisRepository.findColisEchange(barCode).orElse(null);
	}
	@Transactional
	public List<ColisDebriefBSResponse> treatColisDebriefBS(ColisBSTreatment colisBSTreatment) {
		List<ColisDebriefBSResponse> colisDebriefBSResponses =new ArrayList<>();
		colisBSTreatment.getBarcodes().stream().forEach(barcode->{
			ColisDebriefBSResponse colisDebriefBSResponse = new ColisDebriefBSResponse();
			Colis colis = findColisByBarCode(barcode);
		    if(findNewColisEchange(colis.getBar_code()) != null) {
				colisDebriefBSResponse.setStartingStatus(colis.getEtat());
				colis.setEtat(ColisEtat.retourEchange);
				colisDebriefBSResponse.setArrivalStatus(colis.getEtat());
			}
			else if(colis.getEtat().equals(ColisEtat.enAttenteDEnlevement)) {
				colisDebriefBSResponse.setStartingStatus(ColisEtat.enAttenteDEnlevement);
				if(colisBSTreatment.getNextStatus()!=null) {
					colis.setEtat(colisBSTreatment.getNextStatus());
					colisDebriefBSResponse.setArrivalStatus(colisBSTreatment.getNextStatus());
				}
				else {
					colis.setAnomalie(anomalieService.getAnomalieByType(AnomalieType.enlevement));
					colisDebriefBSResponse.setArrivalStatus(ColisEtat.planificationRetour);
				}
			}
			else if(colis.getEtat().equals(ColisEtat.enCoursDeLivraison)) {
				colisDebriefBSResponse.setStartingStatus(ColisEtat.enCoursDeLivraison);
				if(colisBSTreatment.getNextStatus()!=null) {
					if(colis.getService().equals(com.example.filedemo.model.ColisService.echange)
							&& colis.getBarCodeAncienColis()!=null) {
						Colis ancienColis= getAncienColis(colis.getBarCodeAncienColis());
						ancienColis.setEtat(ColisEtat.planificationRetourEchange);
						colisRepository.save(ancienColis);
					}
					colis.setEtat(colisBSTreatment.getNextStatus());
					colisDebriefBSResponse.setArrivalStatus(colisBSTreatment.getNextStatus());
				}
			}
			else if(colis.getEtat().equals(ColisEtat.planificationRetour)) {
				colisDebriefBSResponse.setStartingStatus(ColisEtat.planificationRetour);
				if(colisBSTreatment.getNextStatus()!=null) {
					colis.setEtat(ColisEtat.retourne);
					colisDebriefBSResponse.setArrivalStatus(colis.getEtat());
				}
				else colisDebriefBSResponse.setArrivalStatus(ColisEtat.planificationRetour);

			}
		    colis.setDebriefed(true);
			colis = colisRepository.save(colis);
			colisDebriefBSResponse.setColis(colis);
			colisDebriefBSResponses.add(colisDebriefBSResponse);
		});
		return colisDebriefBSResponses;
	}
	@Transactional
	public List<ColisDebriefBSResponse> assignAnomalieToColisList(ColisAssignAnomaliePayload colisAssignAnomaliePayload, Long idAnomalie) {
		List<ColisDebriefBSResponse>colisDebriefBSResponses = new ArrayList<>();
		Anomalie anomalie = anomalieService.findAnomalie(idAnomalie);
		List<Colis> colis = colisRepository.findColisByBarCodesList(colisAssignAnomaliePayload.getBarCodes());
		colis.stream().forEach(c->{
			c.setNbrt(c.getNbrt()+1);
			c.setAnomalie(anomalie);
			c.setAnomalieDescription(colisAssignAnomaliePayload.getAnomalieDescription());
			ColisDebriefBSResponse colisDebriefBSResponse = new ColisDebriefBSResponse();
			if(c.getEtat().equals(ColisEtat.enCoursDeLivraison)) {
				colisDebriefBSResponse.setStartingStatus(ColisEtat.enCoursDeLivraison);
				if(c.getNbrt()>=anomalie.getNbrTentative()) {
					c.setEtat(ColisEtat.planificationRetour);
					colisDebriefBSResponse.setArrivalStatus(ColisEtat.planificationRetour);
				}
				else {
					c.setEtat(ColisEtat.enStock);
					colisDebriefBSResponse.setArrivalStatus(ColisEtat.enStock);
				}
			}
		    c.setDebriefed(true);
			c = colisRepository.save(c);
			colisDebriefBSResponse.setColis(c);
			colisDebriefBSResponses.add(colisDebriefBSResponse);
		});
		return colisDebriefBSResponses;
	}
	public Colis findNewColisEchange(String barCode) {
		return colisRepository.findNewColisEchange(barCode).orElse(null);
	}
	@Transactional
	public Colis forceModificationsColis(ColisForceModificationsPayload colisForceModificationsPayload) {
		Colis colis= colisRepository.findById(colisForceModificationsPayload.getRefrence()).get();
		if(colisForceModificationsPayload.getAnomalieId()!=null &&
				colisForceModificationsPayload.getAnomalieId()==0) {
			colis.setAnomalie(null);
			colis.setNbrt(0);
		}
		if(colisForceModificationsPayload.getAnomalieId()!=null &&
				colisForceModificationsPayload.getAnomalieId()!=0) {
			colis.setAnomalie(anomalieService.findAnomalie(colisForceModificationsPayload.getAnomalieId()));
			colis.setNbrt(colis.getNbrt()+1);
		}
		if(colisForceModificationsPayload.getNewColisEtat()!=null) {
			colis.setEtat(ColisEtat.valueOf(colisForceModificationsPayload.getNewColisEtat()));
		}
		if(colisForceModificationsPayload.getAnomalieDescription()!=null) {
			colis.setAnomalieDescription(colisForceModificationsPayload.getAnomalieDescription());
		}
		return colisRepository.save(colis);
	}

	@Transactional
	public List<Colis> forceModificationsColisList(List<ColisForceModificationsPayload> colisPayloadList) {
		List<Colis> modifiedColisList = new ArrayList<>();

		for (ColisForceModificationsPayload payload : colisPayloadList) {
			Colis modifiedColis = forceModificationsColis(payload);
			modifiedColisList.add(modifiedColis);
		}

		return modifiedColisList;
	}

	public List<Colis> getAllColis() {
		return colisList;
	}



	/*public void changerEtatEnleve(List<Long> colisIds) {
		for (Long colisId : colisIds) {
			Optional<Colis> optionalColis = colisRepository.findById(colisId);
			if (optionalColis.isPresent()) {
				Colis colis = optionalColis.get();
				colis.setEtat(ColisEtat.aenleve);
				colisRepository.save(colis);
			}
		}
	}*/
	public void changerEtatEnleve(List<Long> colisIds) {
		for (Long colisId : colisIds) {
			Optional<Colis> optionalColis = colisRepository.findById(colisId);
			System.out.println(colisId);
			if (optionalColis.isPresent()) {
				Colis colis = optionalColis.get();
				colis.setEtat(ColisEtat.aenleve);
				colisRepository.save(colis);
			} else {
				throw new IllegalArgumentException("Colis avec l'ID " + colisId + " n'a pas été trouvé dans la base de données.");
			}
		}
	}


	public List<Colis> findColisByHub(Long idHub) {
		return colisRepository.findColisByHub(idHub);
	}
}
