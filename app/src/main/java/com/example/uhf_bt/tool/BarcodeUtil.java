package com.example.uhf_bt.tool;

public class BarcodeUtil {
    public static String getBarcodeType(String codeId) {
        switch (codeId) {
            case "A":
                return "UPC-A, UPC-E, UPC-E1, EAN-8, EAN-13";
            case "B":
                return "Code 39, Code 32";
            case "C":
                return "Codabar";
            case "D":
                return "Code 128, ISBT 128, ISBT 128 Concatenated";
            case "E":
                return "Code 93";
            case "F":
                return "Interleaved 2 of 5";
            case "G":
                return "Discrete 2 of 5, Discrete 2 of 5 IATA";
            case "H":
                return "Code 11";
            case "J":
                return "MSI";
            case "K":
                return "GS1-128";
            case "L":
                return "Bookland EAN";
            case "M":
                return "Trioptic Code 39";
            case "N":
                return "Coupon Code";
            case "R":
                return "GS1 DataBar Family";
            case "S":
                return "Matrix 2 of 5";
            case "T":
                return "UCC Composite, TLC 39";
            case "U":
                return "Chinese 2 of 5";
            case "V":
                return "Korean 3 of 5";
            case "X":
                return "ISSN EAN, PDF417, Macro PDF417, Micro PDF417";
            case "z":
                return "Aztec, Aztec Rune";
            case "P00":
                return "Data Matrix";
            case "P01":
                return "QR Code, MicroQR";
            case "P02":
                return "Maxicode";
            case "P03":
                return "US Postnet";
            case "P04":
                return "US Planet";
            case "P05":
                return "Japan Postal";
            case "P06":
                return "UK Postal";
            case "P07":
                return "Netherlands KIX Code";
            case "P08":
                return "Australia Post";
            case "P09":
                return "USPS 4CB/One Code/Intelligent Mail";
            case "P0A":
                return "UPU FICS Postal";
            case "P0H":
                return "Han Xin";
            case "P0X":
                return "Signature Capture";
            default:
                return "";
        }
    }

