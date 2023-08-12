package com.example.filedemo.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.filedemo.service.ColisService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ConsolePDFGenerator {
	private static Logger logger1 = LoggerFactory.getLogger(PDFGenerator2.class);
	private final static String defaultLogoPath="src"+File.separator+"main"+File.separator
    		+"resources"+File.separator+"static"+File.separator+"logo"+File.separator+"logo-default.png";
	public static ByteArrayInputStream generateConsolePDF(Console console, String barCodeColisDirectoryPath, 
			SocietePrincipal societePrincipal, String imagesDirectory)
			throws MalformedURLException, IOException {

		Rectangle pageSize = new Rectangle(700, 1000);
		Document document1 = new Document(pageSize);
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		try {
			PdfWriter.getInstance(document1, out1);
			document1.open();
            Font font5 = FontFactory.getFont(FontFactory.HELVETICA ,16, BaseColor.BLACK );
            Phrase bonLivraison = new Phrase ( "                                              Console de transfert N° : "+console.getBarCode() , font5);
            document1 .add(bonLivraison);
            Image image = Image.getInstance(barCodeColisDirectoryPath+File.separator+ console.getBarCode() + ".jpg");
            image.setBorder(1);
            Image logo = Image.getInstance(societePrincipal!=null && societePrincipal.getLogo()!=null?
            		Paths.get(imagesDirectory+File.separator+societePrincipal.getLogo()).toString():defaultLogoPath);
            logo.setBorder(1);
            logo.setAlignment(5);
            logo.setAbsolutePosition(22,833);
            logo.scaleToFit(120,120);
            image.setAbsolutePosition( 400,850);
            image.scaleToFit(250,200);
            document1.add(logo) ;
        	document1.add(new Phrase("\n"));
            document1.add(new Phrase("\n"));
            document1.add(new Phrase("\n"));
            StringBuilder societeInfos=new StringBuilder();
            if(societePrincipal!=null) {
                societeInfos.append("\n Societé: "+societePrincipal.getNomComplet()+ " "+ societePrincipal.getSigle()+"\n");
                societeInfos.append("Matricule Fiscale: "+societePrincipal.getMatriculeFiscale()+"\n");
                societeInfos.append("Adresse: "+societePrincipal.getAdresse()+"\n");
                societeInfos.append("Téléphone: "+societePrincipal.getTelephone()+"\n");

            }
            Paragraph societe = new Paragraph(societeInfos.toString(), FontFactory.getFont(FontFactory.HELVETICA ,10, BaseColor.BLACK ));
            societe.setIndentationLeft(120f);    
            document1.add(societe);
            image.setAlignment(50);
            document1.add(image);
            document1.add(new Phrase("\n"));
            Phrase para111 = new Phrase (console.getBarCode()+"\n \n Date:"+ new SimpleDateFormat("dd/MM/yyyy").format(new Date()) , new Font(FontFamily.HELVETICA ,13, Font.NORMAL));
            PdfPCell cell9 = new PdfPCell(para111);
            cell9.setHorizontalAlignment (Element.ALIGN_CENTER);
            cell9.setBorder(0);
            PdfPTable tableBarcode = new PdfPTable(1);
            tableBarcode.addCell(cell9);
            tableBarcode.setHorizontalAlignment (Element.ALIGN_RIGHT);
            tableBarcode.setWidthPercentage(45f);
            document1.add(tableBarcode);
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			 PdfPTable livreurTbl = new PdfPTable(2);
             livreurTbl.setTotalWidth(630f);
             livreurTbl.setLockedWidth(true);
             float[] columnsSize = new float[] {100f, 90f};
             livreurTbl.setWidths(columnsSize);
             Stream.of("" ,"").forEach(headerTitle -> {
                 PdfPCell header3 = new PdfPCell();
                 header3.setBackgroundColor(BaseColor.WHITE);
                 header3.setHorizontalAlignment(Element.ALIGN_CENTER);
                 header3.setBorderColor(BaseColor.WHITE);
                 livreurTbl.addCell(header3);
             });
             PdfPCell celCachet = new PdfPCell();
             Phrase paraCachet = new Phrase ("         Cachet société", FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));
             celCachet.addElement(paraCachet);
             celCachet.setPaddingLeft(4);
             celCachet.setVerticalAlignment(Element.ALIGN_TOP);
             celCachet.setHorizontalAlignment(Element.ALIGN_LEFT);
             celCachet.setBorderColor(BaseColor.WHITE);
             PdfPCell celSignature = new PdfPCell();
             Phrase paraSignature = new Phrase ("                                                      Signature livreur", FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK ));
             celSignature.addElement(paraSignature);
             celSignature.setPaddingLeft(4);
             celSignature.setVerticalAlignment(Element.ALIGN_TOP);
             celSignature.setHorizontalAlignment(Element.ALIGN_RIGHT);
             celSignature.setBorderColor(BaseColor.WHITE);
             livreurTbl.addCell(celCachet);
             livreurTbl.addCell(celSignature);
             document1.add(livreurTbl);
             document1.add(new Phrase("\n"));
             document1.add(new Phrase("\n"));
             document1.add(new Phrase("\n"));
             document1.add(new Phrase ("                                                                     Agence de départ >> Agence d\'arrivée\n", FontFactory.getFont(FontFactory.HELVETICA ,11, BaseColor.BLACK )));
             document1.add(new Phrase ("                                                                         "+ console.getTitreHubDepart() +" >> "+console.getTitreHubArrivee(), new Font(FontFamily.HELVETICA ,13, Font.BOLD )));
             document1.add(new Phrase("\n"));
             document1.add(new Phrase("\n"));
             Phrase nbrColis = new Phrase ("Nombre des colis: "+ console.getColis().size(), new Font(FontFamily.HELVETICA ,11, Font.BOLD));
             PdfPCell cellNbrColis = new PdfPCell(nbrColis);
             cellNbrColis.setHorizontalAlignment (Element.ALIGN_CENTER);
             cellNbrColis.setBorder( Rectangle.BOX) ;
             cellNbrColis.setBorderColor( new BaseColor(253, 254, 254));
             cellNbrColis.setBorderWidth(1f);
             PdfPTable tableNbrColis = new PdfPTable(1);
             tableNbrColis.addCell(cellNbrColis);
             tableNbrColis.setHorizontalAlignment (Element.ALIGN_RIGHT);
             tableNbrColis.setWidthPercentage(30f);
             document1.add(tableNbrColis);
			PdfPTable table9 = new PdfPTable(4);
			table9.setTotalWidth(650f);
			table9.setLockedWidth(true);

			float[] columnWidths = new float[] { 120f,110f,110f,100f};
			table9.setWidths(columnWidths);
			// Add PDF Table Header ->
			Stream.of("Code à barre ", "Expéditeur", "Client", "COD")
					.forEach(headerTitle -> {
						PdfPCell header3 = new PdfPCell();
						Font headFont4 = new Font(FontFamily.HELVETICA, 13, Font.BOLD);
						header3.setBackgroundColor(BaseColor.WHITE);
						header3.setHorizontalAlignment(Element.ALIGN_CENTER);
						header3.setBorderWidth(1);
						header3.setPhrase(new Phrase(headerTitle, headFont4));
						table9.addCell(header3);
					});

			for (Colis c4 : console.getColis()) {
				Font font10 = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
				Image image2 = Image.getInstance(barCodeColisDirectoryPath + File.separator + c4.getBar_code() + ".jpg");
				image2.setBorder(1);
				PdfPCell RefCell = new PdfPCell();
				RefCell.setPaddingLeft(4);
				RefCell.addElement(new Phrase("\n"));
				RefCell.addElement(image2);
				RefCell.addElement(new Phrase("              " + c4.getBar_code(), font10));
				RefCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				RefCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				RefCell.setBorderWidth(1);
				table9.addCell(RefCell);
				
				PdfPCell expCell = new PdfPCell(new Phrase(
						c4.getFournisseur().getNom_f() + " " + c4.getFournisseur().getPrenom_f(), font10));
				expCell.setPaddingLeft(4);
				expCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				expCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				expCell.setBorderWidth(1);
				expCell.setBorderColor(BaseColor.BLACK);
				table9.addCell(expCell);
				
				PdfPCell clCell = new PdfPCell(new Phrase(c4.getNom_c() + " " + c4.getPrenom_c(), font10));
				clCell.setPaddingLeft(4);
				clCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				clCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				clCell.setBorderWidth(1);
				table9.addCell(clCell);

				PdfPCell coCell = new PdfPCell(new Phrase(String.valueOf(c4.getCod()), font10));
				coCell.setPaddingLeft(4);
				coCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				coCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				coCell.setBorderWidth(1);
				table9.addCell(coCell);
			}
			document1.add(table9);
			Font font2 = new Font(FontFamily.HELVETICA, 14, Font.BOLD);
			float som = 0;
			for (Colis c4 : console.getColis()) {
				som = som + c4.getCod();
			}
			document1.add(new Phrase("\n"));
            Phrase total = new Phrase ("Total: "+ som, new Font(FontFamily.HELVETICA ,16, Font.BOLD ));
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
            document1.add(tableTtoal);
			/*document1.add(new Phrase("\n"));
	        document1.add(new Phrase("\n"));
			Paragraph para33 = new Paragraph(
					"            Cachet Livreur                                                                 Signature Fournisseur ",
					font2);
			document1.add(new Phrase("\n"));
			document1.add(para33);
			document1.add(new Phrase("\n"));*/
			document1.close();

		} catch (DocumentException e) {
			logger1.error(e.toString());

		}
		return new ByteArrayInputStream(out1.toByteArray());
	}
}