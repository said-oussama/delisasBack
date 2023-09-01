package com.example.filedemo.model;

import java.awt.Color;




import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.example.filedemo.service.ColisService;
import com.example.filedemo.repository.ColisRepository;
import com.example.filedemo.controller.ColisController;
import  com.example.filedemo.model.Colis;
import  com.example.filedemo.model.Fournisseur;
import  com.example.filedemo.service.FournisseurService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.* ;
import com.itextpdf.text.Font.FontFamily;


@CrossOrigin("*")
public class PDFGenerator {

    private final static String defaultLogoPath="src"+File.separator+"main"+File.separator
            +"resources"+File.separator+"static"+File.separator+"logo"+File.separator+"logo-default.png";

    private  final ColisService colisService ;

    @Autowired
    public PDFGenerator (ColisService colisService) {
        this.colisService = colisService ;
    }





    private static Logger logger = LoggerFactory.getLogger(PDFGenerator.class);

    public static ByteArrayInputStream colisPDFReport (List <Colis> coliss, String barCodeColisDirectoryPath,
                                                       SocietePrincipal societePrincipal, String imagesDirectory ) throws MalformedURLException, IOException {
        Rectangle pageSize = new Rectangle(700, 1000) ;
        Document document = new Document(pageSize);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final DecimalFormat decimalFormat = new DecimalFormat("0.00");
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            for (Colis c3: coliss) {
                Font font5 = FontFactory.getFont(FontFactory.HELVETICA ,16, BaseColor.BLACK );
                Phrase bonLivraison = new Phrase ( "                                              Bordereau de livraison N° : "+c3.getBar_code() , font5);
                document .add(bonLivraison);
                Image image = Image.getInstance(barCodeColisDirectoryPath+File.separator+ c3.getBar_code() + ".jpg");
                image.setBorder(1);
                Image logo = Image.getInstance(societePrincipal!=null && societePrincipal.getLogo()!=null?
                        Paths.get(imagesDirectory+File.separator+societePrincipal.getLogo()).toString():defaultLogoPath);
                logo.setBorder(1);
                logo.setAlignment(5);
                logo.setAbsolutePosition(22,834);
                logo.scaleToFit(120,120);
                image.setAbsolutePosition( 400,850);
                image.scaleToFit(250,200);
                document.add(logo) ;
                if(coliss.indexOf(c3)==0) {
                    document.add(new Phrase("\n"));
                }
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
                document.add(image);
                Phrase para111 = new Phrase (c3.getBar_code() , font5);
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
                Phrase cachet = new Phrase(
                        "    Cachet:", new Font(FontFamily.HELVETICA ,11, Font.BOLD ));
                PdfPCell cellCachet = new PdfPCell(cachet);
                cellCachet.setHorizontalAlignment (Element.ALIGN_LEFT);
                cellCachet.setBorder( Rectangle.BOX) ;
                cellCachet.setBorderColor( new BaseColor(253, 254, 254));
                cellCachet.setBorderWidth(1f);
                PdfPTable tableCachet = new PdfPTable(1);
                tableCachet.addCell(cellCachet);
                tableCachet.setHorizontalAlignment (Element.ALIGN_LEFT);
                tableCachet.setWidthPercentage(30f);
                document.add(tableCachet);
                document.add(new Phrase("\n"));
                document.add(new Phrase("\n"));
                document.add(new Phrase("\n"));
                PdfPTable expedtDest = new PdfPTable(2);
                expedtDest.setTotalWidth(600f);
                expedtDest.setLockedWidth(true);
                float[] columnsSize = new float[] {125f, 125f};
                expedtDest.setWidths(columnsSize);
                Stream.of("Coordonnés expéditeur" ,"Coordonnées destinataire").forEach(headerTitle -> {
                    PdfPCell header3 = new PdfPCell();
                    Font headFont4 = new Font(FontFamily.HELVETICA,12,Font.BOLD);
                    header3.setBackgroundColor(BaseColor.WHITE);
                    header3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header3.setBorderWidth(1);
                    header3.setPhrase(new Phrase(headerTitle, headFont4));
                    expedtDest.addCell(header3);
                });
                PdfPCell celExp = new PdfPCell();
                StringBuilder expContent= new StringBuilder();
                expContent.append("Nom: "+ societePrincipal.getNomComplet()+"\n");
                expContent.append("Adresse: "+societePrincipal.getAdresse()+"\n");
                expContent.append("Téléphone: "+societePrincipal.getTelephone()+"\n");
                Phrase celExpContent = new Phrase(expContent.toString());
                celExp.addElement(celExpContent);
                celExp.setPaddingLeft(4);
                celExp.setHorizontalAlignment(Element.ALIGN_CENTER);
                celExp.setBorderWidth(1);

                PdfPCell celDes = new PdfPCell();
                StringBuilder desContent= new StringBuilder();
                desContent.append("Nom et Prénom  :   " + c3.getNom_c()+ " "  + c3.getPrenom_c()+"\n");
                desContent.append("Téléphone 1    :   " + c3.getTel_c_1()+"\n");
                desContent.append("Téléphone 2    :   " + c3.getTel_c_2()+"\n");
                desContent.append("Governorat      :   " + c3.getGouvernorat()+"\n");
                desContent.append("Adresse           :   " + c3.getDelegation()+" "+ c3.getAdresse()+"\n");
                desContent.append("\n");
                Phrase celDesContent = new Phrase(desContent.toString());
                celDes.addElement(celDesContent);
                celDes.setPaddingLeft(4);
                celDes.setBorderWidth(1);
                celDes.setVerticalAlignment(Element.ALIGN_MIDDLE);
                celDes.setHorizontalAlignment(Element.ALIGN_CENTER);
                expedtDest.addCell(celExp);
                expedtDest.addCell(celDes);
                document.add(expedtDest);

                // Add Text to PDF file ->// Add Text to PDF file ->
                Font font = FontFactory.getFont(FontFactory.HELVETICA ,15, BaseColor.WHITE ) ;
                Font font1 = FontFactory.getFont(FontFactory.HELVETICA ,11 , BaseColor.BLACK);
                PdfPTable table = new PdfPTable(5);
                table.setTotalWidth(600f);
                table.setLockedWidth(true);
                PdfPTable table6 = new PdfPTable(5);
                table6.setTotalWidth(600f);
                table6.setLockedWidth(true);
                float[] columnWidths = new float[] {120f,120f,120f,120f,120f};
                table.setWidths(columnWidths);
                Font font10 = FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK);
                // Add PDF Table Header ->
                Stream.of("Date de création" ,"Longeur","Largeur",  "Hauteur" , "Poids")
                        .forEach(headerTitle -> {
                            PdfPCell header = new PdfPCell();
                            Font headFont = new Font(FontFamily.HELVETICA,11,font.BOLD);
                            header.setBackgroundColor(BaseColor.WHITE);
                            header.setHorizontalAlignment(Element.ALIGN_CENTER);
                            header.setBorderWidth(1);
                            header.setBorderColor(BaseColor.BLACK);
                            header.setPhrase(new Phrase(headerTitle, headFont));
                            table.addCell(header);
                        });

                PdfPCell dateCell = new PdfPCell(new Phrase(new SimpleDateFormat("yyyy-MM-dd").format(Date.from(c3.getDate_creation().atZone(ZoneId.systemDefault()).toInstant())) , font10));
                dateCell.setPaddingLeft(4);
                dateCell.setBorderWidth(1);
                dateCell.setVerticalAlignment(Element.ALIGN_CENTER);
                dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                dateCell.setBorderColor(BaseColor.BLACK);

                PdfPCell longCell = new PdfPCell(new Phrase(c3.getLongeur().toString() +  "                           "  , font10));
                longCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                longCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                longCell.setPaddingRight(4);
                longCell.setBorderWidth(1);
                longCell.setBorderColor(BaseColor.BLACK);


                PdfPCell largeurCell = new PdfPCell(new Phrase(c3.getLargeur().toString() , font10));
                largeurCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                largeurCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                largeurCell.setPaddingRight(4);
                largeurCell.setBorderWidth(1);
                largeurCell.setBorderColor(BaseColor.BLACK);

                PdfPCell hautCell = new PdfPCell(new Phrase(c3.getHauteur().toString() , font10));
                hautCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                hautCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                hautCell.setPaddingRight(4);
                hautCell.setBorderWidth(1);
                hautCell.setBorderColor(BaseColor.BLACK);

                PdfPCell poidsCell = new PdfPCell(new Phrase(c3.getPoids().toString() , font10));
                poidsCell.setPaddingLeft(4);
                poidsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                poidsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                poidsCell.setBorderWidth(1);
                poidsCell.setBorderColor(BaseColor.BLACK);

                PdfPCell modeCell = new PdfPCell(new Phrase(c3.getMode_paiement().toString() , font10));
                modeCell.setPaddingLeft(4);
                modeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                modeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                modeCell.setBorderWidth(1);
                modeCell.setBorderColor(new BaseColor(248, 155, 63));
                //table.addCell(modeCell);



                PdfPCell serCell = new PdfPCell(new Phrase(c3.getService().toString() , font10) );
                serCell.setPaddingLeft(4);
                serCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                serCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                serCell.setBorderWidth(1);


                PdfPCell designCell = new PdfPCell(new Phrase(c3.getDesignation() , font10));
                designCell.setPaddingLeft(4);
                designCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                designCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                designCell.setBorderWidth(1);

                PdfPCell rqCell = new PdfPCell(new Phrase(c3.getRemarque() , font10));
                rqCell.setPaddingLeft(4);
                rqCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                rqCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                rqCell.setBorderWidth(1);


                PdfPCell desCell = new PdfPCell(new Phrase(c3.getDesignation()));
                desCell.setPaddingLeft(4);
                desCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                desCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                desCell.setBorderWidth(1);

                String prixString = decimalFormat.format(new Double(c3.getNb_p()*c3.getCod())*0.93);
                prixString = prixString.replace(",", ".");
                double prix2 = Double.parseDouble(prixString);
                PdfPCell cod2Cell = new PdfPCell(new Phrase (String.valueOf(prix2) , font10));
                cod2Cell.setPaddingLeft(4);
                cod2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cod2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cod2Cell.setBorderWidth(1);



                PdfPCell tvaCell = new PdfPCell(new Phrase("7%" , font10) );
                tvaCell.setPaddingLeft(4);
                tvaCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tvaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tvaCell.setBorderWidth(1);


                PdfPCell Nb2Cell = new PdfPCell(new Phrase(c3.getNb_p().toString() , font10));
                Nb2Cell.setPaddingLeft(4);
                Nb2Cell.setBorderWidth(1);
                Nb2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Nb2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell tot2Cell = new PdfPCell(new Phrase (String.valueOf(decimalFormat.format(c3.getNb_p()*c3.getCod())) ,new Font(FontFamily.HELVETICA ,15, Font.BOLD)));
                tot2Cell.setPaddingLeft(4);
                tot2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tot2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tot2Cell.setBorderWidth(1);

                table.addCell(dateCell);
                table.addCell(longCell);
                table.addCell(largeurCell);
                table.addCell(hautCell);
                table.addCell(poidsCell);
                document.add(table);
                document.add(new Phrase("\n"));
                float[] columnWidths2 = new float[] {90f,40f, 40f, 40f, 90f};
                table6.setWidths(columnWidths2);
                Stream.of("Désignation" ,"Quantité" , "Prix Hors Taxe" , "TVA", "Total").forEach(headerTitle -> {
                    PdfPCell header3 = new PdfPCell();
                    Font headFont4=null;
                    if(headerTitle.equals("Total")) {
                        headFont4=new Font(FontFamily.HELVETICA,15,Font.BOLD);
                    }
                    else {
                        headFont4=new Font(FontFamily.HELVETICA,12,Font.BOLD);
                    }

                    header3.setBackgroundColor(BaseColor.WHITE);
                    header3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header3.setBorderWidth(1);
                    header3.setPhrase(new Phrase(headerTitle, headFont4));
                    table6.addCell(header3);
                });
                table6.addCell(desCell);
                table6.addCell(Nb2Cell);
                table6.addCell(cod2Cell);
                table6.addCell(tvaCell);
                table6.addCell(tot2Cell);
                document.add(table6);
                /*Phrase para77 = new Phrase ( "Total : " +String.valueOf(c3.getCod())  , font6) ;

                PdfPCell cell7 = new PdfPCell(para77);
                cell7.setBorder( Rectangle.BOX) ;
                cell7.setHorizontalAlignment(120) ;
                cell7.setBorderColor( new BaseColor(255,255,255));
                cell7.setBorderWidth(1f);

                PdfPTable table8 = new PdfPTable(1);
                table8.addCell(cell7);
                table8.setHorizontalAlignment (Element.ALIGN_RIGHT);
                table8.setWidthPercentage(20f);
                document.add(new Phrase("\n"));

                document.add(table8);*/
                document.add(new Phrase("\n"));
                document.newPage();
            }
            document.close(); }

        catch(DocumentException e) {
            logger.error(e.toString());

        }
        return new ByteArrayInputStream(out.toByteArray());
    }}
