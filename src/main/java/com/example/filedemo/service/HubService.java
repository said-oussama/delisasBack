package com.example.filedemo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.filedemo.model.Hub;
import com.example.filedemo.model.Personnel;
import com.example.filedemo.payload.HubPayload;
import com.example.filedemo.repository.HubRepository;
import com.example.filedemo.repository.PersonnelRepository;



@Service
public class HubService {

	@Autowired
    private HubRepository hubrepository ;
	@Autowired
    private PersonnelRepository personnelRepository;
	public List<Hub> retrieveAllHubs() {
        return (List<Hub>) hubrepository.findAll();
    }
	
    public Hub addHub(Hub hub) {
    	ArrayList<Hub> hubs = (ArrayList<Hub>) hubrepository.retrieveHubsHavingGovernoratLie(hub.getGouvernorat());
    	if(hubs.size()!=0) {
    		hubs.stream().forEach(h->{
    			h.getGouvernorat_lie().remove(hub.getGouvernorat());
    			hubrepository.save(h);
    		});
    	}
    	return (hubrepository.save(hub));	
    }

    @Transactional
    public List<String> deleteHub(Long id) {
    	Hub hub = hubrepository.findById(id).get();
    	if(hub.getColis().size()==0) {
    		hub.getPersonnel().stream().forEach(personnel->{
    			personnel.setHub(null);
    			personnelRepository.save(personnel);
    		});
        	hubrepository.deleteById(id);
        	return new ArrayList<>();
    	}
    	return hub.getColis().stream().
    			map(c->c.getBar_code()).collect(Collectors.toList());
    }


    public Hub updateHub(Hub p) {
        return (hubrepository.save(p));
    }


    public Hub retrieveHub(Long id) {
        return hubrepository.findById(id).orElse(null);
    }
    
	public Hub updateGovernoratsLiesHub(HubPayload hubPayload) {
		Hub hub= hubrepository.findById(hubPayload.getIdHub()).get();
		hub.setGouvernorat_lie(hubPayload.getGovernoratsLies());
		hub = hubrepository.save(hub);
		return hub;
	}
	public Hub checkHub(String titre) {
    	return hubrepository.findByTitre(titre).orElse(null);
	}
}
