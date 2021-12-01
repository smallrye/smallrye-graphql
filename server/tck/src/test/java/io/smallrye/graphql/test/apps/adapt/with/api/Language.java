package io.smallrye.graphql.test.apps.adapt.with.api;

/**
 * Simple language POJO
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Language {

    private ISO6391 iso6391;
    private String nativeName;
    private String enName;
    private String please;
    private String thankyou;

    public Language() {
    }

    public Language(ISO6391 iso6391, String nativeName, String enName, String please, String thankyou) {
        this.iso6391 = iso6391;
        this.nativeName = nativeName;
        this.enName = enName;
        this.please = please;
        this.thankyou = thankyou;
    }

    public ISO6391 getIso6391() {
        return iso6391;
    }

    public void setIso6391(ISO6391 iso6391) {
        this.iso6391 = iso6391;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getPlease() {
        return please;
    }

    public void setPlease(String please) {
        this.please = please;
    }

    public String getThankyou() {
        return thankyou;
    }

    public void setThankyou(String thankyou) {
        this.thankyou = thankyou;
    }
}
