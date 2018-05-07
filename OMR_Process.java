/*import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import javax.imageio.ImageIO;
 */

import java.io.File;
import java.util.Scanner;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OMR_Process {

    public static String omrFilename = "scan1.png";
    public static int nQuestions, nOptions, ansTotalColumns, ansMaxQuesInCol;
    public static String keyFname = "key1.txt";
    public static double finalMark, negMark = 0.00;
    public static String usn = "";
    public static boolean validUsn = true;
    static int[][] ans, key;
    static double[] res;
    static int usnLength = 10;
    static int digitLength = 10;


    public static void omrProcess() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            // Read the image
            Mat im_input = Imgcodecs.imread(omrFilename, Imgcodecs.CV_LOAD_IMAGE_COLOR);

            // Resize the image
            Size sz = new Size(600, 400);
            Imgproc.resize(im_input, im_input, sz);

            // Convert into gray Scale
            Mat im_gray = new Mat();
            Imgproc.cvtColor(im_input, im_gray, Imgproc.COLOR_RGB2GRAY);

            Imgcodecs.imwrite(".//results//im0_resized.png", im_input);
            //System.out.println("Size of the image resized,gray: " + im_input.rows() + "," + im_input.cols() + "," + im_input.channels());

            // Convert it into Black and white by Thresholding th=100
            Imgproc.threshold(im_input, im_input, 100, 255, Imgproc.THRESH_BINARY_INV);
            //System.out.println("Size of the image thresh_inv: " + im_input.rows() + "," + im_input.cols() + "," + im_input.channels());

            // Convert into 1 Channel (to apply connected components)
            Mat im_bw = new Mat(im_input.rows(), im_input.cols(), CvType.CV_8UC1);
            Core.extractChannel(im_input, im_bw, 0);
            Imgcodecs.imwrite(".//results//im1_bw.png", im_bw);
            //System.out.println("Size of the image BW 1ch: " + im_bw.rows() + "," + im_bw.cols() + "," + im_bw.channels());

            // Extract the Rectangles
            Mat im_sq = new Mat();
            Mat ele1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8));
            Imgproc.erode(im_bw, im_sq, ele1);
            Mat ele2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8));
            Imgproc.dilate(im_sq, im_sq, ele2);
            Imgcodecs.imwrite(".//results//im1_sq.png", im_sq);

            Mat im_sqlbls = new Mat();
            Mat stats = new Mat();
            Mat im_centroid = new Mat();
            Imgproc.connectedComponentsWithStats(im_sq, im_sqlbls, stats, im_centroid);
            //System.out.println(stats.dump());
            //System.out.println(im_sqlbls.dump());

            // Erode & Dilate the above results
            int erosion_size = 1;
            int dilation_size = 1;
            //Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
            Imgproc.erode(im_bw, im_bw, element);
            Imgcodecs.imwrite(".//results//im2_erosion.png", im_bw);

            //Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * dilation_size + 1, 2 * dilation_size + 1));
            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * dilation_size + 1, 2 * dilation_size + 1));
            Imgproc.dilate(im_bw, im_bw, element1);
            Imgcodecs.imwrite(".//results//im3_dilation.png", im_bw);
            //System.out.println("dilation and erosion is applied");

            // Implement bwAreaOpen  (to suppress the small objects)
            im_bw = bwAreaOpen(im_bw, 25);
            //Imgcodecs.imwrite("im4_areaopen.png", im_bw);
            erosion_size = 5;
            element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
            Imgproc.dilate(im_bw, im_bw, element);
            Imgproc.erode(im_bw, im_bw, element);
            Imgcodecs.imwrite(".//results//im4_areaopen.png", im_bw);

            int[] r = new int[7];
            int[] c = new int[7];
            double[] data;
            data = stats.get(1, 1);
            r[1] = (int) data[0];
            data = stats.get(1, 0);
            c[1] = (int) data[0];
            data = stats.get(2, 1);
            r[2] = (int) data[0];
            data = stats.get(2, 0);
            c[2] = (int) data[0];
            data = stats.get(3, 1);
            r[3] = (int) data[0];
            data = stats.get(3, 0);
            c[3] = (int) data[0];
            data = stats.get(4, 1);
            r[4] = (int) data[0];
            data = stats.get(4, 0);
            c[4] = (int) data[0];
            data = stats.get(5, 1);
            r[5] = (int) data[0];
            data = stats.get(5, 0);
            c[5] = (int) data[0];
            data = stats.get(6, 1);
            r[6] = (int) data[0];
            data = stats.get(6, 0);
            c[6] = (int) data[0];

            Rect rectCrop = new Rect(c[1] + 2, r[1] + 36, 156, 122);
            Mat usn_image = new Mat(im_bw, rectCrop);
            Imgcodecs.imwrite(".//results//im5_usn.png", usn_image);

            rectCrop = new Rect(c[3], r[3] + 8, c[6] - c[3]+5, r[6] - r[3] - 9);
            Mat bubble_image = new Mat(im_bw, rectCrop);
            Imgcodecs.imwrite(".//results//im6_bubble.png", bubble_image);

            // Read Config file and get nQuestions and nOptions
            File file1 = new File("config.txt");
            Scanner sc = new Scanner(file1);
            nQuestions = sc.nextInt();
            nOptions = sc.nextInt();
            ans = new int[nQuestions + 1][nOptions + 1];
            key = new int[nQuestions + 1][nOptions + 1];
            res = new double[nQuestions + 1];

            // Process USN // This updates "usn" 
            usn = "";
            process_Usn(usn_image);
            System.out.println("USN: " + usn);

            // Process Bubbles and Identify answers // This updates "ans" 
            process_Bubbles(bubble_image);  

            // Read the key answers from file into "key" and using "ans" answers, 
            // compute the result "res" & "finalMark"
            getResult();

        } catch (Exception e) {
            System.out.println("Catched Error: " + e.getMessage());
        }
    }

    static void process_Usn(Mat usn_image) {

        int i, j, k, x1, x2, y1, y2, m, n;
        
        m = usn_image.rows();
        n = usn_image.cols();

        double[] fillarea = new double[digitLength + 1];
        for (i = 1; i <= usnLength; i++) {
            y1 = Math.round((i - 1) * n / usnLength) + 1;
            y2 = Math.round(i * n / usnLength) - 1;

            Rect rect1 = new Rect(y1, 0, y2 - y1, m - 1);
            Mat digit_image_col = new Mat(usn_image, rect1);
            //String fname = ".//results//im7_usn_col_" + Integer.toString(i) + ".png";
            //Imgcodecs.imwrite(fname, digit_image_col);

            int flag = 0;
            double[] data;
            int s = 0;
            for (k = 1; k <= digitLength; k++) {

                x1 = Math.round((k - 1) * m / digitLength);
                x2 = Math.round(k * m / digitLength) - 1;

                Rect rect = new Rect(3, x1, y2 - y1-3, x2 - x1);
                Mat digit_image = new Mat(digit_image_col, rect);
                //String fname1 = ".//results//im8_digit_" + Integer.toString(i - 1) + Integer.toString(k - 1) + ".png";
                //Imgcodecs.imwrite(fname1, digit_image);

                //System.out.println(Core.sumElems(digit_image));
                for (int p = 0; p < digit_image.rows(); p++) {
                    for (int q = 0; q < digit_image.cols(); q++) {
                        data = digit_image.get(p, q);
                        if (data[0] > 0) {
                            s = s + 1;
                        }
                    }
                }
                double m2 = digit_image.rows();
                double n2 = digit_image.rows();
                fillarea[k] = (double) s / (double) (m2 * n2);
                if (fillarea[k] > 0.2 && flag == 0) {
                    flag = 1;
                    usn = usn + Integer.toString(k - 1);
                } else {
                    if (fillarea[k] > 0.2) {
                        validUsn = false;
                    }
                }
            }
        }
    }

    static void process_Bubbles(Mat bubble_image) {
        try {
            if (nOptions <= 4) { ansTotalColumns = 6;
            } else {             ansTotalColumns = 5;
            }
            ansMaxQuesInCol = (int) Math.ceil(nQuestions / (double) ansTotalColumns);

            int i, j, k, x1, x2, y1, y2, y3, y4, m, n;
        
            m = bubble_image.rows();
            n = bubble_image.cols();

            double fillarea;
            int cnt = 1;
            for (i = 1; i <= ansTotalColumns; i++) {
                y1 = Math.round((i - 1) * n / ansTotalColumns) + 1;
                y2 = Math.round(i * n / ansTotalColumns) - 1;

                Rect rect1 = new Rect(y1, 0, y2 - y1 + 1, m - 1);
                Mat bub_img_col = new Mat(bubble_image, rect1);
                String fname = ".//results//im9_ans_col_" + Integer.toString(i) + ".png";
                Imgcodecs.imwrite(fname, bub_img_col);

                
                double[] data;
                int s = 0;
                
                for (j = 1; j <= ansMaxQuesInCol; j++) {

                    x1 = Math.round((j - 1) * m / ansMaxQuesInCol);
                    x2 = Math.round(j * m / ansMaxQuesInCol) - 1;

                    Rect rect = new Rect(0, x1, y2 - y1, x2 - x1);
                    Mat ans_single = new Mat(bub_img_col, rect);
                    rect = new Rect(19, 0, ans_single.cols()-24, ans_single.rows());
                    ans_single = new Mat(ans_single, rect);

                    String fname1 = ".//results//im10_ans_" + Integer.toString(i - 1) + Integer.toString(j - 1) + ".png";
                    Imgcodecs.imwrite(fname1, ans_single);
                    
                    int options = nOptions;
                    if (nOptions <= 4) 
                        options = 4;
                    for (k = 1; k <= options; k++) {
                        y3 = Math.round((k - 1) * ans_single.cols()  / options);
                        y4 = Math.round(k * ans_single.cols() / options) - 1;
                        
                        rect = new Rect(y3, 1, y4 - y3,ans_single.rows()-2);
                        Mat ans_single_col = new Mat(ans_single, rect);                        
                        
                        //Scalar data1 = Core.sumElems(ans_single_col);
                        //System.out.println(Core.sumElems(ans_single_col)/255);
                        int m2 = ans_single_col.rows();
                        int n2 = ans_single_col.cols();
                        
                        s = 0;
                        for (int p = 0; p < m2; p++) {
                            for (int q = 0; q < n2; q++) {
                                data = ans_single_col.get(p, q);
                                if (data[0] > 0) {
                                    s = s + 1;
                                }
                            }
                        }
                        
                        fillarea = (double) s / (double) (m2 * n2);
                        //System.out.print(fillarea+", ");
                        if (fillarea > 0.2) {
                            ans[cnt][k] = 1;
                            //System.out.println(k);
                        }
                    }
                    cnt=cnt+1;
                    //System.out.println();  
                }    
            }
            /*
            for(i=1;i<=60;i++){
                for (j=1;j<=4; j++)
                    System.out.print(ans[i][j]+",");
                System.out.println();
            }*/
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    static void getResult() {
        double result = 0;
        try {
            // Read the key answers and update "key"
            File file2 = new File(keyFname);
            Scanner sc2 = new Scanner(file2);

            int val, cnt = 0;
            while (sc2.hasNextInt()) {
                val = sc2.nextInt();
                cnt = cnt + 1;
                for (int i = 1; i <= nOptions; i++) {
                    if (i == val) {
                        key[cnt][i] = 1;
                    } else {
                        key[cnt][i] = 0;
                    }
                }
            }

            // Display the keys
            /*
            for(int i=1; i<=nQuestions; i++){
                for(int j=1; j<=nOptions; j++){
                    System.out.print(key[i][j]+","); 
                }
                System.out.println();
            }
             */
            // Compare answers with Key 
            int i, j, sum;
            for (i = 1; i <= nQuestions; i++) {
                sum = 0;
                for (j = 1; j <= nOptions; j++) {
                    sum = sum + ans[i][j];
                }
                switch (sum) {
                    case 0:
                        res[i] = 0.0;
                        break;
                    case 1:
                        res[i] = -negMark;
                        for (j = 1; j <= nOptions; j++) {
                            if (ans[i][j] == 1 && key[i][j] == 1) {
                                res[i] = 1.0;
                            }
                        }
                        break;
                    default:
                        res[i] = negMark;
                }
                result = result + res[i];
            }

        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
        finalMark =  result;
        System.out.println("Final Mark : " +finalMark);
        
    }

    static Mat bwAreaOpen(Mat im_bw, int minArea) {

        // Perform Connectd component labeling
        Mat im_lbls = new Mat();
        Mat stats = new Mat();
        Mat im_centroid = new Mat();
        Imgproc.connectedComponentsWithStats(im_bw, im_lbls, stats, im_centroid);

        int i, j, k, m, n;
        int label, area;
        double[] data;

        m = im_bw.rows();
        n = im_bw.cols();

        for (k = 1; k < stats.rows(); k++) {
            // get the area
            data = stats.get(k, 4);
            area = (int) data[0];
            // If the area is less small, replace by 0
            if (area < minArea) {
                for (i = 0; i < m; i++) {
                    for (j = 0; j < n; j++) {
                        data = im_lbls.get(i, j);
                        label = (int) data[0];
                        if (k == label) {
                            data[0] = 0;
                            im_bw.put(i, j, data);
                        }
                    }
                }
            }
        }
        return im_bw;
    }
    

    //static int val,cnt=0;    
    public static void main(String[] args) {
        // Process
        omrProcess();
    }
    
}
