package org.jcodings.transcode;

import org.jcodings.CaseFoldCodeItem;
import org.jcodings.specific.UTF8Encoding;


public class Main {
    public static void main(String[] args) {

        byte []source = "EUC-JP".getBytes();
        byte []destination = "SJIS-SoftBank".getBytes();


//        int result = TranscoderDB.searchPath(source, destination, new TranscoderDB.SearchPathCallback() {
//
//            @Override
//            public void call(byte[] source, byte[] destination, int depth) {
//                System.out.println("source: " + new String(source));
//                System.out.println("destination: " + new String(destination));
//                System.out.println(depth);
//            }
//        });
//
//        System.out.println(result);
        //EConv e = TranscoderDB.open(source, destination, 0);
        //System.out.println(e.toStringFull());

        byte[]str = "ff21".getBytes();
        CaseFoldCodeItem[]items = UTF8Encoding.INSTANCE.caseFoldCodesByString(1073741824, "ff".getBytes(), 0, 2);
        System.out.println(items.length);

        //System.out.println(TranscoderDB.transcoders.get(source).get("UTF-8".getBytes()).getTranscoder());
        // )


        // TranscoderDB.class
        // byte[]bytes = ArrayReader.readByteArray("Big5_TranscoderByteArray");
        // int[]ints = ArrayReader.readIntArray("Big5_TranscoderWordArray");
        // System.out.println("##");
        // System.out.println(TranscodeTableSupport.WORDINDEX2INFO(69));
        // System.out.println(From_Big5_HKSCS_Transcoder.INSTANCE);
        // int [][]a = new int[][] {{1, 2}, {3, 3}};
        // System.out.println(Transcoder.load("From_Big5_HKSCS"));
        // System.out.println(TranscoderDB.transcoders.get("Big5-HKSCS".getBytes()).get("UTF-8".getBytes()).getTranscoder().toStringFull());
        // System.out.println(TranscoderDB.transcoders.get("UTF-8".getBytes()).get("UTF-32".getBytes()).getTranscoder().toStringFull());
        // int[][] ar = ArrayReader.readNestedIntArray("CaseUnfold_13_To");

        // int[][] ar2 = ArrayReader.readNestedIntArray("CaseUnfold_11_To");
        // dumpArray(ar2);

        // int[] ar2 = ArrayReader.readIntArray("CaseUnfold_11_From");
        // dumpArray(ar2);
        // System.out.println(EncodingDB.getEncodings().get("GBK".getBytes()).getEncoding());
        // int type = UTF8Encoding.INSTANCE.propertyNameToCType("ps".getBytes(), 0, 2);
        // System.out.println(type);
        //
        // int[] xarr = ArrayReader.readIntArray("CR_Ps");
        // System.out.println(xarr.length);
        // System.out.println(UTF8Encoding.INSTANCE.isCodeCType('a', 71));

        // =>

        // System.out.println("@@@");
        // byte[] prop = "ASCII".getBytes();
        // int len = ASCIIEncoding.INSTANCE.propertyNameToCType(prop, 0, prop.length);
        // System.out.println(len);
        // CaseInsensitiveBytesHash<String> h = new CaseInsensitiveBytesHash<String>();
        // h.put("a".getBytes(), "dupa");
        // h.put("b".getBytes(), "kapa");
        // h.put("c".getBytes(), "srupa");
        //
        // h.put("b".getBytes(), "GAGA");
        //
        // for (String s : h) {
        // System.out.println(s);
        // }
        //
        // for (Hash.HashEntry<String> e : h.entryIterator()) {
        // CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<String> entry =
        // (CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<String>) e;
        // System.out.println(Arrays.toString(entry.bytes));
        // }

        // System.out.println(CaseInsensitiveBytesHash.caseInsensitiveEquals("".getBytes(), "".getBytes()));

        // CaseInsensitiveBytesHashEntryIterator i = h.entryIterator();
        // while (i.hasNext()) {
        // CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<String> e = i.next();
        // System.out.println(Arrays.toString(e.bytes));
        // }

        // for(Hash.HashEntry<String> e: h.entryIterator()) {
        //
        // }

        // System.out.println("!!");
    }

    public static void dumpArray(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            int[] iar = arr[i];
            System.out.print("{");
            for (int j = 0; j < iar.length; j++) {
                System.out.print(String.format("0x%04x, ", iar[j]));
            }
            System.out.print("}\n");
        }
    }

    public static void dumpArray(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.println(String.format("0x%04x, ", arr[i]));
        }
    }

}
