import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LZ77 {
    // Function to read the uncompressed file and return its contents as a string
    public String readUncompressesdFile(String fileName) {
        String txt = "";
        try {
            File file = new File(fileName);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                txt += sc.nextLine();
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return txt;
    }

    // Function to compress the input file using the LZ77 algorithm
    public void compress(String fileName) {
        String txt = readUncompressesdFile(fileName);
        String current = "";
        int cursor = 0;
        Boolean found = false;
        Boolean isLast = false;
        String searchWindow = "";
        String output = "";
        ArrayList<String> outputList = new ArrayList<String>();

        for (int i = 0; i < txt.length(); i++) {
            current = txt.charAt(i) + "";
            cursor = i;
            searchWindow = txt.substring(0, i);
            int position = 0;

            // Search for the longest match in the search window
            while (searchWindow.contains(current)) {
                cursor++;
                position = searchWindow.lastIndexOf(current);
                found = true;
                if (cursor >= txt.length()) {
                    isLast = true;
                    break;
                }
                current += txt.charAt(cursor);
            }

            if (found) {
                if (isLast) {
                    // Output the compressed data with a reference to a previous position and length
                    output = (i - position) + "," + (current.length()) + "," + null;
                } else {
                    output = (i - position) + "," + (current.length() - 1) + ","
                            + current.charAt(current.length() - 1) + ",";
                }
                found = false;
                i = cursor;
            } else {
                // Output the compressed data with a reference to a previous position and length
                output = position + "," + (current.length() - 1) + "," + current + ",";
            }
            outputList.add(output);
            current = "";
        }

        writeBits(outputList);
        System.out.println("Compression Done");
    }

    // Function to write the compressed data to a file
    public void writeBits(ArrayList<String> outputList) {
        try (FileOutputStream out = new FileOutputStream("compressed.txt")) {
            int position, length;
            String nextSymbol;
            String[] lineArr;
            int maxPos = 0, maxLen = 0;
            int numOfBitsOfPos = 0, numOfBitsOfLen = 0, numOfExtraBits = 0;

            // Calculate the maximum position and length for encoding
            for (String line : outputList) {
                lineArr = line.split(",");
                position = Integer.parseInt(lineArr[0]);
                length = Integer.parseInt(lineArr[1]);
                if (position > maxPos) {
                    maxPos = position;
                }
                if (length > maxLen) {
                    maxLen = length;
                }
            }

            // Calculate the number of bits required to represent the maximum position and
            // length
            numOfBitsOfPos = (int) Math.ceil(Math.log(maxPos) / Math.log(2));
            if (numOfBitsOfPos == 0) {
                numOfBitsOfPos = 1;
            }
            numOfBitsOfLen = (int) Math.ceil(Math.log(maxLen) / Math.log(2));
            if (numOfBitsOfLen == 0) {
                numOfBitsOfLen = 1;
            }

            // Calculate the number of extra bits for padding
            numOfExtraBits = 8 - ((numOfBitsOfPos + numOfBitsOfLen + 8) * outputList.size() % 8);
            if (numOfExtraBits == 8) {
                numOfExtraBits = 0;
            }

            // Write the maximum position, maximum length, and number of extra bits to the
            // output file
            out.write((byte) maxPos);
            out.write((char) maxLen);
            out.write((byte) numOfExtraBits);

            String bits = "";

            // Encode the compressed data into binary and write it to the output file
            for (String line : outputList) {
                lineArr = line.split(",");
                position = Integer.parseInt(lineArr[0]);
                length = Integer.parseInt(lineArr[1]);
                nextSymbol = lineArr[2];
                bits += String.format("%" + numOfBitsOfPos + "s", Integer.toBinaryString(position)).replace(' ', '0');
                bits += String.format("%" + numOfBitsOfLen + "s", Integer.toBinaryString(length)).replace(' ', '0');
                if (nextSymbol.equals("null")) {
                    bits += "00000000";
                } else {
                    bits += String.format("%8s", Integer.toBinaryString(nextSymbol.charAt(0))).replace(' ', '0');
                }
            }

            // Add padding bits to ensure a multiple of 8 bits is written to the output file
            for (int i = 0; i < numOfExtraBits; i++) {
                bits += "0";
            }

            // Write the binary data to the output file
            for (int i = 0; i < bits.length(); i += 8) {
                out.write((byte) Integer.parseInt(bits.substring(i, i + 8), 2));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to read the compressed data from a file
    public byte[] readCompressedFile(String fileName) {
        try {
            File file = new File(fileName);
            FileInputStream in = new FileInputStream(file);
            byte[] compressedBytes = new byte[in.available()];
            in.read(compressedBytes);
            in.close();
            return compressedBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Function to decompress the compressed data and write the result to a file
    public void decompress(String fileName) {
        try (FileOutputStream out = new FileOutputStream("decompressed.txt")) {
            byte[] compressedBytes = readCompressedFile(fileName);

            int maxPos = compressedBytes[0];
            int maxLen = compressedBytes[1];
            int numOfExtraBits = compressedBytes[2];
            int numOfBitsOfPos = (int) Math.ceil(Math.log(maxPos) / Math.log(2));
            if (numOfBitsOfPos == 0) {
                numOfBitsOfPos = 1;
            }
            int numOfBitsOfLen = (int) Math.ceil(Math.log(maxLen) / Math.log(2));
            if (numOfBitsOfLen == 0) {
                numOfBitsOfLen = 1;
            }

            String bits = "";
            for (int i = 3; i < compressedBytes.length; i++) {
                bits += String.format("%8s", Integer.toBinaryString(compressedBytes[i] & 0xFF)).replace(' ', '0');
            }

            // Remove extra bits
            bits = bits.substring(0, bits.length() - numOfExtraBits);

            String decompressed = "";

            // Decode the compressed data and build the decompressed string
            for (int i = 0; i < bits.length(); i += (numOfBitsOfPos + numOfBitsOfLen + 8)) {
                int position = Integer.parseInt(bits.substring(i, i + numOfBitsOfPos), 2);
                int length = Integer.parseInt(bits.substring(i + numOfBitsOfPos, i + numOfBitsOfPos + numOfBitsOfLen),
                        2);
                String nextSymbol = "";
                if (bits.substring(i + numOfBitsOfPos + numOfBitsOfLen, i + numOfBitsOfPos + numOfBitsOfLen + 8)
                        .equals("00000000")) {
                    nextSymbol = "null";
                    decompressed += decompressed.substring(decompressed.length() - position,
                            decompressed.length() - position + length);
                } else {
                    nextSymbol = String
                            .valueOf((char) Integer.parseInt(bits.substring(i + numOfBitsOfPos + numOfBitsOfLen,
                                    i + numOfBitsOfPos + numOfBitsOfLen + 8), 2));
                    decompressed += decompressed.substring(decompressed.length() - position,
                            decompressed.length() - position + length) + nextSymbol;
                }
            }

            // Write the decompressed string to the output file
            out.write(decompressed.getBytes());
            out.close();
            System.out.println("Decompression Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
