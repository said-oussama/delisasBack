package com.example.filedemo.payload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColisForceModificationsPayload {
   private Long refrence;
   private Long anomalieId;
   private String newColisEtat;
   private String anomalieDescription;
}
