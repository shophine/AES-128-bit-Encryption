import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Plain Text / Message (16 ASCII Characters only)");
        String plainText = scanner.nextLine();


        System.out.println("Enter the Key(16 ASCII Characters only)");
        String key = scanner.nextLine();

        //String plainText = "Two One Nine Two";
        //String key = "Thats my Kung Fu";

        //check inputs
        if (plainText.length() != 16) {
            System.out.println("ERROR : Plaintext must be in 16 ASCII characters!!!\n");
            return;
        }

        if (key.length() != 16) {
            System.out.println("ERROR : Key must be in 16 ASCII characters!!!\n");
            return;
        }



        //Input to Hex
        String hexPT = ASCIItoHEX(plainText);
        String hexKey = ASCIItoHEX(key);

        System.out.println("\nPT in Hex : " + hexPT);
        System.out.println("\nKey in Hex: " + hexKey);

        //printing the plaintext as matrix
        String hexPTArray[] = ASCIItoHEXArray(plainText);
        /*System.out.println("\n\nPrinting the Plaintext array in grid");
        for (int i = 0; i < hexPTArray.length; i++) {
            if (i % 4 == 0) {
                System.out.println("\n");
            }
            System.out.print(hexPTArray[i] + "\t");
        }
*/
        String[] splitKeyArray = splitToNChar(hexKey, 2);
        System.out.println("\n\n");
        //System.out.println(Arrays.toString(splitKeyArray));
        //System.out.println("Length of SplitArray : " + splitKeyArray.length);


        //creating w0,w1,w2 and w3 from Key
        String[] word0 = Arrays.copyOfRange(splitKeyArray, 0, 4);
        String[] word1 = Arrays.copyOfRange(splitKeyArray, 4, 8);
        String[] word2 = Arrays.copyOfRange(splitKeyArray, 8, 12);
        String[] word3 = Arrays.copyOfRange(splitKeyArray, 12, 16);

        System.out.println("\nGenerating words\n");
        System.out.printf("\n\n Word 0 : ");
        System.out.println(Arrays.toString(word0));
        System.out.printf("\n\n Word 1 : ");
        System.out.println(Arrays.toString(word1));
        System.out.printf("\n\n Word 2 : ");
        System.out.println(Arrays.toString(word2));
        System.out.printf("\n\n Word 3 : ");
        System.out.println(Arrays.toString(word3));

        System.out.println("\n\n");

        //Round key generation

        //For the words with indices that are a multiple of 4 (w4k) - Here to find w4

        // 1. Bytes of w4k-1 are rotated left shift - here w3 is left shifted
        String[] leftRotatedWord3 = Arrays.copyOf(word3, word3.length);
        System.out.println("\nLeft Rotating the matrix : " + Arrays.toString(leftRotatedWord3) + " ...");
        leftRotate(leftRotatedWord3, 1, leftRotatedWord3.length);
        System.out.println("\n\nLeft Rotated Matrix : " + Arrays.toString(leftRotatedWord3));

        // 2. Bytes Substitution using S-Box
        String[] sBoxOutput = new String[leftRotatedWord3.length];
        for (int i = 0; i < 4; i++) {
            sBoxOutput[i] = getSBoxOutput(leftRotatedWord3[i]);
        }
        System.out.println("S Box Output Matrix : " + Arrays.toString(sBoxOutput));

        // 3. Adding Round Constant (01, 00, 00, 00)
        String[] roundConstant = {"01", "00", "00", "00"};
        String[] gOfWord3 = new String[sBoxOutput.length];

        // 4. Above step gives g(w[k-1]) - here g(w[3])
        for (int i = 0; i < gOfWord3.length; i++) {
            gOfWord3[i] = xor(sBoxOutput[i], roundConstant[i]);
        }
        System.out.println("g(word3) : " + Arrays.toString(gOfWord3));

        // 5. w[4k] = g(w[k-1]) ⊕ w[4k-4] - here g(w[3]) xor w[0]
        String[] word4 = new String[word3.length];
        for (int i = 0; i < word4.length; i++) {
            word4[i] = xor(word0[i], gOfWord3[i]);
        }

        //w[i] = w[i-1]+ w[i-4] for all values of i that are not multiples of 4.
        //finding w[5],w[6] and w[7]

        String[] word5 = addWords(word4, word1);
        String[] word6 = addWords(word5, word2);
        String[] word7 = addWords(word6, word3);

        System.out.printf("\n\n Word 4 : ");
        System.out.println(Arrays.toString(word4));
        System.out.printf("\n\n Word 5 : ");
        System.out.println(Arrays.toString(word5));
        System.out.printf("\n\n Word 6 : ");
        System.out.println(Arrays.toString(word6));
        System.out.printf("\n\n Word 7 : ");
        System.out.println(Arrays.toString(word7));

        //Round key 1 = w[4],w[5],w[6] and w[7]
        String roundKey1 = "";

        for (int i = 0; i < word4.length; i++) {
            roundKey1 += word4[i];
        }
        for (int i = 0; i < word5.length; i++) {
            roundKey1 += word5[i];
        }
        for (int i = 0; i < word6.length; i++) {
            roundKey1 += word6[i];
        }
        for (int i = 0; i < word7.length; i++) {
            roundKey1 += word7[i];
        }
        System.out.println("\n\nRound Key 1 : " + roundKey1);

        //converting the roundkey in to AES matrix form - filling it column wise in a matrix
        String[][] roundKey1MatrixForm= aesMatrixForm(roundKey1);

        //key is ready
        String roundKey0 = hexKey;

        //Plain Text in Matrix form
        String[][] plainTextMatrixForm = aesMatrixForm(hexPT);
        System.out.println("\n\n\nANSWERS\nAnswer a : Showing plaintext as a 4x4matrix \n");
        printMatrix(plainTextMatrixForm);
        String[][] roundKey0MatrixForm = aesMatrixForm(roundKey0);

        //creating a state matrix
        String[][] stateMatrix = new String[4][4];

        //add PT and Round Key 0
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                stateMatrix[i][j] = xor(plainTextMatrixForm[i][j], roundKey0MatrixForm[i][j]);
            }
        }
        System.out.println("Answer b : Result after adding Round Key 0\n");
        printMatrix(stateMatrix);

        //substitute bytes

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                stateMatrix[i][j] = getSBoxOutput(stateMatrix[i][j]);
            }
        }
        System.out.println("Answer c : Result after SubBytes\n");
        printMatrix(stateMatrix);

        //shift rows
        String tempStateMatrixValue = "";
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tempStateMatrixValue += stateMatrix[i][j];
            }
        }
        /*System.out.println("State Matrix : ");
        printMatrix(stateMatrix);
*/
        String[] splitStateMatrixValue = splitToNChar(tempStateMatrixValue, 2);

        String[] sm0 = Arrays.copyOfRange(splitStateMatrixValue, 0, 4);
        String[] sm1 = Arrays.copyOfRange(splitStateMatrixValue, 4, 8);
        String[] sm2 = Arrays.copyOfRange(splitStateMatrixValue, 8, 12);
        String[] sm3 = Arrays.copyOfRange(splitStateMatrixValue, 12, 16);

        leftRotate(sm1, 1, sm1.length);
        leftRotate(sm2, 2, sm2.length);
        leftRotate(sm3, 3, sm3.length);

        String updatedStateMatrixValueAfterShiftRows = "";
        for (int i = 0; i < sm0.length; i++) {
            updatedStateMatrixValueAfterShiftRows += sm0[i];
        }
        for (int i = 0; i < sm1.length; i++) {
            updatedStateMatrixValueAfterShiftRows += sm1[i];
        }
        for (int i = 0; i < sm2.length; i++) {
            updatedStateMatrixValueAfterShiftRows += sm2[i];
        }
        for (int i = 0; i < sm3.length; i++) {
            updatedStateMatrixValueAfterShiftRows += sm3[i];
        }

        //System.out.println("\nState Matrix Value After Shift Rows : ");
        String[] updatedStateMatrixValueAfterShiftRowsArray = splitToNChar(updatedStateMatrixValueAfterShiftRows, 2);

        int counter = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                stateMatrix[i][j] = updatedStateMatrixValueAfterShiftRowsArray[counter++];
            }
        }
        System.out.println("Answer d : Result after ShiftRows\n");
        printMatrix(stateMatrix);

        //mix columns
        String[][] fixedMatrix = {
                {"02", "03", "01", "01"},
                {"01", "02", "03", "01"},
                {"01", "01", "02", "03"},
                {"03", "01", "01", "02"}
        };

        stateMatrix = matrixMultiplyForMixedColumns(fixedMatrix, stateMatrix);
        System.out.println("\nAnswer e : Result after Mix Columns \n");
        printMatrix(stateMatrix);

        //Add roundKey 1

        for(int i=0;i<stateMatrix.length;i++){
            for(int j=0;j<stateMatrix.length;j++){
                stateMatrix[i][j] = xor(stateMatrix[i][j],roundKey1MatrixForm[i][j]);
            }
        }

        System.out.println("Answer f : Result after Round 1 \n");
        printMatrix(stateMatrix);


        System.out.println("\n\nAES-128 Implementation\ndone by\n\nSHOPHINE SIVARAJA - ss06878\nAISHWARYA VENKATRAJ - av99869\nKARAN JADHAV - kpj51092");

        //end of main function
    }

    private static String[][] matrixMultiplyForMixedColumns(String[][] fixedMatrix, String [][] stateMatrix){
        String temp="";
        String [][] result = new String[4][4];
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                result[i][j]="00";
                for(int k=0;k<4;k++)
                {
                    temp = lookUpTables(fixedMatrix[i][k],stateMatrix[k][j]);
                    result[i][j] = xor(result[i][j],temp);
                }
                //System.out.print(result[i][j]+" ");
            }
           // System.out.println();
        }
        return result;
    }
    private static String lookUpTables(String a, String b){
        String aRowInString = a.charAt(0)+"";
        String aColInString = a.charAt(1)+"";
        String bRowInString = b.charAt(0)+"";
        String bColInString = b.charAt(1)+"";

        //System.out.println("A row : "+aRowInString+"A col : "+aColInString);
        //System.out.println("B row : "+bRowInString+"B col : "+bColInString);


        int aRow = Integer.parseInt(aRowInString,16);
        int aCol = Integer.parseInt(aColInString,16);
        int bRow = Integer.parseInt(bRowInString,16);
        int bCol = Integer.parseInt(bColInString,16);
       // System.out.println("\n\nINT TABLE \n\n A row : "+aRow+"A col : "+aCol);
       // System.out.println("B row : "+bRow+" B col : "+bCol);

        String aOutput = getLTableOutput(aRow,aCol);
       // System.out.println("\nA output from L Table : "+aOutput);
        String bOutput = getLTableOutput(bRow,bCol);
        //System.out.println("\nB output from L Table : "+bOutput);

        int aOutputInInteger = Integer.parseInt(aOutput,16);
        int bOutputInInteger = Integer.parseInt(bOutput,16);
        //System.out.println("\nA output from L Table in Int: "+aOutputInInteger);
        //System.out.println("\nB output from L Table in Int: "+bOutputInInteger);

        int cOutputInteger = aOutputInInteger + bOutputInInteger;
        if(cOutputInteger >= 256){
            cOutputInteger = cOutputInteger - 255;
        }
        //System.out.println("\ncOutputInteger : "+cOutputInteger);

        String cOutputInHex = Integer.toHexString(cOutputInteger);
        //System.out.println("cOutputInHex : "+cOutputInHex);

        String eTableInput = cOutputInHex;
        //System.out.println("\nMatMulInterVal : "+eTableInput);

        String eTableInputRowString = eTableInput.charAt(0)+"";
        String eTableInputColString = eTableInput.charAt(1)+"";

        int eTableInputRow = Integer.parseInt(eTableInputRowString,16);
        int eTableInputCol = Integer.parseInt(eTableInputColString,16);
        //System.out.println("\nE Table Row: "+eTableInputRow);
        //System.out.println("\nE Table Column: "+eTableInputCol);

        String tempEtableOutput =  getETableOutput(eTableInputRow,eTableInputCol);
        //System.out.println("Etable o/p : "+tempEtableOutput);

        return tempEtableOutput;



    }

