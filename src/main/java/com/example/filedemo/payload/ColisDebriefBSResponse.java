package com.example.filedemo.payload;

import com.example.filedemo.model.Colis;
import com.example.filedemo.model.ColisEtat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColisDebriefBSResponse {
  private ColisEtat startingStatus;
  private ColisEtat arrivalStatus;
  private Colis colis;
}
