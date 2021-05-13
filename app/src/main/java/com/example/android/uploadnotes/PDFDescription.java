package com.example.android.uploadnotes;

import java.io.Serializable;

public class PDFDescription implements Serializable {

    public String examName;
    public String selectType;
    public String pdfName;
    public String url;

    public PDFDescription(){

    }

    public PDFDescription(String examName, String selectType, String pdfName, String url) {
        this.examName = examName;
        this.selectType = selectType;
        this.pdfName = pdfName;
        this.url = url;
    }

}
