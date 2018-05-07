/*
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Element;
//import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.ColumnText;
import java.io.File;
*/

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.awt.PdfGraphics2D;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.BasicStroke;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class OMR_GeneratePDF {

    //public static final String DEST = "omr_template.pdf";
    public static int nQuestions;
    public static int nOptions;
    public static int verQPexists;
    


    //public static void createPdf(String dest) throws IOException, DocumentException {
    public static void createPdf() throws IOException, DocumentException {

        // Get total questions
        File file = new File("config.txt");
        Scanner sc = new Scanner(file);

 
         //while (sc.hasNextLine())
        nQuestions = sc.nextInt();
        nOptions = sc.nextInt();
        verQPexists = sc.nextInt();
        
        /************ Initialize Software Parameters *******************/
        String dest = "omr_template.pdf";
        //int nQuestions = 80;
        //int nOptions = 4;    
        int nQPversionExists = 0;
        int nNameExists = 1;

        int pageTotalWidth = 597, pageTotalHeight = 842;  // A4 size 8.3" x 11.7"
        int pageTopMargin = 30;
        int pageLeftMargin = 40;
        int pageBotMargin = 30;
        int pageRightMargin = 50;
        int pageCanvasWidth = pageTotalWidth - pageLeftMargin - pageRightMargin;
        int pageCanvasHeight = pageTotalHeight - pageTopMargin - pageBotMargin;

        /****************** Create document object ***************************/
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        PdfContentByte canvas = writer.getDirectContent();
        PdfTemplate template = canvas.createTemplate(pageTotalWidth, pageTotalHeight);
        Graphics2D g = new PdfGraphics2D(template, pageTotalWidth, pageTotalHeight);
        
        // Set line width
        g.setStroke(new BasicStroke((float) 0.5));

        // Draw top rectangle markers
        g.fillRect(pageLeftMargin, pageTopMargin, 10, 8);    
        g.fillRect(pageLeftMargin + pageCanvasWidth, pageTopMargin, 10, 8); 
        
        // To draw option circles/ovals
        int x, i, j;
        int qcnt = 1;

        /********************* USN / ROLL Number ******************************/
        
        int xUsn = pageLeftMargin;
        int yUsn = pageTopMargin; 
        
        int usnRectH = 20;
        int usnRectW = 15;
        int usnLength = 10;
        String usnFormat;
        usnFormat = new String("DDDDDDDDDD");
        
        // Write and Draw USN label, box and ovals
        int usnBoxX1 = xUsn;
        int usnBoxX2;
        int usnBoxY1 = yUsn;
        int usnBoxY2;
        int usnGapFromLeftBox = 5;
        
        int usnContainsLetters = 0;

        
        //int usnMaskX1 = xUsn + 2;
        //int usnMaskY1 = yUsn + usnRectH +2;
        //int usnMaskX2, usnMaskY2;        
        
        yUsn += 13;
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
        g.drawString(String.format("USN"), xUsn + 15, yUsn);
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 6));

        xUsn += usnGapFromLeftBox;
        yUsn += 2; 
        for (i = 0; i < usnLength; i++) {
            g.drawRect(xUsn + i * usnRectW, yUsn, usnRectW, usnRectH);
            if (usnFormat.charAt(i) == 'L') {
                for (int r = 0; r < 26; r++) {
                    g.drawOval(xUsn + i * usnRectW + 3, yUsn + 25 + r * 12, 10, 8);
                    //g.fillOval(xUsn + i * usnRectW + 3, yUsn + 25 + r * 12, 10, 8);
                    g.drawString(String.format("%c", r + 'A'), xUsn + i * usnRectW + 6, yUsn + 31 + r * 12);
                }
            } else if (usnFormat.charAt(i) == 'D') {
                for (int r = 0; r < 10; r++) {
                    g.drawOval(xUsn + i * usnRectW + 2, yUsn + 25 + r * 12, 10, 8);
                    //g.fillOval(xUsn + i * usnRectW + 2, yUsn + 25 + r * 12, 10, 8);

                    g.drawString(String.format("%s", r), xUsn + i * usnRectW + 6, yUsn + 31 + r * 12);
                }
            }
        }
        usnBoxX2 = usnBoxX1 + usnLength * usnRectW + 2 * usnGapFromLeftBox;
         
        if (usnContainsLetters == 0) {
            usnBoxY2 = usnBoxY1 + 40 + 10 * 12;  
        } else {
            usnBoxY2 = usnBoxY1 + 40 + 26 * 12; 
        }
        g.drawRect(usnBoxX1, yUsn - 15, usnBoxX2-usnBoxX1, usnBoxY2-usnBoxY1);
        //g.fillRect(maskUsnX1,maskUsnY1,maskUsnX2-maskUsnX1+1,maskUsnY2-maskUsnY1+1);

        /********************* TITLE / SUBJECT / NAME   **********************/    
        int xName = usnBoxX2 + 10;
        int yName = pageTopMargin;

        // Give Title and Subtitle
        /*
        g.setFont(new Font(Font.SERIF, Font.BOLD, 12));
        g.setColor(Color.blue);
        g.drawString(String.format("Vivekananda College of Engineering and Technology, Puttur"), xTitle, yTitle + 10);
        g.setFont(new Font(Font.SERIF, Font.BOLD, 12));
        g.setColor(Color.black);
        g.drawString(String.format("Automata Theory and Computability (15CS54)"), xTitle, yTitle + 25);
        */
        if (nNameExists==1){
            g.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
            g.drawString(String.format("Name: "), xName+3, yName+13);
            g.drawRect(xName, yName, pageTotalWidth - pageRightMargin -xName+10, usnRectH);
        }


        /******************************* QP Version ***************************/
        int xVersion = xName;
        int yVersion = yName + 45;
        int verTotal = 7;
         if(nNameExists==0){
            yVersion = yName + 15; 
            verTotal = 10;
         }
        
        int verBoxX1 = xVersion;
        int verBoxY1 = yVersion - 15;
        int verBoxX2 = xVersion + usnRectW + 10;
        int verBoxY2 = usnBoxY2; // yVersion + 7 * 12 + 26;
        
        int insBoxX1 = verBoxX1;
        int insBoxY1 = verBoxY1;
        int insBoxX2 = verBoxX2;
        int insBoxY2 = verBoxY2;  
        

        if (verQPexists == 1) {

            g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
            g.drawString(String.format("QP"), xVersion + 4, yVersion - 5);
            
            
            g.setFont(new Font(Font.SERIF, Font.PLAIN, 6));
            for (i = 0; i < 1; i++) {
                /*g.drawRect(xVersion + 8, yVersion + i * usnRectH, usnRectW, usnRectH);
                if (i == 0) {
                    for (int r = 0; r < 4; r++) {
                        g.drawOval(xVersion + 30 + r * 16, yVersion + i * usnRectH + 5, 10, 8);
                        g.drawString(String.format("%c", r + 'A'), xVersion + 33 + r * 16, yVersion + i * usnRectH + 11);
                    }
                } else if (i == 1) {
                    for (int r = 0; r < 4; r++) {
                        g.drawOval(xVersion + 30 + r * 16, yVersion + i * usnRectH + 5, 10, 8);
                        g.drawString(String.format("%s", r), xVersion + 33 + r * 16, yVersion + i * usnRectH + 11);
                    }
                }*/
                g.drawRect(xVersion + i * usnRectW + 5, yVersion, usnRectW, usnRectH);
                if (i == 0) {
                    for (int r = 0; r < verTotal; r++) {
                        g.drawOval(xVersion + i * usnRectW + 7, yVersion + 25 + r * 12, 10, 8);
                        //g.fillOval(xVersion + i * usnRectW + 7, yVersion + 25 + r * 12, 10, 8);
                        
                        
                        g.drawString(String.format("%c", r + 'A'), xVersion + i * usnRectW + 10, yVersion + 31 + r * 12);
                    }
                } else if (i == 1) {
                    for (int r = 0; r < verTotal; r++) {
                        g.drawOval(xVersion + i * usnRectW + 7, yVersion + 25 + r * 12, 10, 8);
                        //g.fillOval(xVersion + i * usnRectW + 7, yVersion + 25 + r * 12, 10, 8);

                        g.drawString(String.format("%s", r), xVersion + i * usnRectW + 10, yVersion + 31 + r * 12);
                    }
                }
                
            }
            
            g.drawRect(verBoxX1, verBoxY1, verBoxX2 - verBoxX1, verBoxY2 - verBoxY1);       
            insBoxX1 = verBoxX2 + 10;
                             
        }

        g.drawRect(insBoxX1, insBoxY1, pageTotalWidth - pageRightMargin-insBoxX1+10, insBoxY2 - insBoxY1);
        g.drawRect(insBoxX1, insBoxY1, 100, insBoxY2 - insBoxY1);
        g.drawRect(insBoxX1, insBoxY1+(insBoxY2 - insBoxY1)/3, 100, (insBoxY2 - insBoxY1)/3);
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
        g.drawString(String.format("Serial Number"), insBoxX1+5, insBoxY1 + 12 );
        g.drawString(String.format("Invigilator Signature"), insBoxX1+5, insBoxY1 + 55 );
        g.drawString(String.format("Candidate Signature"), insBoxX1+5, insBoxY1 + 98 );
        g.drawString(String.format("Instructions:"), insBoxX1+105, insBoxY1 + 12 );
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15 );
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15*2);
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15*3);
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15*4);
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15*5);
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15*6);
        g.drawString(String.format("-"), insBoxX1+110, insBoxY1 + 12 + 15*7);
                        
        /******************** ANSWERS **************************************/        
        int xAns = pageLeftMargin;
        int yAns = usnBoxY2+8;
        
        // Draw Q.Nos and bubbles
        int ansMaxQuesInCol;
        int ansTotalColumns;
        int ansGapBetweenBubbles = 15;
        int ansGapBetweenNoAndBubble = 17;
        int ansGapBetweenQuestions = 18;
        int ansBubbleWidth = 10;
        int ansBubbleHeight = 10;
        int ansGapBetweenCols = 24;   
        int ansBoxX1,ansBoxX2, ansBoxY1, ansBoxY2;
        int ansGapFromLeftBox = 5;
        int ansGapFromRightBox = 10;
        
        ansBoxX1 = xAns;
        ansBoxY1 = yAns;
        
        if (nOptions <= 4)
            ansTotalColumns = 6;
        else
            ansTotalColumns = 5;
        
        ansMaxQuesInCol = (int) Math.ceil(nQuestions / (double) ansTotalColumns);
        
        g.setColor(Color.black);

        //yAns += 30;
        xAns += ansGapFromLeftBox; 
        yAns += 15;    
        for (int c = 0; c < ansTotalColumns; c++) {
            
            /*if ((ansQuesInCol * (c + 1) < 100) || (nQuestions<100) ) {
                ansGapBetweenNoAndBubble = 20;
            } else {
                ansGapBetweenNoAndBubble = 25;
            } */

            for (j = 0; j < ansMaxQuesInCol; j++) {
                if (qcnt > nQuestions) {
                    break;
                }

                /*if (c==0)
                    x = xAns + andGapFromLeftBox; 
                else*/
                    x = xAns;
                            
                // Draw Question No.
                g.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
                if (qcnt < 10  || (qcnt<100 && ansGapBetweenNoAndBubble == 25)) {
                    g.drawString(String.format("%s", qcnt++), x + 6, yAns + j * ansGapBetweenQuestions);
                } else { 
                    g.drawString(String.format("%s", qcnt++), x, yAns + j * ansGapBetweenQuestions);
                }
                
                // Draw bubbles
                x += ansGapBetweenNoAndBubble;
                
                g.setFont(new Font(Font.SERIF, Font.PLAIN, 8));
                for (i = 0; i < 4; i++) {
                    if(i<nOptions){
                    g.drawOval(x + i * ansGapBetweenBubbles, yAns + j * ansGapBetweenQuestions - 9, ansBubbleWidth, ansBubbleHeight);
                    //g.fillOval(x + i * ansGapBetweenBubbles, yAns + j * ansGapBetweenQuestions - 9, ansBubbleWidth, ansBubbleHeight);
                    g.drawString(String.format("%c", i + 'a'), x + 3 + i * ansGapBetweenBubbles, yAns + 7 + j * ansGapBetweenQuestions - 9);
                    //g2d.drawString(String.format("%c", i + '1'), x +3+ i * gapBetweenBubbles, yAns +8+ j * gapBetweenQues - 9);
                    }
                }
                
            }
            xAns = xAns + (4-1) * ansGapBetweenBubbles + ansGapBetweenCols + ansGapBetweenNoAndBubble ;
        }
        
        g.setColor(Color.black);
        //ansBoxX2 = pageTotalWidth - pageRightMargin;
        ansBoxX2 = xAns - 5;
        //ansBoxX1 +  andGapFromLeftBox + ansTotalColumns * (nOptions * ansGapBetweenBubbles + ansGapBetweenNoAndBubble);
        ansBoxY2 = yAns + (ansMaxQuesInCol-1) * ansGapBetweenQuestions + 7;

        //g.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
        //g.drawString(String.format("Answers"), ansBoxX1+(ansBoxX2-ansBoxX1)/2-13, ansBoxY1+12);
        
        g.drawRect(ansBoxX1, ansBoxY1, ansBoxX2 - ansBoxX1, ansBoxY2 - ansBoxY1);
        /*if (ansBoxY2 > yMax) {
            yMax = ansBoxY2;
        }*/
        
        g.fillRect(pageLeftMargin, ansBoxY1-8, 10, 8);
        g.fillRect(pageTotalWidth - pageRightMargin, ansBoxY1-8, 10, 8);
        g.fillRect(pageLeftMargin, ansBoxY2, 10, 8);
        g.fillRect(pageTotalWidth - pageRightMargin, ansBoxY2, 10, 8);
        
        //g2d.fillRect(leftMargin - 10, yAns + (nQuesInCol - 1) * gapBetweenQues - 3, 10, 8);
        //g2d.fillRect(totalWidth - rightMargin, yAns + (nQuesInCol - 1) * gapBetweenQues - 3, 10, 8);
        
        g.dispose();

        /*int pages = 1;
        for (int p = 0; p < pages; p++) {
            canvas.addTemplate(template, 0, (pages * totalHeight) - totalHeight);
            document.newPage();
        }
        */
        canvas.addTemplate(template, 0, 0);
       if (nQuestions<=60){
        g.drawLine(pageLeftMargin, pageCanvasHeight/2+pageTopMargin, pageLeftMargin + pageCanvasWidth, pageCanvasHeight/2+pageTopMargin);
        canvas.addTemplate(template, 0, -(pageTotalHeight/2));
       }
        document.newPage();
        document.close();
    }
    
    public static void main(String[] args) throws IOException, DocumentException {
        new OMR_GeneratePDF().createPdf();
        //new OMR_GeneratePDF().createPdf(DEST);
    }
    
}
