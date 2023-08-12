package com.example.filedemo.model;

import java.io.ByteArrayInputStream;
import com.example.filedemo.service.ColisService;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;

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

@CrossOrigin("*")
public class PDFGenerator2 {
	/**
	 * private List<Colis> c4;
	 * 
	 * public PDFGenerator2(List<Colis> c4) { this.c4 = c4; }
	 */
	private final static String defaultLogoPath="src"+File.separator+"main"+File.separator
    		+"resources"+File.separator+"static"+File.separator+"logo"+File.separator+"logo-default.png";

	@Autowired
	private final ColisService colisService;

	@Autowired
	public PDFGenerator2(ColisService colisService) {
		this.colisService = colisService;
	}

	private static Logger logger1 = LoggerFactory.getLogger(PDFGenerator2.class);

	public static ByteArrayInputStream colisDechargeReport(List<Colis> coliss, String barCodeColisDirectoryPath,
			SocietePrincipal societePrincipal, String imagesDirectory) throws MalformedURLException, IOException {

		Rectangle pageSize = new Rectangle(700, 1000);
		Document document1 = new Document(pageSize);
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		try {
			PdfWriter.getInstance(document1, out1);
			document1.open();
			Font font5 = new Font(FontFamily.HELVETICA, 16, Font.NORMAL);
			Phrase bonLivraison = new Phrase("                                              Bon de sortie d'enlèvement",
					font5);
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));