private static String getLTableOutput(int row, int column){
        String LTable [][] = {
                {"00","00","19","01","32","02","1A","C6","4B","C7","1B","68","33","EE","DF","03"},
                {"64","04","E0","0E","34","8D","81","EF","4C","71","08","C8","F8","69","1C","C1"},
                {"7D","C2","1D","B5","F9","B9","27","6A","4D","E4","A6","72","9A","C9","09","78"},
                {"65","2F","8A","05","21","0F","E1","24","12","F0","82","45","35","93","DA","8E"},
                {"96","8F","DB","BD","36","D0","CE","94","13","5C","D2","F1","40","46","83","38"},
                {"66","DD","FD","30","BF","06","8B","62","B3","25","E2","98","22","88","91","10"},
                {"7E","6E","48","C3","A3","B6","1E","42","3A","6B","28","54","FA","85","3D","BA"},
                {"2B","79","0A","15","9B","9F","5E","CA","4E","D4","AC","E5","F3","73","A7","57"},
                {"AF","58","A8","50","F4","EA","D6","74","4F","AE","E9","D5","E7","E6","AD","E8"},
                {"2C","D7","75","7A","EB","16","0B","F5","59","CB","5F","B0","9C","A9","51","A0"},
                {"7F","0C","F6","6F","17","C4","49","EC","D8","43","1F","2D","A4","76","7B","B7"},
                {"CC","BB","3E","5A","FB","60","B1","86","3B","52","A1","6C","AA","55","29","9D"},
                {"97","B2","87","90","61","BE","DC","FC","BC","95","CF","CD","37","3F","5B","D1"},
                {"53","39","84","3C","41","A2","6D","47","14","2A","9E","5D","56","F2","D3","AB"},
                {"44","11","92","D9","23","20","2E","89","B4","7C","B8","26","77","99","E3","A5"},
                {"67","4A","ED","DE","C5","31","FE","18","0D","63","8C","80","C0","F7","70","07"}
        };
        String output = LTable[row][column];
        return output;
    }


    private static String getETableOutput(int row, int column){

        String ETable [][]={
                {"01","03","05","0F","11","33","55","FF","1A","2E","72","96","A1","F8","13","35"},
                {"5F","E1","38","48","D8","73","95","A4","F7","02","06","0A","1E","22","66","AA"},
                {"E5","34","5C","E4","37","59","EB","26","6A","BE","D9","70","90","AB","E6","31"},
                {"53","F5","04","0C","14","3C","44","CC","4F","D1","68","B8","D3","6E","B2","CD"},
                {"4C","D4","67","A9","E0","3B","4D","D7","62","A6","F1","08","18","28","78","88"},
                {"83","9E","B9","D0","6B","BD","DC","7F","81","98","B3","CE","49","DB","76","9A"},
                {"B5","C4","57","F9","10","30","50","F0","0B","1D","27","69","BB","D6","61","A3"},
                {"FE","19","2B","7D","87","92","AD","EC","2F","71","93","AE","E9","20","60","A0"},
                {"FB","16","3A","4E","D2","6D","B7","C2","5D","E7","32","56","FA","15","3F","41"},
                {"C3","5E","E2","3D","47","C9","40","C0","5B","ED","2C","74","9C","BF","DA","75"},
                {"9F","BA","D5","64","AC","EF","2A","7E","82","9D","BC","DF","7A","8E","89","80"},
                {"9B","B6","C1","58","E8","23","65","AF","EA","25","6F","B1","C8","43","C5","54"},
                {"FC","1F","21","63","A5","F4","07","09","1B","2D","77","99","B0","CB","46","CA"},
                {"45","CF","4A","DE","79","8B","86","91","A8","E3","3E","42","C6","51","F3","0E"},
                {"12","36","5A","EE","29","7B","8D","8C","8F","8A","85","94","A7","F2","0D","17"},
                {"39","4B","DD","7C","84","97","A2","FD","1C","24","6C","B4","C7","52","F6","01",}
        };
        String output = ETable[row][column];
        return output;
    }

    private static void printMatrix(String[][] input){
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                System.out.printf(input[i][j].toUpperCase()+"\t");
            }
            System.out.printf("\n");
        }

        System.out.println("\n\n");

    }
    private static String[][] aesMatrixForm(String input){
        String[][] aesMatrixForm = new String[4][4];
        String [] splitInputIntoArray = splitToNChar(input,2);
        int temp=0;
        for(int i=0;i<4;i++){
            temp=i;
            for(int j=0;j<4;j++){
                aesMatrixForm[i][j] = splitInputIntoArray[temp];
                temp=temp+4;
            }
        }
        //printMatrix(aesMatrixForm);
        return aesMatrixForm;
    }
    private static String getSBoxOutput(String a){
        String[][] sBox = {
                {"63","7c","77","7b","f2","6b","6f","c5","30","01","67","2b","fe","d7","ab","76"},
                {"ca","82","c9","7d","fa","59","47","f0","ad","d4","a2","af","9c","a4","72","c0"},
                {"b7","fd","93","26","36","3f","f7","cc","34","a5","e5","f1","71","d8","31","15"},
                {"04","c7","23","c3","18","96","05","9a","07","12","80","e2","eb","27","b2","75"},
                {"09","83","2c","1a","1b","6e","5a","a0","52","3b","d6","b3","29","e3","2f","84"},
                {"53","d1","00","ed","20","fc","b1","5b","6a","cb","be","39","4a","4c","58","cf"},
                {"d0","ef","aa","fb","43","4d","33","85","45","f9","02","7f","50","3c","9f","a8"},
                {"51","a3","40","8f","92","9d","38","f5","bc","b6","da","21","10","ff","f3","d2"},
                {"cd","0c","13","ec","5f","97","44","17","c4","a7","7e","3d","64","5d","19","73"},
                {"60","81","4f","dc","22","2a","90","88","46","ee","b8","14","de","5e","0b","db"},
                {"e0","32","3a","0a","49","06","24","5c","c2","d3","ac","62","91","95","e4","79"},
                {"e7","c8","37","6d","8d","d5","4e","a9","6c","56","f4","ea","65","7a","ae","08"},
                {"ba","78","25","2e","1c","a6","b4","c6","e8","dd","74","1f","4b","bd","8b","8a"},
                {"70","3e","b5","66","48","03","f6","0e","61","35","57","b9","86","c1","1d","9e"},
                {"e1","f8","98","11","69","d9","8e","94","9b","1e","87","e9","ce","55","28","df"},
                {"8c","a1","89","0d","bf","e6","42","68","41","99","2d","0f","b0","54","bb","16"},
        };

        String rowAsString = a.charAt(0)+"";
        String columnAsString = a.charAt(1)+"";

        int row = Integer.parseInt(rowAsString,16);
        int column = Integer.parseInt(columnAsString,16);

        /*System.out.println("Row as String : "+rowAsString);
        System.out.println("Column as String : "+columnAsString);
        System.out.println("Row : "+row);
        System.out.println("Column  : "+column);*/

        String outputFromSBox = sBox[row][column];

        //System.out.println("Output from SBox : "+outputFromSBox);


        return outputFromSBox;
    }





    private static void leftRotate(String arr[], int d, int n)
    {
        for (int i = 0; i < d; i++)
            leftRotatebyOne(arr, n);

        //System.out.println("Rotated Matrix : "+Arrays.toString(arr));
    }

    private static String[] leftRotatebyOne(String arr[], int n)
    {
        String temp = arr[0];
        int i;
        for (i = 0; i < n - 1; i++)
            arr[i] = arr[i + 1];
        arr[i] = temp;

        //System.out.println("Rotated Array : "+Arrays.toString(arr));
        return arr;
    }

    private static String[] addWords(String[] a, String[] b){
        //System.out.println(Arrays.toString(a));
       // System.out.println(Arrays.toString(b));
        String[] result = new String[a.length];
        for(int i=0;i<a.length;i++){
            result[i] = xor(a[i],b[i]);
        }

        return result;
    }

    private static String xor(String a,String b){

        int decimalA = Integer.parseInt(a,16);
        int decimalB = Integer.parseInt(b,16);
        //System.out.println("\nDecimal A : "+decimalA);
        //System.out.println("\nDecimal B : "+decimalB);
        int result = decimalA ^ decimalB;
        String resultXor = Integer.toHexString(result);
        //System.out.println("\n\nXOR output in int : "+result);
        if(resultXor.length()!=2){
            resultXor = "0"+resultXor;
        }
        return resultXor;
    }


    public static String[] ASCIItoHEXArray(String ascii)
    {

        String hexTemp[] = new String[ascii.length()];
        // String hex = "";
        for (int i = 0; i < ascii.length(); i++) {
            char ch = ascii.charAt(i);
            int in = (int)ch;
            String part = Integer.toHexString(in);
            hexTemp[i] = part;
            // hex += part;
        }
        //return hex;
        return hexTemp;
    }


    private static String[] splitToNChar(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        return parts.toArray(new String[0]);
    }


    public static String ASCIItoHEX(String ascii)
    {
        String hex = "";
        for (int i = 0; i < ascii.length(); i++) {

            char ch = ascii.charAt(i);

            int in = (int)ch;

            String part = Integer.toHexString(in);
            hex += part;
        }
        return hex;
    }

    static void HexToBin(char hexdec[])
    {
        int i = 0;

        while (hexdec[i] != '\u0000') {

            switch (hexdec[i]) {
                case '0':
                    System.out.print("0000");
                    break;
                case '1':
                    System.out.print("0001");
                    break;
                case '2':
                    System.out.print("0010");
                    break;
                case '3':
                    System.out.print("0011");
                    break;
                case '4':
                    System.out.print("0100");
                    break;
                case '5':
                    System.out.print("0101");
                    break;
                case '6':
                    System.out.print("0110");
                    break;
                case '7':
                    System.out.print("0111");
                    break;
                case '8':
                    System.out.print("1000");
                    break;
                case '9':
                    System.out.print("1001");
                    break;
                case 'A':
                case 'a':
                    System.out.print("1010");
                    break;
                case 'B':
                case 'b':
                    System.out.print("1011");
                    break;
                case 'C':
                case 'c':
                    System.out.print("1100");
                    break;
                case 'D':
                case 'd':
                    System.out.print("1101");
                    break;
                case 'E':
                case 'e':
                    System.out.print("1110");
                    break;
                case 'F':
                case 'f':
                    System.out.print("1111");
                    break;
                default:
                    System.out.print("\nInvalid hexadecimal digit " + hexdec[i]);
            }
            i++;
        }
    }

    static void divideString(String str, int n)
    {
        int str_size = str.length();
        int part_size;
        String splitArray [][] = new String[n][2];
        // Check if string can be divided in
        // n equal parts
        if (str_size % n != 0)
        {
            System.out.println("Invalid Input: String size" +
                    "is not divisible by n");
            return;
        }

        // Calculate the size of parts to find
        // the division points
        part_size = str_size / n;

        for (int i = 0; i< str_size; i++)
        {
            if(i % part_size == 0)
                System.out.println();
            System.out.print(str.charAt(i));
        }
    }

}
