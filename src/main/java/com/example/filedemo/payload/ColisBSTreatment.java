package com.example.filedemo.payload;

import java.util.List;

import com.example.filedemo.model.ColisEtat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColisBSTreatment {
  private List<String> barcodes;
  private ColisEtat nextStatus;
}
