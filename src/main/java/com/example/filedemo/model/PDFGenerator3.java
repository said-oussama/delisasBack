
package com.example.filedemo.model;
import java.awt.Color;





import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.* ;
import com.itextpdf.text.Font.FontFamily;


@CrossOrigin("*")
public class PDFGenerator3 {
	private final static String defaultLogoPath="src"+File.separator+"main"+File.separator
    		+"resources"+File.separator+"static"+File.separator+"logo"+File.separator+"logo-default.png";

    private static Logger logger = LoggerFactory.getLogger(PDFGenerator3.class);
    public static ByteArrayInputStream runsheetPDFReport(Runsheet r, String barCodeColisDirectoryPath, 
    		SocietePrincipal societePrincipal, String imagesDirectory) throws MalformedURLException, IOException {
        Rectangle pageSize = new Rectangle(700, 1000) ;
        Document document = new Document(pageSize);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            PdfWriter.getInstance(document, out);
            document.open();
            List<ColisEtat> colisEtats=new ArrayList<>();
            if(r.getColis().stream().filter(cl->cl.getEtat().equals(ColisEtat.enCoursDeLivraison)).count()>0) {
            	colisEtats.add(ColisEtat.enCoursDeLivraison);
            }
            if(r.getColis().stream().filter(cl->cl.getEtat().equals(ColisEtat.planificationRetour)||cl.getEtat().equals(ColisEtat.planificationRetourEchange)).count()>0) {
            	colisEtats.add(ColisEtat.planificationRetour);
            }
            for(int j=0; j<colisEtats.size();j++) {
                  int indexEtat=j;
            	  Font font5 = FontFactory.getFont(FontFactory.HELVETICA ,16, BaseColor.BLACK );
                  Image image = Image.getInstance(barCodeColisDirectoryPath+File.separator+ r.getBarCode() + ".jpg");
                  image.setBorder(1);
                  Image logo = Image.getInstance(societePrincipal!=null && societePrincipal.getLogo()!=null?
                		  Paths.get(imagesDirectory+File.separator+societePrincipal.getLogo()).toString():defaultLogoPath);
                  logo.setBorder(1);
                  logo.setAlignment(5);
                  logo.setAbsolutePosition(22,833);
                  logo.scaleToFit(120,120);
                  image.setAbsolutePosition( 400,850);
                  image.scaleToFit(250,200);
                  document.add(logo) ;
                  document.add(new Phrase("\n"));
                  document.add(new Phrase("\n"));
                  document.add(new Phrase("\n"));
                  StringBuilder societeInfos=new StringBuilder();
                  if(societePrincipal!=null) {
                      societeInfos.append("\n Societé: "+societePrincipal.getNomComplet()+ " "+ societePrincipal.getSigle()+"\n");
                      societeInfos.append("Matricule Fiscale: "+societePrincipal.getMatriculeFiscale()+"\n");
                      societeInfos.append("Adresse: "+societePrincipal.getAdresse()+"\n");
                      societeInfos.append("Téléphone: "+societePrincipal.getTelephone()+"\n");

                  }
                  Paragraph societe = new Paragraph(societeInfos.toString(), FontFactory.getFont(FontFactory.HELVETICA ,10, BaseColor.BLACK ));
                  societe.setIndentationLeft(120f);    
                  document.add(societe);
                  image.setAlignment(50);
                  document.add(new Phrase("\n"));
                  //document.add(new Phrase("\n"));
                  //document.add(new Phrase("\n"));
                  document.add(image);
                  Phrase para111 = new Phrase (r.getBarCode()+"\n \n Date:"+ new SimpleDateFormat("dd/MM/yyyy").format(new Date()), FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));
                  PdfPCell cell9 = new PdfPCell(para111);
                  cell9.setHorizontalAlignment (Element.ALIGN_CENTER);
                  cell9.setBorder( Rectangle.BOX) ;
                  cell9.setBorderColor( new BaseColor(253, 254, 254));
                  cell9.setBorderWidth(1f);
                  PdfPTable table9 = new PdfPTable(1);
                  table9.addCell(cell9);
                  table9.setHorizontalAlignment (Element.ALIGN_RIGHT);
                  table9.setWidthPercentage(50f);
                  document.add(table9);
                  document.add(new Phrase("\n"));
                  document.add(new Phrase("\n"));
                  document.add(new Phrase("\n"));
                  String entetePDF="                                              ";
                  if(colisEtats.get(indexEtat).equals(ColisEtat.enCoursDeLivraison)) {
                	  entetePDF+="Bon de livraison N° : ";
                  }	
                  else entetePDF+="Bon de retour N° : ";
                  Phrase bonLivraison = new Phrase (entetePDF+r.getBarCode() , font5);
                  document .add(bonLivraison);
                  document.add(new Phrase("\n"));
                  document.add(new Phrase("\n"));
      			Phrase commitment = new Phrase(
      					"Je soussigné chauffeur chargé par "+ societePrincipal.getSigle()+" à livrer les colis ci-dessous et à encaisser les prix auprès des destination indiqués dans le présent planning, reconnais être responsable des montants encaissés et mengage à les restituer en fin de journée sans différence ni manquant au service financier.", FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));
      			PdfPCell cellComt = new PdfPCell(commitment);
      			cellComt.setHorizontalAlignment (Element.ALIGN_JUSTIFIED);
      			cellComt.setBorder( Rectangle.BOX) ;
      			cellComt.setBorderColor( new BaseColor(253, 254, 254));
      			cellComt.setBorderWidth(1f);
                  PdfPTable tableComt = new PdfPTable(1);
                  tableComt.addCell(cellComt);
                  tableComt.setHorizontalAlignment (Element.ALIGN_LEFT);
                  tableComt.setWidthPercentage(100f);
                  document.add(tableComt);
                  document.add(new Phrase("\n"));
                  PdfPTable livreurTbl = new PdfPTable(3);
                  livreurTbl.setTotalWidth(630f);
                  livreurTbl.setLockedWidth(true);
                  float[] columnsSize = new float[] {100f, 90f, 50f};
                  livreurTbl.setWidths(columnsSize);
                  Stream.of("" ,"", "").forEach(headerTitle -> {
                      PdfPCell header3 = new PdfPCell();
                      header3.setBackgroundColor(BaseColor.WHITE);
                      header3.setHorizontalAlignment(Element.ALIGN_CENTER);
                      header3.setBorderColor(BaseColor.WHITE);
                      livreurTbl.addCell(header3);
                  });
                  PdfPCell celLivr = new PdfPCell();
                  StringBuilder LivreurInfos=new StringBuilder();
                  LivreurInfos.append(
      					"Nom Complet: " + r.getLivreur().getNom()+" "+r.getLivreur().getPrenom() + "\n");
                  LivreurInfos.append("CIN: " + r.getLivreur().getCin() + "\n");
                  LivreurInfos.append("Matricule: " + r.getLivreur().getMatricule_veh() + "\n");
                  Phrase paraLivreur = new Phrase (LivreurInfos.toString(), FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));

                  celLivr.addElement(paraLivreur);
                  celLivr.setPaddingLeft(4);
                  celLivr.setHorizontalAlignment(Element.ALIGN_CENTER);
                  celLivr.setBorderColor(BaseColor.WHITE);
                  PdfPCell celCachet = new PdfPCell();
                  Phrase paraCachet = new Phrase ("Cachet société", FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));
                  celCachet.addElement(paraCachet);
                  celCachet.setPaddingLeft(4);
                  celCachet.setVerticalAlignment(Element.ALIGN_TOP);
                  celCachet.setHorizontalAlignment(Element.ALIGN_CENTER);
                  celCachet.setBorderColor(BaseColor.WHITE);
                  PdfPCell celSignature = new PdfPCell();
                  Phrase paraSignature = new Phrase ("Signature livreur", FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));
                  celSignature.addElement(paraSignature);
                  celSignature.setPaddingLeft(4);
                  celSignature.setVerticalAlignment(Element.ALIGN_TOP);
                  celSignature.setHorizontalAlignment(Element.ALIGN_CENTER);
                  celSignature.setBorderColor(BaseColor.WHITE);
                  livreurTbl.addCell(celLivr);
                  livreurTbl.addCell(celCachet);
                  livreurTbl.addCell(celSignature);
                  document.add(livreurTbl);
                  document.add(new Phrase("\n"));
                  document.add(new Phrase("\n"));
                  Phrase nbrColis = new Phrase ("Nombre des colis: "+ r.getColis().stream().filter(cl->cl.getEtat().equals(colisEtats.get(indexEtat))).count(), new Font(FontFamily.HELVETICA ,11, Font.BOLD));
                  PdfPCell cellNbrColis = new PdfPCell(nbrColis);
                  cellNbrColis.setHorizontalAlignment (Element.ALIGN_CENTER);
                  cellNbrColis.setBorder( Rectangle.BOX) ;
                  cellNbrColis.setBorderColor( new BaseColor(253, 254, 254));
                  cellNbrColis.setBorderWidth(1f);
                  PdfPTable tableNbrColis = new PdfPTable(1);
                  tableNbrColis.addCell(cellNbrColis);
                  tableNbrColis.setHorizontalAlignment (Element.ALIGN_RIGHT);
                  tableNbrColis.setWidthPercentage(30f);
                  document.add(tableNbrColis);
            	  ArrayList<ColisEtat> etats= new ArrayList<>();
                  if(colisEtats.get(indexEtat).equals(ColisEtat.enCoursDeLivraison)) {
          			  etats.add(ColisEtat.enCoursDeLivraison);
          			  buildTable(document, r, barCodeColisDirectoryPath, etats);
                  }
                  else {
                	  etats.clear();
                      etats.add(ColisEtat.planificationRetour);
                      etats.add(ColisEtat.planificationRetourEchange);
          			  buildTable(document, r, barCodeColisDirectoryPath, etats);
                  }
                  
      			  
                 /* Font font2 = FontFactory.getFont(FontFactory.HELVETICA,15,  BaseColor.DARK_GRAY);

                  LocalDateTime dateCreation1 = LocalDateTime.now();
                  Paragraph para33 = new Paragraph(       "      Date                                        Signature Coursier                                 Cachet Socièté"  , font2  );
                  Paragraph para44 = new Paragraph(                        String.valueOf( dateCreation1.getYear()) +"-" +String.valueOf(dateCreation1.getMonthValue()) + "-"+ String.valueOf(dateCreation1.getDayOfMonth()) + "                                         ..................                                            .................."  , font2);

                  document.add(new Phrase("\n"));


                  document.add(para33) ;
                  document.add(new Phrase("\n"));

                  document.add(para44) ;*/
                  document.newPage();
            }
          


            document.close(); }

        catch(DocumentException e) {
            logger.error(e.toString());

        }
        return new ByteArrayInputStream(out.toByteArray());
    }
    
    private static void buildTable(Document document, Runsheet r, String barCodeColisDirectoryPath, List<ColisEtat> colisEtat) {
        if(r.getColis().stream().filter(c->colisEtat.contains(c.getEtat())).count()>0) {
        	PdfPTable table = new PdfPTable(7);
            table.setTotalWidth(650f);
            table.setLockedWidth(true);
            float[] columnWidths = new float[] {90f,90f, 90f, 100f, 90f,90f, 110f};
            try {
            	String infoTxt = colisEtat.contains(ColisEtat.enCoursDeLivraison)?"en cours de livraison: ":" planification de retour/retour échange";
            	String info = "Liste des colis"+ infoTxt;
             	Phrase infoTabEncrLiv = new Phrase (info, new Font(FontFamily.HELVETICA ,13, Font.BOLD ));
                PdfPCell cellInfoTabEncrLiv = new PdfPCell(infoTabEncrLiv);
                cellInfoTabEncrLiv.setHorizontalAlignment (Element.ALIGN_CENTER);
                cellInfoTabEncrLiv.setBorder( Rectangle.BOX) ;
                cellInfoTabEncrLiv.setBorderColor(BaseColor.WHITE);
                cellInfoTabEncrLiv.setBorderWidth(1f);
                PdfPTable tableInfoTabEncrLiv = new PdfPTable(1);
                tableInfoTabEncrLiv.addCell(cellInfoTabEncrLiv);
                tableInfoTabEncrLiv.setHorizontalAlignment (Element.ALIGN_CENTER);
                tableInfoTabEncrLiv.setWidthPercentage(30f);
                document.add(tableInfoTabEncrLiv);
                document.add(new Phrase("\n"));
                
    			table.setWidths(columnWidths);
    			  Font font10 = FontFactory.getFont(FontFactory.HELVETICA ,12, BaseColor.BLACK);
    		        Stream.of("Code à barre " ,"Expéditeur","Client",  " Téléphone Client" , "Adresse" ,"COD" , "Remarque")
    		                .forEach(headerTitle -> {
    		                    PdfPCell header = new PdfPCell();
    		                    Font headFont = new Font(FontFamily.HELVETICA,12,Font.BOLD);
    		                    header.setBackgroundColor(BaseColor.WHITE);
    		                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
    		                    header.setBorderWidth(1);
    		                    header.setBorderColor(BaseColor.BLACK);
    		                    header.setPhrase(new Phrase(headerTitle, headFont));
    		                    //header.setBackgroundColor(new BaseColor(53, 122, 183)) ;
    		                    table.addCell(header);
    		                });
    		        List<Colis> c = r.getColis().stream().filter(cl->colisEtat.contains(cl.getEtat())).collect(Collectors.toList());
    		        float totalPrix= (float) c.stream().mapToDouble(cl->cl.getCod()).sum();
    		        for(Colis c4 : c)
    		        {
    		            Image image2 = Image.getInstance(barCodeColisDirectoryPath+File.separator+ c4.getBar_code() + ".jpg");
    		            image2.setBorder(1);
    		            Font font11 = FontFactory.getFont(FontFactory.HELVETICA ,12, BaseColor.BLACK);
    		            PdfPCell RefCell = new PdfPCell(new Phrase(  c4.getBar_code()  , font11) );
    		            RefCell.addElement(image2);
    		            RefCell.addElement(new Phrase("   "+c4.getBar_code() , font11)) ;
    		            RefCell.setPaddingLeft(4);
    		            RefCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            RefCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            RefCell.setBorderWidth(1);
    		            RefCell.setBorderColor(BaseColor.BLACK);

    		            table.addCell(RefCell);
    		            PdfPCell  expCell = new PdfPCell(new Phrase(c4.getFournisseur().getNom_f() +" "+ c4.getFournisseur().getPrenom_f() , font10));
    		            expCell.setPaddingLeft(4);
    		            expCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            expCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            expCell.setBorderWidth(1);
    		            expCell.setBorderColor(BaseColor.BLACK);
    		            table.addCell(expCell);

    		            PdfPCell clCell = new PdfPCell(new Phrase(c4.getNom_c()+ " " +  c4.getPrenom_c() , font10));
    		            clCell.setPaddingLeft(4);
    		            clCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            clCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            clCell.setBorderWidth(1);
    		            clCell.setBorderColor(BaseColor.BLACK);
    		            table.addCell(clCell);


    		            PdfPCell telCell = new PdfPCell(new Phrase(String.valueOf(c4.getTel_c_1()) , font10));
    		            telCell.setPaddingLeft(4);
    		            telCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            telCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            telCell.setBorderWidth(1);
    		            telCell.setBorderColor(BaseColor.BLACK);
    		            table.addCell(telCell);


    		            PdfPCell adCell = new PdfPCell(new Phrase( c4.getAdresse() , font10));
    		            adCell.setPaddingLeft(4);
    		            adCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            adCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            adCell.setBorderWidth(1);
    		            adCell.setBorderColor(BaseColor.BLACK);
    		            table.addCell(adCell);



    		            PdfPCell coCell = new PdfPCell(new Phrase(String.valueOf(c4.getCod()) , font10));
    		            coCell.setPaddingLeft(4);
    		            coCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            coCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            coCell.setBorderWidth(1);
    		            coCell.setBorderColor(BaseColor.BLACK);
    		            table.addCell(coCell);



    		            PdfPCell reCell = new PdfPCell(new Phrase("        " , font10));
    		            reCell.setPaddingLeft(4);
    		            reCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    		            reCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    		            reCell.setBorderWidth(1);
    		            reCell.setBorderColor(BaseColor.BLACK);
    		            table.addCell(reCell);
    		        }
    	            document.add(table);
    	            document.add(new Phrase("\n"));
                    document.add(new Phrase("\n"));

                    Phrase total = new Phrase ("Total: "+ totalPrix, new Font(FontFamily.HELVETICA ,16, Font.BOLD ));
                    PdfPCell cellTotal = new PdfPCell(total);
                    cellTotal.setHorizontalAlignment (Element.ALIGN_CENTER);
                    cellTotal.setBorder( Rectangle.BOX) ;
                    cellTotal.setBorderColor(BaseColor.BLACK);
                    cellTotal.setBorderWidth(1f);
                    cellTotal.setPadding(5f);
                    PdfPTable tableTtoal = new PdfPTable(1);
                    tableTtoal.addCell(cellTotal);
                    tableTtoal.setHorizontalAlignment (Element.ALIGN_RIGHT);
                    tableTtoal.setWidthPercentage(15f);
                    document.add(tableTtoal);
                    document.add(new Phrase("\n"));
    		} catch (DocumentException e) {
    			e.printStackTrace();
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
    }
    
}

    
    