    public static String getBarcodeType(int ssiId) {
        switch (ssiId) {
            case 0x01:
                return "Code 39";
            case 0x02:
                return "Codabar";
            case 0x03:
                return "Code 128";
            case 0x04:
                return "D25";
            case 0x05:
                return "IATA";
            case 0x06:
                return "ITF";
            case 0x07:
                return "Code 93";
            case 0x08:
                return "UPCA";
            case 0x09:
                return "UPCE 3";
            case 0x0A:
                return "EAN-8";
            case 0x0B:
                return "EAN-13";
            case 0x0C:
                return "Code 11";
            case 0x0D:
                return "Code 49";
            case 0x0E:
                return "MSI";
            case 0x0F:
                return "EAN-128 (GS1-128)";
            case 0x10:
                return "UPCE1";
            case 0x11:
                return "PDF-417";
            case 0x12:
                return "Code 16K";
            case 0x13:
                return "Code 39 Full ASCII";
            case 0x14:
                return "UPCD";
            case 0x15:
                return "Trioptic";
            case 0x16:
                return "Bookland";
            case 0x17:
                return "Coupon Code";
            case 0x18:
                return "NW7";
            case 0x19:
                return "ISBT-128";
            case 0x1A:
                return "Micro PDF";
            case 0x1B:
                return "Data Matrix";
            case 0x1C:
                return "QR Code";
            case 0x1D:
                return "Micro PDF CCA";
            case 0x1E:
                return "Postnet (US)";
            case 0x1F:
                return "Planet (US)";
            case 0x20:
                return "Code 32";
            case 0x21:
                return "ISBT-128 Concat.";
            case 0x22:
                return "Postal (Japan)";
            case 0x23:
                return "Postal (Australia)";
            case 0x24:
                return "Postal (Dutch)";
            case 0x25:
                return "Maxicode";
            case 0x26:
                return "Postbar (CA)";
            case 0x27:
                return "Postal (UK)";
            case 0x28:
                return "Macro PDF-417";
            case 0x29:
                return "Macro QR Code";
            case 0x2C:
                return "Micro QR Code";
            case 0x2D:
                return "Aztec Code";
            case 0x2E:
                return "Aztec Rune Code";
            case 0x2F:
                return "French Lottery";
            case 0x30:
                return "GS1 Databar";
            case 0x31:
                return "GS1 Databar Limited";
            case 0x32:
                return "GS1 Databar Expanded";
            case 0x33:
                return "Parameter (FNC3)";
            case 0x34:
                return "4State US";
            case 0x35:
                return "4State US4";
            case 0x36:
                return "ISSN";
            case 0x37:
                return "Scanlet Webcode";
            case 0x38:
                return "Cue CAT Code";
            case 0x39:
                return "Matrix 2 of 5";
            case 0x48:
                return "UPCA + 2";
            case 0x49:
                return "UPCE + 2";
            case 0x4A:
                return "EAN-8 + 2";
            case 0x4B:
                return "EAN-13 + 2";
            case 0x50:
                return "UPCE1 + 2";
            case 0x51:
                return "CC-A + EAN-128";
            case 0x52:
                return "CC-A + EAN-13";
            case 0x53:
                return "CC-A + EAN-8";
            case 0x54:
                return "CC-A + RSS Expanded";
            case 0x55:
                return "CC-A + RSS Limited";
            case 0x56:
                return "CC-A + RSS-14";
            case 0x57:
                return "CC-A + UPC-A";
            case 0x58:
                return "CC-A + UPC-E";
            case 0x59:
                return "CC-C + EAN-128";
            case 0x5A:
                return "TLC-39";
            case 0x61:
                return "CC-B + EAN-128";
            case 0x62:
                return "CC-B + EAN-13";
            case 0x63:
                return "CC-B + EAN-8";
            case 0x64:
                return "CC-B + RSS Expanded";
            case 0x65:
                return "CC-B + RSS Limited";
            case 0x66:
                return "CC-B + RSS-14";
            case 0x67:
                return "CC-B + UPC-A";
            case 0x68:
                return "CC-B + UPC-E";
            case 0x69:
                return "Signature";
            case 0x70:
                return "Δ PDF-417 Parameter";
            case 0x71:
                return "Δ Matrix 2 of 5 – obsolete";
            case 0x72:
                return "C 2 of 5";
            case 0x73:
                return "Korean 3 of 5";
            case 0x74:
                return "Δ Datamatrix Parameter";
            case 0x88:
                return "UPCA + 5";
            case 0x89:
                return "UPCE + 5";
            case 0x8A:
                return "EAN-8 + 5";
            case 0x8B:
                return "EAN-13 + 5";
            case 0x90:
                return "UPCE1 + 5";
            case 0x99:
                return "Multipacket Format";
            case 0x9A:
                return "Macro Micro PDF";
            case 0xA0:
                return "OCRB";
            case 0xB4:
                return "GS1 Databar Expanded Coupon";
            case 0xB6:
                return "Han Xin Code";
            case 0xC1:
                return "GS1 Datamatrix";
            case 0xC2:
                return "GS1 QR";
            case 0xC3:
                return "Mailmark";
            case 0xC4:
                return "DotCode";
            case 0xC5:
                return "Plural Stage";
            case 0xC6:
                return "Multicode";
            case 0xC7:
                return "UK Plessey";
            case 0xC8:
                return "GridMatrix";
            case 0xC9:
                return "HP Link";
            case 0xCA:
                return "Telepen";
            case 0xCB:
                return "C365";
            case 0xCC:
                return "UDI Parsed Code";
            case 0xCD:
                return "PostI4S";
            case 0xE0:
                return "RFID Raw";
            case 0xE1:
                return "RFID URI";
            default:
                return "";
        }
    }
}