			document1.add(bonLivraison);
            Image logo = Image.getInstance(societePrincipal!=null && societePrincipal.getLogo()!=null?
            		Paths.get(imagesDirectory+File.separator+societePrincipal.getLogo()).toString():defaultLogoPath);
            logo.setBorder(1);
            logo.setAlignment(5);
            logo.setAbsolutePosition(22,797);
            logo.scaleToFit(120,120);
			document1.add(logo);
			StringBuilder societeInfos = new StringBuilder();
			if (societePrincipal != null) {
                societeInfos.append("\n Societé: "+societePrincipal.getNomComplet()+ " "+ societePrincipal.getSigle()+"\n");
                societeInfos.append("Matricule Fiscale: "+societePrincipal.getMatriculeFiscale()+"\n");
                societeInfos.append("Adresse: "+societePrincipal.getAdresse()+"\n");
                societeInfos.append("Téléphone: "+societePrincipal.getTelephone()+"\n");

			}
			Paragraph societe = new Paragraph(societeInfos.toString(),
					FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK));
			societe.setIndentationLeft(120f);
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			document1.add(societe);
			Phrase paraDate = new Phrase("                               Date:" + new SimpleDateFormat("dd/MM/yyyy").format(new Date()),
					new Font(FontFamily.HELVETICA, 13, Font.NORMAL));
			PdfPCell cellDate = new PdfPCell(paraDate);
			cellDate.setBorder(Rectangle.BOX);
			cellDate.setBorderColor(new BaseColor(253, 254, 254));
			cellDate.setBorderWidth(1f);
			PdfPTable tableDate = new PdfPTable(1);
			tableDate.addCell(cellDate);
			tableDate.setHorizontalAlignment(Element.ALIGN_RIGHT);
			tableDate.setWidthPercentage(20);
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			document1.add(tableDate);
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			PdfPTable table9 = new PdfPTable(5);
			table9.setTotalWidth(650f);
			table9.setLockedWidth(true);
			float[] columnWidths = new float[] { 110f, 100f, 90f, 100f, 90f };
			table9.setWidths(columnWidths);
			// Add PDF Table Header ->
			Stream.of("Code à barres", "Client", "Téléphone", "Adresse", "Cod").forEach(headerTitle -> {
				PdfPCell header3 = new PdfPCell();
				Font headFont4 = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
				header3.setHorizontalAlignment(Element.ALIGN_CENTER);
				header3.setBorderWidth(1);
				header3.setPhrase(new Phrase(headerTitle, headFont4));
				table9.addCell(header3);
			});
			for (Colis c4 : coliss) {
				/*
				 * Image image =
				 * Image.getInstance(barCodeColisDirectoryPath+"\\" + c4.getBar_code() + ".jpg")
				 * ; image.setBorder(1); image.setAbsolutePosition( 400,850);
				 * image.scaleToFit(250,200); image.setAlignment(50); document1.add(image);
				 */
				/*
				 * Phrase paraDateBarCode = new Phrase (c4.getBar_code()+"\n \n Date:"+ new
				 * SimpleDateFormat("dd/MM/yyyy").format(new Date()), new
				 * Font(FontFamily.HELVETICA ,13, Font.NORMAL)); PdfPCell cellDateBarCode = new
				 * PdfPCell(paraDateBarCode); cellDateBarCode.setHorizontalAlignment
				 * (Element.ALIGN_CENTER); cellDateBarCode.setBorder( Rectangle.BOX) ;
				 * cellDateBarCode.setBorderColor( new BaseColor(253, 254, 254));
				 * cellDateBarCode.setBorderWidth(1f); PdfPTable tableDateBarCode = new
				 * PdfPTable(1); tableDateBarCode.addCell(cellDateBarCode);
				 * tableDateBarCode.setHorizontalAlignment (Element.ALIGN_RIGHT);
				 * tableDateBarCode.setWidthPercentage(50f); document1.add(new Phrase("\n"));
				 * document1.add(new Phrase("\n")); document1.add(new Phrase("\n"));
				 * document1.add(new Phrase("\n")); document1.add(new Phrase("\n"));
				 * document1.add(new Phrase("\n")); document1.add(new Phrase("\n"));
				 * document1.add(tableDateBarCode); document1.add(new Phrase("\n"));
				 * document1.add(new Phrase("\n")); document1.add(new Phrase("\n"));
				 */
				Font font10 = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
				Image image2 = Image.getInstance(barCodeColisDirectoryPath + File.separator + c4.getBar_code() + ".jpg");
				image2.setBorder(1);
				PdfPCell RefCell = new PdfPCell(new Phrase(c4.getBar_code(), font10));
				RefCell.addElement(image2);
				RefCell.addElement(new Phrase("               " + c4.getBar_code(), font10));
				RefCell.setPaddingLeft(4);
				RefCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				RefCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				RefCell.setBorderWidth(1);
				table9.addCell(RefCell);

				PdfPCell clCell = new PdfPCell(new Phrase(c4.getNom_c() + " " + c4.getPrenom_c(), font10));
				clCell.setPaddingLeft(4);
				clCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				clCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				clCell.setBorderWidth(1);
				table9.addCell(clCell);

				PdfPCell telCell = new PdfPCell(new Phrase(String.valueOf(c4.getTel_c_1()), font10));
				telCell.setPaddingLeft(4);
				telCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				telCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				telCell.setBorderWidth(1);
				table9.addCell(telCell);

				PdfPCell adCell = new PdfPCell(new Phrase(c4.getAdresse(), font10));
				adCell.setPaddingLeft(4);
				adCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				adCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				adCell.setBorderWidth(1);
				table9.addCell(adCell);

				PdfPCell coCell = new PdfPCell(new Phrase(String.valueOf(c4.getCod()), font10));
				coCell.setPaddingLeft(4);
				coCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				coCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				coCell.setBorderWidth(1);
				table9.addCell(coCell);
			}

			document1.add(table9);
			document1.add(new Phrase("\n"));
			document1.add(new Phrase("\n"));
			Font font2 = new Font(FontFamily.HELVETICA, 13, Font.BOLD);
			Paragraph para33 = new Paragraph(
					"            Cachet Livreur                                                                 Signature Fournisseur ",
					font2);
			document1.add(new Phrase("\n"));
			document1.add(para33);
			document1.add(new Phrase("\n"));
			document1.close();
		} catch (DocumentException e) {
			logger1.error(e.toString());

		}
		return new ByteArrayInputStream(out1.toByteArray());
	}
}
