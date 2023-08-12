package com.example.filedemo.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.filedemo.model.Runsheet;
@Repository
public interface RunsheetRepository extends CrudRepository<Runsheet, Long>{
	Optional<Runsheet> findByBarCode(String barCode);
	@Query("SELECT r FROM Runsheet r WHERE r.livreur.iduser= :livreurId AND r.etat= 'nonCloture' ")
	List<Runsheet> retrieveAllRunsheetsNCltrByLivreurId(@Param("livreurId")Long livreurId);
}
