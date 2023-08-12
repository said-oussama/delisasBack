package com.example.filedemo.model;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.format.DateTimeFormatter;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Colis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reference;
	private String bar_code;
	private String nom_c;
	private String prenom_c;
	private int tel_c_1;
	private int tel_c_2;
	private LocalDateTime date_creation = LocalDateTime.now();

	public String getFormattedDate_Creation() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		return DateUtils.formatDateTime(date_creation, "yyyy/MM/dd");
	}


	private String adresse;
	private String gouvernorat;
	private String delegation;
	private String localite;
	private float cod;
	private float latitude;
	private float longitude;
	private ColisModePaiement mode_paiement;
	private ColisService service;
	private String designation;
	private String remarque;
	private int nbrt;
	@Audited
	private ColisEtat etat;
	@ManyToOne
	private Anomalie anomalie;
	private Integer nb_p = 0;
	private Integer longeur = 0;
	private Integer largeur = 0;
	private Integer hauteur = 0;
	private Integer poids = 0;
	private String barCodeAncienColis;
	@Transient
	private Long ancienColisId;
	@Temporal(TemporalType.DATE)
	private Date dateLivraison;
	@ManyToOne
	public Fournisseur fournisseur;
	private boolean isDebriefed = false;
	@ManyToOne
	public Hub hub;
	@ManyToOne
	public Runsheet runsheet;
	@JsonIgnore
	@ManyToOne
	public Console console;
	@JsonIgnore
	@ManyToOne
	public Debrief debrief;
	@JsonIgnore
	@ManyToOne
	private Dispatch dispatch;
	private ColisTaille taille;
	private boolean isFragile;
	private String anomalieDescription;

	public String createDataToBarQrCodes() {
		int serviceCode = 0;
		serviceCode = this.service.equals(ColisService.livraison) ? 1 : 2;
		return "010" + serviceCode + (this.hub != null ? this.hub.getId_hub().toString() : "")
				+ (this.fournisseur != null ? this.fournisseur.getIduser() + "" : "")
				+ (this.ancienColisId != null ? this.ancienColisId.toString() : "") + this.reference;

	}

	public String getFormattedDateCreation() {
		String formattedDate = date_creation.getDayOfMonth() + "-" + date_creation.getMonthValue() + "-"
				+ date_creation.getYear();
		return formattedDate;
	}
	public static class DateUtils {
		public static String formatDateTime(LocalDateTime dateTime, String pattern) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			return dateTime.format(formatter);
		}
	}

}