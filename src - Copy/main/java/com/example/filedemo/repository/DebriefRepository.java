package com.example.filedemo.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.filedemo.model.Debrief;

@Repository
public interface DebriefRepository extends CrudRepository<Debrief, Long> {
   @Query("SELECT d from Debrief d WHERE d.livreur.iduser= :idLivreur")
   public List<Debrief> getDebriefByIdLivreur(@Param("idLivreur") Long idLivreur);
   
   @Query("SELECT d from Debrief d WHERE d.validator.iduser= :idValidator")
   public List<Debrief> getDebriefByIdValidator(@Param("idValidator") Long idValidator);
   
   @Query("SELECT d from Debrief d WHERE d.livreur.iduser= :idLivreur AND d.creationDate= :creationDate")
   public Optional<Debrief> checkDebriefEligibility(@Param("idLivreur") Long idLivreur, @Param("creationDate")@Temporal(TemporalType.DATE) Date creationDate);
   @Query("SELECT count(d) from Debrief d WHERE d.etat=1")
   public int getNbrDebriefCloture();
   @Query("SELECT count(d) from Debrief d WHERE d.etat=0")
   public int getNbrDebriefNonCloture();
   @Query(value="SELECT count(*) from colis c Left join debrief d on c.debrief_id=d.id WHERE d.id is not null and c.etat='livre'",nativeQuery = true )
   public int getNbrColisDebriefLivre();
   @Query(value ="SELECT count(*) from colis c Left join debrief d on c.debrief_id=d.id WHERE d.id is not null", nativeQuery = true)
   public int getTotalColisDebrief();
}
