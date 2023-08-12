package com.example.filedemo.model;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy=InheritanceType.JOINED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iduser;
	@Column(unique = true)
	private String username;
	private String Tel;
	@Email
	private String email;
	private String image;
	private String password;
	private Roles roleUser;
	public boolean isDeleted=false;
	@JsonIgnore
	@OneToMany(mappedBy = "creator")
	private List<Console> assignedConsoles;
	@JsonIgnore
	@OneToMany(mappedBy = "validator")
	private List<Console> validatedConsoles;
	@JsonIgnore
	@OneToMany(mappedBy = "dispatcher")
	private List<Dispatch> assignedDispatchs;
	@JsonIgnore
	@OneToMany(mappedBy = "validator")
	private List<Debrief> validatedDebrief;
}
